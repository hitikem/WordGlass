"""
WordGlass + Cloudflare Tunnel
================================
用 Cloudflare 免费隧道把你的电脑变成公网服务器！

步骤：
1. 下载 cloudflared: https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/
2. 启动本服务器: python dict_server.py
3. 启动隧道: cloudflared tunnel --url http://localhost:8765
4. 会得到一个免费公网地址: https://xxx.trycloudflare.com
5. 把地址填到 App 设置里，全球都能用！

完全免费！无需域名！无需服务器！HTTPS 自动！
"""

import requests
import socket
from flask import Flask, jsonify, request

app = Flask(__name__)

# ---------- Dictionary sources ----------

def lookup_free_dict(word: str) -> dict:
    """Free Dictionary API - English definitions, phonetics, examples."""
    try:
        r = requests.get(
            f"https://api.dictionaryapi.dev/api/v2/entries/en/{word}",
            timeout=5,
        )
        if r.status_code != 200:
            return {}
        data = r.json()[0]
        result = {
            "word": data.get("word", word),
            "phonetic": "",
            "meanings": [],
            "audio": "",
        }
        for p in data.get("phonetics", []):
            if p.get("text"):
                result["phonetic"] = p["text"]
            if p.get("audio"):
                result["audio"] = p["audio"]
        for m in data.get("meanings", []):
            pos = m.get("partOfSpeech", "")
            for d in m.get("definitions", [])[:3]:
                result["meanings"].append({
                    "partOfSpeech": pos,
                    "definition": d.get("definition", ""),
                    "example": d.get("example", ""),
                })
        return result
    except Exception as e:
        return {"error": str(e)}


def lookup_youdao(word: str) -> dict:
    """Youdao - Chinese meanings and phrases."""
    try:
        r = requests.get(f"https://dict.youdao.com/jsonapi?q={word}", timeout=5)
        if r.status_code != 200:
            return {}
        data = r.json()
        result = {"chinese_meanings": [], "phrases": []}
        trs = data.get("ec", {}).get("word", [{}])[0].get("trs", [])
        for tr in trs[:5]:
            text = tr.get("tr", [{}])[0].get("l", {}).get("i", [""])
            if text:
                result["chinese_meanings"].append(text[0])
        phrases = data.get("phrs", {}).get("phrs", [])[:5]
        for p in phrases:
            hw = p.get("phr", {}).get("headword", "")
            tr = p.get("phr", {}).get("trs", [{}])[0].get("tr", {}).get("l", {}).get("i", [""])
            if hw and tr:
                result["phrases"].append({"phrase": hw, "meaning": tr[0]})
        return result
    except Exception as e:
        return {"error": str(e)}


def analyze_roots(word: str) -> dict:
    prefixes = {
        "un": "不", "re": "再次", "dis": "不", "pre": "预先", "over": "过度",
        "under": "不足", "mis": "错误", "sub": "下面", "inter": "之间",
        "trans": "跨越", "super": "超级", "anti": "反对", "auto": "自动",
    }
    suffixes = {
        "tion": "行为", "ment": "结果", "ness": "性质", "ity": "性质",
        "er": "人", "ist": "人", "ful": "充满", "less": "没有",
        "ous": "具有", "able": "能够", "ive": "倾向", "ize": "使化",
    }
    result = {"root": "", "prefix": "", "suffix": "", "analysis": ""}
    w = word.lower()
    for p in sorted(prefixes.keys(), key=len, reverse=True):
        if w.startswith(p) and len(w) > len(p) + 2:
            result["prefix"] = p
            result["analysis"] += f"前缀[{p}]={prefixes[p]}; "
            w = w[len(p):]
            break
    for s in sorted(suffixes.keys(), key=len, reverse=True):
        if w.endswith(s) and len(w) > len(s) + 2:
            result["suffix"] = s
            result["analysis"] += f"后缀[{s}]={suffixes[s]}; "
            w = w[:-len(s)]
            break
    result["root"] = w
    if result["analysis"]:
        result["analysis"] += f"词根[{w}]"
    return result


# ---------- API routes ----------

@app.route("/api/lookup")
def lookup():
    word = request.args.get("word", "").strip()
    if not word:
        return jsonify({"error": "Missing word"}), 400
    return jsonify({
        "word": word,
        "free_dict": lookup_free_dict(word),
        "youdao": lookup_youdao(word),
        "roots": analyze_roots(word),
    })

@app.route("/api/audio/<word>")
def audio(word):
    try:
        r = requests.get(f"https://api.dictionaryapi.dev/api/v2/entries/en/{word}", timeout=5)
        data = r.json()[0]
        for p in data.get("phonetics", []):
            if p.get("audio"):
                return jsonify({"audio_url": p["audio"]})
    except:
        pass
    return jsonify({"audio_url": f"https://dict.youdao.com/dictvoice?audio={word}&type=2"})

@app.route("/api/health")
def health():
    return jsonify({"status": "ok", "server": "WordGlass Dictionary", "mode": "cloudflare-tunnel"})


# ---------- Data Sync ----------

SYNC_FILE = "wordglass_sync.json"
UPDATE_FILE = "wordglass_update.json"

@app.route("/api/sync/upload", methods=["POST"])
def sync_upload():
    """Receive learning data from the app."""
    data = request.get_json(force=True)
    with open(SYNC_FILE, "w", encoding="utf-8") as f:
        import json
        json.dump(data, f, ensure_ascii=False, indent=2)
    return jsonify({"status": "ok", "message": f"Synced {data.get('words', []) and len(data.get('words', [])) or 0} words"})

@app.route("/api/sync/download")
def sync_download():
    """Send learning data back to the app."""
    import json
    import os
    if not os.path.exists(SYNC_FILE):
        return jsonify({"words": []})
    with open(SYNC_FILE, "r", encoding="utf-8") as f:
        data = json.load(f)
    return jsonify(data)

# ---------- Auto Update ----------

@app.route("/api/update/check")
def update_check():
    """Check if a new version is available."""
    import json
    import os
    current = request.args.get("version", "")
    if not os.path.exists(UPDATE_FILE):
        return jsonify({"hasUpdate": False})
    with open(UPDATE_FILE, "r", encoding="utf-8") as f:
        info = json.load(f)
    latest = info.get("version", "")
    return jsonify({
        "hasUpdate": latest != current and latest != "",
        "version": latest,
        "downloadUrl": info.get("downloadUrl", ""),
        "changelog": info.get("changelog", ""),
    })

@app.route("/")
def index():
    return jsonify({
        "name": "WordGlass Dictionary API",
        "endpoints": {
            "lookup": "/api/lookup?word=hello",
            "audio": "/api/audio/hello",
            "health": "/api/health",
        }
    })


if __name__ == "__main__":
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    local_ip = s.getsockname()[0]
    s.close()

    print(f"""
╔═══════════════════════════════════════════════════╗
║  WordGlass 词典服务器                              ║
╠═══════════════════════════════════════════════════╣
║  本机: http://{local_ip}:8765                      ║
║                                                   ║
║  🌐 公网接入（Cloudflare Tunnel）:                ║
║  1. 下载 cloudflared                               ║
║  2. 运行: cloudflared tunnel --url http://localhost:8765  ║
║  3. 得到免费公网地址 https://xxx.trycloudflare.com ║
║  4. 把地址填入 App 设置                            ║
║                                                   ║
║  完全免费！无需域名！HTTPS 自动！                 ║
╚═══════════════════════════════════════════════════╝
    """)
    app.run(host="0.0.0.0", port=8765, debug=False)
