import urllib.request, urllib.parse, json, os

base = 'https://raw.githubusercontent.com/KyleBing/english-vocabulary/master/full_line_jsonl/sentence/'
path = urllib.parse.quote('正序/人教高中.jsonl')
url = base + path
print(f'URL: {url}')
resp = urllib.request.urlopen(url, timeout=60)
raw = resp.read().decode('utf-8')

words = []
for line in raw.strip().split('\n'):
    try:
        item = json.loads(line)
        word = item.get('word', '')
        trans = item.get('translations', [])
        parts = []
        for t in trans:
            typ = t.get('type', '')
            txt = t.get('translation', '')
            if typ and txt:
                parts.append(f"{typ} {txt}")
            elif txt:
                parts.append(txt)
        meaning = '; '.join(parts)
        phonetic = item.get('us', item.get('uk', ''))
        sentences = item.get('sentences', [])
        example = sentences[0].get('sContent', '') if sentences else ''
        example_cn = sentences[0].get('sCn', '') if sentences else ''
        if word:
            words.append({
                'word': word,
                'phonetic': phonetic,
                'meaning': meaning,
                'example': example,
                'translation': example_cn,
            })
    except json.JSONDecodeError:
        pass

os.makedirs(r'D:\ds\WordGlass\app\src\main\assets\wordbooks', exist_ok=True)
out = r'D:\ds\WordGlass\app\src\main\assets\wordbooks\pep_senior.json'
with open(out, 'w', encoding='utf-8') as f:
    json.dump({'book': '人教版高中英语', 'bookId': 'pep_senior', 'words': words}, f, ensure_ascii=False, indent=1)

print(f'Saved {len(words)} words')
if words:
    w = words[0]
    print(f'Sample: {w["word"]} /{w["phonetic"]}/ {w["meaning"][:50]}')
    print(f'Example: {w["example"][:60]}')
