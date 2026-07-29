package com.sunspot.progress

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.sunspot.base.R
import com.sunspot.progress.widget.RotatingCircularProgressIndicator

/**
 * 展示固定长度圆弧无限旋转的自定义 View。
 */
class RotatingProgressIndicatorFragment : Fragment(R.layout.progress_fragment_rotating) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val indicators = listOf(
            view.findViewById<RotatingCircularProgressIndicator>(R.id.progress_rotating_green),
            view.findViewById<RotatingCircularProgressIndicator>(R.id.progress_rotating_blue)
        )

        view.findViewById<Button>(R.id.progress_btn_start_rotation).setOnClickListener {
            indicators.forEach(RotatingCircularProgressIndicator::startRotation)
        }
        view.findViewById<Button>(R.id.progress_btn_stop_rotation).setOnClickListener {
            indicators.forEach(RotatingCircularProgressIndicator::stopRotation)
        }
    }
}
