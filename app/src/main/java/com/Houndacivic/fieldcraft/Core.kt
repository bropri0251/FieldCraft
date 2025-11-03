package com.Houndacivic.fieldcraft

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/* -----------------------------
   Data models
------------------------------ */
data class Article(
    val id: Int = 0,
    val title: String,
    val summary: String,
    val tags: String,
    val updatedUtc: Long,
    val author: String
)

/* ------------------------------------------------------------
   SQLite helper with safe, incremental migrations + onOpen fix
   Schema v6:

   articles(
     id INTEGER PK AUTOINCREMENT,
     title TEXT NOT NULL,
     summary TEXT NOT NULL DEFAULT '',
     tags TEXT NOT NULL DEFAULT '',
     updatedUtc INTEGER NOT NULL,
     author TEXT NOT NULL DEFAULT ''
   )

   users(
     id INTEGER PK AUTOINCREMENT,
     email TEXT NOT NULL UNIQUE,
     password TEXT NOT NULL,
     displayName TEXT NOT NULL DEFAULT '',
     isAdmin INTEGER NOT NULL DEFAULT 0
   )
------------------------------------------------------------ */
class FieldCraftDbHelper(ctx: Context) :
    SQLiteOpenHelper(ctx, "fieldcraft.db", null, /* DB VERSION */ 6) {

    override fun onCreate(db: SQLiteDatabase) {
        createArticles(db)
        createUsers(db)
    }

    // Defensive: ensure schema is correct every time DB is opened
    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        ensureArticlesShape(db)
        if (!tableExists(db, "users")) createUsers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Articles columns introduced gradually
        if (oldVersion < 2) safeAddColumn(db, "articles", "tags", "TEXT NOT NULL DEFAULT ''")
        if (oldVersion < 3) {
            safeAddColumn(db, "articles", "author", "TEXT NOT NULL DEFAULT ''")
            safeAddColumn(db, "articles", "updatedUtc", "INTEGER NOT NULL DEFAULT 0")
        }
        if (oldVersion < 4) safeAddColumn(db, "articles", "summary", "TEXT NOT NULL DEFAULT ''")

        // Users table introduced later
        if (oldVersion < 5 && !tableExists(db, "users")) createUsers(db)

        // Final sanity pass
        ensureArticlesShape(db)
    }

    /* ----------------- Schema creators ----------------- */

    private fun createArticles(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS articles(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                summary TEXT NOT NULL DEFAULT '',
                tags TEXT NOT NULL DEFAULT '',
                updatedUtc INTEGER NOT NULL,
                author TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()
        )
    }

    private fun createUsers(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS users(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                displayName TEXT NOT NULL DEFAULT '',
                isAdmin INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    /* ----------------- Articles: CRUD + search ----------------- */

    fun insertArticle(title: String, summary: String, tags: String, author: String) {
        writableDatabase.compileStatement(
            """
            INSERT INTO articles (title, summary, tags, updatedUtc, author)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent()
        ).apply {
            bindString(1, title)
            bindString(2, summary)
            bindString(3, tags)
            bindLong(4, System.currentTimeMillis())
            bindString(5, author)
        }.executeInsert()
    }

    fun updateArticle(a: Article) {
        writableDatabase.compileStatement(
            """
            UPDATE articles
               SET title = ?, summary = ?, tags = ?, updatedUtc = ?, author = ?
             WHERE id = ?
            """.trimIndent()
        ).apply {
            bindString(1, a.title)
            bindString(2, a.summary)
            bindString(3, a.tags)
            bindLong(4, System.currentTimeMillis())
            bindString(5, a.author)
            bindLong(6, a.id.toLong())
        }.executeUpdateDelete()
    }

    fun deleteArticle(id: Int) {
        writableDatabase.compileStatement("DELETE FROM articles WHERE id = ?")
            .apply { bindLong(1, id.toLong()) }
            .executeUpdateDelete()
    }

    fun searchArticles(query: String): List<Article> {
        val q = "%$query%"
        val res = mutableListOf<Article>()
        readableDatabase.rawQuery(
            """
            SELECT id, title, summary, tags, updatedUtc, author
              FROM articles
             WHERE title   LIKE ?
                OR summary LIKE ?
                OR tags    LIKE ?
             ORDER BY updatedUtc DESC
            """.trimIndent(),
            arrayOf(q, q, q)
        ).use { c ->
            while (c.moveToNext()) {
                res += Article(
                    id = c.getInt(0),
                    title = c.getString(1),
                    summary = c.getString(2),
                    tags = c.getString(3),
                    updatedUtc = c.getLong(4),
                    author = c.getString(5)
                )
            }
        }
        return res
    }

    /* ----------------- Users: helpers used by Login/Settings ----------------- */

    fun userExistsByEmail(email: String): Boolean {
        readableDatabase.rawQuery(
            "SELECT 1 FROM users WHERE email = ? LIMIT 1",
            arrayOf(email)
        ).use { return it.moveToFirst() }
    }

    fun insertUser(email: String, password: String, displayName: String, isAdmin: Boolean = false) {
        writableDatabase.compileStatement(
            """
            INSERT INTO users (email, password, displayName, isAdmin)
            VALUES (?, ?, ?, ?)
            """.trimIndent()
        ).apply {
            bindString(1, email)
            bindString(2, password) // per assignment week: plaintext OK
            bindString(3, displayName)
            bindLong(4, if (isAdmin) 1 else 0)
        }.executeInsert()
    }

    fun verifyUser(email: String, password: String): Boolean {
        readableDatabase.rawQuery(
            "SELECT 1 FROM users WHERE email = ? AND password = ? LIMIT 1",
            arrayOf(email, password)
        ).use { return it.moveToFirst() }
    }

    fun displayNameFor(email: String): String {
        readableDatabase.rawQuery(
            "SELECT displayName FROM users WHERE email = ? LIMIT 1",
            arrayOf(email)
        ).use { c ->
            return if (c.moveToFirst()) {
                val dn = c.getString(0) ?: ""
                if (dn.isBlank()) email else dn
            } else email
        }
    }

    fun setDisplayName(email: String, displayName: String) {
        writableDatabase.compileStatement(
            "UPDATE users SET displayName = ? WHERE email = ?"
        ).apply {
            bindString(1, displayName)
            bindString(2, email)
        }.executeUpdateDelete()
    }

    fun setAdmin(email: String, isAdmin: Boolean) {
        writableDatabase.compileStatement(
            "UPDATE users SET isAdmin = ? WHERE email = ?"
        ).apply {
            bindLong(1, if (isAdmin) 1 else 0)
            bindString(2, email)
        }.executeUpdateDelete()
    }

    fun isAdmin(email: String): Boolean {
        readableDatabase.rawQuery(
            "SELECT isAdmin FROM users WHERE email = ? LIMIT 1",
            arrayOf(email)
        ).use { c -> return c.moveToFirst() && c.getInt(0) == 1 }
    }

    /* ----------------- Internal helpers ----------------- */

    private fun ensureArticlesShape(db: SQLiteDatabase) {
        // defensively add any missing columns
        safeAddColumn(db, "articles", "summary", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(db, "articles", "tags", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(db, "articles", "updatedUtc", "INTEGER NOT NULL DEFAULT 0")
        safeAddColumn(db, "articles", "author", "TEXT NOT NULL DEFAULT ''")

        // rebuild if still malformed (very old/corrupt installs)
        if (!hasAllColumns(db, "articles", listOf("id", "title", "summary", "tags", "updatedUtc", "author"))) {
            db.execSQL("ALTER TABLE articles RENAME TO articles_old")
            createArticles(db)
            db.execSQL(
                """
                INSERT INTO articles (id, title, summary, tags, updatedUtc, author)
                SELECT id,
                       title,
                       COALESCE(summary, ''),
                       COALESCE(tags, ''),
                       COALESCE(updatedUtc, 0),
                       COALESCE(author, '')
                  FROM articles_old
                """.trimIndent()
            )
            db.execSQL("DROP TABLE articles_old")
        }
    }

    private fun safeAddColumn(db: SQLiteDatabase, table: String, name: String, definition: String) {
        if (!columnExists(db, table, name)) {
            db.execSQL("ALTER TABLE $table ADD COLUMN $name $definition")
        }
    }

    private fun columnExists(db: SQLiteDatabase, table: String, name: String): Boolean {
        db.rawQuery("PRAGMA table_info($table)", null).use { c ->
            val idx = c.getColumnIndex("name")
            while (c.moveToNext()) {
                if (c.getString(idx).equals(name, ignoreCase = true)) return true
            }
        }
        return false
    }

    private fun hasAllColumns(db: SQLiteDatabase, table: String, cols: List<String>): Boolean {
        db.rawQuery("PRAGMA table_info($table)", null).use { c ->
            val have = mutableSetOf<String>()
            val idx = c.getColumnIndex("name")
            while (c.moveToNext()) have += c.getString(idx)
            return have.containsAll(cols.toSet())
        }
    }

    private fun tableExists(db: SQLiteDatabase, table: String): Boolean {
        db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(table)
        ).use { return it.moveToFirst() }
    }
}
