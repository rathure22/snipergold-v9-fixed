package com.snipergold.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.snipergold.app.ui.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    var showSideBar by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SniperGold v9 - GOLD SNIPER",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showSideBar = !showSideBar },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D18C))
            ) {
                Text(
                    text = if (showSideBar) "Close Settings" else "Open Binance Settings",
                    color = Color.Black
                )
            }
        }
    }
}
