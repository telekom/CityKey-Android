package com.telekom.citykey.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.telekom.citykey.utils.extensions.dpToPixel
import java.security.MessageDigest

class GlideCircleWithBorder(context: Context, private val borderWidth: Int, private var borderColor: Int) :
    BitmapTransformation() {

    companion object {
        private const val VERSION = 1
        private const val ID = "com.bumptech.glide.load.resource.bitmap.CircleCrop.$VERSION"
    }

    private var borderToImagePadding = 2

    init {
        borderToImagePadding = borderToImagePadding.dpToPixel(context)
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val circle = TransformationUtils.circleCrop(
            pool,
            toTransform,
            outWidth + borderToImagePadding,
            outHeight + borderToImagePadding
        )
        return addBorderToCircularBitmap(circle)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID.toByteArray(Key.CHARSET))
    }

    // Custom method to add a border around circular bitmap
    private fun addBorderToCircularBitmap(srcBitmap: Bitmap): Bitmap {
        // Calculate the circular bitmap width with border
        val dstBitmapWidth = srcBitmap.width + borderWidth * 2 + borderToImagePadding * 2
        val dstBitmap = Bitmap.createBitmap(dstBitmapWidth, dstBitmapWidth, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dstBitmap)

        canvas.drawBitmap(
            srcBitmap,
            borderWidth.toFloat() + borderToImagePadding,
            borderWidth.toFloat() + borderToImagePadding,
            null
        )
        val paint = Paint()
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth.toFloat()
        paint.isAntiAlias = true

        canvas.drawCircle(
            (canvas.width / 2).toFloat(), // cx
            (canvas.width / 2).toFloat(), // cy
            (srcBitmap.width / 2 + borderWidth / 2 + borderToImagePadding).toFloat(), // Radius
            paint // Paint
        )

        // Free the native object associated with this bitmap.
        srcBitmap.recycle()

        // Return the bordered circular bitmap
        return dstBitmap
    }
}
