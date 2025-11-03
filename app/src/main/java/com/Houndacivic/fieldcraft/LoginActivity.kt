package com.Houndacivic.fieldcraft

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbh = FieldCraftDbHelper(this)
        val prefs = FieldPrefs()

        setContent {
            com.Houndacivic.fieldcraft.ui.theme.FieldCraftTheme {
                Scaffold(topBar = { BackTopBar(title = "Account", onBack = { finish() }) }) { pad ->
                    var tab by remember { mutableStateOf(1) } // 0=Sign in, 1=Create, 2=Change pw (placeholder)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(pad)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TabRow(selectedTabIndex = tab) {
                            Tab(selected = tab==0, onClick = { tab=0 }, text = { Text("Sign in") })
                            Tab(selected = tab==1, onClick = { tab=1 }, text = { Text("Create account") })
                            Tab(selected = tab==2, onClick = { tab=2 }, text = { Text("Change password") })
                        }

                        when (tab) {
                            0 -> SignInPane(dbh, prefs) { finish() }
                            1 -> RegisterPane(dbh, prefs) { finish() }
                            else -> Text("Coming soon…")
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- Sign in ---------------- */

@Composable
private fun SignInPane(
    dbh: FieldCraftDbHelper,
    prefs: FieldPrefs,
    onSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    fun attemptLogin() {
        error = ""
        if (email.isBlank() || pass.isBlank()) {
            error = "Enter email and password."
            return
        }
        val u = email.trim()

        // Admin override (demo)
        if (u.equals("admin@fieldcraft.app", true) && pass == "FieldCraft#2025") {
            prefs.signIn(u)
            prefs.setDisplayName("Admin")
            prefs.setAdmin(true)
            Toast.makeText(AppContext.get(), "Admin signed in", Toast.LENGTH_SHORT).show()
            onSuccess()
            return
        }

        if (dbh.verifyUser(u, pass)) {
            prefs.signIn(u)
            prefs.setDisplayName(dbh.displayNameFor(u))
            prefs.setAdmin(false)
            Toast.makeText(AppContext.get(), "Signed in", Toast.LENGTH_SHORT).show()
            onSuccess()
        } else {
            error = "Invalid email or password."
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
        Button(onClick = { attemptLogin() }, modifier = Modifier.fillMaxWidth()) { Text("Sign in") }
    }
}

/* ---------------- Register ---------------- */

@Composable
private fun RegisterPane(
    dbh: FieldCraftDbHelper,
    prefs: FieldPrefs,
    onSuccess: () -> Unit
) {
    var regEmail by remember { mutableStateOf("") }
    var regDisplayName by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    fun isValidEmail(s: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()

    fun isValidPassword(s: String) =
        s.length >= 10 && s.any { it in "#@\$!%".toCharArray() }

    fun attemptRegister() {
        error = ""
        val email = regEmail.trim()
        val displayName = regDisplayName.trim()
        val pw = regPassword

        if (!isValidEmail(email)) { error = "Enter a valid email."; return }
        if (displayName.length !in 3..32) { error = "Username 3–32 characters."; return }
        if (!isValidPassword(pw)) { error = "Password ≥10 chars with one of # @ $ ! %."; return }
        if (dbh.userExistsByEmail(email)) { error = "That email is already registered. Try Sign in."; return }

        try {
            dbh.insertUser(email, pw, displayName)
            prefs.signIn(email)
            prefs.setDisplayName(displayName)
            prefs.setAdmin(email.equals("admin@fieldcraft.app", true))
            Toast.makeText(AppContext.get(), "Account created", Toast.LENGTH_SHORT).show()
            onSuccess()
        } catch (e: Exception) {
            error = e.message ?: "Registration failed."
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = regEmail, onValueChange = { regEmail = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = regDisplayName, onValueChange = { regDisplayName = it }, label = { Text("Username (display name)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = regPassword, onValueChange = { regPassword = it }, label = { Text("Create password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
        Button(onClick = { attemptRegister() }, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
    }
}
