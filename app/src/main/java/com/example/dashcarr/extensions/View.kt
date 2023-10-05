package com.example.dashcarr.extensions

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator

private const val DEFAULT_WIDTH_SMOOTH_ANIMATION_DURATION: Long = 100
fun View.setWidthSmooth(duration: Long = DEFAULT_WIDTH_SMOOTH_ANIMATION_DURATION, newWidth:Int){
    val widthAnimator = ValueAnimator.ofInt(this.width, newWidth)
    widthAnimator.duration = duration
    widthAnimator.interpolator = DecelerateInterpolator()
    widthAnimator.addUpdateListener { animation ->
        this.layoutParams.width = animation.animatedValue as Int
        this.requestLayout()
    }
    widthAnimator.start()
}