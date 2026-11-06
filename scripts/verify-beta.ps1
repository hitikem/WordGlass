# WordGlass device acceptance helper.
# Usage: powershell -ExecutionPolicy Bypass -File scripts/verify-beta.ps1
# Requires an unlocked phone with USB debugging enabled.

$ErrorActionPreference = "Stop"
$adb = "D:\AndroidTools\adb.exe"
$root = Split-Path $PSScriptRoot -Parent
$apk = Join-Path $root "dist\WordGlass-0.2.0-beta-debug.apk"
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$out = Join-Path $root "docs\device-evidence\$stamp"
New-Item -ItemType Directory -Force -Path $out | Out-Null

if (-not (Test-Path -LiteralPath $apk)) { throw "APK not found: $apk" }
$devices = & $adb devices | Select-String "`tdevice$"
if (-not $devices) { throw "No adb device. Unlock phone and enable USB debugging." }

Write-Host "Installing $apk"
& $adb install -r -t $apk

Write-Host "Resetting gfxinfo"
& $adb shell dumpsys gfxinfo com.wordglass.app.debug reset | Out-Null

$routes = @("home", "study", "library", "profile")
foreach ($route in $routes) {
    Write-Host "Launching route: $route"
    & $adb shell am start -S -W -n com.wordglass.app.debug/com.wordglass.app.MainActivity --es wordglass_start $route | Out-Null
    Start-Sleep -Seconds 3
    & $adb shell screencap -p "/sdcard/wg-$route.png" | Out-Null
    & $adb pull "/sdcard/wg-$route.png" (Join-Path $out "$route.png") | Out-Null
}

& $adb shell dumpsys gfxinfo com.wordglass.app.debug > (Join-Path $out "gfxinfo.txt")
& $adb logcat -d -t 800 > (Join-Path $out "logcat-tail.txt")
& $adb shell dumpsys package com.wordglass.app.debug | Select-String "versionName|versionCode" > (Join-Path $out "package.txt")

$fatal = Select-String -Path (Join-Path $out "logcat-tail.txt") -Pattern "FATAL EXCEPTION|Fatal signal|ANR in com.wordglass"
if ($fatal) {
    Write-Host "CRASH MARKERS FOUND:"
    $fatal | ForEach-Object { Write-Host $_.Line }
    exit 2
}

Write-Host "Screenshots and logs: $out"
Write-Host "No FATAL/ANR markers in the captured tail."
Write-Host "Run instrumentation separately: .\gradlew.bat :app:connectedDebugAndroidTest"
