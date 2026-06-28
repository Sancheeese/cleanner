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
    param([string[]]$CommandArgs)
    if ($Device.Trim().Length -gt 0) {
        return @("-s", $Device) + $CommandArgs
    }
    return $CommandArgs
}

function Assert-ExitCode {
    param([string]$Step)
    if ($LASTEXITCODE -ne 0) {
        throw "$Step failed with exit code $LASTEXITCODE"
    }
}

function Get-ConnectedDevices {
    $deviceOutput = & $adb devices
    Assert-ExitCode "adb devices"

    return $deviceOutput |
        Select-Object -Skip 1 |
        Where-Object { $_.Trim().Length -gt 0 } |
        ForEach-Object {
            $parts = $_ -split "\s+"
            [PSCustomObject]@{
                Serial = $parts[0]
                State = $parts[1]
                Raw = $_
            }
        }
}

function Assert-DeviceReady {
    $devices = @(Get-ConnectedDevices)
    if ($Device.Trim().Length -gt 0) {
        $matched = $devices | Where-Object { $_.Serial -eq $Device }
        if (-not $matched) {
            throw "Device '$Device' was not found. Run 'adb devices -l' to check the connected device id."
        }
        if ($matched.State -ne "device") {
            throw "Device '$Device' is '$($matched.State)', not ready. Check the phone screen and allow USB debugging."
        }
        return
    }

    $readyDevices = @($devices | Where-Object { $_.State -eq "device" })
    if ($readyDevices.Count -eq 0) {
        throw "No ready Android device found. Connect the phone, enable USB debugging, and allow the computer on the phone."
    }
    if ($readyDevices.Count -gt 1) {
        $ids = ($readyDevices | ForEach-Object { $_.Serial }) -join ", "
        throw "Multiple Android devices are connected: $ids. Re-run with -Device <adb-device-id>."
    }
}

$adb = Find-Adb
Assert-DeviceReady

Write-Host "Showing recent crash/error logs for $PackageName. Press Ctrl+C to stop."
$clearArgs = New-AdbArgs @("logcat", "-c")
& $adb @clearArgs
Assert-ExitCode "adb logcat clear"

$logcatArgs = New-AdbArgs @("logcat", "$PackageName:E", "AndroidRuntime:E", "*:S")
& $adb @logcatArgs

