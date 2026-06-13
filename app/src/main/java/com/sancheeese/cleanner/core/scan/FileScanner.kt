package com.sancheeese.cleanner.core.scan

import android.os.Environment
import com.sancheeese.cleanner.core.model.AppOwner
import com.sancheeese.cleanner.core.model.ScannedFile
import java.io.File
import java.time.Instant

data class ScanRoot(
    val app: AppOwner,
    val root: File,
    val label: String = root.absolutePath
)

data class ScanProgress(
    val currentRoot: ScanRoot,
    val currentPath: String,
    val filesFound: Int,
    val bytesFound: Long
)

data class ScanResult(
    val files: List<ScannedFile>,
    val totalBytes: Long,
    val incomplete: Boolean,
    val missingRoots: List<ScanRoot>
)

fun interface ScanCancellation {
    fun isCancelled(): Boolean
}

class LocalFileScanner {
    fun defaultRoots(externalStorage: File = Environment.getExternalStorageDirectory()): List<ScanRoot> {
        return listOf(
            ScanRoot(AppOwner.WeChat, File(externalStorage, "Android/media/com.tencent.mm"), "微信 Android/media"),
            ScanRoot(AppOwner.WeChat, File(externalStorage, "tencent/MicroMsg"), "微信 tencent/MicroMsg"),
            ScanRoot(AppOwner.QQ, File(externalStorage, "Android/media/com.tencent.mobileqq"), "QQ Android/media"),
            ScanRoot(AppOwner.QQ, File(externalStorage, "tencent/QQfile_recv"), "QQ 接收文件"),
            ScanRoot(AppOwner.QQ, File(externalStorage, "Tencent/MobileQQ"), "QQ MobileQQ")
        )
    }

    fun scanRoots(
        roots: List<ScanRoot>,
        cancellation: ScanCancellation = ScanCancellation { false },
        onProgress: (ScanProgress) -> Unit = {}
    ): ScanResult {
        val files = mutableListOf<ScannedFile>()
        val missing = mutableListOf<ScanRoot>()
        var bytes = 0L
        var incomplete = false

        for (root in roots) {
            if (cancellation.isCancelled()) {
                incomplete = true
                break
            }
            if (!root.root.exists()) {
                missing += root
                continue
            }
            val result = scanRoot(root, cancellation) { progress ->
                onProgress(progress.copy(filesFound = files.size + progress.filesFound, bytesFound = bytes + progress.bytesFound))
            }
            files += result.files
            bytes += result.totalBytes
            if (result.incomplete) {
                incomplete = true
                break
            }
        }

        return ScanResult(files = files, totalBytes = bytes, incomplete = incomplete, missingRoots = missing)
    }

    fun scanRoot(
        root: ScanRoot,
        cancellation: ScanCancellation = ScanCancellation { false },
        onProgress: (ScanProgress) -> Unit = {}
    ): ScanResult {
        val files = mutableListOf<ScannedFile>()
        var bytes = 0L
        var incomplete = false

        root.root.walkTopDown().onEnter {
            !cancellation.isCancelled()
        }.forEach { file ->
            if (cancellation.isCancelled()) {
                incomplete = true
                return@forEach
            }
            if (file.isFile) {
                val scanned = file.toScannedFile(root.app)
                files += scanned
                bytes += scanned.sizeBytes
                if (files.size % 25 == 0) {
                    onProgress(ScanProgress(root, file.absolutePath, files.size, bytes))
                }
            }
        }
        onProgress(ScanProgress(root, root.root.absolutePath, files.size, bytes))
        return ScanResult(files = files, totalBytes = bytes, incomplete = incomplete, missingRoots = emptyList())
    }

    private fun File.toScannedFile(app: AppOwner): ScannedFile {
        val ext = extension.lowercase()
        return ScannedFile(
            id = absolutePath,
            app = app,
            absolutePath = absolutePath,
            parentPath = parentFile?.absolutePath.orEmpty(),
            name = name,
            extension = ext,
            sizeBytes = length(),
            lastModifiedAt = Instant.ofEpochMilli(lastModified()),
            detectedMimeType = guessMimeType(ext)
        )
    }

    private fun guessMimeType(extension: String): String? {
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "apk" -> "application/vnd.android.package-archive"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            else -> null
        }
    }
}
