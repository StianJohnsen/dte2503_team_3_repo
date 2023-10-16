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
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * This class holds and draws live data for the current drive on a canvas.
 *
 * @property context this context will be used for sending API Requests in [updateStreet]
 * @property textSizes The available text sizes for drawing. The first one is the most preferred size. This array should be ordered descending.
 * @property rotateDrawing If the drawing should be rotated by 90 degree to allow portrait mode
 * @property onSizeChanged Use this to adapt the canvas size to needed height
 */
class SessionInformationDrawable(
    private val context: Context,
    private var textSizes: Array<Float>,
    private val rotateDrawing: Boolean,
    private var onSizeChanged: (Int) -> Unit
) : Drawable() {
    private val paint: Paint = Paint()
    private var street: String = ""
    private var location: Location? = null
    private var lastStreetUpdate = LocalDateTime.now().minusHours(1)
    private var speed: Float? = null

    /**
     * This function refreshes the speed and might trigger a request to refresh the current street.
     *
     * @param location the new GPS Location
     */
    fun updateLocation(location: Location) {
        if (Duration.between(lastStreetUpdate, LocalDateTime.now())
                .get(ChronoUnit.SECONDS) > 2 && (this.location == null || this.location!!.distanceTo(location) > 100)
        ) {
            lastStreetUpdate = LocalDateTime.now()
            updateStreet(location)
        }
        this.speed = location.speed
        this.invalidateSelf()
    }

    /**
     * Sends a new API Request to the nominatim server, to resolve the provided coordinates into street names.
     * The nomination guidelines don't allow more than one request per second, otherwise your IP might get banned.
     *
     * @param location location with GPS coordinates
     */
    private fun updateStreet(location: Location) {
        val url =
            "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${location.latitude}&lon=${location.longitude}&layer=address"
        val queue = Volley.newRequestQueue(context)
        val stringRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                Log.d("HudView", "OSM Request successful! Content: $response")
                val address = response.getJSONObject("address")
                var road = ""
                if (address.has("road")) {
                    road = address.get("road").toString()
                }
                val town = when {
                    address.has("town") -> address.get("town")
                    address.has("municipality") -> address.get("municipality")
                    address.has("city") -> address.get("city")
                    address.has("region") -> address.get("region")
                    address.has("neighbourhood") -> address.get("neighbourhood")
                    address.has("farm") -> address.get("farm")
                    address.has("county") -> address.get("county")
                    else -> ""
                }.toString()
                street = if (road.isEmpty() || town.isEmpty()) {
                    town + road
                } else {
                    "${road}, $town"
                }
                this.location = location
                this.invalidateSelf()
            },
            { error ->
                Log.e("HUD View", "Can't access Url: $url with error $error")
                this.location = null
                street = ""
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
        if (this.speed != null) {
            words.add("${(speed!! * 3.6).roundToInt()} km/h")
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

    /**
     * Draws multiple lines of text on a canvas.
     *
     * @param words All strings that you want to draw, one element per line
     * @param centeredX The x coordinate of the center of all lines
     * @param centeredY The y coordinate of the center of all lines
     * @param width The available width of each line in pixel. If the line is to long the text size will be decreased
     * @param canvas The canvas where the operations are applied
     * @return The height of the whole text block in pixels
     */
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
