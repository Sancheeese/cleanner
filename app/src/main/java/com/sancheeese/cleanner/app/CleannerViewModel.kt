package com.sancheeese.cleanner.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sancheeese.cleanner.core.analyze.FileAnalyzer
import com.sancheeese.cleanner.core.delete.DeletionRequest
import com.sancheeese.cleanner.core.delete.DeletionResult
import com.sancheeese.cleanner.core.delete.SafeDeletionEngine
import com.sancheeese.cleanner.core.history.CleanupHistoryStore
import com.sancheeese.cleanner.core.history.PreferencesCleanupHistoryStore
import com.sancheeese.cleanner.core.history.toCleanupRecord
import com.sancheeese.cleanner.core.metadata.AndroidMetadataReader
import com.sancheeese.cleanner.core.model.AnalyzedFile
import com.sancheeese.cleanner.core.model.AppOwner
import com.sancheeese.cleanner.core.model.CleanupRecord
import com.sancheeese.cleanner.core.model.FileCategory
import com.sancheeese.cleanner.core.model.FileMetadata
import com.sancheeese.cleanner.core.model.RiskLevel
import com.sancheeese.cleanner.core.model.ScannedFile
import com.sancheeese.cleanner.core.rules.FolderRuleRegistry
import com.sancheeese.cleanner.core.scan.LocalFileScanner
import com.sancheeese.cleanner.core.scan.ScanProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

class CleannerViewModel(
    application: Application,
    private val permissionGateway: PermissionGateway = PermissionGateway(application),
    private val scanner: LocalFileScanner = LocalFileScanner(),
    private val analyzer: FileAnalyzer = FileAnalyzer(FolderRuleRegistry.default()),
    private val deletionEngine: SafeDeletionEngine = SafeDeletionEngine(),
    private val historyStore: CleanupHistoryStore = PreferencesCleanupHistoryStore(application),
    private val metadataReader: AndroidMetadataReader = AndroidMetadataReader(application)
) : AndroidViewModel(application) {
    constructor(application: Application) : this(
        application = application,
        permissionGateway = PermissionGateway(application),
        scanner = LocalFileScanner(),
        analyzer = FileAnalyzer(FolderRuleRegistry.default()),
        deletionEngine = SafeDeletionEngine(),
        historyStore = PreferencesCleanupHistoryStore(application),
        metadataReader = AndroidMetadataReader(application)
    )

    private val _state = MutableStateFlow(
        CleannerUiState(
            hasPermission = permissionGateway.hasAllFilesAccess(),
            latestRecord = historyStore.latest()
        )
    )
    val state: StateFlow<CleannerUiState> = _state.asStateFlow()

    private var scanJob: Job? = null
    private var cancelScan = false

    fun refreshPermission() {
        _state.update { it.copy(hasPermission = permissionGateway.hasAllFilesAccess()) }
    }

    fun openPermissionSettings() {
        getApplication<Application>().startActivity(permissionGateway.permissionIntent())
    }

    fun startScan() {
        if (!_state.value.hasPermission) return
        cancelScan = false
        scanJob?.cancel()
        _state.update {
            it.copy(
                screen = CleannerScreen.Scanning,
                scanProgress = null,
                scanIncomplete = false,
                errorMessage = null
            )
        }
        scanJob = viewModelScope.launch(Dispatchers.IO) {
            val result = scanner.scanRoots(
                roots = scanner.defaultRoots(),
                cancellation = { cancelScan },
                onProgress = { progress ->
                    _state.update { it.copy(scanProgress = progress) }
                }
            )
            val pairs = result.files.map { scanned ->
                scanned to runCatching { metadataReader.read(scanned) }.getOrDefault(FileMetadata.Unknown)
            }
            val analyzed = analyzer.analyze(pairs)
            _state.update {
                it.copy(
                    screen = CleannerScreen.Analysis,
                    analyzedFiles = analyzed,
                    selectedIds = analyzed.filter { file -> file.isDefaultSelected }.map { file -> file.scannedFile.id }.toSet(),
                    scanIncomplete = result.incomplete,
                    missingRootLabels = result.missingRoots.map { root -> root.label },
                    scanProgress = null
                )
            }
        }
    }

    fun cancelScan() {
        cancelScan = true
        _state.update { it.copy(scanIncomplete = true) }
    }

    fun showHome() {
        _state.update { it.copy(screen = CleannerScreen.Home, selectedFile = null, cleanupResult = null) }
    }

    fun showAnalysis() {
        _state.update { it.copy(screen = CleannerScreen.Analysis, selectedFile = null) }
    }

    fun showDetails(file: AnalyzedFile) {
        _state.update { it.copy(screen = CleannerScreen.Detail, selectedFile = file) }
    }

    fun showConfirmCleanup() {
        _state.update { it.copy(screen = CleannerScreen.ConfirmCleanup) }
    }

    fun toggleFile(fileId: String) {
        _state.update { current ->
            val next = current.selectedIds.toMutableSet()
            if (!next.add(fileId)) next.remove(fileId)
            current.copy(selectedIds = next)
        }
    }

    fun selectRisk(riskLevel: RiskLevel) {
        _state.update { current ->
            current.copy(selectedIds = current.selectedIds + current.analyzedFiles.filter { it.riskLevel == riskLevel }.map { it.scannedFile.id })
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedIds = emptySet()) }
    }

    fun runCleanup() {
        val current = _state.value
        val selected = current.analyzedFiles.filter { it.scannedFile.id in current.selectedIds }
        viewModelScope.launch(Dispatchers.IO) {
            val result = deletionEngine.delete(
                selected.map {
                    DeletionRequest(
                        path = it.scannedFile.absolutePath,
                        sizeBytes = it.scannedFile.sizeBytes,
                        selected = true
                    )
                }
            )
            val record = result.toCleanupRecord(
                appsIncluded = selected.map { it.scannedFile.app }.toSet(),
                riskSummary = selected.groupingBy { it.riskLevel }.eachCount()
            )
            historyStore.save(record)
            _state.update {
                it.copy(
                    screen = CleannerScreen.CleanupResult,
                    cleanupResult = result,
                    latestRecord = record,
                    selectedIds = emptySet()
                )
            }
        }
    }

    fun loadDemoData() {
        val now = Instant.now()
        val samples = listOf(
            ScannedFile(
                id = "demo-wechat-image",
                app = AppOwner.WeChat,
                absolutePath = "/storage/emulated/0/Android/media/com.tencent.mm/MicroMsg/image2/a/demo.jpg",
                parentPath = "/storage/emulated/0/Android/media/com.tencent.mm/MicroMsg/image2/a",
                name = "demo.jpg",
                extension = "jpg",
                sizeBytes = 2_400_000,
                lastModifiedAt = now,
                detectedMimeType = "image/jpeg"
            ) to FileMetadata.Image(1080, 1920, "jpg"),
            ScannedFile(
                id = "demo-qq-apk",
                app = AppOwner.QQ,
                absolutePath = "/storage/emulated/0/tencent/QQfile_recv/app.apk",
                parentPath = "/storage/emulated/0/tencent/QQfile_recv",
                name = "app.apk",
                extension = "apk",
                sizeBytes = 120_000_000,
                lastModifiedAt = now,
                detectedMimeType = "application/vnd.android.package-archive"
            ) to FileMetadata.Apk("绀轰緥搴旂敤", "com.example", "1.0")
        )
        val analyzed = analyzer.analyze(samples)
        _state.update {
            it.copy(
                screen = CleannerScreen.Analysis,
                analyzedFiles = analyzed,
                selectedIds = analyzed.filter { file -> file.isDefaultSelected }.map { file -> file.scannedFile.id }.toSet(),
                scanIncomplete = false
            )
        }
    }
}

