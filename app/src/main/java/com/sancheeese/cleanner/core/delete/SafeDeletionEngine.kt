package com.sancheeese.cleanner.core.delete

import java.io.File

data class DeletionRequest(
    val path: String,
    val sizeBytes: Long,
    val selected: Boolean
)

data class DeletedFile(
    val path: String,
    val sizeBytes: Long
)

data class FailedDeletion(
    val path: String,
    val reason: String
)

data class DeletionResult(
    val deleted: List<DeletedFile>,
    val failed: List<FailedDeletion>
) {
    val freedBytes: Long = deleted.sumOf { it.sizeBytes }
}

class SafeDeletionEngine {
    fun delete(requests: List<DeletionRequest>): DeletionResult {
        val deleted = mutableListOf<DeletedFile>()
        val failed = mutableListOf<FailedDeletion>()

        requests.filter { it.selected }.forEach { request ->
            val file = File(request.path)
            when {
                !file.exists() -> failed += FailedDeletion(request.path, "文件不存在")
                !file.isFile -> failed += FailedDeletion(request.path, "不是普通文件，已跳过")
                file.delete() -> deleted += DeletedFile(request.path, request.sizeBytes)
                else -> failed += FailedDeletion(request.path, "删除失败，可能权限不足或文件被占用")
            }
        }

        return DeletionResult(deleted, failed)
    }
}
