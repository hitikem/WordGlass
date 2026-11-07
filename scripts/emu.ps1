# WordGlass emulator launcher
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File scripts\emu.ps1 -Start api35
#   powershell -ExecutionPolicy Bypass -File scripts\emu.ps1 -Start api37
#   powershell -ExecutionPolicy Bypass -File scripts\emu.ps1 -Stop
#   powershell -ExecutionPolicy Bypass -File scripts\emu.ps1 -Status
#
# Both AVDs are tuned for this host (i5-8250U, 4 cores, ~12 GB RAM):
#   720x1600 @ 320dpi, 3 GB RAM, 4 cores, GPU enabled, swiftshader_indirect.
#
# Why swiftshader_indirect: this machine exposes four display adapters (Intel UHD,
# NVIDIA 930MX, OrayIddDriver, GameViewer), and "-gpu host" fails with
# "DisplaySurfaceGlContextHelper context was preempted by others". The software
# backend is slower but stable.

param(
    [ValidateSet("api35", "api37")]
    [string]$Start,
    [switch]$Stop,
    [switch]$Status
)

$ErrorActionPreference = "Stop"

$Sdk = "D:\AndroidSdk"
$Adb = "D:\AndroidTools\adb.exe"
$Emulator = Join-Path $Sdk "emulator\emulator.exe"
$LogDir = "D:\ds\WordGlass\docs"

$Avds = @{
    api35 = @{ Name = "WordGlass_API35"; Label = "Android 15 / API 35"; Note = "works" }
    api37 = @{ Name = "WordGlass_API37_Play"; Label = "Android 17 / API 37 (Google Play)"; Note = "system crashes (SurfaceFlinger assert) - not usable yet" }
}

function Get-RunningEmulator {
    Get-Process -ErrorAction SilentlyContinue |
        Where-Object { $_.ProcessName -match 'qemu-system|emulator' }
}

function Show-Status {
    Write-Host "=== AVDs ==="
    & $Emulator -list-avds
    Write-Host ""
    Write-Host "=== Processes ==="
    $p = Get-RunningEmulator
    if ($p) {
        $p | Select-Object Id, ProcessName, @{n = 'RAM_MB'; e = { [math]::Round($_.WorkingSet64 / 1MB, 0) } } | Format-Table -AutoSize
    } else {
        Write-Host "none"
    }
    Write-Host "=== adb ==="
    & $Adb devices -l
    $boot = (& $Adb -e shell getprop sys.boot_completed 2>&1) -join ''
    if ($boot -match '1') {
        $ver = (& $Adb -e shell getprop ro.build.version.release 2>&1) -join ''
        $sdkv = (& $Adb -e shell getprop ro.build.version.sdk 2>&1) -join ''
        Write-Host "booted: Android $ver (API $sdkv)"
    } else {
        Write-Host "emulator not booted"
    }
}

function Stop-All {
    $p = Get-RunningEmulator
    if (-not $p) {
        Write-Host "No emulator running."
        return
    }
    & $Adb -e emu kill 2>&1 | Out-Null
    Start-Sleep -Seconds 5
    Get-RunningEmulator | ForEach-Object { Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue }
    Write-Host "Emulator stopped."
}

function Start-Avd([string]$Key) {
    $avd = $Avds[$Key]
    if (-not $avd) { throw "Unknown AVD key: $Key" }

    if (Get-RunningEmulator) {
        Write-Host "An emulator is already running; stopping it first."
        Stop-All
        Start-Sleep -Seconds 3
    }

    $env:ANDROID_SDK_ROOT = $Sdk
    $env:ANDROID_HOME = $Sdk

    $log = Join-Path $LogDir ("emu-" + $Key + ".log")
    Write-Host "Starting $($avd.Label)  [$($avd.Name)]"
    if ($avd.Note -ne "works") { Write-Host "WARNING: $($avd.Note)" }

    Start-Process -FilePath $Emulator `
        -ArgumentList @("-avd", $avd.Name, "-no-snapshot", "-gpu", "swiftshader_indirect",
                        "-no-boot-anim", "-no-audio", "-memory", "3072", "-cores", "4") `
        -WorkingDirectory $Sdk `
        -RedirectStandardOutput $log `
        -RedirectStandardError "$log.err" | Out-Null

    Write-Host "Waiting for boot (this can take a few minutes)..."
    for ($i = 1; $i -le 60; $i++) {
        Start-Sleep -Seconds 6
        $boot = (& $Adb -e shell getprop sys.boot_completed 2>&1) -join ''
        $svc = (& $Adb -e shell "service check package" 2>&1) -join ''
        if ($boot -match '1' -and $svc -match 'found') {
            $ver = (& $Adb -e shell getprop ro.build.version.release 2>&1) -join ''
            Write-Host "Booted: Android $ver"
            & $Adb devices -l
            return
        }
        if ($i % 5 -eq 0) { Write-Host "  still booting ($i)..." }
    }

    Write-Host "Boot did not complete. Check the device screen or:"
    Write-Host "  $log"
    Write-Host "  $log.err"
}

if ($Status -or (-not $Start -and -not $Stop)) { Show-Status; return }
if ($Stop) { Stop-All; return }
if ($Start) { Start-Avd $Start; return }
