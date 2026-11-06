# UI 技术选型：Glasmio vs PrismalAGSL

- 调查日期：2026-11-06
- 目标：为 WordGlass 的 Jetpack Compose UI 原型选择玻璃渲染方案
- 结论：**不引入 Glasmio 整套框架；采用 PrismalAGSL 的底层玻璃渲染能力，自行实现 WordGlass 的导航、卡片、动效和设计系统。**

---

## 1. 结论先行

| 维度 | Glasmio | PrismalAGSL |
|---|---|---|
| 许可证 | Apache-2.0 | 仓库 LICENSE 为 MIT；发布 POM 写 Apache-2.0（存在不一致） |
| 定位 | 完整 iOS 27 风格 Compose UI 框架 | 聚焦 Compose 液态玻璃渲染库 |
| 最低系统 | API 33 | API 25；API 33+ 才是完整 AGSL |
| 构建要求 | JDK 21、SDK 37、NDK 27.1、CMake 3.22.1 | JDK 17、SDK 37（AAR `minCompileSdk=37`）、Kotlin 2.2+ |
| 依赖体积/耦合 | 六个模块，主题、导航、控件、图标、偏好设置全绑定 | 单个 AAR，只依赖 Compose Foundation/UI/Graphics |
| 视觉控制权 | 采用其框架后，视觉语言容易被其 iOS 复刻体系主导 | 只提供玻璃表面，颜色、布局、组件由 WordGlass 控制 |
| 实现成本 | 高：工具链升级、框架适配、样式改造成本都高 | 中：JitPack 依赖 + 自定义组件封装 |
| 本项目决策 | **只借鉴材质参数与动效思想，不引入** | **采用底层 API，不照搬示例界面** |

---

## 2. Glasmio 审计

### 2.1 许可证与来源

- 仓库：<https://github.com/NEORUAA/Glasmio>
- LICENSE：Apache License 2.0
- 发布版本：0.1.2，Maven 坐标 `io.github.neoruaa.glasmio:*`
- 仓库内 `THIRD_PARTY_NOTICES.md` 说明玻璃捕获/效果基础设施改编自 AndroidLiquidGlass，保留 Apache-2.0 归属；形状库为独立依赖。
- 若复制或修改其代码，需要保留 LICENSE、NOTICE 与第三方归属；不能只复制源码而删掉声明。

### 2.2 技术门槛

README 明确要求：

- JDK 21
- Android SDK 37
- NDK `27.1.12297006`
- CMake `3.22.1`
- 最低 Android 13（API 33）
- Gradle 9.6

实际 AAR `glasmio-glass-0.1.2.aar` 的 `aar-metadata.properties` 显示：

```text
minCompileSdk=37
```

Glasmio 玻璃模块包含 C++/CMake 工程与完整渲染管线，不是“一个轻量玻璃组件”。

### 2.3 设计可借鉴点

- 规则材质的光学参数：小半径模糊、饱和度约 1.2–1.3、白色填充与亮度上限。
- “模糊 + 边缘折射 + 高光 + 内阴影”是一个整体材质，而不是一张半透明白色卡片。
- 导航栏的选择胶囊是一个独立的光学透镜，而不是简单换色。
- 使用响应式弹簧与按压时间，而不是固定线性动画。
- 支持减少透明度、自动明暗、HDR/性能降级。

这些思想会进入 WordGlass 设计系统，但不会复制其代码或整体组件外观。

### 2.4 不采用原因

1. WordGlass 需要自己的蓝白品牌识别，不需要一套“复刻 iOS 27”的完整组件语言。
2. 要求 JDK 21 + NDK/CMake，当前机器只有 JDK 17 与命令列 SDK；引入会显著提高构建脆弱性。
3. 六个模块会接管主题、导航、图标与大量控件，和“不要直接照搬整个项目”的要求冲突。
4. API 33 起步会直接排除 Android 8–12 设备，不符合长期可用目标。

---

## 3. PrismalAGSL 审计

### 3.1 许可证与发布状态

- 仓库：<https://github.com/styropyr0/PrismalAGSL>
- 仓库 LICENSE：MIT，版权人为 Saurav Sajeev
- 版本：1.0.6，2026-11-04 发布
- 实际 JitPack 构建产物：
  - group：`com.github.styropyr0`
  - artifact：`PrismalAGSL`
  - version：`1.0.6`
  - 可用依赖：`com.github.styropyr0:PrismalAGSL:1.0.6`
- README 里的多模块坐标 `com.github.styropyr0:PrismalAGSL:Prismal:TAG` 与实际 JitPack 发布布局不一致；本项目以实际构建日志与 AAR 为准。
- **许可证风险：** 仓库根 LICENSE 是 MIT，但 `Prismal/build.gradle.kts` 生成的 POM 把 license 写成 Apache-2.0。当前按 MIT 处理并在 `THIRD_PARTY_NOTICES.md` 记录；商业发布前应向作者确认 POM 字段，或替换为自研玻璃层。

### 3.2 技术能力

`PrismalGlass` 自动选择三级管线：

| 级别 | API | 能力 |
|---|---|---|
| Legacy | 25–30 | 采样背景 + 磨砂表面，无真实模糊/折射 |
| Standard | 31–32 | RenderEffect 背景模糊 + 色彩过滤 |
| Liquid | 33+ | AGSL 边缘折射、色散、镜面高光、深度内阴影 |

核心 API：

