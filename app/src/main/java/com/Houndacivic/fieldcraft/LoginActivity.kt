package com.Houndacivic.fieldcraft

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * LoginActivity
 * Single entry for: Sign in, Create account, Change Password
 * Password rules: at least 10 characters and at least one special char from [#@$!%].
 * Week 3 note: still stores plaintext in 'password_hash' column (hash in a later phase).
 */

class LoginActivity : ComponentActivity() {

    companion object {
        const val EXTRA_AUTH_TAB = "auth_tab" // "login" | "register" | "changePin" (now changePassword)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbh = FieldCraftDbHelper(this)

        val initialTab = when (intent.getStringExtra(EXTRA_AUTH_TAB)) {
            "register" -> 1
            "changePin", "changePassword" -> 2
            else -> 0
        }

        setContent {
            // Purple/black look just for this screen
            val purpleBlack = darkColorScheme(
                primary = androidx.compose.ui.graphics.Color(0xFF8A79FF),
                onPrimary = androidx.compose.ui.graphics.Color.White,
                background = androidx.compose.ui.graphics.Color(0xFF0E0B14),
                onBackground = androidx.compose.ui.graphics.Color(0xFFE6E1FF),
                surface = androidx.compose.ui.graphics.Color(0xFF13111B),
                onSurface = androidx.compose.ui.graphics.Color(0xFFE6E1FF)
            )

            BackHandler(true) { finish() }

            MaterialTheme(colorScheme = purpleBlack) {
                Scaffold(
                    topBar = { BackTopBar(title = "Account") { finish() } }
                ) { padding ->
                    AuthTabs(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(20.dp),
                        dbh = dbh,
                        initialTabIndex = initialTab,
                        onSuccess = {
                            setResult(RESULT_OK)
                            finish()
                        },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthTabs(
    modifier: Modifier = Modifier,
    dbh: FieldCraftDbHelper,
    initialTabIndex: Int,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var tabIndex by remember { mutableStateOf(initialTabIndex.coerceIn(0, 2)) }
    val tabs = listOf("Sign in", "Create account", "Change password")

    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("FieldCraft account (local)", style = MaterialTheme.typography.titleMedium)

        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { i, label ->
                Tab(
                    selected = tabIndex == i,
                    onClick = { tabIndex = i },
                    text = { Text(label) }
                )
            }
        }

        when (tabIndex) {
            0 -> SignInPane(dbh, onSuccess, onCancel)
            1 -> RegisterPane(dbh, onSuccess, onCancel)
            2 -> ChangePasswordPane(dbh, onSuccess, onCancel)
        }
    }
}

@Composable
private fun SignInPane(
    dbh: FieldCraftDbHelper,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val ctx = LocalContext.current
    val prefs = remember { FieldPrefs() }

    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun attemptLogin() {
        error = ""
        if (user.isBlank() || pass.isBlank()) {
            error = "Enter username and password."
            return
        }
        if (verifyLogin(dbh, user, pass)) {
            prefs.save(user.trim(), pass) // stores plaintext locally (Week 3)
            Toast.makeText(ctx, "Signed in", Toast.LENGTH_SHORT).show()
            onSuccess()
        } else {
            error = "Invalid username or password."
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = user, onValueChange = { user = it },
            label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
            supportingText = { Text("At least 10 characters and include one of: # @ $ ! %") },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { show = !show }) { Text(if (show) "Hide" else "Show") }
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)

        Button(onClick = { attemptLogin() }, modifier = Modifier.fillMaxWidth()) { Text("Sign in") }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}

@Composable
private fun RegisterPane(
    dbh: FieldCraftDbHelper,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val ctx = LocalContext.current
    val prefs = remember { FieldPrefs() }

    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun attemptRegister() {
        error = ""
        when {
            !isValidUsername(user)      -> error = "Username: 3–32 letters/digits/underscore."
            !isValidPassword(pass)      -> error = "Password: ≥10 chars and include one of # @ $ ! %."
            else -> {
                try {
                    registerUser(dbh, user, pass)
                    prefs.save(user.trim(), pass)
                    Toast.makeText(ctx, "Account created", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } catch (e: Exception) {
                    error = e.message ?: "Registration failed."
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = user, onValueChange = { user = it },
            label = { Text("Create username") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Create password") },
            singleLine = true,
            visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
            supportingText = { Text("At least 10 characters and include one of: # @ $ ! %") },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { show = !show }) { Text(if (show) "Hide" else "Show") }
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)

        Button(onClick = { attemptRegister() }, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}

@Composable
private fun ChangePasswordPane(
    dbh: FieldCraftDbHelper,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val ctx = LocalContext.current
    val prefs = remember { FieldPrefs() }
    val username = prefs.getUsername()

    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNext by remember { mutableStateOf(false) }

    fun attemptChange() {
        error = ""
        if (username.isBlank()) {
            error = "You must be signed in."
            return
        }
        if (!isValidPassword(next)) {
            error = "Password: ≥10 chars and include one of # @ $ ! %."
            return
        }
        val ok = updatePassword(dbh, username, current, next)
        if (ok) {
            Toast.makeText(ctx, "Password updated", Toast.LENGTH_SHORT).show()
            onSuccess()
        } else {
            error = "Current password is incorrect."
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = current, onValueChange = { current = it },
            label = { Text("Current password") }, singleLine = true,
            visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { showCurrent = !showCurrent }) { Text(if (showCurrent) "Hide" else "Show") }

        OutlinedTextField(
            value = next, onValueChange = { next = it },
            label = { Text("New password") }, singleLine = true,
            visualTransformation = if (showNext) VisualTransformation.None else PasswordVisualTransformation(),
            supportingText = { Text("At least 10 characters and include one of: # @ $ ! %") },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { showNext = !showNext }) { Text(if (showNext) "Hide" else "Show") }

        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)

        Button(onClick = { attemptChange() }, modifier = Modifier.fillMaxWidth()) { Text("Update password") }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}

/* ---------------- Local validation + DB helpers ---------------- */

// Username: 3–32 letters/digits/underscore
private fun isValidUsername(u: String): Boolean = u.matches(Regex("^[A-Za-z0-9_]{3,32}$"))

// Password: ≥10 chars and at least one special from [#@$!%]
private fun isValidPassword(pw: String): Boolean {
    if (pw.length < 10) return false
    val special = Regex("[#@\\$!%]") // escaped $ in Kotlin strings
    return special.containsMatchIn(pw)
}

private fun registerUser(dbh: FieldCraftDbHelper, username: String, password: String): Long {
    require(isValidUsername(username)) { "Invalid username." }
    require(isValidPassword(password)) { "Invalid password." }
    val db = dbh.writableDatabase
    val cv = android.content.ContentValues().apply {
        put("username", username.trim())
        put("email", "")
        put("password_hash", password) // Week 3: plaintext; hash in later phase
        put("created_at", System.currentTimeMillis())
    }
    return db.insertOrThrow("users", null, cv)
}

private fun verifyLogin(dbh: FieldCraftDbHelper, username: String, password: String): Boolean {
    val db = dbh.readableDatabase
    db.rawQuery(
        "SELECT 1 FROM users WHERE username = ? AND password_hash = ? LIMIT 1",
        arrayOf(username.trim(), password)
    ).use { c -> return c.moveToFirst() }
}

private fun updatePassword(dbh: FieldCraftDbHelper, username: String, currentPassword: String, newPassword: String): Boolean {
    if (!verifyLogin(dbh, username, currentPassword)) return false
    val db = dbh.writableDatabase
    val cv = android.content.ContentValues().apply { put("password_hash", newPassword) }
    val rows = db.update("users", cv, "username = ?", arrayOf(username.trim()))
    return rows > 0
}
