package com.sancheeese.cleanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun PermissionScreen(
    onOpenPermission: () -> Unit,
    onRefresh: () -> Unit,
    onDemo: () -> Unit
) {
    ScreenScaffold(
        title = "开启文件扫描权限",
        subtitle = "Cleanner 需要访问本机共享存储，才能分析 QQ 和微信文件。"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard(
                title = "隐私原则",
                body = "扫描只在本机进行，不上传文件；删除前必须由你选择并确认。"
            )
            InfoCard(
                title = "权限用途",
                body = "用于读取 QQ/微信相关目录、识别文件类型、计算占用空间和执行你确认的删除。"
            )
            ActionRow(
                primaryText = "去开启权限",
                onPrimary = onOpenPermission,
                secondaryText = "我已开启",
                onSecondary = onRefresh
            )
            Text("如果你只是想先看界面，可以进入演示数据。")
            ActionRow(primaryText = "查看演示数据", onPrimary = onDemo)
        }
    }
}
