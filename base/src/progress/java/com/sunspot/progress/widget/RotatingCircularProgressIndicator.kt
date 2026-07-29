package com.sunspot.progress.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.sunspot.base.R
import androidx.core.view.isVisible

/**
 * 固定长度圆弧无限旋转的 CircularProgressIndicator。
 *
 * View 自己管理动画生命周期，添加到窗口后可自动开始，移除时自动释放 Animator。
 */
class RotatingCircularProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.circularProgressIndicatorStyle
) : CircularProgressIndicator(context, attrs, defStyleAttr) {

    private var rotationAnimator: ObjectAnimator? = null
    private var rotationDuration = DEFAULT_ROTATION_DURATION
    private var rotationDirection = DIRECTION_CLOCKWISE
    private var autoStart = true

    init {
        isIndeterminate = false

        context.obtainStyledAttributes(
            attrs,
            R.styleable.RotatingCircularProgressIndicator,
            defStyleAttr,
            0
        ).apply {
            try {
                if (hasValue(R.styleable.RotatingCircularProgressIndicator_progressRotatingIndicatorColor)) {
                    setIndicatorColor(
                        getColor(
                            R.styleable.RotatingCircularProgressIndicator_progressRotatingIndicatorColor,
                            0
                        )
                    )
                }
                if (hasValue(R.styleable.RotatingCircularProgressIndicator_progressRotatingTrackColor)) {
                    trackColor = getColor(
                        R.styleable.RotatingCircularProgressIndicator_progressRotatingTrackColor,
                        trackColor
                    )
                }
                if (hasValue(R.styleable.RotatingCircularProgressIndicator_progressRotatingIndicatorSize)) {
                    indicatorSize = getDimensionPixelSize(
                        R.styleable.RotatingCircularProgressIndicator_progressRotatingIndicatorSize,
                        indicatorSize
                    )
                }
                if (hasValue(R.styleable.RotatingCircularProgressIndicator_progressRotatingTrackThickness)) {
                    trackThickness = getDimensionPixelSize(
                        R.styleable.RotatingCircularProgressIndicator_progressRotatingTrackThickness,
                        trackThickness
                    )
                }
                if (hasValue(R.styleable.RotatingCircularProgressIndicator_progressRotatingTrackCornerRadius)) {
                    trackCornerRadius = getDimensionPixelSize(
                        R.styleable.RotatingCircularProgressIndicator_progressRotatingTrackCornerRadius,
                        trackCornerRadius
                    )
                }
                if (hasValue(R.styleable.RotatingCircularProgressIndicator_progressRotatingGapSize)) {
                    indicatorTrackGapSize = getDimensionPixelSize(
                        R.styleable.RotatingCircularProgressIndicator_progressRotatingGapSize,
                        indicatorTrackGapSize
                    )
                }

                setProgressCompat(
                    getInt(
                        R.styleable.RotatingCircularProgressIndicator_progressRotatingValue,
                        DEFAULT_PROGRESS
                    ).coerceIn(0, 100),
                    false
                )
                rotationDuration = getInt(
                    R.styleable.RotatingCircularProgressIndicator_progressRotatingDuration,
                    DEFAULT_ROTATION_DURATION.toInt()
                ).coerceAtLeast(MIN_ROTATION_DURATION).toLong()
                rotationDirection = getInt(
                    R.styleable.RotatingCircularProgressIndicator_progressRotatingDirection,
                    DIRECTION_CLOCKWISE
                )
                autoStart = getBoolean(
                    R.styleable.RotatingCircularProgressIndicator_progressRotatingAutoStart,
                    true
                )
            } finally {
                recycle()
            }
        }
    }

    /**
     * 从当前位置开始匀速无限旋转。重复调用不会创建多个 Animator。
     */
    fun startRotation() {
        if (rotationAnimator?.isRunning == true) return

        val sweep = if (rotationDirection == DIRECTION_COUNTERCLOCKWISE) -360f else 360f
        rotationAnimator = ObjectAnimator.ofFloat(
            this,
            View.ROTATION,
            rotation,
            rotation + sweep
        ).apply {
            duration = rotationDuration
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            start()
        }
    }

    /**
     * 停止旋转并保留当前角度，再次启动时从当前位置继续。
     */
    fun stopRotation() {
        rotationAnimator?.cancel()
        rotationAnimator = null
    }

    fun isRotationRunning(): Boolean = rotationAnimator?.isRunning == true

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (autoStart && isVisible) {
            startRotation()
        }
    }

    override fun onDetachedFromWindow() {
        stopRotation()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (!isAttachedToWindow) return

        if (visibility == VISIBLE && autoStart) {
            startRotation()
        } else {
            stopRotation()
        }
    }

    private companion object {
        const val DEFAULT_PROGRESS = 25
        const val DEFAULT_ROTATION_DURATION = 1_000L
        const val MIN_ROTATION_DURATION = 1
        const val DIRECTION_CLOCKWISE = 0
        const val DIRECTION_COUNTERCLOCKWISE = 1
    }
}
