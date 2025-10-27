package com.Houndacivic.fieldcraft

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.Houndacivic.fieldcraft.ui.theme.FieldCraftTheme

/**
 * MainActivity – FieldCraft
 * - Global purple/black theme via FieldCraftTheme at setContent(). (WIP)
 * - Home with category cards (with icons).
 * - Inline Search screen (Compose) with visible & system back handling.
 * - Launches Settings/Login/Category/Contribute Activities.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContext.init(applicationContext)

        // Initialize SQLite schema (creates tables on first run)
        FieldCraftDbHelper(this).writableDatabase.close()

        setContent {
            FieldCraftTheme {
                // Simple in-app navigation. "Home" or "Search"
                var currentScreen by remember { mutableStateOf("Home") }

                val context = LocalContext.current
                val dbh = remember { FieldCraftDbHelper(context) }

                if (currentScreen == "Search") {
                    // In-app Search (visible back + system back)
                    SearchScreen(
                        dbh = dbh,
                        onBack = { currentScreen = "Home" },
                        onOpenArticle = { /* TODO: open detail screen if you add one */ }
                    )
                } else {
                    // App shell with toolbar + Home content; other areas launch Activities
                    FieldCraftApp(
                        onOpenSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                        onOpenLogin = { loginLauncher.launch(Intent(this, LoginActivity::class.java)) },
                        onOpenCategory = { category ->
                            startActivity(Intent(this, CategoryActivity::class.java).apply {
                                putExtra(CategoryActivity.EXTRA_CATEGORY, category)
                            })
                        },
                        onOpenContribute = { startActivity(Intent(this, ContributeActivity::class.java)) },
                        onOpenSearch = { currentScreen = "Search" }
                    )
                }
            }
        }
    }

    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* no-op; UI reads prefs */ }
}

