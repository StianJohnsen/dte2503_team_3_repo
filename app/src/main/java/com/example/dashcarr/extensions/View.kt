package com.example.dashcarr.extensions

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

private const val DEFAULT_WIDTH_SMOOTH_ANIMATION_DURATION: Long = 100

/**
 * Smoothly adjusts the height of the view using a ValueAnimator.
 *
 * This extension function animates the height of the view to a specified value or its current height.
 * The animation has a decelerating interpolation for a smooth effect.
 *
 * @param duration The duration of the animation in milliseconds (default is 100 milliseconds).
 * @param newHeight The target height to animate to. If null, the current height is used.
 * @param avoidWrapContent If true, avoids setting the height to -1 (WRAP_CONTENT) during the animation.
 * @param avoidZero If true, avoids setting the height to 0 during the animation.
 */
fun View.setHeightSmooth(
    duration: Long = DEFAULT_WIDTH_SMOOTH_ANIMATION_DURATION,
    newHeight: Int? = null,
    avoidWrapContent: Boolean = false,
    avoidZero: Boolean = false,
    doOnStart: () -> Unit = {},
    doOnEnd: () -> Unit = {},
) {
    val widthAnimator = ValueAnimator.ofInt(this.height, newHeight ?: this.height)
    widthAnimator.duration = duration
    widthAnimator.interpolator = DecelerateInterpolator()
    widthAnimator.addUpdateListener { animation ->
        if (animation.animatedValue as Int == -1 && avoidWrapContent) return@addUpdateListener
        if (animation.animatedValue as Int == 0 && avoidZero) return@addUpdateListener
        this.layoutParams.height = animation.animatedValue as Int
        this.requestLayout()
    }
    widthAnimator.doOnStart { doOnStart() }
    widthAnimator.doOnEnd { doOnEnd() }
    widthAnimator.start()
}