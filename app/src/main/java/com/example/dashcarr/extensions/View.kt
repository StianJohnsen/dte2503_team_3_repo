package com.example.dashcarr.extensions

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator

private const val DEFAULT_WIDTH_SMOOTH_ANIMATION_DURATION: Long = 100

fun View.setHeightSmooth(
    duration: Long = DEFAULT_WIDTH_SMOOTH_ANIMATION_DURATION,
    newHeight:Int? = null,
    avoidWrapContent: Boolean = false,
    avoidZero: Boolean = false
) {
    val widthAnimator = ValueAnimator.ofInt(this.height, newHeight?:this.height)
    widthAnimator.duration = duration
    widthAnimator.interpolator = DecelerateInterpolator()
    widthAnimator.addUpdateListener { animation ->
        if (animation.animatedValue as Int == -1 && avoidWrapContent) return@addUpdateListener
        if (animation.animatedValue as Int == 0 && avoidZero) return@addUpdateListener
        this.layoutParams.height = animation.animatedValue as Int
        this.requestLayout()
    }
    widthAnimator.start()
}