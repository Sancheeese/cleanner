package com.sancheeese.cleanner.core.metadata

import android.content.Context
import android.media.MediaMetadataRetriever
import com.sancheeese.cleanner.core.model.FileMetadata
import com.sancheeese.cleanner.core.model.ScannedFile

interface MetadataReader {
    fun read(file: ScannedFile): FileMetadata
}

class AndroidMetadataReader(private val context: Context) : MetadataReader {
    override fun read(file: ScannedFile): FileMetadata {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg", "png", "webp", "gif", "heic" -> readImage(file)
            "mp4", "mov", "m4v", "3gp", "mkv", "avi" -> readVideo(file)
            "apk" -> readApk(file)
            "zip", "rar", "7z" -> FileMetadata.Archive(entryCount = null, compressedSize = file.sizeBytes)
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt" -> FileMetadata.Document(file.extension.uppercase())
            else -> FileMetadata.Unknown
        }
    }

    private fun readImage(file: ScannedFile): FileMetadata {
        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        android.graphics.BitmapFactory.decodeFile(file.absolutePath, options)
        return FileMetadata.Image(
            width = options.outWidth.takeIf { it > 0 },
            height = options.outHeight.takeIf { it > 0 },
            format = file.extension
        )
    }

    private fun readVideo(file: ScannedFile): FileMetadata {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            retriever.release()
            FileMetadata.Video(durationMillis = duration, width = width, height = height)
        } catch (_: RuntimeException) {
            FileMetadata.Video(durationMillis = null, width = null, height = null)
        }
    }

    private fun readApk(file: ScannedFile): FileMetadata {
        val packageInfo = context.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
        return FileMetadata.Apk(
            appName = packageInfo?.applicationInfo?.loadLabel(context.packageManager)?.toString(),
            packageName = packageInfo?.packageName,
            versionName = packageInfo?.versionName
        )
    }
}

class BasicMetadataReader : MetadataReader {
    override fun read(file: ScannedFile): FileMetadata {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg", "png", "webp", "gif", "heic" -> FileMetadata.Image(null, null, file.extension)
            "mp4", "mov", "m4v", "3gp", "mkv", "avi" -> FileMetadata.Video(null, null, null)
            "apk" -> FileMetadata.Apk(null, null, null)
            "zip", "rar", "7z" -> FileMetadata.Archive(null, file.sizeBytes)
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt" -> FileMetadata.Document(file.extension.uppercase())
            else -> FileMetadata.Unknown
        }
    }
}
