package com.novascanner.network

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novascanner.network.ui.navigation.AppNavigation
import com.novascanner.network.ui.theme.NovaRadarTheme
import com.novascanner.network.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val isDark by vm.isDark.collectAsState()
            NovaRadarTheme(isDark = isDark) { AppNavigation(vm) }
        }
    }
}
