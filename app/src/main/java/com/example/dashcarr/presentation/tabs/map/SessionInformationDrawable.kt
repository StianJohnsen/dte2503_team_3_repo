package com.example.dashcarr.presentation.tabs.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.location.Location
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlin.math.roundToInt


class SessionInformationDrawable(
    private val context: Context,
    private var textSizes: Array<Float>,
    private val rotateDrawing: Boolean,
    private var onSizeChanged: (Int) -> Unit
) : Drawable() {
    private val paint: Paint = Paint()
    private var street: String = ""
    private lateinit var location: Location
    private var missingStreetUpdate = true

    fun updateLocation(location: Location) {
        if (missingStreetUpdate || this.location.distanceTo(location) > 50) {
            updateStreet(location)
        }
        this.location = location
        this.invalidateSelf()
    }

    private fun updateStreet(location: Location) {
        val url =
            "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${location.latitude}&lon=${location.longitude}"
        val queue = Volley.newRequestQueue(context)
        val stringRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val address = response.getJSONObject("address")
                street = "${address.get("road")}, ${address.get("town")}"
                missingStreetUpdate = false
                this.invalidateSelf()
            },
            { error ->
                missingStreetUpdate = true
            }
        )
        queue.add(stringRequest)
    }

    init {
        paint.color = Color.WHITE
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
    }

    override fun draw(canvas: Canvas) {
        val words = emptyList<String>().toMutableList()
        if (this::location.isInitialized) {
            words.add("${(location.speed * 3.6).roundToInt()} km/h")
        }
        if (street.isNotEmpty()) words.add(street)
        if (rotateDrawing) {
            canvas.rotate(90F, bounds.width() / 2F, bounds.height() / 2F)
        }
        val height =
            drawCenteredText(
                words.toTypedArray(),
                bounds.width() / 2F,
                bounds.height() / 2F,
                if (rotateDrawing) bounds.height() else bounds.width(),
                canvas
            )
        onSizeChanged(height)
    }

    private fun drawCenteredText(
        words: Array<String>,
        centeredX: Float,
        centeredY: Float,
        width: Int,
        canvas: Canvas
    ): Int {
        if (words.isEmpty()) {
            return 1
        }
        val baselineOffset = 15
        val neededHeight = emptyList<Int>().toMutableList()
        val selectedSizes = emptyList<Float>().toMutableList()
        val textBounds = Rect()
        for (word in words) {
            for (size in textSizes) {
                paint.textSize = size
                paint.getTextBounds(word, 0, word.length, textBounds)
                if (width > textBounds.width()) {
                    break
                }
            }
            neededHeight.add(textBounds.height())
            selectedSizes.add(paint.textSize)
        }
        var y = -baselineOffset + centeredY - neededHeight.sum() / 2F
        for (i in words.indices) {
            y += neededHeight[i]
            paint.textSize = selectedSizes[i]
            canvas.drawText(
                words[i],
                centeredX,
                y,
                paint
            )
        }
        return neededHeight.sum() + 2 * baselineOffset
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
