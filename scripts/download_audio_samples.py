"""
Download real human pronunciation samples from open sources.
Sources:
  1. Wiktionary (CC-BY-SA 4.0) - real human recordings
  2. Youdao dictionary voice API - real human pronunciations
"""
import os
import urllib.request

OUT = r"D:\ds\WordGlass\docs\audio-samples"
os.makedirs(OUT, exist_ok=True)

# Youdao voice API (free, real human pronunciation, MP3)
youdao = {
    "change_us": "https://dict.youdao.com/dictvoice?audio=change&type=2",
    "change_uk": "https://dict.youdao.com/dictvoice?audio=change&type=1",
    "experience_us": "https://dict.youdao.com/dictvoice?audio=experience&type=2",
    "ability_us": "https://dict.youdao.com/dictvoice?audio=ability&type=2",
    "present_us": "https://dict.youdao.com/dictvoice?audio=present&type=2",
    "beautiful_us": "https://dict.youdao.com/dictvoice?audio=beautiful&type=2",
}

# Add header to avoid 403
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "Referer": "https://www.youdao.com/",
}

for name, url in youdao.items():
    out_path = os.path.join(OUT, f"{name}.mp3")
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=15) as r:
            data = r.read()
        with open(out_path, "wb") as f:
            f.write(data)
        print(f"OK  {name}.mp3  {len(data)} bytes")
    except Exception as e:
        print(f"FAIL {name}: {e}")

print(f"\nSamples saved to {OUT}")
