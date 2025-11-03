package com.Houndacivic.fieldcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.Houndacivic.fieldcraft.ui.theme.FieldCraftTheme

class ContributeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FieldCraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContributeScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeScreen(onBack: () -> Unit) {
    val dbh = remember { FieldCraftDbHelper(AppContext.get()) }
    val prefs = remember { FieldPrefs() }
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = "Contribute a Tip", onBack = onBack) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = summary, onValueChange = { summary = it },
                label = { Text("Summary") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tags, onValueChange = { tags = it },
                label = { Text("Tags (comma-separated)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val author = prefs.getDisplayName().ifBlank { prefs.getUsername() }
                    if (title.isNotBlank() && summary.isNotBlank()) {
                        dbh.insertArticle(title, summary, tags, author)
                        title = ""; summary = ""; tags = ""
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Submit") }
        }
    }
}
