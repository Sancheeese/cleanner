param(
    [string]$Device = "",
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ApkPath = Join-Path $ProjectRoot "app/build/outputs/apk/debug/app-debug.apk"
$PackageName = "com.sancheeese.cleanner"

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

function New-AdbArgs {
    param([string[]]$Args)
    if ($Device.Trim().Length -gt 0) {
        return @("-s", $Device) + $Args
    }
    return $Args
}

function Assert-ExitCode {
    param([string]$Step)
    if ($LASTEXITCODE -ne 0) {
        throw "$Step failed with exit code $LASTEXITCODE"
    }
}

Set-Location $ProjectRoot

if (-not $SkipBuild) {
    & "$ProjectRoot/gradlew.bat" assembleDebug
    Assert-ExitCode "Gradle assembleDebug"
}

if (-not (Test-Path $ApkPath)) {
    throw "Debug APK not found at $ApkPath"
}

$adb = Find-Adb
$devicesArgs = New-AdbArgs @("devices")
& $adb @devicesArgs
Assert-ExitCode "adb devices"

$installArgs = New-AdbArgs @("install", "-r", $ApkPath)
& $adb @installArgs
Assert-ExitCode "adb install"

$launchArgs = New-AdbArgs @("shell", "monkey", "-p", $PackageName, "-c", "android.intent.category.LAUNCHER", "1")
& $adb @launchArgs
Assert-ExitCode "adb launch"

Write-Host ""
Write-Host "Installed and launched $PackageName"
Write-Host "APK: $ApkPath"
