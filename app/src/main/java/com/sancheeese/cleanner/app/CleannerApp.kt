package com.sancheeese.cleanner.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sancheeese.cleanner.ui.screens.AnalysisScreen
import com.sancheeese.cleanner.ui.screens.CleanupConfirmScreen
import com.sancheeese.cleanner.ui.screens.CleanupResultScreen
import com.sancheeese.cleanner.ui.screens.DetailScreen
import com.sancheeese.cleanner.ui.screens.HomeScreen
import com.sancheeese.cleanner.ui.screens.PermissionScreen
import com.sancheeese.cleanner.ui.screens.ScanningScreen

@Composable
fun CleannerApp(viewModel: CleannerViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshPermission()
    }

    if (!state.hasPermission) {
        PermissionScreen(
            onOpenPermission = viewModel::openPermissionSettings,
            onRefresh = viewModel::refreshPermission,
            onDemo = viewModel::loadDemoData
        )
        return
    }

    when (state.screen) {
        CleannerScreen.Home -> HomeScreen(
            state = state,
            onScan = viewModel::startScan,
            onOpenAnalysis = viewModel::showAnalysis,
            onDemo = viewModel::loadDemoData
        )
        CleannerScreen.Scanning -> ScanningScreen(
            state = state,
            onCancel = viewModel::cancelScan
        )
        CleannerScreen.Analysis -> AnalysisScreen(
            state = state,
            onToggleFile = viewModel::toggleFile,
            onOpenFile = viewModel::showDetails,
            onClearSelection = viewModel::clearSelection,
            onSelectRisk = viewModel::selectRisk,
            onCleanup = viewModel::showConfirmCleanup,
            onBack = viewModel::showHome
        )
        CleannerScreen.Detail -> DetailScreen(
            state = state,
            onToggleFile = viewModel::toggleFile,
            onBack = viewModel::showAnalysis
        )
        CleannerScreen.ConfirmCleanup -> CleanupConfirmScreen(
            state = state,
            onConfirm = viewModel::runCleanup,
            onBack = viewModel::showAnalysis
        )
        CleannerScreen.CleanupResult -> CleanupResultScreen(
            state = state,
            onDone = viewModel::showHome
        )
    }
}

