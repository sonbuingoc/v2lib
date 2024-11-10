package com.sonbn.admobutilslibrary.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.sonbn.admobutilslibrary.R

class UIImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    private var cornersHelper: CornersHelper

    init {
        setWillNotDraw(false)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.UIImageView, 0, 0)
        val radius = typedArray.getDimension(R.styleable.UIImageView_android_radius, 0f)
        val topLeft =
            typedArray.getDimension(R.styleable.UIImageView_android_topLeftRadius, 0f)
        val topRight =
            typedArray.getDimension(R.styleable.UIImageView_android_topRightRadius, 0f)
        val bottomLeft =
            typedArray.getDimension(R.styleable.UIImageView_android_bottomLeftRadius, 0f)
        val bottomRight =
            typedArray.getDimension(R.styleable.UIImageView_android_bottomRightRadius, 0f)
        typedArray.recycle()
        cornersHelper =
            CornersHelper(context, CornersModel(radius, topLeft, topRight, bottomLeft, bottomRight))
    }

    override fun draw(canvas: Canvas) {
        cornersHelper.draw(Pair(width.toFloat(), height.toFloat()), canvas)
        super.draw(canvas)
    }

    fun setCorners(cornersModel: CornersModel) {
        cornersHelper.setCornersModel(cornersModel)
        invalidate()
    }
}