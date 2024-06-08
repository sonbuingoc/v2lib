package com.sonbn.admobutilslibrary

import android.view.View

fun View.gone() {
    visibility = View.GONE
}
fun runTryCatch(block: () -> Unit) {
    try {
        block()
    }catch (e: Throwable) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace()
        }
    }
}