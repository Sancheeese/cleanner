package com.sancheeese.cleanner.scripts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class PhoneTestingScriptTest {
    private val dollar = '$'

    @Test
    fun installScriptUsesSplattingForAdbArguments() {
        val script = readSource("scripts/install-debug.ps1")

        assertTrue(script.contains("& ${dollar}adb @installArgs"))
        assertTrue(script.contains("& ${dollar}adb @launchArgs"))
        assertTrue(script.contains("Assert-DeviceReady"))
        assertTrue(script.contains("No ready Android device found"))
        assertTrue(script.contains("Multiple Android devices are connected"))
        assertTrue(script.contains("INSTALL_FAILED_USER_RESTRICTED"))
        assertTrue(script.contains("Install via USB"))
        assertTrue(script.contains("${dollar}CommandArgs"))
        assertFalse(script.contains("[string[]]${dollar}Args"))
        assertFalse(script.contains("& ${dollar}adb (Adb-Args"))
    }

    @Test
    fun logcatScriptUsesSplattingForAdbArguments() {
        val script = readSource("scripts/logcat-cleanner.ps1")

        assertTrue(script.contains("& ${dollar}adb @clearArgs"))
        assertTrue(script.contains("& ${dollar}adb @logcatArgs"))
        assertTrue(script.contains("${dollar}CommandArgs"))
        assertFalse(script.contains("[string[]]${dollar}Args"))
        assertFalse(script.contains("& ${dollar}adb (Adb-Args"))
    }

    private fun readSource(path: String): String {
        val direct = Paths.get(path)
        val parent = Paths.get("..").resolve(path).normalize()
        val sourcePath = when {
            Files.exists(direct) -> direct
            Files.exists(parent) -> parent
            else -> direct
        }
        return String(Files.readAllBytes(sourcePath))
    }
}





