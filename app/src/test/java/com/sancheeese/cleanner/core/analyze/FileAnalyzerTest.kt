package com.sancheeese.cleanner.core.analyze

import com.sancheeese.cleanner.core.model.AppOwner
import com.sancheeese.cleanner.core.model.FileCategory
import com.sancheeese.cleanner.core.model.FileMetadata
import com.sancheeese.cleanner.core.model.RiskLevel
import com.sancheeese.cleanner.core.model.ScannedFile
import com.sancheeese.cleanner.core.rules.FolderRuleRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class FileAnalyzerTest {
    private val analyzer = FileAnalyzer(FolderRuleRegistry.default())

    @Test
    fun analyzesWeChatChatImageAsReviewRequiredWithFolderPurpose() {
        val file = scannedFile(
            app = AppOwner.WeChat,
            path = "/storage/emulated/0/Android/media/com.tencent.mm/MicroMsg/image2/a/b/IMG_001.jpg",
            extension = "jpg"
        )

        val analyzed = analyzer.analyze(file, FileMetadata.Image(width = 1080, height = 1920, format = "jpg"))

        assertEquals(FileCategory.ChatImage, analyzed.category)
        assertEquals(RiskLevel.ReviewRequired, analyzed.riskLevel)
        assertFalse(analyzed.isDefaultSelected)
        assertTrue(analyzed.folderProfile.purpose.contains("图片"))
        assertTrue(analyzed.explanation.contains("查看"))
    }

    @Test
    fun analyzesApkPackageAsLikelyCleanableAndDefaultSelected() {
        val file = scannedFile(
            app = AppOwner.QQ,
            path = "/storage/emulated/0/tencent/QQfile_recv/example.apk",
            extension = "apk"
        )

        val analyzed = analyzer.analyze(
            file,
            FileMetadata.Apk(appName = "Example", packageName = "com.example", versionName = "1.0")
        )

        assertEquals(FileCategory.ApkPackage, analyzed.category)
        assertEquals(RiskLevel.LikelyCleanable, analyzed.riskLevel)
        assertTrue(analyzed.isDefaultSelected)
        assertTrue(analyzed.metadataSummary.contains("Example"))
        assertTrue(analyzed.recommendation.contains("安装包"))
    }

    private fun scannedFile(app: AppOwner, path: String, extension: String): ScannedFile {
        return ScannedFile(
            id = path,
            app = app,
            absolutePath = path,
            parentPath = path.substringBeforeLast('/'),
            name = path.substringAfterLast('/'),
            extension = extension,
            sizeBytes = 2_400_000,
            lastModifiedAt = Instant.parse("2026-06-01T12:00:00Z"),
            detectedMimeType = null
        )
    }
}
