package com.telekom.citykey.custom.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.ceil
import kotlin.math.min

/**
 * Image view in e.g. article view: the drawable should be scaled (according to design documents) as follows:
 * - Full width
 * - Height according to maximum as specified and cropped centered vertically
 */
class VerticallyCroppedImageView : AppCompatImageView {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    /**
     * Scale image according to design docs
     */
    override fun setFrame(frameLeft: Int, frameTop: Int, frameRight: Int, frameBottom: Int): Boolean {
        val drawable = drawable ?: return super.setFrame(frameLeft, frameTop, frameRight, frameBottom)

        val frameWidth = frameRight - frameLeft
        val scaleFactor = frameWidth.toFloat() / drawable.intrinsicWidth.toFloat()
        val tmpScaledImgHeight = drawable.intrinsicHeight * scaleFactor
        val verticalCropAmount = if (tmpScaledImgHeight > maxHeight) (tmpScaledImgHeight - maxHeight) / 2.0f else 0f

        val tmpMatrix = matrix
        tmpMatrix.setScale(scaleFactor, scaleFactor, .0f, .0f)
        if (verticalCropAmount > 0) {
            tmpMatrix.postTranslate(0f, -verticalCropAmount)
        }
        imageMatrix = tmpMatrix

        return super.setFrame(frameLeft, frameTop, frameRight, frameBottom)
    }

    /**
     * Set height of image view, since it doesn't seem to happen automatically in {@see setFrame()}
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val drawable = drawable ?: return super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val scaleFactor: Float = specWidth.toFloat() / drawable.intrinsicWidth.toFloat()
        val tmpScaledImgHeight = drawable.intrinsicHeight * scaleFactor

        setMeasuredDimension(specWidth, min(maxHeight, ceil(tmpScaledImgHeight).toInt()))
    }
}
