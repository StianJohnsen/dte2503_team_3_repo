package com.example.dashcarr.presentation.tabs.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable


class TextDrawable : Drawable() {
    private val paint: Paint = Paint()
    private var text: String = ""
    fun setText(newText: String) {
        text = newText
        this.invalidateSelf()
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
        canvas.drawText(text, bounds.width() / 2F, bounds.height() / 2F, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}
