package com.sonbn.admobutilslibrary.utils

import android.content.SharedPreferences

private inline fun SharedPreferences.edit(
    action: SharedPreferences.Editor.() -> Unit
) {
    val editor = edit()
    action(editor)
    editor.apply()
}