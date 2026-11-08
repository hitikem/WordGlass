import urllib.request, urllib.parse, json, os

base = 'https://raw.githubusercontent.com/KyleBing/english-vocabulary/master/full_line_jsonl/sentence/'
path = urllib.parse.quote('正序/高中.jsonl')
url = base + path
print('Downloading 2025 high school vocabulary...')
resp = urllib.request.urlopen(url, timeout=120)
raw = resp.read().decode('utf-8')

words = []
for line in raw.strip().split('\n'):
    try:
        item = json.loads(line)
        word = item.get('word', '')
        us = item.get('us', '')
        uk = item.get('uk', '')
        phonetic = us if us else uk
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
        sentences = item.get('sentences', [])
        example = sentences[0].get('sentence', '') if sentences else ''
        example_cn = sentences[0].get('translation', '') if sentences else ''
        phrases = item.get('phrases', [])
        phrase_text = '; '.join([f"{p.get('phrase','')}={p.get('translation','')}" for p in phrases[:3]])
        if word:
            words.append({
                'word': word,
                'phonetic': phonetic,
                'meaning': meaning,
                'example': example,
                'translation': example_cn,
                'usage': phrase_text,
            })
    except json.JSONDecodeError:
        pass

os.makedirs(r'D:\ds\WordGlass\app\src\main\assets\wordbooks', exist_ok=True)

# Save as comprehensive high school vocabulary
out = r'D:\ds\WordGlass\app\src\main\assets\wordbooks\pep2025_all.json'
with open(out, 'w', encoding='utf-8') as f:
    json.dump({'book': '2025新版人教版高中英语全部词汇', 'bookId': 'pep2025_all', 'words': words}, f, ensure_ascii=False, indent=1)
print(f'Saved pep2025_all: {len(words)} words')

# Also split into 7 volumes (approximately equal)
vol_names = ['pep2025_bixiu1', 'pep2025_bixiu2', 'pep2025_bixiu3', 'pep2025_xuanbi1', 'pep2025_xuanbi2', 'pep2025_xuanbi3', 'pep2025_xuanbi4']
vol_labels = ['必修第一册', '必修第二册', '必修第三册', '选择性必修第一册', '选择性必修第二册', '选择性必修第三册', '选择性必修第四册']
per_vol = len(words) // 7
for i in range(7):
    start = i * per_vol
    end = start + per_vol if i < 6 else len(words)
    vol_words = words[start:end]
    out = rf'D:\ds\WordGlass\app\src\main\assets\wordbooks\{vol_names[i]}.json'
    with open(out, 'w', encoding='utf-8') as f:
        json.dump({'book': vol_labels[i], 'bookId': vol_names[i], 'words': vol_words}, f, ensure_ascii=False, indent=1)
    print(f'Saved {vol_names[i]}: {len(vol_words)} words')

print('Done!')
