package com.sonbn.admobutilslibrary.utils

import android.content.Context
import android.util.TypedValue

object DimensionUtils {

    // Chuyển đổi dp sang pixel
    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    // Chuyển đổi pixel sang dp
    fun pxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    // Chuyển đổi sp sang pixel
    fun spToPx(context: Context, sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
    }

    // Chuyển đổi pixel sang sp
    fun pxToSp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.scaledDensity
    }
}