package com.sunspot.collect.fragl

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import com.google.android.material.tabs.TabLayout
import com.sunspot.base.BaseActivity
import com.sunspot.base.BaseFragment
import com.sunspot.collect.R
import com.sunspot.collect.databinding.FraglActivityLayoutBinding
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
class FragShowHideActivity : BaseActivity<FraglActivityLayoutBinding>() {

    //首先不能这样实例化fragment，activity重建时,FragmentManager 会重建一份fragmentList，这里声明的list又会重建一份，会造成生命周期混乱
//    private val fragments = listOf(FirstFragment(), SecondFragment())

    private var currentIndex: Int = -1
    private var pendingTabSwitchIndex = -1

    //tag 不要用类名 用静态常量更稳定
    private val tags = listOf(FRAGMENT_TAG_FIRST, FRAGMENT_TAG_SECOND)

    override fun createViewBinding(layoutInflater: LayoutInflater): FraglActivityLayoutBinding =
        FraglActivityLayoutBinding.inflate(layoutInflater)

    override fun initView(binding: FraglActivityLayoutBinding) {
        binding.tabLayout.also {
            it.addTab(it.newTab().setText("First"), true)
            it.addTab(it.newTab().setText("Second"), false)
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
    }

    override fun initData(binding: FraglActivityLayoutBinding, savedInstanceState: Bundle?) {
        //这里如果直接写0 activity重建时，fm恢复的fragment，second没隐藏，但check 0后first显示出来了，会出现两个fragment重叠的问题
        //重建时 先从savedInstanceState中取出上次选中的tab
        val restoreTabIndex = savedInstanceState?.getInt(SAVED_TAB_INDEX, -1) ?: -1
        applyTabSwitch(max(restoreTabIndex, 0))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SAVED_TAB_INDEX, currentIndex)
        super.onSaveInstanceState(outState)
        DLog.logCommon(TAG, "restore tab Index $currentIndex")
    }

    private fun createFragment(tag: String): BaseFragment<*> {
        return when (tag) {
            FRAGMENT_TAG_FIRST -> FirstFragment()
            FRAGMENT_TAG_SECOND -> SecondFragment()
            else -> {
                DLog.logCommon(TAG, "Invalid fragment tag: $tag")
                SecondFragment()
            }
        }
    }

    /**
     * 只能在主线程调用 Fragment 事务只能在主线程操作
     * 不要在网络接口回来可能的后台 非state可用状态调用
     */
    private fun selectFragment(index: Int) {
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
                    add(R.id.frag_container, next, nextTag)
                }
                setMaxLifecycle(next, Lifecycle.State.RESUMED)
            }.commit()
        currentIndex = index
        //不要因为接口回来切换tab，就用这种形式，tab选择不适合这种形式，这个给dialogFragment那种一次性UI使用的;如果允许状态丢失，用户选择了第二页，但 Activity 重建时可能又回到第一页，UI 状态和业务状态可能不一致;
        //ft.commitAllowingStateLoss()
    }

    private fun applyTabSwitch(index: Int) {
        selectFragment(index)
        binding.tabLayout.getTabAt(index)?.let {
            if (!it.isSelected) {
                it.select()
            }
        }
    }

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
        const val FRAGMENT_TAG_FIRST = "FirstFragment"
        const val FRAGMENT_TAG_SECOND = "SecondFragment"
        const val SAVED_TAB_INDEX = "saved_tab_index"
    }


//    这段代码 逻辑不对，核心在findFragmentById 通过containerId获取的currentFragment有缓存，后续只会返回最后放进去的一个fragment
//    private fun selectFragment(position: Int) {
//        val ft = supportFragmentManager.beginTransaction()
//        val currentFragment = supportFragmentManager.findFragmentById(R.id.frag_container)
//        Log.e("TAG", "currentFragment: ${currentFragment?.javaClass?.simpleName}")
//        if (currentFragment != null) {
//            //1）hide 时会走onHiddenChanged方法，fragment状态仍然是Resumed，不走onPause，所以一些释放资源，暂停动画都要在onHiddenChanged做
//            ft.hide(currentFragment)
//                //2）⚠️fragment 状态 Lifecycle.State.DESTROYED → INITIALIZED → CREATED → STARTED → RESUMED(前台fragment)⚠️
//                //如果加上这个，让fragment从RESUMED 变成STARTED状态，相当于回退生命周期状态，类似于熄屏操作，会走onPause，同时保留了页面元素。
//                .setMaxLifecycle(currentFragment, Lifecycle.State.STARTED)
//        }
//        if (tags[position].isAdded) {
//            //3）show/hide 的目的就是保留view，不重走onCreate 重新new fragment。换成用replace就会重走onCreate，每次重新new fragment。
//            ft.show(tags[position])
//                //设置fragment生命周期状态为RESUMED，会走onResume
//                .setMaxLifecycle(tags[position], Lifecycle.State.RESUMED)
//        } else {
//            ft.add(R.id.frag_container, tags[position])
//        }
//        //4）这样就可以在show/hide时在onPause/onResume里统一管理生命周期相关资源;同时也会走onHiddenChanged方法。但onPause里，熄屏这种操作也会走，所以会统一管理 类似动画暂停的操作。
//        ft.commitAllowingStateLoss()
//    }
}