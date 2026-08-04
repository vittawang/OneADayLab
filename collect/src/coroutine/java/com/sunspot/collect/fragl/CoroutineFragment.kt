package com.sunspot.collect.fragl

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.sunspot.base.BaseFragment
import com.sunspot.collect.databinding.CoroutineFragmentCorBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2026/7/29 17:23
 * -------------------------------------
 * 描述：
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */
internal class CoroutineFragment : BaseFragment<CoroutineFragmentCorBinding>() {
    override fun createViewBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): CoroutineFragmentCorBinding = CoroutineFragmentCorBinding.inflate(inflater, container, false)

    override fun initView(binding: CoroutineFragmentCorBinding) {
        binding.corBtnLaunch.setOnClickListener { launchCoroutine() }
        binding.corBtnKillCallback.setOnClickListener { killCallback() }
    }

    override fun initData(binding: CoroutineFragmentCorBinding, savedInstanceState: Bundle?) {

    }

    /**
     * （1）Android经典场景，子线程请求网络，主线程更新UI
     */
    private fun killCallback() {
        //老写法
        requestUserInfo(onGetDataCallback = { userInfo ->
            Log.e(TAG, "Java initData: 调用方取到了网络耗时请求到的数据：$userInfo")
        })
        Log.e(TAG, "------------")

        //协程写法
        lifecycleScope.launch {
            val userInfo = requestUserInfo()
            Log.e(TAG, "KT initData: 调用方直接拿到了return的返回值数据 不用callback $userInfo")
            //            tv.setText(userInfo.name)
        }
    }

    //协程是一段可以原地暂停再原地继续执行的代码，他不是线程
    //线程 是系统创建调度的，数量多了系统吃不消
    //协程就是一个封装类 无数个数量都没事，可以成千上万个协程跑在一个线程上。它暂停是不占用线程，那条线程可以立刻去干别的活。
    private fun launchCoroutine() {
        lifecycleScope.launch {
            Log.e(TAG, "启动一个协程 线程名：${Thread.currentThread().name} ")//main
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    /**
     * 主线程调用 期望抛回主线程 然后更新UI
     */
    private fun requestUserInfo(onGetDataCallback: (DataUserInfo) -> Unit) {
        Thread(Runnable() {
            //模拟网络请求
            Log.e(TAG, "Java requestUserInfo: 网络请求UserInfo")
            Thread.sleep(1000)
            val userInfo = DataUserInfo(name = "vitta", age = 18)
            Log.e(TAG, "Java requestUserInfo: 请求到User信息 $userInfo")
            //🎯抛回主线程
            handler.post {
                Log.e(
                    TAG,
                    "Java requestUserInfo: handler post 到：${Thread.currentThread().name} 再callback 给调用方"
                )//main
                //再回调给调用方 callback 可能有很多层嵌套callback
                onGetDataCallback.invoke(userInfo)
            }
        }).start()
    }

    /**
     * suspend 挂起函数 暂停函数
     * 方法返回值直接是 数据，耗时方法
     * 按道理应该在外面子线程中调用，现在可以声明成suspend 可暂停方法，在协程里调用
     */
    private suspend fun requestUserInfo(): DataUserInfo {
        Log.e(TAG, "KT requestUserInfo: 模拟网络请求，线程名 ${Thread.currentThread().name}") //在主线程！
        delay(10000) //模拟耗时 主线程delay1000 不会ANR吗？不会！- 原因在协程的delay是挂起，相当于给协程封装类加了标记，现在暂停执行方法，5s后原地恢复执行，主线程打完标记立马可以执行别的UI刷新。
//        Thread.sleep(10000)//把这句打开，在10s内点击其他的UI，因为主线程遭阻塞了，点击操作无法在5s内dispatch完毕，就会ANR；10s内不点其他UI，不会ANR，主线程被占死10s，这期间不能刷新UI。刷就会ANR。
        //线程的阻塞状态？广义上讲，线程的Runnable 中run方法不执行了，不占CPU，包括三个线程模型中状态 SLEEP进入TIME_WAITING，锁竞争synchronized进入BLOCKED，wait进入WAITING。不执行runnable意味着代码都不执行了呗。
        Log.e(TAG, "KT requestUserInfo: 网络请求成功，耗时5s")
        return DataUserInfo(name = "vitta", age = 18)
    }


    data class DataUserInfo(val name: String, val age: Int)


    private companion object {
        private const val TAG = "CoroutineFragment"
    }
}