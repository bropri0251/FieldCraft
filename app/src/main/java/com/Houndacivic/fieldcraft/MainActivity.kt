package com.Houndacivic.fieldcraft

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.Houndacivic.fieldcraft.ui.theme.FieldCraftTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContext.init(applicationContext)

        setContent {
            FieldCraftTheme {
                val ctx = this
                val dbh = remember { FieldCraftDbHelper(ctx) }
                val prefs = remember { FieldPrefs() }

                var username by remember { mutableStateOf(prefs.getUsername()) }
                var isAdmin by remember { mutableStateOf(prefs.isAdmin()) }
                var currentScreen by remember { mutableStateOf("Home") }

                LaunchedEffect(Unit) {
                    username = prefs.getUsername()
                    isAdmin = prefs.isAdmin()
                }

                when (currentScreen) {
                    "Search" -> {
                        SearchScreen(
                            onBack = { currentScreen = "Home" },
                            dbh = dbh,
                            prefs = prefs
                        )
                    }

                    "AdminPanel" -> {
                        AdminPanelScreen(onBack = { currentScreen = "Home" })
                    }

                    else -> {
                        Scaffold(
                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = { Text("FieldCraft") },
                                    actions = {
                                        if (isAdmin) {
                                            TextButton(onClick = { currentScreen = "AdminPanel" }) {
                                                Text("Admin")
                                            }
                                        }
                                        if (username.isNotBlank()) {
                                            TextButton(onClick = {
                                                prefs.signOut()
                                                username = ""
                                                isAdmin = false
                                            }) { Text("Log out") }
                                        } else {
                                            TextButton(onClick = {
                                                startActivity(Intent(ctx, LoginActivity::class.java))
                                                // refresh in case login returns
                                                username = prefs.getUsername()
                                                isAdmin = prefs.isAdmin()
                                            }) { Text("Sign in") }
                                        }
                                    }
                                )
                            }
                        ) { padding ->
                            HomeScreen(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding)
                                    .padding(horizontal = 16.dp),
                                isLoggedIn = username.isNotBlank(),
                                username = prefs.getDisplayName().ifBlank { username },
                                onOpenCategory = { category ->
                                    startActivity(Intent(ctx, CategoryActivity::class.java).apply {
                                        putExtra(CategoryActivity.EXTRA_CATEGORY, category)
                                    })
                                },
                                onOpenContribute = {
                                    startActivity(Intent(ctx, ContributeActivity::class.java))
                                },
                                onOpenSearch = { currentScreen = "Search" }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------
   HomeScreen composable (kept here so this file compiles alone)
------------------------------------------------------------ */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean,
    username: String,
    onOpenCategory: (String) -> Unit,
    onOpenContribute: () -> Unit,
    onOpenSearch: () -> Unit
) {
    Scaffold(
        bottomBar = {
            TextButton(
                onClick = onOpenContribute,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) { Text("Contribute a Tip") }
        }
    ) { pad ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoggedIn) {
                Text(
                    text = "Welcome back, $username!",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text(
                    text = "FieldCraft is an airsoft compendium for the field.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = onOpenSearch,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Search Articles") }

            val categories = listOf(
                "Community", "Tech & Maintenance", "FAQ", "Tactics", "Gear", "Safety"
            )
            categories.forEach { cat ->
                ElevatedCard(
                    onClick = { onOpenCategory(cat) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(cat, style = MaterialTheme.typography.titleMedium)
                            Text("Tap to open", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                    }
                }
            }
        }
    }
}
