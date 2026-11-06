# WordGlass 开发计划

- 文档版本：0.2
- 当前版本：**0.2.0-beta**
- 日期：2026-11-06（Asia/Shanghai）
- 项目：`D:\ds\WordGlass`
- APK：`dist/WordGlass-0.2.0-beta-debug.apk`
- 当前阶段：**阶段 7 / 8 — 数据与备份完成，等待真机验收与云 TTS**

---

## 1. 当前结论

WordGlass 已经不是“只有 UI 的空壳”。

已具备：

- 可构建、可安装的 Compose 应用
- 882 条真实 PDF 词条的 Room 数据库
- 学习状态、收藏、掌握度、复习日志的持久化
- SM-2 风格调度
- 四种学习模式
- 首页真实统计（含学习会话计时）
- DataStore 设置持久化
- JSON 备份 / 恢复 / 词库导入
- FlClash 风格可拖拽液态透镜底栏
- 系统 TTS 临时发音

尚不能称为“最终版”：

- 0.2.0 没有在真机上安装验收（手机已断开）
- 云 TTS 因没有 Microsoft 凭证未接入
- 学习时长只覆盖正常离开学习页的会话，进程被杀不补记
- 没有正式签名与 release 包
- 词库版权仍需确认

---

## 2. 阶段状态

| 阶段 | 内容 | 状态 |
|---|---|---|
| 0 | 环境 / 数据源审计 | 完成 |
| 1 | Android 工程、主题、四页容器 | 完成 |
| 2 | Liquid Glass 设计系统与 Prismal 集成 | 完成 |
| 3 | 词库解析、Room、种子数据 | 完成 |
| 4 | 学习模式、SM-2、复习日志 | 完成 |
| 5 | 设置持久化、真实统计、学习会话计时 | 完成 |
| 6 | 备份 / 恢复 / JSON 导入 | 完成（合并写入） |
| 7 | 云 TTS、音频缓存 | **框架完成，云供应商未接入**（缺凭证） |
| 8 | 真机验收、性能、签名、发布 | **进行中 / 阻塞于设备** |

---

## 3. 本版完成明细

### 3.1 数据

- `WordEntity` / `LearningStateEntity` / `ReviewLogEntity` / `StudySessionEntity`
- Room 2.8.5 + KSP 2.3.10
- 词库种子 `app/src/main/assets/words.json`
- 解析结果：
  - 882 条
  - 668–672 缺失
  - 10 条缺音标，保持空值
  - `present` 两个读音保留为两条
- 提取脚本 `scripts/extract_words.py`
- 解析器不再把频次/“熟词僻义”行误判为新词条

### 3.2 学习

- 卡片翻转
- 左右滑动
- 选择题（选项来自真实词库）
- 听写
- Again / Hard / Good / Easy 四档
- 复习日志记录模式、评分、间隔、ease
- 选择题在进入下一题时写日志
- 听写在提交时写日志
- 收藏、掌握度、错词数持久化

### 3.3 设置与数据

- DataStore：主题、发音人、语速、自动播放、每日目标
- JSON 备份包含词条、状态、日志、设置
- JSON 恢复 / 词库导入通过系统文件选择器
- 导入时给新词补学习状态行

### 3.4 UI / 性能

- PrismalAGSL 只用于首页 Hero 与底栏透镜
- 其它卡片改为静态磨砂，避免每屏多个 RenderEffect
- 底栏从“整块高亮胶囊”改为 FlClash 风格圆形液态透镜
- 透镜采样底栏内容层，拖拽时只更新 offset/图形层
- 词汇滑动使用单个 `dragX` 状态 + `graphicsLayer`，不再每个手势事件启动协程

---

## 4. 关键事实与风险

1. **882 不是 887**：原文编号 668–672 缺失，不能补造。
2. **10 条缺音标**：306、543、617、812–818；需合法数据源补齐。
3. **词库版权**：源 PDF 不随仓库分发；商用前必须确认授权。
4. **TTS 凭证**：未提供 Microsoft Key；阿里云 Key 不应进入客户端。
5. **备份是合并**：恢复不会删除现有词条；需要“覆盖恢复”时必须先做预览与二次确认。
6. **学习时长**：已记录进入/离开学习页的会话；进程被杀时未结束的会话不会补记。
7. **工具链过渡**：`android.builtInKotlin=false` + `android.newDsl=false` 是为了 KSP；AGP 10 前需迁移。
8. **FlClash 许可**：仅参考交互；其 `prism_ui` 为 GPL-3.0，未复制。

---

## 5. 下一步

### 优先级 P0 — 真机验收

手机重新连接后：

1. 运行 `scripts/install-beta.ps1`
2. 首次启动等待种子导入
3. 验证：
   - 词库数量 882
   - 冷启动不崩
   - 底栏拖拽 / 点击
   - 卡片翻转、滑动、选择、听写
   - 杀进程后收藏与掌握度仍在
   - 导出备份 -> 修改 -> 恢复
4. 用 `adb shell dumpsys gfxinfo` 或系统跟踪确认滑动掉帧
5. 修复 0.2.0 真机问题后再发 0.2.1-beta

### 优先级 P1 — 云 TTS

已完成：

- `SpeechService` 接口
- `SystemSpeechService`（离线回退）
- `AudioCache`：SHA-256 内容寻址、32 MB 上限、原子写入、按修改时间修剪
- UI 只依赖 `SpeechService`，不依赖具体引擎

待完成：

- 确认 Microsoft Azure Speech Key / Region
- 通过服务端或构建期注入，不把长期密钥写入 APK
- 实现 `AzureSpeechService` 并复用 `AudioCache`
- 失败时回退系统 TTS
- 先用单词级音频，例句音频后续

### 优先级 P2 — 发布准备

- 覆盖式恢复预览
- PDF/XLSX 导入
- 正式签名、release AAB
- 词库授权与隐私说明

---

## 6. 构建与验证命令

```powershell
cd D:\ds\WordGlass
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebugAndroidTest
```

当前结果：

- `assembleDebug`：通过
- `testDebugUnitTest`：通过
- `lintDebug`：通过
- `assembleDebugAndroidTest`：通过
- `connectedDebugAndroidTest`：本版未执行（无设备）

---

## 7. 交付判定

**可以给内部试用，但不能称为最终版。**  
满足以下条件后才能标记最终版：

- 真机安装、启动、四页切换、四种模式、备份恢复全部通过
- 滑动无明显掉帧
- 云 TTS 或明确的系统 TTS 产品决策落地
- 词库授权确认
- 正式签名与 release 包生成
