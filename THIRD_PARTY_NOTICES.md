# Third-party notices

WordGlass UI prototype includes or depends on the following third-party components.
All names and licenses are recorded for review; this is not legal advice.

## PrismalAGSL 1.0.6

- Project: <https://github.com/styropyr0/PrismalAGSL>
- Author: Saurav Sajeev
- Repository license: MIT
- Used as a local AAR at `app/libs/PrismalAGSL-1.0.6.aar`
- AAR SHA-256: `FF99681709C5CD44B87306A905C39A47C7CAFE37F381C62B7BEDDA9C5FAF654B`

MIT License

Copyright (c) 2026 Saurav Sajeev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

**License note:** The upstream `Prismal/build.gradle.kts` publishes a POM whose
license element says Apache-2.0, while the repository `LICENSE` is MIT. WordGlass
records the repository license above and will confirm or replace this dependency
before commercial distribution.

## Manrope

- Project: <https://github.com/google/fonts/tree/main/ofl/manrope>
- License: SIL Open Font License 1.1
- Full text: `licenses/OFL-Manrope.txt`
- File: `app/src/main/res/font/manrope_variable.ttf`

## Glasmio (research only)

- Project: <https://github.com/NEORUAA/Glasmio>
- License: Apache-2.0
- **Not bundled.** Glasmio was evaluated for material recipes, motion behavior,
  and rendering architecture. No Glasmio source, fonts, icons, or sample app
  assets are included in WordGlass.

## AndroidX / Jetpack Compose / Material 3

Copyright The Android Open Source Project, licensed under Apache License 2.0.
Dependency versions are managed by `androidx.compose:compose-bom:2026.06.01`.

## Kotlin

Copyright JetBrains s.r.o., licensed under Apache License 2.0.

## AndroidX Room / DataStore

Copyright The Android Open Source Project, licensed under Apache License 2.0.

## Kotlin Symbol Processing (KSP) / Robolectric

- KSP: Copyright Google LLC, Apache License 2.0; build-time code generation only.
- Robolectric: Copyright Robolectric contributors, MIT License; test-only dependency, not packaged in the APK.

## FlClash / prism_ui

- Project: <https://github.com/chen08209/FlClash>
- License: GPL-3.0
- **No source code, shaders, icons, or assets are bundled.** FlClash was inspected only as an interaction reference for the floating dock and liquid lens. WordGlass implements the behavior independently with PrismalAGSL (MIT).
