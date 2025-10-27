package com.Houndacivic.fieldcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BackHandler(true) { finish() }
            Scaffold(
                topBar = { BackTopBar(title = "Settings") { finish() } }
            ) { padding ->
                SettingsScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    var dark by remember { mutableStateOf(true) }

    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("App preferences", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Dark theme")
            Switch(checked = dark, onCheckedChange = { dark = it })
        }
        Divider()
        Text("About", style = MaterialTheme.typography.titleMedium)
        Text("FieldCraft v0.1 – Airsoft knowledge hub.")
    }
}
