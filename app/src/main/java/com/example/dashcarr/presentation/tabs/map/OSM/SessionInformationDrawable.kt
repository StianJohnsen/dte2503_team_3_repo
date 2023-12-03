package com.example.dashcarr.presentation.tabs.map.OSM

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.example.dashcarr.R
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * This class holds and draws live data for the current drive on a canvas.
 *
 * @property context this context will be used for accessing the selected unit system
 * @property rotateDrawing If the drawing should be rotated by 90 degree to allow portrait mode
 * @property onSizeChanged Use this to adapt the canvas size to needed height
 * @constructor Creates a new empty drawable
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
    private val speedLimitPaint: Paint = Paint()
    private var street: String = ""
    private var lastSpeedUpdate = LocalDateTime.now().minusHours(1)
    private var currentSpeed: Float = 0F
    private var speedLimit: Int? = 49

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

        speedLimitPaint.textSize = 220F
        speedLimitPaint.color = Color.BLACK
        speedLimitPaint.isAntiAlias = true
        speedLimitPaint.style = Paint.Style.FILL
        speedLimitPaint.isFakeBoldText = true
        speedLimitPaint.textAlign = Paint.Align.CENTER

        val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        displayInMph = sharedPref.getBoolean("DisplayInMph", false)

        OSMFetcher.getInstance().addMapPositionChangedListener(object : MapPositionChangedListener {
            override fun onStreetChangedListener(streetName: String) {
                street = streetName
                invalidateSelf()
            }

            override fun onSpeedChanged(speed: Float) {
                this@SessionInformationDrawable.currentSpeed = speed
                lastSpeedUpdate = LocalDateTime.now()
                invalidateSelf()
            }

            override fun onSpeedLimitChanged(speedLimit: Int) {
                this@SessionInformationDrawable.speedLimit = speedLimit
            }
        })
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
            currentSpeed = 0F
        val speedText = if (displayInMph) {
            "${(currentSpeed * 2.237).roundToInt()} mph"
        } else {
            "${(currentSpeed * 3.6).roundToInt()} km/h"
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
        if (rotateDrawing && speedLimit != null) {
            val speedLimitText: String
            val trafficSign: Drawable
            val yOffset: Int
            if (displayInMph) {
                speedLimitText = "${(speedLimit!! * 2.237).roundToInt()} mph"
                trafficSign = AppCompatResources.getDrawable(context, R.drawable.maximum_speed_usa)!!
                yOffset = 170
            } else {
                speedLimitText = "${(speedLimit!! * 3.6).roundToInt()} km/h"
                trafficSign = AppCompatResources.getDrawable(context, R.drawable.maximum_speed_europe)!!
                yOffset = 80
            }

            val x = (bounds.width() - bounds.height()) / 2 + 50
            val y = (bounds.height() - bounds.width()) / 2 + 50
            val bounds = Rect(x, y, x + trafficSign.intrinsicWidth, y + trafficSign.intrinsicHeight)
            trafficSign.bounds = bounds
            trafficSign.draw(canvas)
            canvas.drawText(
                speedLimitText,
                0,
                2,
                bounds.exactCenterX(),
                bounds.exactCenterY() + yOffset,
                speedLimitPaint
            )

        }
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