- `rememberPrismalGlassLayer()` + `Modifier.prismalGlassLayer(backdrop)`：记录背景采样层。
- `PrismalGlassSurface(backdrop, shape, surfaceColor, tint, blurRadius, ...)`：开箱即用的玻璃容器。
- `PrismalRoundedRectangle(radius)`：连续曲率圆角。
- `PrismalGlassBottomTabs` / `PrismalGlassButton` 等：可选用组件，但本项目不直接使用其默认外观。

### 3.3 构建兼容性

- 实际 AAR `minCompileSdk=37`，因此应用必须 `compileSdk 37`。
- 已在当前机器安装：
  - `platforms;android-37.0`
  - `build-tools;37.0.0`
- 项目使用：
  - JDK 17
  - Gradle 9.5.0
  - AGP 9.3.1
  - Kotlin/Compose Compiler 2.2.21
  - Compose BOM 2026.06.01
- AGP 9 内置 Kotlin 支持，应用模块只应用 `com.android.application` 与 `org.jetbrains.kotlin.plugin.compose`；这与 Prismal 自身的构建方式一致。

### 3.4 性能注意

- 玻璃表面会记录背景并执行 RenderEffect/AGSL，数量越多成本越高。
- 本项目只让以下表面使用真实玻璃：
  - 悬浮底部导航
  - 首页 Hero、统计卡、待复习/周节奏卡
  - 学习主卡、完成卡
  - 搜索/摘要/设置组
- 词库长列表行不使用 AGSL，只用半透明填充、边框和阴影，避免长列表滚动时反复采样。
- 背景使用**静态**渐变，不做无限动画；否则会持续触发 backdrop 重新记录。
- 低端设备/省电模式由 Prismal 自动退化；应用层还会提供“减少透明/玻璃质量”设置（后续阶段）。

---

## 4. 成本对比

| 成本项 | Glasmio | PrismalAGSL + 自研组件 |
|---|---|---|
| 初次接入 | 高（工具链 + 六模块 + 主题/导航接管） | 中（AAR + 背景层 + 玻璃容器） |
| 视觉改造 | 高（要摆脱其 iOS 模板感） | 低（颜色/布局/组件完全自有） |
| 低端兼容 | 差（API 33+） | 好（API 25+ 三级降级） |
| 构建稳定性 | 低（JDK21/NDK/CMake/SDK37） | 中（SDK37 + JitPack；已实测可构建） |
| 后续维护 | 高（跟随框架版本） | 中（可封装替换；接口边界清晰） |
| 商业授权 | Apache-2.0，需保留 NOTICE | MIT（需澄清 POM 字段），保留版权声明 |
| 适配 WordGlass 品牌 | 低 | 高 |

---

## 5. 最终技术方案

```text
WordGlass UI
  ├─ 自有设计系统：颜色 / 字体 / 间距 / 圆角 / 阴影 / 动效
  ├─ 自有组件：GlassCard / 悬浮导航 / 模式切换 / 词卡 / 设置行
  └─ PrismalAGSL（只使用底层 API）
       ├─ rememberPrismalGlassLayer
       ├─ Modifier.prismalGlassLayer
       └─ PrismalGlassSurface + PrismalRoundedRectangle
```

明确不做：

- 不把 Glasmio 的示例 App、主题或组件目录复制进 WordGlass。
- 不把 Prismal 的示例页面或整库源码复制进 WordGlass。
- 不使用 Glasmio 的 SF Symbols/字体资产。
- 不以玻璃效果为由牺牲文字对比度；所有玻璃表面都叠加可读性保护层。

## 6. 许可证落地

项目根目录保留：

- `licenses/OFL-Manrope.txt`：Manrope 字体许可证
- `THIRD_PARTY_NOTICES.md`：PrismalAGSL MIT 声明、Glasmio 仅研究不引入的说明
- PrismalAGSL 的 MIT 版权声明会随 AAR 依赖保留；发布前在“关于/开源许可”页面展示。

## 7. 参考链接

- Glasmio：<https://github.com/NEORUAA/Glasmio>
- Glasmio native materials 文档：<https://github.com/NEORUAA/Glasmio/blob/main/docs/native-materials.md>
- PrismalAGSL：<https://github.com/styropyr0/PrismalAGSL>
- PrismalAGSL JitPack：<https://jitpack.io/#styropyr0/PrismalAGSL>

---

## 8. 0.2.0-beta 工具链补充（2026-11-06）

为接入 Room 代码生成，KSP 与 AGP 9.3 的 built-in Kotlin 无法共存。实际落地方案：

- AGP 9.3.1
- Kotlin Android / Compose Compiler 2.3.10
- KSP 2.3.10
- Room 2.8.5
- `android.builtInKotlin=false`
- `android.newDsl=false`
- 显式应用 `org.jetbrains.kotlin.android`

这是过渡方案，不是长期架构。AGP 10 前必须迁移回 built-in Kotlin，或替换为不依赖 KSP 的持久化代码生成方案。

### 性能决策

- PrismalAGSL 只用于首页 Hero 与底栏液态透镜。
- 列表和普通卡片使用静态磨砂层，避免每屏多个 RenderEffect。
- 导航透镜采样一个只包含底栏内容的 `PrismalGlassLayer`，而不是全屏背景。
- 拖拽位置使用 `mutableFloatStateOf` + `offset`/`graphicsLayer`，不在每帧重组整页。

### FlClash 参考边界

FlClash 的 `plugins/prism_ui` 为 GPL-3.0。WordGlass 只观察其“薄磨砂条 + 圆形液态透镜 + 拖拽吸附”的交互，不复制其 Dart 代码、shader、图标或资源。WordGlass 的透镜由 MIT 的 PrismalAGSL 原语独立实现。
