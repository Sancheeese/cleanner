package com.sancheeese.cleanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sancheeese.cleanner.app.CleannerApp
import com.sancheeese.cleanner.ui.theme.CleannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CleannerTheme {
                CleannerApp()
            }
        }
    }
}
