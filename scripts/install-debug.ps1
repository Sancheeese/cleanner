param(
    [string]$Device = "",
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ApkPath = Join-Path $ProjectRoot "app/build/outputs/apk/debug/app-debug.apk"
$PackageName = "com.sancheeese.cleanner"
$ActivityName = ".MainActivity"

function Find-Adb {
    $localAdb = Join-Path $ProjectRoot ".tmp/android-sdk/platform-tools/adb.exe"
    if (Test-Path $localAdb) {
        return $localAdb
    }

    $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
    if ($adbCommand) {
        return $adbCommand.Source
    }

    throw "adb was not found. Install Android Studio platform-tools or build once in this repo so .tmp/android-sdk exists."
}

function Adb-Args {
    param([string[]]$Args)
    if ($Device.Trim().Length -gt 0) {
        return @("-s", $Device) + $Args
    }
    return $Args
}

Set-Location $ProjectRoot

if (-not $SkipBuild) {
    & "$ProjectRoot/gradlew.bat" assembleDebug
}

if (-not (Test-Path $ApkPath)) {
    throw "Debug APK not found at $ApkPath"
}

$adb = Find-Adb
& $adb devices

& $adb (Adb-Args @("install", "-r", $ApkPath))
& $adb (Adb-Args @("shell", "monkey", "-p", $PackageName, "-c", "android.intent.category.LAUNCHER", "1"))

Write-Host ""
Write-Host "Installed and launched $PackageName"
Write-Host "APK: $ApkPath"
