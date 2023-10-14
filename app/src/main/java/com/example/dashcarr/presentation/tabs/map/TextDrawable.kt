package com.example.dashcarr.presentation.tabs.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable


class TextDrawable(private var text: String, private var onSizeChanged: (Int) -> Unit) : Drawable() {
    private val paint: Paint = Paint()
    private val textBounds = Rect()

    fun setText(newText: String) {
        text = newText
        this.invalidateSelf()
    }

    fun setTextSize(size: Float) {
        paint.textSize = size
    }

    init {
        paint.color = Color.WHITE
        paint.textSize = 130f
        paint.isAntiAlias = true
        paint.isFakeBoldText = true
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
    }

    override fun draw(canvas: Canvas) {
        paint.getTextBounds(text, 0, text.length, textBounds)
        onSizeChanged(textBounds.height())
        canvas.drawText(
            text,
            bounds.width() / 2F,
            bounds.height() / 2F - textBounds.exactCenterY(),
            paint
        )
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

}