enum class CleannerScreen {
    Home,
    Scanning,
    Analysis,
    Detail,
    ConfirmCleanup,
    CleanupResult
}

data class CleannerUiState(
    val hasPermission: Boolean,
    val screen: CleannerScreen = CleannerScreen.Home,
    val scanProgress: ScanProgress? = null,
    val scanIncomplete: Boolean = false,
    val missingRootLabels: List<String> = emptyList(),
    val analyzedFiles: List<AnalyzedFile> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val selectedFile: AnalyzedFile? = null,
    val cleanupResult: DeletionResult? = null,
    val latestRecord: CleanupRecord? = null,
    val errorMessage: String? = null
) {
    val selectedFiles: List<AnalyzedFile> = analyzedFiles.filter { it.scannedFile.id in selectedIds }
    val selectedBytes: Long = selectedFiles.sumOf { it.scannedFile.sizeBytes }
    val totalBytes: Long = analyzedFiles.sumOf { it.scannedFile.sizeBytes }
    val suggestedBytes: Long = analyzedFiles.filter {
        it.riskLevel == RiskLevel.SafeToClean || it.riskLevel == RiskLevel.LikelyCleanable
    }.sumOf { it.scannedFile.sizeBytes }
    val reviewBytes: Long = analyzedFiles.filter { it.riskLevel == RiskLevel.ReviewRequired }.sumOf { it.scannedFile.sizeBytes }
    val categories: List<FileCategory> = analyzedFiles.map { it.category }.distinct()
}

