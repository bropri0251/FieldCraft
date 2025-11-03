package com.Houndacivic.fieldcraft

import android.content.Context
import android.content.SharedPreferences

class FieldPrefs(ctx: Context = AppContext.get()) {
    private val sp: SharedPreferences =
        ctx.getSharedPreferences("field_prefs", Context.MODE_PRIVATE)

    fun getUsername(): String = sp.getString("username", "") ?: ""
    fun getDisplayName(): String = sp.getString("display_name", "") ?: getUsername()
    fun isAdmin(): Boolean = sp.getBoolean("is_admin", false)

    fun setDisplayName(name: String) {
        sp.edit().putString("display_name", name).apply()
    }

    fun setAdmin(admin: Boolean) {
        sp.edit().putBoolean("is_admin", admin).apply()
    }

    fun signIn(username: String, isAdmin: Boolean = false, displayName: String = username) {
        sp.edit()
            .putString("username", username)
            .putString("display_name", displayName)
            .putBoolean("is_admin", isAdmin)
            .apply()
    }

    fun signOut() {
        sp.edit().clear().apply()
    }
}
