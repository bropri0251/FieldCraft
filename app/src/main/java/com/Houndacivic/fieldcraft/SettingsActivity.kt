package com.Houndacivic.fieldcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SettingsScreen() }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var dark by remember { mutableStateOf(true) }

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
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
}
