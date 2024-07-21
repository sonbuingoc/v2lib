package com.sonbn.admobutilslibrary.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import com.sonbn.admobutilslibrary.BuildConfig

class CornersHelper(val context: Context, private val cornersModel: CornersModel) {
    private val path = Path()
    private var mCornersModel: CornersModel? = null

    init {
        this.mCornersModel = cornersModel
    }

    fun setCornersModel(cornersModel: CornersModel) {
        this.mCornersModel = cornersModel
    }

    fun draw(pair: Pair<Float, Float>, canvas: Canvas) {
        try {
            val width = pair.first
            val height = pair.second

            val radius = cornersModel.radius
            val topLeft = cornersModel.topLeft
            val topRight = cornersModel.topRight
            val bottomLeft = cornersModel.bottomLeft
            val bottomRight = cornersModel.bottomRight

            path.reset()
            if (radius != 0f) {
                path.addRoundRect(0f, 0f, width, height, radius, radius, Path.Direction.CW)
            } else {
                path.addRoundRect(
                    0f, 0f, width, height,
                    floatArrayOf(
                        topLeft, topLeft, // Top-left corner
                        topRight, topRight, // Top-right corner
                        bottomRight, bottomRight, // Bottom-right corner
                        bottomLeft, bottomLeft // Bottom-left corner
                    ),
                    Path.Direction.CW
                )
            }
            canvas.clipPath(path)
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
    }
}