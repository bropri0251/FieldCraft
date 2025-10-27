package com.Houndacivic.fieldcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class CategoryActivity : ComponentActivity() {
    companion object { const val EXTRA_CATEGORY = "extra_category" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Category"
        val dbh = FieldCraftDbHelper(this)
        val prefs = FieldPrefs()
        val username = prefs.getUsername()
        val isLoggedIn = username.isNotBlank()

        setContent {
            BackHandler(true) { finish() }

            Scaffold(
                topBar = { BackTopBar(title = category) { finish() } }
            ) { padding ->
                val mod = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)

                if (category == "Community") {
                    CommunityBlogScreen(
                        dbh = dbh,
                        isLoggedIn = isLoggedIn,
                        username = username
                    )
                } else {
                    CategoryScreen(
                        category = category,
                        modifier = mod
                    )
                }
            }
        }
    }
}

/* ---------------- Generic category listing (non-Community) ---------------- */

@Composable
fun CategoryScreen(
    category: String,
    modifier: Modifier = Modifier
) {
    val items = remember(category) {
        when (category) {
            "Tech & Maintenance" -> listOf(
                "HPA setup guide",
                "AEG shimming 101",
                "Hop-up tuning",
                "Barrel cleaning",
                "Gearbox lubrication points",
                "Battery care"
            )
            "FAQ" -> listOf("What BB weight?", "Eye pro standards", "FPS vs Joules")
            "Tactics" -> listOf("Room entry basics", "Bounding overwatch", "Comms brevity")
            "Gear" -> listOf("Plate carrier fit", "Radio options", "Boots & gloves")
            "Safety" -> listOf("Safe engagement distances", "Medic rules", "Hydration tips")
            else -> listOf("Coming soon")
        }
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text("Resources for $category", style = MaterialTheme.typography.titleMedium)
            Divider(Modifier.padding(vertical = 8.dp))
        }
        items(items) { row ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Text(row, Modifier.padding(16.dp))
            }
        }
    }
}

/* ---------------- Community blog (public read, only signed-in can post) ---------------- */

@Composable
fun CommunityBlogScreen(
    dbh: FieldCraftDbHelper,
    isLoggedIn: Boolean,
    username: String
) {
    var posts by remember { mutableStateOf(queryPosts(dbh)) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Community blog", style = MaterialTheme.typography.titleMedium)
        Text("Read posts from everyone. Only registered users can contribute.")

        if (isLoggedIn) {
            Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("New post")
            }
        }

        Divider()

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(posts) { p ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(p.title.ifBlank { "(Untitled)" }, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(p.body)
                    }
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    enabled = title.isNotBlank() && body.isNotBlank(),
                    onClick = {
                        val uid = lookupUserId(dbh, username)
                        insertPost(dbh, uid, title, body)
                        posts = queryPosts(dbh)
                        showDialog = false
                    }
                ) { Text("Post") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
            title = { Text("New community post") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Body") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

/* ---------------- Data + DB helpers for community posts ---------------- */

data class CommunityPost(
    val id: Long,
    val title: String,
    val body: String,
    val createdAt: Long
)

private fun queryPosts(dbh: FieldCraftDbHelper): List<CommunityPost> {
    val out = mutableListOf<CommunityPost>()
    val db = dbh.readableDatabase
    db.rawQuery(
        "SELECT id, title, body, created_at FROM community_posts ORDER BY created_at DESC",
        emptyArray()
    ).use { c ->
        val id = c.getColumnIndexOrThrow("id")
        val t  = c.getColumnIndexOrThrow("title")
        val b  = c.getColumnIndexOrThrow("body")
        val cr = c.getColumnIndexOrThrow("created_at")
        while (c.moveToNext()) {
            out += CommunityPost(
                id = c.getLong(id),
                title = c.getString(t) ?: "",
                body = c.getString(b) ?: "",
                createdAt = c.getLong(cr)
            )
        }
    }
    return out
}

private fun insertPost(dbh: FieldCraftDbHelper, userId: Long?, title: String, body: String) {
    val db = dbh.writableDatabase
    val cv = android.content.ContentValues().apply {
        put("user_id", userId)
        put("title", title.trim())
        put("body", body.trim())
        put("created_at", System.currentTimeMillis())
    }
    db.insertOrThrow("community_posts", null, cv)
}

private fun lookupUserId(dbh: FieldCraftDbHelper, username: String): Long? {
    if (username.isBlank()) return null
    val db = dbh.readableDatabase
    db.rawQuery(
        "SELECT id FROM users WHERE username = ? LIMIT 1",
        arrayOf(username.trim())
    ).use { c ->
        return if (c.moveToFirst()) c.getLong(0) else null
    }
}