/* ---------------- App Shell ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldCraftApp(
    onOpenSettings: () -> Unit,
    onOpenLogin: () -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenContribute: () -> Unit,
    onOpenSearch: () -> Unit
) {
    val prefs = remember { FieldPrefs() }
    var username by remember { mutableStateOf(prefs.getUsername()) }
    val isLoggedIn = username.isNotBlank()

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
                            prefs.clear(); username = ""
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
                .padding(padding)
                .padding(horizontal = 16.dp),
            isLoggedIn = isLoggedIn,
            username = username,
            onOpenCategory = onOpenCategory,
            onOpenContribute = onOpenContribute,
            onOpenSearch = onOpenSearch
        )
    }
}

/* ---------------- Home ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean,
    username: String,
    onOpenCategory: (String) -> Unit,
    onOpenContribute: () -> Unit,
    onOpenSearch: () -> Unit
) {
    // Merged "Tech Corner" + "Maintenance"
    val categories = listOf(
        "Community",
        "Tech & Maintenance",
        "FAQ",
        "Tactics",
        "Gear",
        "Safety"
    )

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            val subtitle = if (isLoggedIn)
                "Welcome back, $username!"
            else
                "FieldCraft is an airsoft compendium for the field."
            Text(subtitle, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text("Browse categories below, or search articles.", style = MaterialTheme.typography.bodySmall)
            Divider(Modifier.padding(top = 8.dp, bottom = 8.dp))

            ElevatedButton(
                onClick = onOpenSearch,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Search Articles") }

            Spacer(Modifier.height(8.dp))
        }

        items(categories) { title ->
            val icon = iconFor(title)
            CategoryWideCard(title = title, icon = icon) { onOpenCategory(title) }
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

/** Map categories to on-theme icons (Outlined set). */
@Composable
private fun iconFor(category: String): ImageVector = when (category) {
    "Community" -> Icons.Outlined.Groups
    "Tech & Maintenance" -> Icons.Outlined.Handyman
    "FAQ" -> Icons.Outlined.HelpOutline
    "Tactics" -> Icons.Outlined.MilitaryTech
    "Gear" -> Icons.Outlined.Backpack
    "Safety" -> Icons.Outlined.HealthAndSafety
    else -> Icons.Outlined.Label
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryWideCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ---------------- Search (inline screen with back) ---------------- */

data class ArticleRow(
    val id: Long,
    val title: String,
    val content: String
)

fun searchArticles(dbh: FieldCraftDbHelper, q: String): List<ArticleRow> {
    val query = q.trim()
    if (query.isEmpty()) return emptyList()
    val out = mutableListOf<ArticleRow>()
    val db = dbh.readableDatabase
    db.rawQuery(
        """
        SELECT id, title, content FROM articles
        WHERE is_published = 1
          AND (title LIKE '%' || ? || '%' OR content LIKE '%' || ? || '%')
        ORDER BY created_at DESC
        """.trimIndent(),
        arrayOf(query, query)
    ).use { c ->
        val idIdx = c.getColumnIndexOrThrow("id")
        val tIdx  = c.getColumnIndexOrThrow("title")
        val bIdx  = c.getColumnIndexOrThrow("content")
        while (c.moveToNext()) {
            out += ArticleRow(
                id = c.getLong(idIdx),
                title = c.getString(tIdx) ?: "",
                content = c.getString(bIdx) ?: ""
            )
        }
    }
    return out
}

@Composable
fun SearchScreen(
    dbh: FieldCraftDbHelper,
    onBack: () -> Unit,
    onOpenArticle: (Long) -> Unit
) {
    var q by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<ArticleRow>>(emptyList()) }

    // Hardware/gesture back
    BackHandler(enabled = true) { onBack() }

    Scaffold(
        topBar = { BackTopBar(title = "Search") { onBack() } }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Enter a keyword. Wildcards supported (%). Example: %hop%")
            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                label = { Text("Search title or content") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { results = searchArticles(dbh, q) },
                enabled = q.trim().isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Search") }

            Divider()

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results) { row ->
                    ElevatedCard(
                        onClick = { onOpenArticle(row.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                row.title.ifBlank { "(Untitled)" },
                                style = MaterialTheme.typography.titleMedium
                            )
                            val snippet = row.content.replace('\n', ' ')
                            Text(
                                if (snippet.length > 120) snippet.take(120) + "…" else snippet,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- Simple Prefs & App Context ---------------- */

class FieldPrefs {
    private val ctx get() = AppContext.holder
    private val sp by lazy { ctx.getSharedPreferences("fieldcraft_prefs", 0) }
    fun save(username: String, password: String) {
        sp.edit().putString("u", username).putString("p", password).apply()
    }
    fun getUsername(): String = sp.getString("u", "") ?: ""
    fun clear() { sp.edit().clear().apply() }
}

object AppContext {
    lateinit var holder: android.content.Context
        private set
    fun init(c: android.content.Context) { holder = c.applicationContext }
}

/* ---------------- SQLite Helper (version 2 with community_posts) ---------------- */

class FieldCraftDbHelper(context: android.content.Context) :
    android.database.sqlite.SQLiteOpenHelper(context, "fieldcraft.db", null, 2) {

    override fun onCreate(db: android.database.sqlite.SQLiteDatabase) {
        // Users
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                email TEXT UNIQUE,
                password_hash TEXT,
                role TEXT DEFAULT 'user',
                created_at INTEGER
            );
        """.trimIndent())

        // Categories
        db.execSQL("""
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE,
                description TEXT
            );
        """.trimIndent())

        // Articles/tips
        db.execSQL("""
            CREATE TABLE articles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER,
                title TEXT,
                content TEXT,
                level TEXT DEFAULT 'all',
                is_published INTEGER DEFAULT 1,
                created_at INTEGER,
                updated_at INTEGER,
                FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE
            );
        """.trimIndent())

        // Resource links
        db.execSQL("""
            CREATE TABLE resource_links (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                article_id INTEGER,
                url TEXT,
                title TEXT,
                source_type TEXT DEFAULT 'article',
                added_by_user_id INTEGER,
                created_at INTEGER,
                FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE,
                FOREIGN KEY(added_by_user_id) REFERENCES users(id) ON DELETE SET NULL
            );
        """.trimIndent())

        // Comments
        db.execSQL("""
            CREATE TABLE comments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                article_id INTEGER,
                user_id INTEGER,
                parent_id INTEGER,
                body TEXT,
                created_at INTEGER,
                FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(parent_id) REFERENCES comments(id) ON DELETE CASCADE
            );
        """.trimIndent())

        // Votes
        db.execSQL("""
            CREATE TABLE votes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                article_id INTEGER,
                comment_id INTEGER,
                value INTEGER,
                created_at INTEGER,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE,
                FOREIGN KEY(comment_id) REFERENCES comments(id) ON DELETE CASCADE
            );
        """.trimIndent())

        // Bookmarks
        db.execSQL("""
            CREATE TABLE bookmarks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                article_id INTEGER,
                created_at INTEGER,
                UNIQUE(user_id, article_id),
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE
            );
        """.trimIndent())

        // Contributions
        db.execSQL("""
            CREATE TABLE contributions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                category_id INTEGER,
                title TEXT,
                content TEXT,
                status TEXT DEFAULT 'pending',
                reviewer_user_id INTEGER,
                reviewed_at INTEGER,
                created_at INTEGER,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE,
                FOREIGN KEY(reviewer_user_id) REFERENCES users(id) ON DELETE SET NULL
            );
        """.trimIndent())

        // Community posts (blog) – public read, only signed-in users post
        db.execSQL("""
            CREATE TABLE community_posts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                title TEXT,
                body TEXT,
                created_at INTEGER,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE SET NULL
            );
        """.trimIndent())
    }

    override fun onUpgrade(db: android.database.sqlite.SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS community_posts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    title TEXT,
                    body TEXT,
                    created_at INTEGER,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE SET NULL
                );
            """.trimIndent())
        }
    }
}
