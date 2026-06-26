package com.sancheeese.cleanner.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class ScreenBackNavigationSourceTest {
    @Test
    fun sharedScreenScaffoldExposesOptionalBackAction() {
        val source = readSource("app/src/main/java/com/sancheeese/cleanner/ui/screens/Common.kt")

        assertTrue(source.contains("onBack: (() -> Unit)? = null"))
        assertTrue(source.contains("IconButton(onClick = onBack)"))
    }

    @Test
    fun nonHomeScreensPassBackActionsToScaffold() {
        val files = listOf(
            "AnalysisScreen.kt",
            "DetailScreen.kt",
            "CleanupConfirmScreen.kt",
            "CleanupResultScreen.kt",
            "ScanningScreen.kt"
        )

        files.forEach { file ->
            val source = readSource("app/src/main/java/com/sancheeese/cleanner/ui/screens/$file")
            assertTrue("$file should pass onBack to ScreenScaffold", source.contains("onBack ="))
        }
    }

    private fun readSource(path: String): String {
        val direct = Paths.get(path)
        val fromAppModule = Paths.get("..").resolve(path).normalize()
        val resolved = when {
            Files.exists(direct) -> direct
            Files.exists(fromAppModule) -> fromAppModule
            else -> direct
        }
        return String(Files.readAllBytes(resolved))
    }
}


