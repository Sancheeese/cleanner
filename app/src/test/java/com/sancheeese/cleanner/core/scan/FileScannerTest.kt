package com.sancheeese.cleanner.core.scan

import com.sancheeese.cleanner.core.model.AppOwner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class FileScannerTest {
    @Test
    fun scansFilesUnderProvidedRootWithAppOwner() {
        val root = Files.createTempDirectory("cleanner-scan-test")
        val nested = Files.createDirectories(root.resolve("MicroMsg/image2/a"))
        Files.write(nested.resolve("image.jpg"), "image".toByteArray())

        val scanner = LocalFileScanner()
        val result = scanner.scanRoot(ScanRoot(AppOwner.WeChat, root.toFile()))

        assertEquals(1, result.files.size)
        assertEquals(AppOwner.WeChat, result.files.single().app)
        assertEquals("jpg", result.files.single().extension)
        assertTrue(result.totalBytes > 0)
    }
}
