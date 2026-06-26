package com.sancheeese.cleanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sancheeese.cleanner.app.CleannerUiState

@Composable
fun ScanningScreen(
    state: CleannerUiState,
    onCancel: () -> Unit,
    onBack: () -> Unit = onCancel
) {
    val progress = state.scanProgress
    ScreenScaffold(
        title = "正在扫描",
        subtitle = "正在读取 QQ 和微信相关目录，扫描过程可以取消。",
        onBack = onBack
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LinearProgressIndicator(modifier = Modifier)
            InfoCard(
                title = progress?.currentRoot?.label ?: "准备扫描",
                body = progress?.currentPath ?: "正在查找常见目录"
            )
            Text("已发现 ${progress?.filesFound ?: 0} 个文件 · ${formatBytes(progress?.bytesFound ?: 0)}")
            ActionRow(primaryText = "取消扫描", onPrimary = onCancel)
        }
    }
}
