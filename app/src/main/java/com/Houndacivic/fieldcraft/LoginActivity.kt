package com.Houndacivic.fieldcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LoginScreen(
            onSubmit = { user, pin ->
                FieldPrefs().save(user.trim(), pin)
                setResult(RESULT_OK)
                finish()
            },
            onCancel = { finish() }
        ) }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onSubmit: (String, String) -> Unit, onCancel: () -> Unit) {
    var user by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    val valid = user.isNotBlank() && pin.length >= 4

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Sign in") }) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter { ch -> ch.isDigit() }.take(8) },
                label = { Text("PIN (4–8 digits)") },
                singleLine = true,
                visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { showPin = !showPin }) { Text(if (showPin) "Hide PIN" else "Show PIN") }

            Button(onClick = { onSubmit(user, pin) }, enabled = valid, modifier = Modifier.fillMaxWidth()) {
                Text("Sign in")
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}
