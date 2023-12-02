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
import org.json.JSONException
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * This class holds and draws live data for the current drive on a canvas.
 *
 * @property context this context will be used for sending API Requests in [updateStreet]
 * @property rotateDrawing If the drawing should be rotated by 90 degree to allow portrait mode
 * @property onSizeChanged Use this to adapt the canvas size to needed height
 * @constructor Creates a new empty drawable that ready to get information through [updateLocation].
 * @param speedTextSize the text size of the speed in px
 * @param streetTextSize the text size of the additional information in px
 */
class SessionInformationDrawable(
    private val context: Context,
    speedTextSize: Float,
    streetTextSize: Float,
    private val rotateDrawing: Boolean,
    private var onSizeChanged: (Int) -> Unit
) : Drawable() {
    private val smallPaint: Paint = Paint()
    private val bigPaint: Paint = Paint()
    private var street: String = ""
    private var location: Location? = null
    private var lastStreetUpdate = LocalDateTime.now().minusHours(1)
    private var lastSpeedUpdate = LocalDateTime.now().minusHours(1)
    private var speed: Float = 0F

    private var displayInMph = false

    init {
        smallPaint.textSize = streetTextSize
        smallPaint.color = Color.WHITE
        smallPaint.isAntiAlias = true
        smallPaint.style = Paint.Style.FILL
        smallPaint.isFakeBoldText = true
        smallPaint.textAlign = Paint.Align.CENTER

        bigPaint.textSize = speedTextSize
        bigPaint.color = Color.WHITE
        bigPaint.isAntiAlias = true
        bigPaint.style = Paint.Style.FILL
        bigPaint.isFakeBoldText = true
        bigPaint.textAlign = Paint.Align.CENTER

        val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        displayInMph = sharedPref.getBoolean("DisplayInMph", false)
    }

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
        lastSpeedUpdate = LocalDateTime.now()
        this.invalidateSelf()
    }

    /**
     * Sends a new API Request to the nominatim server, to resolve the provided coordinates into street names.
     * The nomination guidelines don't allow more than one request per second, otherwise your IP might get banned.
     *
     * @param location location with GPS coordinates
     */
    private fun updateStreet(location: Location) {
        val addressUrl =
            "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${location.latitude}&lon=${location.longitude}&layer=address"
        val queue = Volley.newRequestQueue(context)
        val addressRequest = JsonObjectRequest(
            Request.Method.GET, addressUrl, null,
            { response ->
                Log.d(this::class.simpleName, "OSM Request successful! Content: $response")
                if (response.getString("osm_type") == "way") {
                    val wayId = response.get("osm_id")
                    val speedLimitUrl = "https://overpass-api.de/api/interpreter?data=[out:json];way($wayId);out;"
                    val request = JsonObjectRequest(Request.Method.GET, speedLimitUrl, null,
                        { addressResponse ->
                            try {
                                val node = addressResponse.getJSONArray("elements").get(0) as JSONObject
                                val speedLimit = node.getJSONObject("tags").getInt("maxspeed")
                                Log.d(this::class.simpleName, "max speed: $speedLimit")
                            } catch (e: JSONException) {
                                Log.d(
                                    this::class.simpleName,
                                    "No Speed limit available for way id: $wayId, exception: ${e.stackTraceToString()}"
                                )
                            }
                        },
                        { addressError ->
                            Log.e(this::class.simpleName, "Can't access Url: $speedLimitUrl with error $addressError")
                        }
                    )
                    queue.add(request)
                }
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
                Log.e(this::class.simpleName, "Can't access Url: $addressUrl with error $error")
                this.location = null
                street = ""
            }
        )
        queue.add(addressRequest)
    }


    fun toggleSpeedUnit() {
        displayInMph = !displayInMph

        // Save preference in SharedPreferences
        val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("DisplayInMph", displayInMph)
            apply()
        }

        invalidateSelf()
    }


    override fun draw(canvas: Canvas) {
        if (Duration.between(lastSpeedUpdate, LocalDateTime.now()).get(ChronoUnit.SECONDS) > 5)
            speed = 0F
        val speedText = if (displayInMph) {
            "${(speed * 2.237).roundToInt()} mph"
        } else {
            "${(speed * 3.6).roundToInt()} km/h"
        }

        if (rotateDrawing) {
            canvas.rotate(90F, bounds.width() / 2F, bounds.height() / 2F)
        }

        val padding = 4
        val height =
            drawCenteredText(
                speedText,
                street,
                bounds.width() / 2F,
                bounds.height() / 2F,
                (if (rotateDrawing) bounds.height() else bounds.width()) - padding,
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
        bigText: String,
        smallText: String,
        centeredX: Float,
        centeredY: Float,
        width: Int,
        canvas: Canvas
    ): Int {
        // calculate text heights for centering
        val bigTextBounds = Rect()
        bigPaint.getTextBounds(bigText, 0, bigText.length, bigTextBounds)

        val smallTextBounds = Rect()
        var shortenedSmallText: String
        smallPaint.getTextBounds(smallText, 0, smallText.length, smallTextBounds)
        if (smallTextBounds.width() > width) {
            var cutOff = smallText.length
            do {
                cutOff--
                shortenedSmallText = smallText.substring(0, cutOff) + "…"
                smallPaint.getTextBounds(shortenedSmallText, 0, shortenedSmallText.length, smallTextBounds)
            } while (smallTextBounds.width() > width)
        } else {
            shortenedSmallText = smallText
        }

        // draw to calculated position
        val baselineOffset = 15
        var additionalOffset = 0
        if (displayInMph) {
            additionalOffset = 15
        }

        var y =
            -baselineOffset + centeredY - (smallTextBounds.height() + bigTextBounds.height()) / 2F + bigTextBounds.height()
        canvas.drawText(
            bigText,
            centeredX,
            y,
            bigPaint
        )
        y += smallTextBounds.height() + additionalOffset
        canvas.drawText(
            shortenedSmallText,
            centeredX,
            y,
            smallPaint
        )
        return bigTextBounds.height() + smallTextBounds.height() + 2 * baselineOffset
    }

    override fun setAlpha(alpha: Int) {
        smallPaint.alpha = alpha
        bigPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        smallPaint.colorFilter = cf
        bigPaint.colorFilter = cf
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

}
