package com.Houndacivic.fieldcraft

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class ContributeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ContributeScreen(
            onSubmit = { title, body, link ->
                // For now: pretend save; later we'll hook Room/remote
                Toast.makeText(this, "Submitted: $title", Toast.LENGTH_SHORT).show()
                finish()
            },
            onCancel = { finish() }
        ) }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeScreen(onSubmit: (String, String, String) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    val valid = title.isNotBlank() && body.isNotBlank()

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Contribute Tip") }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Tip / Content") }, minLines = 5, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = link, onValueChange = { link = it }, label = { Text("Source link (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { onSubmit(title.trim(), body.trim(), link.trim()) }, enabled = valid, modifier = Modifier.fillMaxWidth()) { Text("Submit") }
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}
