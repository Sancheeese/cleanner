package com.sancheeese.cleanner.core.scan

import com.sancheeese.cleanner.core.model.AppOwner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
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

    @Test
    fun defaultRootsCoverObservedWeChatAndQqSharedStorageLocations() {
        val storage = File("/storage/emulated/0")
        val roots = LocalFileScanner().defaultRoots(storage)
        val relativePaths = roots.map { it.root.invariantSeparatorsPath.removePrefix("/storage/emulated/0/") }

        assertTrue(relativePaths.contains("tencent/MicroMsg"))
        assertTrue(relativePaths.contains("Pictures/WeiXin"))
        assertTrue(relativePaths.contains("tencent/MobileQQ"))
        assertTrue(relativePaths.contains("tencent/QQfile_recv"))
        assertTrue(relativePaths.contains("tencent/qq_images"))
        assertTrue(relativePaths.contains("tencent/audio"))
        assertTrue(relativePaths.contains("tencent/msflogs"))
    }
}
