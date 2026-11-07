"""
Download Wiktionary real human pronunciations (CC-BY-SA 4.0).
These are genuine human recordings, not TTS.
"""
import os
import urllib.request

OUT = r"D:\ds\WordGlass\docs\audio-samples"
os.makedirs(OUT, exist_ok=True)

headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
}

# Wiktionary audio files (real human recordings, OGG)
wiktionary = {
    "wk_change_us": "https://upload.wikimedia.org/wikipedia/commons/4/4b/En-us-change.ogg",
    "wk_beautiful_us": "https://upload.wikimedia.org/wikipedia/commons/6/6e/En-us-beautiful.ogg",
    "wk_experience_us": "https://upload.wikimedia.org/wikipedia/commons/2/2e/En-us-experience.ogg",
    "wk_ability_us": "https://upload.wikimedia.org/wikipedia/commons/a/a2/En-us-ability.ogg",
}

for name, url in wiktionary.items():
    out_path = os.path.join(OUT, f"{name}.ogg")
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=15) as r:
            data = r.read()
        with open(out_path, "wb") as f:
            f.write(data)
        print(f"OK  {name}.ogg  {len(data)} bytes")
    except Exception as e:
        print(f"FAIL {name}: {e}")

print(f"\nWiktionary samples saved to {OUT}")
