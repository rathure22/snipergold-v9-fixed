package com.snipergold.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snipergold.app.ui.AdaptiveShell
import com.snipergold.app.ui.MainViewModel
import com.snipergold.app.ui.screens.MainScreen
import com.snipergold.app.ui.theme.DarkBg
import com.snipergold.app.ui.theme.SniperGoldTheme

// KRITIKAL: Husto nga package com.snipergold.app - dili com.snipergold.v9
// Kung v9 ang package, mag-crash ang app kay wala sa Manifest
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            SniperGoldTheme(accentTheme = vm.accentTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = DarkBg) {
                    AdaptiveShell {
                        MainScreen(vm)
                    }
                }
            }
        }
    }
}
