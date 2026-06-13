package com.sancheeese.cleanner.core.history

import android.content.Context
import com.sancheeese.cleanner.core.delete.DeletionResult
import com.sancheeese.cleanner.core.model.AppOwner
import com.sancheeese.cleanner.core.model.CleanupRecord
import com.sancheeese.cleanner.core.model.RiskLevel
import java.time.Instant
import java.util.UUID

interface CleanupHistoryStore {
    fun latest(): CleanupRecord?
    fun save(record: CleanupRecord)
}

class InMemoryCleanupHistoryStore : CleanupHistoryStore {
    private var record: CleanupRecord? = null

    override fun latest(): CleanupRecord? = record

    override fun save(record: CleanupRecord) {
        this.record = record
    }
}

class PreferencesCleanupHistoryStore(context: Context) : CleanupHistoryStore {
    private val prefs = context.getSharedPreferences("cleanup_history", Context.MODE_PRIVATE)

    override fun latest(): CleanupRecord? {
        val id = prefs.getString("id", null) ?: return null
        return CleanupRecord(
            id = id,
            createdAt = Instant.ofEpochMilli(prefs.getLong("createdAt", 0L)),
            appsIncluded = prefs.getStringSet("appsIncluded", emptySet()).orEmpty().mapNotNull {
                runCatching { AppOwner.valueOf(it) }.getOrNull()
            }.toSet(),
            deletedFileCount = prefs.getInt("deletedFileCount", 0),
            failedFileCount = prefs.getInt("failedFileCount", 0),
            freedBytes = prefs.getLong("freedBytes", 0L),
            riskSummary = emptyMap(),
            deletedPathSamples = prefs.getStringSet("deletedPathSamples", emptySet()).orEmpty().toList(),
            failureReasons = prefs.getStringSet("failureReasons", emptySet()).orEmpty().toList()
        )
    }

    override fun save(record: CleanupRecord) {
        prefs.edit()
            .putString("id", record.id)
            .putLong("createdAt", record.createdAt.toEpochMilli())
            .putStringSet("appsIncluded", record.appsIncluded.map { it.name }.toSet())
            .putInt("deletedFileCount", record.deletedFileCount)
            .putInt("failedFileCount", record.failedFileCount)
            .putLong("freedBytes", record.freedBytes)
            .putStringSet("deletedPathSamples", record.deletedPathSamples.toSet())
            .putStringSet("failureReasons", record.failureReasons.toSet())
            .apply()
    }
}

fun DeletionResult.toCleanupRecord(
    appsIncluded: Set<AppOwner>,
    riskSummary: Map<RiskLevel, Int>
): CleanupRecord {
    return CleanupRecord(
        id = UUID.randomUUID().toString(),
        createdAt = Instant.now(),
        appsIncluded = appsIncluded,
        deletedFileCount = deleted.size,
        failedFileCount = failed.size,
        freedBytes = freedBytes,
        riskSummary = riskSummary,
        deletedPathSamples = deleted.take(5).map { it.path },
        failureReasons = failed.map { it.reason }.distinct()
    )
}
