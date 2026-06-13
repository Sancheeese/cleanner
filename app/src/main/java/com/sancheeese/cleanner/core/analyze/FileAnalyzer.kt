package com.sancheeese.cleanner.core.analyze

import com.sancheeese.cleanner.core.model.AnalyzedFile
import com.sancheeese.cleanner.core.model.FileCategory
import com.sancheeese.cleanner.core.model.FileMetadata
import com.sancheeese.cleanner.core.model.RiskLevel
import com.sancheeese.cleanner.core.model.ScannedFile
import com.sancheeese.cleanner.core.rules.FolderRuleRegistry

class FileAnalyzer(private val folderRules: FolderRuleRegistry) {
    fun analyze(file: ScannedFile, metadata: FileMetadata = FileMetadata.Unknown): AnalyzedFile {
        val folderProfile = folderRules.match(file.app, file.parentPath)
        val category = detectCategory(file, metadata)
        val riskLevel = detectRisk(category, file)
        return AnalyzedFile(
            scannedFile = file,
            folderProfile = folderProfile,
            category = category,
            riskLevel = riskLevel,
            recommendation = recommendation(category, riskLevel),
            explanation = explanation(category, folderProfile.cleaningHint),
            metadataSummary = metadataSummary(metadata, file),
            isDefaultSelected = riskLevel == RiskLevel.SafeToClean || riskLevel == RiskLevel.LikelyCleanable
        )
    }

    fun analyze(files: List<Pair<ScannedFile, FileMetadata>>): List<AnalyzedFile> {
        return files.map { (file, metadata) -> analyze(file, metadata) }
    }

    private fun detectCategory(file: ScannedFile, metadata: FileMetadata): FileCategory {
        val path = file.absolutePath.lowercase()
        val ext = file.extension.lowercase()
        return when {
            metadata is FileMetadata.Apk || ext == "apk" -> FileCategory.ApkPackage
            path.contains("cache") -> FileCategory.Cache
            path.contains("thumb") || path.contains("thumbnail") -> FileCategory.Thumbnail
            path.contains("log") || ext == "log" || ext == "xlog" -> FileCategory.Log
            path.contains("tmp") || ext == "tmp" || ext == "temp" -> FileCategory.Temp
            ext in imageExtensions -> FileCategory.ChatImage
            ext in videoExtensions -> FileCategory.ChatVideo
            ext in documentExtensions -> FileCategory.ReceivedFile
            ext in archiveExtensions -> FileCategory.ReceivedFile
            ext in databaseExtensions -> FileCategory.Database
            file.sizeBytes >= LARGE_FILE_BYTES -> FileCategory.LargeFile
            else -> FileCategory.Unknown
        }
    }

    private fun detectRisk(category: FileCategory, file: ScannedFile): RiskLevel {
        return when (category) {
            FileCategory.Cache,
            FileCategory.Thumbnail,
            FileCategory.Temp -> RiskLevel.SafeToClean
            FileCategory.Log,
            FileCategory.ApkPackage -> RiskLevel.LikelyCleanable
            FileCategory.ChatImage,
            FileCategory.ChatVideo,
            FileCategory.ReceivedFile,
            FileCategory.Download,
            FileCategory.LargeFile,
            FileCategory.DuplicateCandidate -> RiskLevel.ReviewRequired
            FileCategory.Database,
            FileCategory.Unknown -> if (file.sizeBytes >= LARGE_FILE_BYTES) {
                RiskLevel.ReviewRequired
            } else {
                RiskLevel.KeepRecommended
            }
        }
    }

    private fun recommendation(category: FileCategory, riskLevel: RiskLevel): String {
        return when (category) {
            FileCategory.ApkPackage -> "这是安装包文件，安装完成后通常可以清理。"
            FileCategory.Cache -> "这是缓存文件，通常可以清理，应用需要时会重新生成。"
            FileCategory.Thumbnail -> "这是缩略图文件，通常可以清理。"
            FileCategory.Log -> "这是日志文件，旧日志通常可以清理。"
            FileCategory.Temp -> "这是临时文件，通常可以清理。"
            FileCategory.ChatImage -> "这是聊天图片，需要查看内容后决定是否清理。"
            FileCategory.ChatVideo -> "这是聊天视频，通常占用较大，建议确认后清理。"
            FileCategory.ReceivedFile -> "这是接收或下载的文件，可能仍有用，建议确认后清理。"
            FileCategory.Database -> "这是数据库类文件，可能包含应用关键数据，建议保留。"
            FileCategory.LargeFile -> "这是大文件，建议确认用途后再清理。"
            FileCategory.Download -> "这是下载文件，建议确认后清理。"
            FileCategory.Unknown -> if (riskLevel == RiskLevel.KeepRecommended) "用途未知，建议保留。" else "用途未知但占用较大，建议确认后清理。"
            FileCategory.DuplicateCandidate -> "这是未来重复文件检测的候选项，当前版本需要手动确认。"
        }
    }

    private fun explanation(category: FileCategory, folderHint: String): String {
        return "${category.displayName}。$folderHint"
    }

    private fun metadataSummary(metadata: FileMetadata, file: ScannedFile): String {
        return when (metadata) {
            is FileMetadata.Image -> listOfNotNull(
                metadata.format?.uppercase(),
                dimensions(metadata.width, metadata.height)
            ).joinToString(" · ").ifBlank { "图片文件" }
            is FileMetadata.Video -> listOfNotNull(
                duration(metadata.durationMillis),
                dimensions(metadata.width, metadata.height)
            ).joinToString(" · ").ifBlank { "视频文件" }
            is FileMetadata.Apk -> listOfNotNull(
                metadata.appName,
                metadata.packageName,
                metadata.versionName?.let { "v$it" }
            ).joinToString(" · ").ifBlank { "安装包文件" }
            is FileMetadata.Archive -> metadata.entryCount?.let { "压缩包 · $it 个条目" } ?: "压缩包"
            is FileMetadata.Document -> metadata.documentType ?: "文档文件"
            FileMetadata.Unknown -> file.detectedMimeType ?: "未读取详细元数据"
        }
    }

    private fun dimensions(width: Int?, height: Int?): String? {
        return if (width != null && height != null) "${width}x$height" else null
    }

    private fun duration(durationMillis: Long?): String? {
        if (durationMillis == null) return null
        val totalSeconds = durationMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private companion object {
        const val LARGE_FILE_BYTES = 100L * 1024L * 1024L
        val imageExtensions = setOf("jpg", "jpeg", "png", "webp", "gif", "heic")
        val videoExtensions = setOf("mp4", "mov", "m4v", "3gp", "mkv", "avi")
        val documentExtensions = setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")
        val archiveExtensions = setOf("zip", "rar", "7z", "tar", "gz")
        val databaseExtensions = setOf("db", "sqlite", "sqlite3")
    }
}
