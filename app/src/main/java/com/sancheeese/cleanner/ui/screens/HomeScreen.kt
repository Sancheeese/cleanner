package com.sancheeese.cleanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.sancheeese.cleanner.app.CleannerUiState

@Composable
fun HomeScreen(
    state: CleannerUiState,
    onScan: () -> Unit,
    onOpenAnalysis: () -> Unit,
    onDemo: () -> Unit
) {
    ScreenScaffold(
        title = "Cleanner",
        subtitle = "扫描 QQ 和微信文件，解释用途后再清理。"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard("微信", if (state.analyzedFiles.any { it.scannedFile.app.displayName == "微信" }) "已有扫描结果" else "未扫描")
            InfoCard("QQ", if (state.analyzedFiles.any { it.scannedFile.app.displayName == "QQ" }) "已有扫描结果" else "未扫描")
            if (state.analyzedFiles.isNotEmpty()) {
                InfoCard(
                    title = "当前扫描结果",
                    body = "总计 ${formatBytes(state.totalBytes)} · 建议清理 ${formatBytes(state.suggestedBytes)} · 需确认 ${formatBytes(state.reviewBytes)}",
                    trailing = "查看",
                    onClick = onOpenAnalysis
                )
            }
            state.latestRecord?.let {
                InfoCard(
                    title = "最近清理",
                    body = "${formatInstant(it.createdAt)} · 删除 ${it.deletedFileCount} 个 · 释放 ${formatBytes(it.freedBytes)}"
                )
            }
            ActionRow(primaryText = "开始扫描", onPrimary = onScan, secondaryText = "演示数据", onSecondary = onDemo)
        }
    }
}
