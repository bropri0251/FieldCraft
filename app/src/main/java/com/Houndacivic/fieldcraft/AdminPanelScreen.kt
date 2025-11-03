package com.Houndacivic.fieldcraft

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { BackTopBar(title = "Admin Panel", onBack = onBack) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Administrator Tools", style = MaterialTheme.typography.titleLarge)

            ElevatedCard(onClick = { /* seed demo data */ }) {
                Column(Modifier.padding(16.dp)) {
                    Text("Seed demo content")
                    Text(
                        "Insert a few example articles",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            ElevatedCard(onClick = { /* export logs or DB */ }) {
                Column(Modifier.padding(16.dp)) {
                    Text("Export data")
                    Text(
                        "Dump articles table to JSON",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            ElevatedCard(onClick = { /* manage users */ }) {
                Column(Modifier.padding(16.dp)) {
                    Text("User management")
                    Text(
                        "Future: list and deactivate users",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
