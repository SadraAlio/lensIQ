package com.example.lensiq

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class ViewfinderOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bracketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(4f)
        strokeCap = Paint.Cap.ROUND
    }

    private val scannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4ade80")
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(2f)
    }

    private var scanLineProgress = 0f
    private var scanLineAnimator: ValueAnimator? = null
    private var bracketAlphaAnimator: ValueAnimator? = null

    private val bracketLength = dpToPx(48f)
    private val boxSize = dpToPx(220f)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        
        scanLineAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                scanLineProgress = animator.animatedValue as Float
                invalidate()
            }
            start()
        }

        bracketAlphaAnimator = ValueAnimator.ofInt(180, 255).apply {
            duration = 1200
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animator ->
                bracketPaint.alpha = animator.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scanLineAnimator?.cancel()
        bracketAlphaAnimator?.cancel()
        scanLineAnimator = null
        bracketAlphaAnimator = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val left = centerX - boxSize / 2f
        val top = centerY - boxSize / 2f
        val right = centerX + boxSize / 2f
        val bottom = centerY + boxSize / 2f

        // Top-left bracket
        canvas.drawLine(left, top, left + bracketLength, top, bracketPaint)
        canvas.drawLine(left, top, left, top + bracketLength, bracketPaint)

        // Top-right bracket
        canvas.drawLine(right, top, right - bracketLength, top, bracketPaint)
        canvas.drawLine(right, top, right, top + bracketLength, bracketPaint)

        // Bottom-left bracket
        canvas.drawLine(left, bottom, left + bracketLength, bottom, bracketPaint)
        canvas.drawLine(left, bottom, left, bottom - bracketLength, bracketPaint)

        // Bottom-right bracket
        canvas.drawLine(right, bottom, right - bracketLength, bottom, bracketPaint)
        canvas.drawLine(right, bottom, right, bottom - bracketLength, bracketPaint)

        // Animated scanner line
        val lineY = top + (boxSize * scanLineProgress)
        val alpha = (0.4f + (1.0f - 0.4f) * (1.0f - Math.abs(0.5f - scanLineProgress) * 2f))
        scannerPaint.alpha = (alpha * 255).toInt()
        
        canvas.drawLine(left + dpToPx(8f), lineY, right - dpToPx(8f), lineY, scannerPaint)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}