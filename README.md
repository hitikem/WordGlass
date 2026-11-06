# WordGlass

WordGlass 是一款 Android 原生英语核心词汇学习应用。视觉目标是 Apple Liquid Glass 风格的蓝白高级感，交互目标是 FlClash / 酷安式的悬浮玻璃底栏与可拖拽液态透镜。

> 当前版本：**0.2.0-beta（内测）**  
> 状态：Room 数据闭环已接通，构建、单元测试、lint 通过；因手机已断开，**尚未完成这一版真机安装验收**。  
> 后端 / 云 TTS：尚未接入（没有 Microsoft 凭证；阿里云 Key 未写入任何文件）。

## 交付物

- 内测 APK：`dist/WordGlass-0.2.0-beta-debug.apk`
- SHA-256：`3D67C8BBF9DB956D5C280F0C8E9E19764DFA2FC3BE9DBBB80AA3FD44B8815426`
- 一键安装（手机连接并授权 USB 调试后）：`scripts/install-beta.ps1`
- 四页截图 + 崩溃日志 + gfxinfo 验收：`scripts/verify-beta.ps1`
- Debug 包名：`com.wordglass.app.debug`
- 正式包名：`com.wordglass.app`

## 已实现

### 数据与学习闭环

- Room 本地数据库：词条、学习状态、复习日志、学习会话表
- 从原始 PDF 解析出的 **882 条可溯源词条**；保留 `sourceNumber` / `sourcePage`
- SM-2 风格调度：Again / Hard / Good / Easy，记录间隔、easiness、lapse、下次复习时间
- 学习模式：卡片翻转、左右滑动、选择题、听写
- 选择题与听写均写入复习日志；不是“只播动画”
- 词库：搜索、待复习 / 学习中 / 已掌握 / 错词本 / 收藏筛选、A–Z / 掌握度 / 最近复习排序
- 首页真实统计：今日完成、待复习、累计学习、已掌握、连续天数、学习时长、近 7 天节奏
- 学习会话计时写入 `study_sessions`；离开学习页时落库，不再用 0 分钟假数据
- 收藏与掌握度持久化，重启不丢

### 设置与数据安全

- DataStore 持久化：主题、发音人、语速、自动播放、每日目标
- JSON 备份：词条、学习状态、复习日志、设置
- JSON 恢复：合并写入，不删除现有词条
- JSON 词库导入：只导入词条，不覆盖学习记录

### UI / 导航

- 四个主页面：首页、背单词、复习词库、我的
- FlClash 风格悬浮底栏：薄磨砂条 + 可拖拽圆形液态透镜；透镜采样底栏内容层，不复制 FlClash GPL 源码
- 真实 PrismalAGSL 玻璃只保留在首页 Hero 与底栏透镜；其余卡片使用静态磨砂层，降低滚动/拖拽掉帧
- 导航拖拽只更新布局/图形层，避免每帧重组整页
- Manrope 英文字体；蓝白固定品牌色；浅色 / 深色 / 跟随系统
- 可替换 `SpeechService` 边界；当前 `SystemSpeechService` 使用系统 TTS
- 32 MB 内容寻址 `AudioCache`（SHA-256 key、原子写入、LRU 修剪）；供云 TTS 复用，当前未产生云音频
- 屏幕代码不依赖具体语音厂商；云 TTS 仍需服务端短期令牌

## 词库事实

- 源 PDF：44 页，SHA-256 `4FE7416D06E82E5C028408A48DBF7EA8187414B4DC79E8DAF90010C678625250`
- 封面标注 887；原文编号 **668–672 缺失**
- 实际解析 **882 条**，全部进入 `app/src/main/assets/words.json`
- 10 条源数据缺音标：306、543、617、812–818；保持 `null`，不补造
- `present` 出现两次：动词 `/prɪˈzent/` 与名词/形容词 `/ˈpreznt/`，保留为两条，不合并
- 源 PDF 不含例句、翻译、词根、词缀、音频；这些字段保持空，不用生成内容填充
- 提取脚本：`scripts/extract_words.py`

## 技术栈

- JDK 17.0.20.1
- Gradle 9.5.0
- AGP 9.3.1
- Kotlin 2.3.10 + Compose Compiler 2.3.10
- Compose BOM 2026.06.01
- Room 2.8.5 + KSP 2.3.10
- DataStore Preferences 1.3.0
- Navigation Compose 2.10.2
- compileSdk / targetSdk 37，minSdk 26
- PrismalAGSL 1.0.6，本地 AAR

### 工具链说明

KSP 不支持 AGP 9.3 的内置 Kotlin，因此当前使用：

```properties
android.builtInKotlin=false
android.newDsl=false
```

并显式应用 `org.jetbrains.kotlin.android`。这是可工作的过渡方案；AGP 10 前需要迁移回内置 Kotlin 或替换代码生成方案。

## 构建

```powershell
cd D:\ds\WordGlass
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

APK 输出：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 测试

- `:app:testDebugUnitTest`：通过
- `WordRepositoryTest`（Robolectric + Room 内存库）验证：
  - 882 条落库
  - 668–672 缺口不被补造
  - `present` 两个读音都保留
  - 收藏持久化
  - Good / Again 调度与复习日志
- `:app:lintDebug`：通过
- `:app:assembleDebugAndroidTest`：通过
- 真机 Compose 导航测试之前出现过一次 instrumentation process crash，尚未在 0.2.0 上复测；不能宣称已通过

## 尚未完成 / 已知限制

1. 手机已断开，0.2.0 未做真机安装、启动、滑动流畅度与备份恢复验收。
2. 云 TTS 未接入：没有 Microsoft Speech Key；当前使用系统 TTS。
3. 学习时长只统计“进入学习页到离开”的会话；应用被系统杀死时未结束的会话不会补记。
4. 备份/恢复与词库导入目前是合并写入，不是“清空后覆盖”；恢复前请先导出备份。
5. PDF/XLSX 直接导入未实现；当前只支持 WordGlass JSON。
6. 无正式签名、无 release AAB、无应用商店材料。
7. 词库版权/商用授权仍需确认；源 PDF 不随仓库分发。

## 安全

- 聊天中出现过的阿里云 `sk-ws-...` Key **没有写入源码、日志、Git 或 APK**；建议立即轮换。
- 客户端不应内置长期云 TTS 密钥；正式方案应使用服务端短期令牌或代理网关。
- `local.properties`、签名文件、API Key 均不得提交。

## 文档

- [开发计划与阶段状态](DEVELOPMENT_PLAN.md)
- [技术选型](docs/TECH_SELECTION.md)
- [UI 设计系统](docs/UI_DESIGN.md)
- [第三方声明](THIRD_PARTY_NOTICES.md)

## 许可

应用代码目前为私有项目。第三方组件许可见 `THIRD_PARTY_NOTICES.md`。FlClash 仅作为交互参考，其 GPL-3.0 源码未被复制进 WordGlass。
