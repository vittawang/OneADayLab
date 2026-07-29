package com.sunspot.progress

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sunspot.collect.R

/**
 * 展示 Android Framework 提供的 ProgressBar 样式及常见用法。
 */
class ProgressBarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progress_activity)
    }
}
