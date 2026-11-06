# Install the current WordGlass internal beta on the first connected device.
$ErrorActionPreference = "Stop"
$adb = "D:\AndroidTools\adb.exe"
$apk = Join-Path $PSScriptRoot "..\dist\WordGlass-0.2.0-beta-debug.apk"
if (-not (Test-Path -LiteralPath $apk)) { throw "APK not found: $apk" }
$devices = & $adb devices | Select-String "`tdevice$"
if (-not $devices) { throw "No adb device connected. Unlock the phone and enable USB debugging." }
& $adb install -r -t $apk
& $adb shell am start -S -W -n com.wordglass.app.debug/com.wordglass.app.MainActivity
Write-Output "Installed WordGlass 0.2.0-beta."
