package com.sunspot.progress

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sunspot.base.R
import com.google.android.material.tabs.TabLayout

/**
 * 展示 Android Framework ProgressBar 和 Material CircularProgressIndicator。
 */
class ProgressBarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progress_activity)

        val tabs = findViewById<TabLayout>(R.id.progress_tabs)
        tabs.addTab(tabs.newTab().setText(R.string.progress_tab_framework))
        tabs.addTab(tabs.newTab().setText(R.string.progress_tab_circular))
        tabs.addTab(tabs.newTab().setText(R.string.progress_tab_rotating))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showFragment(createFragment(tab.position))
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit

            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        if (savedInstanceState == null) {
            showFragment(FrameworkProgressBarFragment())
        } else {
            val currentFragment =
                supportFragmentManager.findFragmentById(R.id.progress_fragment_container)
            val selectedPosition = when (currentFragment) {
                is CircularProgressIndicatorFragment -> 1
                is RotatingProgressIndicatorFragment -> 2
                else -> 0
            }
            tabs.selectTab(tabs.getTabAt(selectedPosition))
        }
    }

    private fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> CircularProgressIndicatorFragment()
            2 -> RotatingProgressIndicatorFragment()
            else -> FrameworkProgressBarFragment()
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.progress_fragment_container, fragment)
            .commit()
    }
}
