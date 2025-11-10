package com.Houndacivic.fieldcraft

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.Houndacivic.fieldcraft.ui.theme.FieldCraftTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FieldCraftTheme {
                LoginAndRegisterScreen(
                    onLoginSuccess = {
                        val ctx = this@LoginActivity
                        // Safe navigation without MainActivity symbol resolution
                        val intent = Intent().setClassName(
                            ctx.packageName,
                            "com.Houndacivic.fieldcraft.MainActivity"
                        )
                        startActivity(intent)
                        finish()
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginAndRegisterScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val dbh = remember { FieldCraftDbHelper(ctx) }

    // mode + fields
    var createMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    // lockout
    val locked = !LoginGuard.canAttempt(ctx)
    val remainingMs = LoginGuard.lockoutRemainingMs(ctx)

    // hardware/gesture back
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (createMode) "Create Account" else "Sign In") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (!createMode && locked) {
                val sec = (remainingMs / 1000).coerceAtLeast(1)
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Too many attempts. Try again in ${sec}s.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(if (createMode) "Password (10+ chars incl. # @ $ % ! %)" else "Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (createMode) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirm password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))

            // Primary action
            Button(
                onClick = {
                    if (createMode) {
                        // CREATE ACCOUNT
                        if (email.isBlank() || password.isBlank() || confirm.isBlank() || displayName.isBlank()) {
                            Toast.makeText(ctx, "All fields are required.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (password != confirm) {
                            Toast.makeText(ctx, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val specials = "#@\$%!%"
                        if (password.length < 10 || !password.any { it in specials }) {
                            Toast.makeText(ctx, "Password must be 10+ chars & include # @ $ % ! %", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val exists = dbh.readableDatabase.rawQuery(
                            "SELECT 1 FROM users WHERE email = ? LIMIT 1",
                            arrayOf(email)
                        ).use { c -> c.moveToFirst() }
                        if (exists) {
                            Toast.makeText(ctx, "Email already registered.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val cv = ContentValues().apply {
                            put("email", email)
                            put("password", password)      // plaintext acceptable for course demo
                            put("display_name", displayName)
                            put("is_admin", 0)
                        }
                        val ok = dbh.writableDatabase.insert("users", null, cv) != -1L
                        if (ok) {
                            Toast.makeText(ctx, "Account created. Please sign in.", Toast.LENGTH_SHORT).show()
                            // Switch back to sign-in and clear sensitive fields
                            createMode = false
                            password = ""
                            confirm = ""
                        } else {
                            Toast.makeText(ctx, "Failed to create account.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // SIGN IN (rate-limited)
                        if (!LoginGuard.canAttempt(ctx)) {
                            val sec = (LoginGuard.lockoutRemainingMs(ctx) / 1000).coerceAtLeast(1)
                            Toast.makeText(ctx, "Locked. Try again in ${sec}s.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val ok = dbh.verifyUser(email, password)
                        if (ok) {
                            LoginGuard.reset(ctx)

                            // Persist session (+ admin flag for UI)
                            val isAdmin = dbh.isAdmin(email)
                            ctx.getSharedPreferences("fieldcraft_prefs", 0).edit()
                                .putString("u", email)
                                .putString("p", password)
                                .putBoolean("is_admin", isAdmin)
                                .apply()

                            onLoginSuccess()
                        } else {
                            LoginGuard.recordFailure(ctx)
                            Toast.makeText(ctx, "Invalid credentials.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = createMode || !locked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (createMode) "Create account" else "Sign in")
            }

            Spacer(Modifier.height(12.dp))

            // toggle
            TextButton(onClick = {
                createMode = !createMode
                password = ""
                confirm = ""
            }) {
                Text(if (createMode) "Have an account? Sign in" else "Create an account")
            }
        }
    }
}
