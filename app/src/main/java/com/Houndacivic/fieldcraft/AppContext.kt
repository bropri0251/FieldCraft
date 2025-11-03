package com.Houndacivic.fieldcraft

import android.content.Context

/** Simple application-context holder used from composables and helpers. */
object AppContext {
    private lateinit var appCtx: Context

    fun init(ctx: Context) {
        appCtx = ctx.applicationContext
    }

    fun get(): Context = appCtx
}
