Key Features

Login with Rate Limiting

Brute-force protection: after N failed attempts, sign-in is locked for a cooldown period.

Session saved to SharedPreferences (u, p, is_admin).

Three Connected Activities

LoginActivity → MainActivity → CategoryActivity (plus optional inline Search).

Top app bars with proper back navigation.

Articles (SQLite)

Simple CRUD helpers: list, search, insert, update, delete.

Admin & Demo Accounts

Modern UI

Material 3 + Compose (dark/purple theme).

Elevated cards, list with search, FAB for quick add.

Architecture & Tech

Language: Kotlin

UI: Jetpack Compose (Material 3)

Data: SQLite (via FieldCraftDbHelper)

State: Compose state (remember, mutableStateOf)

Security: Login rate limiting, session separation, minimal permissions


Project Structure (high-level)
app/
 └─ src/main/java/com/Houndacivic/fieldcraft/
    ├─ MainActivity.kt             // App shell + navigation
    ├─ LoginActivity.kt            // Sign-in + create account, rate limiting, back arrow
    ├─ CategoryActivity.kt         // Article list/search + simple CRUD demo
    ├─ Core.kt                     // FieldCraftDbHelper, Article model, seeding, auth helpers
    ├─ ui/theme/                   // Material 3 theme files
    └─ ...

Security Measures (Week-5 Focus)

Rate-limited login: Tracks failures and enforces a timed cooldown to reduce brute-force risk.

Session scoping: User session data lives in SharedPreferences; no dangerous exports or broad component exposure.

Export rules: Only launch Activity is android:exported="true" (others are false/no intent filters).

No sensitive permissions: App avoids unnecessary runtime permissions.
