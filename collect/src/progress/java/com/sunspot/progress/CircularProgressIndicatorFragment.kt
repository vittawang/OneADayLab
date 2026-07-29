package com.sunspot.progress

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.sunspot.collect.R

/**
 * 展示 Material CircularProgressIndicator 的常见配置和动态进度用法。
 */
class CircularProgressIndicatorFragment : Fragment(R.layout.progress_fragment_circular) {

    private var nextProgress = 75

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val indicator =
            view.findViewById<CircularProgressIndicator>(R.id.progress_circular_demo)
        view.findViewById<Button>(R.id.progress_btn_animate).setOnClickListener {
            //⚠️设置进度 从25 → 75 是有动画效果的
            indicator.setProgressCompat(nextProgress, true)
            nextProgress = if (nextProgress == 75) 25 else 75
        }
    }
}
