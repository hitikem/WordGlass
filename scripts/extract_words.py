#!/usr/bin/env python3
"""Extract the WordGlass seed JSON from the supplied 887-word PDF.

This script intentionally does NOT invent examples, roots or affixes. Source rows that are
missing phonetic/meaning remain null and are reported in the extraction summary.

Requires pypdf. The PDF itself is not stored in the repository.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
from dataclasses import dataclass, asdict
from pathlib import Path

from pypdf import PdfReader

ENTRY_START = re.compile(r"^\s*(\d{1,3})\s+(.+?)\s*$")
PAGE_NUMBER = re.compile(r"^\s*\d{1,3}\s*$")
PHONETIC = re.compile(r"/([^/\n]+)/")
TRAILING_NUMBER = re.compile(r"\s+(\d{1,4})\s*$")
FREQUENCY_BEFORE_POS = re.compile(
    r"\s+\d{1,4}\s+(?=(?:n|v|vt|vi|adj|adv|prep|conj|pron|num|art)\.)"
)
WORD = re.compile(r"^([A-Za-z][A-Za-z'’\-]*)")


@dataclass
class ParsedWord:
    id: int
    sourceNumber: int
    sourcePage: int
    word: str
    phonetic: str | None
    meaning: str | None
    usage: str | None
    example: None
    translation: None
    root: None
    affix: None


def clean(text: str) -> str:
    text = text.replace("\u00a0", " ")
    text = re.sub(r"[ \t]+", " ", text)
    text = re.sub(r"\s*\n\s*", " ", text)
    return text.strip()


def strip_frequency(text: str) -> str:
    text = TRAILING_NUMBER.sub("", text).strip()
    text = FREQUENCY_BEFORE_POS.sub(" ", text).strip()
    return text


def split_entries(pages: list[str]) -> list[tuple[int, int, list[str]]]:
    expected = [n for n in range(1, 888) if n not in range(668, 673)]
    expected_index = 0
    entries: list[tuple[int, int, list[str]]] = []
    current_number: int | None = None
    current_page = 0
    current_lines: list[str] = []

    for page_index, text in enumerate(pages, start=1):
        for raw_line in text.splitlines():
            line = raw_line.rstrip()
            if not line.strip() or PAGE_NUMBER.match(line):
                continue
            match = ENTRY_START.match(line)
            # Only an exact next sequence number is an entry start. Frequency/usage lines
            # such as "97 熟词僻义..." also start with a number and must not split the entry.
            if match and expected_index < len(expected) and int(match.group(1)) == expected[expected_index]:
                if current_number is not None:
                    entries.append((current_number, current_page, current_lines))
                current_number = int(match.group(1))
                current_page = page_index
                current_lines = [match.group(2)]
                expected_index += 1
                continue
            if current_number is not None:
                current_lines.append(line)

    if current_number is not None:
        entries.append((current_number, current_page, current_lines))
    return entries


def parse_entry(number: int, page: int, lines: list[str]) -> ParsedWord:
    content = clean("\n".join(lines))
    word_match = WORD.match(content)
    word = word_match.group(1) if word_match else content.split(" ", 1)[0]
    rest = content[len(word):].strip()

    phonetic_match = PHONETIC.search(rest)
    phonetic = clean(phonetic_match.group(1)) if phonetic_match else None
    if phonetic_match:
        rest = (rest[:phonetic_match.start()] + " " + rest[phonetic_match.end():]).strip()

    usage = None
    markers = ["熟词僻义：", "熟词僻义:", "短语：", "短语:"]
    marker_positions = [(rest.find(marker), marker) for marker in markers if rest.find(marker) >= 0]
    if marker_positions:
        position, _ = min(marker_positions)
        usage = clean(rest[position:])
        rest = strip_frequency(rest[:position])
    else:
        rest = strip_frequency(rest)

    meaning = clean(rest) or None
    # Remove a leading separator left behind by a phonetic split.
    if meaning:
        meaning = re.sub(r"^[;；,，\s]+", "", meaning).strip() or None

    return ParsedWord(
        id=number,
        sourceNumber=number,
        sourcePage=page,
        word=word,
        phonetic=phonetic,
        meaning=meaning,
        usage=usage,
        example=None,
        translation=None,
        root=None,
        affix=None,
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("pdf", type=Path)
    parser.add_argument("out", type=Path)
    args = parser.parse_args()

    raw = args.pdf.read_bytes()
    digest = hashlib.sha256(raw).hexdigest().upper()
    reader = PdfReader(str(args.pdf))
    pages = [(page.extract_text() or "") for page in reader.pages]
    raw_entries = split_entries(pages)
    words = [parse_entry(number, page, lines) for number, page, lines in raw_entries]

    seen: set[str] = set()
    duplicates: list[str] = []
    for word in words:
        key = word.word.lower()
        if key in seen:
            duplicates.append(word.word)
        seen.add(key)

    missing_phonetic = [w.id for w in words if not w.phonetic]
    missing_meaning = [w.id for w in words if not w.meaning]
    with_usage = [w.id for w in words if w.usage]
    numbers = {w.sourceNumber for w in words}
    missing_numbers = [n for n in range(1, 888) if n not in numbers]

    output = {
        "schemaVersion": 1,
        "source": {
            "fileName": args.pdf.name,
            "sha256": digest,
            "pageCount": len(pages),
            "declaredCount": 887,
            "extractedCount": len(words),
            "missingNumbers": missing_numbers,
            "duplicateWords": sorted(set(duplicates)),
            "missingPhoneticNumbers": missing_phonetic,
            "missingMeaningNumbers": missing_meaning,
            "usageCount": len(with_usage),
            "parser": "wordglass-pdf-extractor/1",
        },
        "words": [asdict(w) for w in words],
    }
    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(json.dumps(output, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print(json.dumps({
        "pageCount": len(pages),
        "extractedCount": len(words),
        "missingNumbers": missing_numbers,
        "duplicateCount": len(set(duplicates)),
        "missingPhonetic": len(missing_phonetic),
        "missingMeaning": len(missing_meaning),
        "usageCount": len(with_usage),
        "out": str(args.out),
    }, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
