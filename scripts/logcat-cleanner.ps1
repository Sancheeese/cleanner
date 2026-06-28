param(
    [string]$Device = ""
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
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

$adb = Find-Adb

Write-Host "Showing recent crash/error logs for $PackageName. Press Ctrl+C to stop."
$clearArgs = New-AdbArgs @("logcat", "-c")
& $adb @clearArgs
Assert-ExitCode "adb logcat clear"

$logcatArgs = New-AdbArgs @("logcat", "$PackageName:E", "AndroidRuntime:E", "*:S")
& $adb @logcatArgs
