package com.sunspot.collect.fragl

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.sunspot.base.BaseActivity
import com.sunspot.base.BaseFragment
import com.sunspot.log.DLog
import kotlin.math.max

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2026/7/29 17:05
 * -------------------------------------
 * 描述：Fragment add + show/hide 配合TabLayout实现切换
 * -------------------------------------
 * 备注：
 * 1.页面数量变多或页面很重时，不再使用全部常驻的 show/hide
 * 2.性能特点切换速度快、不销毁view页面全在内存中（内存评估）
 * 3.切换到哪个页面哪个才会加载（延迟初始化）
 * -------------------------------------
 */
abstract class BaseTabFragmentActivity<V : ViewBinding> : BaseActivity<V>() {

    protected var currentIndex: Int = -1
    private var pendingTabSwitchIndex = -1

    //tag 不要用类名 用静态常量更稳定
    protected val tags: List<String> = createFragmentTags()

    override fun initData(binding: V, savedInstanceState: Bundle?) {
        val restoreTabIndex = savedInstanceState?.getInt(SAVED_TAB_INDEX, -1) ?: -1
        applyTabSwitch(max(restoreTabIndex, 0))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SAVED_TAB_INDEX, currentIndex)
        super.onSaveInstanceState(outState)
        DLog.logCommon(TAG, "restore tab Index $currentIndex")
    }

    /**
     * 只能在主线程调用 Fragment 事务只能在主线程操作
     * 不要在网络接口回来可能的后台 非state可用状态调用
     */
    protected fun updateFragmentSelectUI(index: Int) {
        if (currentIndex == index || index !in tags.indices) return
        //上一个
        val currentTag = tags.getOrNull(currentIndex)
        val last = supportFragmentManager.findFragmentByTag(currentTag)
        //下一个
        val nextTag = tags[index]
        val next = supportFragmentManager.findFragmentByTag(nextTag) ?: createFragment(nextTag)
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)//优化当前事务内部的操作顺序和生命周期变化，不会自动合并多次
            .apply {
                //隐藏上一个fragment
                last?.let {
                    hide(it).setMaxLifecycle(it, Lifecycle.State.STARTED)
                }
                //展示下一个fragment
                if (next.isAdded) {
                    show(next)
                } else {
                    add(fragmentContainerId(), next, nextTag)
                }
                setMaxLifecycle(next, Lifecycle.State.RESUMED)
            }.commit()
        currentIndex = index
    }

    private fun applyTabSwitch(index: Int) {
        updateFragmentSelectUI(index)
        updateTabSelectUI(index)
    }


    abstract fun createFragmentTags(): List<String>
    abstract fun createFragment(tag: String): BaseFragment<*>

    @IdRes
    abstract fun fragmentContainerId(): Int

    abstract fun updateTabSelectUI(index: Int)


    /**
     * 可以在后台 isStateSaved=true的状态下使用，已经配合onResumeFragments做了兜底切换
     * 可以在任意线程调用
     */
    fun switchTab(index: Int) {
        if (index !in tags.indices) return
        runOnUiThread {
            if (isFinishing || isDestroyed) return@runOnUiThread
            if (supportFragmentManager.isStateSaved) {
                //Activity 退到后台、旋转或正在保存状态时，暂不提交事务
                pendingTabSwitchIndex = index
                return@runOnUiThread
            }
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                //页面状态未达到Resume 也不处理，页面还不可见，不能操作commit事务
                pendingTabSwitchIndex = index
                return@runOnUiThread
            }
            applyTabSwitch(index)
        }
    }


    override fun onResumeFragments() {
        super.onResumeFragments()
        //FragmentActivity 专门提供的安全时机。执行到这里时，FragmentManager 已经退出“状态已保存”阶段，可以正常提交事务
        //解决IllegalStateException: Can not perform this action after onSaveInstanceState
        if (pendingTabSwitchIndex >= 0) {
            applyTabSwitch(pendingTabSwitchIndex)
            pendingTabSwitchIndex = -1
        }
    }

    private companion object {
        const val TAG = "FragShowHideActivity"
        const val SAVED_TAB_INDEX = "saved_tab_index"
    }

    //    override fun initView(binding: FraglActivityLayoutBinding) {
//        binding.tabLayout.also {
//            it.addTab(it.newTab().setText("First"), true)
//            it.addTab(it.newTab().setText("Second"), false)
//            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//                override fun onTabSelected(tab: TabLayout.Tab?) {
//                    updateFragmentSelectUI(tab?.position ?: 0)
//                }
//
//                override fun onTabUnselected(tab: TabLayout.Tab?) {
//                }
//
//                override fun onTabReselected(tab: TabLayout.Tab?) {
//                }
//
//            })
//        }
//    }

//    override fun updateTabSelectUI(index: Int) {
//        binding.tabLayout.getTabAt(index)?.let {
//            if (!it.isSelected) {
//                it.select()
//            }
//        }
//    }

}