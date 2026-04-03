package com.example.lensiq

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar

fun View.fadeInWithDelay(delayMs: Long) {
    this.alpha = 0f
    this.animate()
        .alpha(1f)
        .setDuration(300)
        .setStartDelay(delayMs)
        .start()
}

fun ProgressBar.animateTo(target: Int, duration: Long) {
    val animator = ValueAnimator.ofInt(0, target)
    animator.duration = duration
    animator.interpolator = AccelerateDecelerateInterpolator()
    animator.addUpdateListener { animation ->
        this.progress = animation.animatedValue as Int
    }
    animator.start()
}