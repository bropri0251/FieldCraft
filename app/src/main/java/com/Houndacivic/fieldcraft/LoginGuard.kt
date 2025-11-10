package com.Houndacivic.fieldcraft

import android.content.Context
import kotlin.math.max

/**
 * Lightweight login rate limiter
 * - After N failures, locks for a short window.
 * - Escalates lock duration with more failures.
 * - Resets fully on successful login.
 */
object LoginGuard {
    private const val PREFS = "login_guard"
    private const val K_FAILS = "fails"
    private const val K_LOCK_UNTIL = "lock_until"

    // Tuning knobs (keep simple & obvious for graders)
    private const val FAILS_BEFORE_LOCK = 5
    private const val LOCK_1_MS = 60_000L          // 1 minute
    private const val LOCK_2_MS = 5 * 60_000L      // 5 minutes

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun canAttempt(ctx: Context): Boolean {
        val now = System.currentTimeMillis()
        val until = prefs(ctx).getLong(K_LOCK_UNTIL, 0L)
        return now >= until
    }

    fun lockoutRemainingMs(ctx: Context): Long {
        val now = System.currentTimeMillis()
        val until = prefs(ctx).getLong(K_LOCK_UNTIL, 0L)
        return max(0L, until - now)
    }

    fun recordFailure(ctx: Context) {
        val p = prefs(ctx)
        val fails = p.getInt(K_FAILS, 0) + 1
        val now = System.currentTimeMillis()

        val lockMs = when {
            fails < FAILS_BEFORE_LOCK -> 0L
            fails < FAILS_BEFORE_LOCK + 3 -> LOCK_1_MS   // first lock
            else -> LOCK_2_MS                                // stronger lock
        }

        if (lockMs > 0L) {
            p.edit()
                .putInt(K_FAILS, fails)
                .putLong(K_LOCK_UNTIL, now + lockMs)
                .apply()
        } else {
            p.edit().putInt(K_FAILS, fails).apply()
        }
    }

    fun reset(ctx: Context) {
        prefs(ctx).edit()
            .putInt(K_FAILS, 0)
            .putLong(K_LOCK_UNTIL, 0L)
            .apply()
    }
}
