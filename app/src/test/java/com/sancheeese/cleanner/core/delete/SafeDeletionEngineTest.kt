package com.sancheeese.cleanner.core.delete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class SafeDeletionEngineTest {
    @Test
    fun deletesOnlySelectedFilesAndReportsFreedBytes() {
        val dir = Files.createTempDirectory("cleanner-delete-test")
        val selected = Files.write(dir.resolve("selected.tmp"), "selected".toByteArray())
        val unselected = Files.write(dir.resolve("unselected.tmp"), "unselected".toByteArray())
        val engine = SafeDeletionEngine()

        val result = engine.delete(
            listOf(
                DeletionRequest(path = selected.toString(), sizeBytes = Files.size(selected), selected = true),
                DeletionRequest(path = unselected.toString(), sizeBytes = Files.size(unselected), selected = false)
            )
        )

        assertEquals(1, result.deleted.size)
        assertEquals(0, result.failed.size)
        assertEquals("selected".length.toLong(), result.freedBytes)
        assertFalse(Files.exists(selected))
        assertTrue(Files.exists(unselected))
    }
}
