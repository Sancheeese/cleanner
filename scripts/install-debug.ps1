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

Set-Location $ProjectRoot

if (-not $SkipBuild) {
    & "$ProjectRoot/gradlew.bat" assembleDebug
    Assert-ExitCode "Gradle assembleDebug"
}

if (-not (Test-Path $ApkPath)) {
    throw "Debug APK not found at $ApkPath"
}

$adb = Find-Adb
Assert-DeviceReady

$installArgs = New-AdbArgs @("install", "-r", $ApkPath)
$previousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$installOutput = & $adb @installArgs 2>&1 | ForEach-Object { $_.ToString() }
$installExitCode = $LASTEXITCODE
$ErrorActionPreference = $previousErrorActionPreference
$installOutput | ForEach-Object { Write-Host $_ }
if ($installExitCode -ne 0) {
    $installText = $installOutput | Out-String
    if ($installText.Contains("INSTALL_FAILED_USER_RESTRICTED")) {
        throw "adb install was blocked by the phone. On Xiaomi/Redmi, enable Developer options -> USB debugging, USB debugging (Security settings), and Install via USB, then accept the install prompt on the phone."
    }
    throw "adb install failed with exit code $installExitCode"
}

$launchArgs = New-AdbArgs @("shell", "monkey", "-p", $PackageName, "-c", "android.intent.category.LAUNCHER", "1")
& $adb @launchArgs
Assert-ExitCode "adb launch"

Write-Host ""
Write-Host "Installed and launched $PackageName"
Write-Host "APK: $ApkPath"




