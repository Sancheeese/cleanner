package com.sancheeese.cleanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.sancheeese.cleanner.app.CleannerUiState

@Composable
fun DetailScreen(
    state: CleannerUiState,
    onToggleFile: (String) -> Unit,
    onBack: () -> Unit
) {
    val file = state.selectedFile
    if (file == null) {
        ScreenScaffold(title = "文件详情") {
            Text("未选择文件")
            ActionRow(primaryText = "返回", onPrimary = onBack)
        }
        return
    }
    ScreenScaffold(
        title = file.scannedFile.name,
        subtitle = file.riskLevel.displayName
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoCard("文件夹用途", file.folderProfile.purpose)
            InfoCard("清理建议", file.recommendation)
            InfoCard("文件特征", "${file.category.displayName} · ${formatBytes(file.scannedFile.sizeBytes)} · ${file.metadataSummary}")
            InfoCard("修改时间", formatInstant(file.scannedFile.lastModifiedAt))
            InfoCard("完整路径", file.scannedFile.absolutePath)
            Text("选择清理")
            Switch(
                checked = file.scannedFile.id in state.selectedIds,
                onCheckedChange = { onToggleFile(file.scannedFile.id) }
            )
            ActionRow(primaryText = "返回结果", onPrimary = onBack)
        }
    }
}
