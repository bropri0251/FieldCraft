package com.Houndacivic.fieldcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class CategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Category"
        setContent { CategoryScreen(category = category) }
    }

    companion object { const val EXTRA_CATEGORY = "extra_category" }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(category: String) {
    val items = remember(category) {
        // placeholder content per category to show purpose
        when (category) {
            "Community" -> listOf("Local groups", "Events calendar", "Field directory")
            "Tech Corner" -> listOf("HPA setup guide", "AEG shimming 101", "Hop-up tuning")
            "FAQ" -> listOf("What BB weight?", "Eye pro standards", "FPS vs Joules")
            "Tactics" -> listOf("Room entry basics", "Bounding overwatch", "Comms brevity")
            "Gear" -> listOf("Plate carrier fit", "Radio options", "Boots & gloves")
            "Maintenance" -> listOf("Barrel cleaning", "Gearbox lube points", "Battery care")
            "Safety" -> listOf("Safe engagement distances", "Medic rules", "Hydration tips")
            else -> listOf("Coming soon")
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(category) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Purpose: $category knowledge and resources.",
                    style = MaterialTheme.typography.titleMedium
                )
                Divider(Modifier.padding(vertical = 8.dp))
            }
            items(items) { row ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Text(row, Modifier.padding(16.dp))
                }
            }
        }
    }
}
