package com.sancheeese.cleanner.core.model

import java.time.Instant

enum class AppOwner(val displayName: String) {
    WeChat("微信"),
    QQ("QQ"),
    Unknown("未知")
}

enum class PrivacyLevel {
    Normal,
    Sensitive,
    Unknown
}

enum class Confidence {
    High,
    Medium,
    Low
}

enum class FileCategory(val displayName: String) {
    Cache("缓存"),
    Thumbnail("缩略图"),
    ChatImage("聊天图片"),
    ChatVideo("聊天视频"),
    ReceivedFile("接收文件"),
    Download("下载文件"),
    ApkPackage("安装包"),
    Log("日志"),
    Temp("临时文件"),
    Database("数据库"),
    Unknown("未知文件"),
    LargeFile("大文件"),
    DuplicateCandidate("疑似重复文件")
}

enum class RiskLevel(val displayName: String) {
    SafeToClean("安全可清理"),
    LikelyCleanable("可能可清理"),
    ReviewRequired("需要确认"),
    KeepRecommended("建议保留")
}

data class FolderProfile(
    val app: AppOwner,
    val pathPattern: String,
    val displayName: String,
    val purpose: String,
    val privacyLevel: PrivacyLevel,
    val cleaningHint: String,
    val confidence: Confidence
)

data class ScannedFile(
    val id: String,
    val app: AppOwner,
    val absolutePath: String,
    val parentPath: String,
    val name: String,
    val extension: String,
    val sizeBytes: Long,
    val lastModifiedAt: Instant,
    val detectedMimeType: String?
)

sealed interface FileMetadata {
    data class Image(val width: Int?, val height: Int?, val format: String?) : FileMetadata
    data class Video(val durationMillis: Long?, val width: Int?, val height: Int?) : FileMetadata
    data class Apk(val appName: String?, val packageName: String?, val versionName: String?) : FileMetadata
    data class Archive(val entryCount: Int?, val compressedSize: Long?) : FileMetadata
    data class Document(val documentType: String?) : FileMetadata
    data object Unknown : FileMetadata
}

data class AnalyzedFile(
    val scannedFile: ScannedFile,
    val folderProfile: FolderProfile,
    val category: FileCategory,
    val riskLevel: RiskLevel,
    val recommendation: String,
    val explanation: String,
    val metadataSummary: String,
    val isDefaultSelected: Boolean
)

data class CleanupRecord(
    val id: String,
    val createdAt: Instant,
    val appsIncluded: Set<AppOwner>,
    val deletedFileCount: Int,
    val failedFileCount: Int,
    val freedBytes: Long,
    val riskSummary: Map<RiskLevel, Int>,
    val deletedPathSamples: List<String>,
    val failureReasons: List<String>
)

data class CleanupCandidate(
    val analyzedFile: AnalyzedFile,
    val selected: Boolean
)
