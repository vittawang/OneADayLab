package com.sunspot.collect.fragl

import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import com.google.android.material.tabs.TabLayout
import com.sunspot.base.BaseActivity
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
class CoroutineActivity : BaseActivity<CoroutineActivityLayoutBinding>() {
    override fun createViewBinding(layoutInflater: LayoutInflater): CoroutineActivityLayoutBinding =
        CoroutineActivityLayoutBinding.inflate(layoutInflater)

    override fun initView(binding: CoroutineActivityLayoutBinding) {
        binding.tabLayout.also {
            it.addTab(it.newTab().setText("Coroutine"), true)
            it.addTab(it.newTab().setText("Java Thread"), false)
            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    selectFragment(tab?.position ?: 0)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })
        }
        selectFragment(0)
    }

    private val fragments = listOf(FirstFragment(), SecondFragment())

    private fun selectFragment(position: Int) {
        val ft = supportFragmentManager.beginTransaction()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frag_container)
        if (currentFragment != null) {
            //hide 时会走onHiddenChanged方法，fragment状态仍然是Resumed，不走onPause，所以一些释放资源，暂停动画都要在onHiddenChanged做
            ft.hide(currentFragment)
                //⚠️fragment 状态 Lifecycle.State.DESTROYED → INITIALIZED → CREATED → STARTED → RESUMED(前台fragment)⚠️
                //如果加上这个，让fragment从RESUMED 变成STARTED状态，相当于回退生命周期状态，类似于熄屏操作，会走onPause，同时保留了页面元素。
                .setMaxLifecycle(currentFragment, Lifecycle.State.STARTED)
        }
        if (fragments[position].isAdded) {
            //show/hide 的目的就是保留view，不重走onCreate 重新new fragment。换成用replace就会重走onCreate，每次重新new fragment。
            ft.show(fragments[position])
                //设置fragment生命周期状态为RESUMED，会走onResume
                .setMaxLifecycle(fragments[position], Lifecycle.State.RESUMED)
        } else {
            ft.add(R.id.frag_container, fragments[position])
        }
        //这样就可以在show/hide时在onPause/onResume里统一管理生命周期相关资源;同时也会走onHiddenChanged方法。但onPause里，熄屏这种操作也会走，所以会统一管理 类似动画暂停的操作。
        ft.commitAllowingStateLoss()
    }
}