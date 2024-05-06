package com.sonbn.admobutilslibrary

import android.util.Log
import android.view.View

fun View.gone() {
    visibility = View.GONE
}
inline fun runTryCatch(block: () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
        //
    }
}