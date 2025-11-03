package com.Houndacivic.fieldcraft

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    dbh: FieldCraftDbHelper,
    prefs: FieldPrefs
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf<Article>()) }

    var dialogOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Article?>(null) }
    var tTitle by remember { mutableStateOf("") }
    var tSummary by remember { mutableStateOf("") }
    var tTags by remember { mutableStateOf("") }

    val username = prefs.getUsername()
    val displayName = prefs.getDisplayName().ifBlank { username }
    val isAdmin = prefs.isAdmin()

    suspend fun refresh() {
        val result = withContext(Dispatchers.IO) { dbh.searchArticles(query) }
        items = result
    }

    LaunchedEffect(query) {
        refresh()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Top-right Add (+) button
                    IconButton(onClick = {
                        editing = null
                        tTitle = ""
                        tSummary = ""
                        tTags = ""
                        dialogOpen = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add article")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                tTitle = ""
                tSummary = ""
                tTags = ""
                dialogOpen = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    scope.launch { refresh() }
                },
                label = { Text("Search Articles") },
                modifier = Modifier.fillMaxWidth()
            )

            items.forEach { a ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(a.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(a.summary, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "by ${a.author.ifBlank { "unknown" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    // Add/Edit dialog
    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            title = {
                Text(if (editing == null) "Add Article" else "Edit Article")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tTitle,
                        onValueChange = { tTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tSummary,
                        onValueChange = { tSummary = it },
                        label = { Text("Summary") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tTags,
                        onValueChange = { tTags = it },
                        label = { Text("Tags") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            if (editing == null) {
                                dbh.insertArticle(tTitle, tSummary, tTags, author = displayName)
                            } else {
                                val canEdit =
                                    isAdmin || editing!!.author.equals(displayName, true)
                                            || editing!!.author.equals(username, true)
                                if (canEdit) {
                                    dbh.updateArticle(
                                        editing!!.copy(
                                            title = tTitle,
                                            summary = tSummary,
                                            tags = tTags
                                        )
                                    )
                                }
                            }
                        }
                        refresh()
                    }
                    dialogOpen = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { dialogOpen = false }) { Text("Cancel") }
            }
        )
    }
}
