package com.sunspot.collect.fragl

import android.view.LayoutInflater
import com.google.android.material.tabs.TabLayout
import com.sunspot.base.BaseFragment
import com.sunspot.collect.R
import com.sunspot.collect.databinding.CoroutineActivityLayoutBinding

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2026/7/29 17:05
 * -------------------------------------
 * 描述：
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */
class CoroutineActivity : BaseTabFragmentActivity<CoroutineActivityLayoutBinding>() {
    override fun createViewBinding(layoutInflater: LayoutInflater): CoroutineActivityLayoutBinding =
        CoroutineActivityLayoutBinding.inflate(layoutInflater)

    override fun initView(binding: CoroutineActivityLayoutBinding) {
        binding.tabLayout.let {
            it.addTab(it.newTab().setText(tags[0]), true)
            it.addTab(it.newTab().setText(tags[1]), false)
            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    updateFragmentSelectUI(tab?.position ?: 0)
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }

                override fun onTabReselected(p0: TabLayout.Tab?) {
                }
            })
        }
    }

    override fun createFragmentTags(): List<String> = listOf("Coroutine", "JavaThread")

    override fun createFragment(tag: String): BaseFragment<*> {
        return when (tag) {
            "Coroutine" -> CoroutineFragment()
            "JavaThread" -> JavaThreadFragment()
            else -> CoroutineFragment()
        }
    }

    override fun fragmentContainerId(): Int = R.id.frag_container

    override fun updateTabSelectUI(index: Int) {
        binding.tabLayout.getTabAt(index)?.let {
            if (!it.isSelected) {
                it.select()
            }
        }
    }
}