package com.sonbn.admobutilslibrary.utils

import android.os.SystemClock
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.sonbn.admobutilslibrary.BuildConfig

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

fun View.setOnSingleClickListener(listener: (View) -> Unit) {
    setOnClickListener(object : OnSingleClickListener() {
        override fun onSingleClick(v: View) {
            listener(v)
        }
    })
}

fun View.setOnScaleClickListener(listener: (View) -> Unit, scale: Float = 0.95f) {
    setOnClickListener {
        val anim: Animation = ScaleAnimation(
            scale, 1f,  // Start and end values for the X axis scaling
            scale, 1f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0.5f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 0.5f
        ) // Pivot point of Y scaling

        anim.fillAfter = true // Needed to keep the result of the animation

        anim.duration = 300
        it.startAnimation(anim)

        listener(it)
    }
}

abstract class OnSingleClickListener : View.OnClickListener {
    companion object {
        private const val MIN_CLICK_INTERVAL: Long = 500
    }

    private var mLastClickTime: Long = 0
    abstract fun onSingleClick(v: View)
    override fun onClick(v: View) {
        val currentClickTime: Long = SystemClock.uptimeMillis()
        val elapsedTime = currentClickTime - mLastClickTime
        mLastClickTime = currentClickTime

        if (elapsedTime <= MIN_CLICK_INTERVAL) return
        onSingleClick(v)
    }
}