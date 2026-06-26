package com.sancheeese.cleanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.sancheeese.cleanner.app.CleannerUiState

@Composable
fun CleanupConfirmScreen(
    state: CleannerUiState,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val riskSummary = state.selectedFiles.groupingBy { it.riskLevel.displayName }.eachCount()
    ScreenScaffold(
        title = "确认清理",
        subtitle = "删除前最后确认一次。",
        onBack = onBack
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard(
                title = "即将删除",
                body = "${state.selectedFiles.size} 个文件 · 预计释放 ${formatBytes(state.selectedBytes)}"
            )
            InfoCard(
                title = "风险分布",
                body = if (riskSummary.isEmpty()) "未选择文件" else riskSummary.entries.joinToString(" · ") { "${it.key} ${it.value} 个" }
            )
            Text("Cleanner 只会删除你已选择的文件，不会递归删除目录。")
            if (state.selectedFiles.isEmpty()) {
                OutlinedButton(onClick = onBack) {
                    Text("返回选择文件")
                }
            } else {
                Button(onClick = onConfirm) {
                    Text("确认删除")
                }
                OutlinedButton(onClick = onBack) {
                    Text("返回修改")
                }
            }
        }
    }
}
