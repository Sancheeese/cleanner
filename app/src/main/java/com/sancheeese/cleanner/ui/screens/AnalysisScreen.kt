package com.sancheeese.cleanner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sancheeese.cleanner.app.CleannerUiState
import com.sancheeese.cleanner.core.model.AnalyzedFile
import com.sancheeese.cleanner.core.model.RiskLevel

@Composable
fun AnalysisScreen(
    state: CleannerUiState,
    onToggleFile: (String) -> Unit,
    onOpenFile: (AnalyzedFile) -> Unit,
    onClearSelection: () -> Unit,
    onSelectRisk: (RiskLevel) -> Unit,
    onCleanup: () -> Unit,
    onBack: () -> Unit
) {
    ScreenScaffold(
        title = "分析结果",
        subtitle = "先看文件夹用途，再决定哪些文件要清理。"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard(
                title = "概览",
                body = "总计 ${formatBytes(state.totalBytes)} · 建议清理 ${formatBytes(state.suggestedBytes)} · 已选择 ${formatBytes(state.selectedBytes)}"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = false,
                    onClick = { onSelectRisk(RiskLevel.SafeToClean) },
                    label = { Text("选安全缓存") }
                )
                FilterChip(
                    selected = false,
                    onClick = { onSelectRisk(RiskLevel.LikelyCleanable) },
                    label = { Text("选可能可清理") }
                )
                FilterChip(selected = false, onClick = onClearSelection, label = { Text("清空") })
            }
            if (state.scanIncomplete) {
                Text("扫描未完成，当前展示的是部分结果。", color = MaterialTheme.colorScheme.tertiary)
            }
            if (state.missingRootLabels.isNotEmpty()) {
                Text("未发现目录：${state.missingRootLabels.joinToString("、")}")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.analyzedFiles.groupBy { it.folderProfile.displayName }.entries.toList()) { group ->
                    itemFolderGroup(
                        title = group.key,
                        files = group.value,
                        selectedIds = state.selectedIds,
                        onToggleFile = onToggleFile,
                        onOpenFile = onOpenFile
                    )
                }
            }
            ActionRow(
                primaryText = "清理已选择 (${state.selectedIds.size})",
                onPrimary = onCleanup,
                secondaryText = "返回首页",
                onSecondary = onBack
            )
        }
    }
}

@Composable
private fun itemFolderGroup(
    title: String,
    files: List<AnalyzedFile>,
    selectedIds: Set<String>,
    onToggleFile: (String) -> Unit,
    onOpenFile: (AnalyzedFile) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val first = files.first()
        InfoCard(
            title = title,
            body = "${first.folderProfile.purpose} · ${files.size} 个文件 · ${formatBytes(files.sumOf { it.scannedFile.sizeBytes })}"
        )
        files.forEach { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenFile(file) }
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = file.scannedFile.id in selectedIds,
                    onCheckedChange = { onToggleFile(file.scannedFile.id) }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(file.scannedFile.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${file.category.displayName} · ${formatBytes(file.scannedFile.sizeBytes)} · ${file.metadataSummary}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(file.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(file.riskLevel.displayName, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
