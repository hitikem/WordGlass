# WordGlass 📖

**一款专注背单词的 Android 应用**

> Apple Liquid Glass 液态玻璃 × 极简主义设计 · 真人发音 · 联网查词 · 科学记忆

![Version](https://img.shields.io/badge/version-2.0.0-blue)
![Platform](https://img.shields.io/badge/platform-Android%2026%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-purple)
![License](https://img.shields.io/badge/license-MIT-orange)

---

## ✨ 核心功能

### 📚 学习模式（5种）
| 模式 | 说明 |
|------|------|
| 🃏 **卡片** | 翻转卡片，先看词再看释义 |
| 👆 **滑动** | 左右滑动切换单词 |
| ✅ **选择** | 四选一，选对选错 |
| 🎧 **听写** | 听发音拼写单词 |
| ✏️ **拼写** | 看中文释义拼出英文 |

### 🗣️ 真人发音
- 有道词典真人美式发音（免费）
- 系统 TTS 多种音色（英式/澳式/印式）
- 男声/女声切换
- 试听选择音色

### 📊 科学记忆
- **SM-2 算法**：根据你的掌握程度智能安排复习
- **学习计划**：设定目标，闹钟式滑轮选择每日词量
- **学习报告**：周/月统计图表，可视化进度
- **错题本**：自动记录答错的词
- **收藏夹**：收藏重要单词

### 🌐 联网查词
- 英文释义（dictionaryapi.dev）
- 中文释义（有道词典）
- 词根词缀分析
- 常用短语搭配
- 搜索历史

### ☁️ 云同步
- 学习记录备份到你自己的电脑
- Cloudflare Tunnel 免费公网访问
- 自动更新检测

### 📱 系统集成
- **桌面小组件**：不用打开 App 也能看进度
- **每日提醒**：通知栏打卡提醒
- **深色模式**：跟随系统
- **自定义头像**：相册选图 + 圆形裁剪

---

## 🛠️ 技术栈

| 技术 | 用途 |
|------|------|
| Kotlin 2.3.20 | 主语言 |
| Jetpack Compose | UI 框架 |
| Room 2.8.5 | 本地数据库 |
| DataStore | 设置持久化 |
| Navigation Compose | 页面导航 |
| Coil | 图片加载 |
| PrismalAGSL (MIT) | 液态玻璃效果 |
| OkHttp | 网络请求 |
| Flask (Python) | 词典服务器 |

---

## 📦 安装

### 从 Release 下载
1. 前往 [Releases](../../releases) 页面
2. 下载最新 `WordGlass-2.0.0.apk`
3. 在 Android 手机上安装

### 从源码构建
```bash
# 环境要求
# JDK 17 · Android SDK 37 · Gradle 9.5

git clone https://github.com/hitikem/WordGlass.git
cd WordGlass
./gradlew assembleDebug

# APK 输出位置
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🖥️ 电脑服务器（可选）

支持联网查词和云同步：

```bash
cd server
pip install flask requests

# 启动服务器
python dict_server.py

# 启动 Cloudflare 免费隧道（另一个终端）
cloudflared tunnel --url http://localhost:8765
# 得到公网地址后填入 App 设置
```

### 服务器功能
| 接口 | 说明 |
|------|------|
| `GET /api/lookup?word=hello` | 查词（英文释义+中文+词根） |
| `GET /api/audio/hello` | 获取发音 |
| `POST /api/sync/upload` | 上传学习记录 |
| `GET /api/sync/download` | 下载学习记录 |
| `GET /api/update/check` | 检查更新 |

---

## 📁 项目结构

```
WordGlass/
├── app/src/main/java/com/wordglass/
│   ├── MainActivity.kt              # 主界面导航
│   ├── ui/
│   │   ├── home/                     # 首页（学习报告、词汇测试）
│   │   ├── study/                    # 学习（5种模式）
│   │   ├── library/                  # 词库（搜索、筛选）
│   │   ├── profile/                  # 我的（设置、头像、音色）
│   │   ├── search/                   # 联网查词
│   │   └── components/               # 通用组件
│   ├── data/
│   │   ├── local/                    # Room 数据库
│   │   ├── repository/               # 数据仓库
│   │   ├── speech/                   # 发音服务
│   │   └── network/                  # 网络 API
│   ├── model/                        # 数据模型
│   ├── notification/                 # 每日提醒
│   └── widget/                       # 桌面小组件
├── app/src/main/assets/
│   └── words.json                    # 882 词词库
├── server/
│   └── dict_server.py               # Python 词典服务器
└── docs/                             # 文档和测试证据
```

---

## 📖 词库说明

- **882 条**高中英语核心词汇
- 来源可溯，不编造任何数据
- 包含：单词、音标、中文释义
- 部分词汇含用法说明（62 条）
- 例句、词根、翻译字段预留（PDF 未提供则为空）

---

## 🔒 隐私

- ✅ 所有学习数据保存在**你的手机**上
- ✅ 云同步只上传到**你自己的电脑**
- ✅ 联网查词只发送**单词文本**
- ✅ 不收集任何个人信息
- ✅ 不经过任何第三方服务器

---

## 📄 开源许可

| 组件 | 许可 |
|------|------|
| WordGlass | MIT |
| PrismalAGSL | MIT |
| Coil | Apache-2.0 |
| Manrope Font | OFL |

---

## 🎯 版本历史

### v2.0.0 (2026-11-08)
- ✅ 自定义照片头像（圆形裁剪）
- ✅ 自定义昵称和签名
- ✅ 拼写练习模式
- ✅ 学习计划（闹钟式滑轮选择）
- ✅ 学习报告（周/月统计）
- ✅ 每日打卡提醒
- ✅ 词汇量测试
- ✅ 音色选择器（全部免费音色+试听）
- ✅ 桌面小组件
- ✅ 联网查词（释义+词根+短语）
- ✅ 云同步（上传/恢复）
- ✅ 自动更新检测
- ✅ 产品说明页面
- ✅ 自定义应用图标

### v1.0.0-final
- 真人发音（有道）
- 5 种学习模式
- SM-2 记忆算法
- 深色模式
- 数据备份/恢复

### v0.4.x
- 液态玻璃 UI
- 导航栏动画

### v0.2.0-beta
- 初始版本

---

**Made with ❤️ for focused learning.**
