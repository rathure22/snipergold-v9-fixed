package com.snipergold.v9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.snipergold.v9.ui.SideBarScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showSideBar by remember { mutableStateOf(true) } // auto open para makita dayon Binance
            
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0D0D0D))
                ) {
                    if (showSideBar) {
                        SideBarScreen(
                            context = this@MainActivity,
                            onClose = { showSideBar = false }
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("SniperGold v9 - GOLD SNIPER", color = Color(0xFFE8D5A3))
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { showSideBar = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D18C))
                            ) {
                                Text("Open Binance Settings", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
