import urllib.request, urllib.parse, json, os

base = 'https://raw.githubusercontent.com/KyleBing/english-vocabulary/master/full_line_jsonl/sentence/'
path = urllib.parse.quote('正序/人教高中.jsonl')
url = base + path
print('Downloading...')
resp = urllib.request.urlopen(url, timeout=60)
raw = resp.read().decode('utf-8')

# Check first few items to see structure
lines = raw.strip().split('\n')
print(f'Total lines: {len(lines)}')
print('--- First item ---')
first = json.loads(lines[0])
with open(r'D:\ds\WordGlass\scripts\pep_sample.json', 'w', encoding='utf-8') as f:
    json.dump(json.loads(lines[0]), f, ensure_ascii=False, indent=2)
print('Written to pep_sample.json')
