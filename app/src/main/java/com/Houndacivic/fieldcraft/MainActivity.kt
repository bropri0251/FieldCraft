package com.Houndacivic.fieldcraft

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContext.init(applicationContext)
        setContent { FieldCraftApp(
            onOpenSettings = {
                startActivity(Intent(this, SettingsActivity::class.java))
            },
            onOpenLogin = {
                // launch LoginActivity and receive result
                loginLauncher.launch(Intent(this, LoginActivity::class.java))
            },
            onOpenCategory = { category ->
                startActivity(Intent(this, CategoryActivity::class.java).apply {
                    putExtra(CategoryActivity.EXTRA_CATEGORY, category)
                })
            },
            onOpenContribute = {
                startActivity(Intent(this, ContributeActivity::class.java))
            }
        ) }
    }

    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // no-op here; UI reads prefs to show logged-in name
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldCraftApp(
    onOpenSettings: () -> Unit,
    onOpenLogin: () -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenContribute: () -> Unit
) {
    val prefs = remember { FieldPrefs() }
    var username by remember { mutableStateOf(prefs.getUsername()) }
    val isLoggedIn = username.isNotBlank()

    MaterialTheme(colorScheme = darkColorScheme()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("FieldCraft") },
                    navigationIcon = {
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    },
                    actions = {
                        if (isLoggedIn) {
                            TextButton(onClick = {
                                prefs.clear()
                                username = ""
                            }) { Text("Log out") }
                        } else {
                            TextButton(onClick = onOpenLogin) { Text("Sign in") }
                        }
                    }
                )
            }
        ) { padding ->
            HomeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                isLoggedIn = isLoggedIn,
                username = username,
                onOpenCategory = onOpenCategory,
                onOpenContribute = onOpenContribute
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean,
    username: String,
    onOpenCategory: (String) -> Unit,
    onOpenContribute: () -> Unit
) {
    val categories = listOf(
        "Community", "Tech Corner", "FAQ",
        "Tactics", "Gear", "Maintenance", "Safety"
    )

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            val subtitle = if (isLoggedIn) "Welcome back, $username!" else "FieldCraft is a airsoft compendium for the field."
            Text(subtitle, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text("Browse categories below.", style = MaterialTheme.typography.bodySmall)
            Divider(Modifier.padding(top = 8.dp, bottom = 8.dp))
        }

        items(categories) { title ->
            CategoryWideCard(title = title) { onOpenCategory(title) }
        }

        item {
            Spacer(Modifier.height(8.dp))
            ElevatedButton(
                onClick = onOpenContribute,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Contribute a Tip") }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryWideCard(title: String, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ---------- super-lightweight prefs for login ---------- */
class FieldPrefs {
    private val ctx get() = AppContext.holder
    private val sp by lazy { ctx.getSharedPreferences("fieldcraft_prefs", 0) }
    fun save(username: String, pin: String) {
        sp.edit().putString("u", username).putString("p", pin).apply()
    }
    fun getUsername(): String = sp.getString("u", "") ?: ""
    fun clear() { sp.edit().clear().apply() }
}

/* AppContext singleton (put in this file for simplicity) */
object AppContext {
    lateinit var holder: android.content.Context
        private set
    fun init(c: android.content.Context) { holder = c.applicationContext }
}
