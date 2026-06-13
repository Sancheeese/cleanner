package com.sancheeese.cleanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.sancheeese.cleanner.app.CleannerUiState

@Composable
fun CleanupResultScreen(
    state: CleannerUiState,
    onDone: () -> Unit
) {
    val result = state.cleanupResult
    ScreenScaffold(
        title = "清理完成",
        subtitle = "结果已记录到本机。"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard(
                title = "释放空间",
                body = formatBytes(result?.freedBytes ?: 0)
            )
            InfoCard(
                title = "删除结果",
                body = "成功 ${result?.deleted?.size ?: 0} 个 · 失败 ${result?.failed?.size ?: 0} 个"
            )
            if (!result?.failed.isNullOrEmpty()) {
                InfoCard(
                    title = "失败原因",
                    body = result!!.failed.joinToString(" · ") { it.reason }.ifBlank { "无" }
                )
            }
            ActionRow(primaryText = "完成", onPrimary = onDone)
        }
    }
}
