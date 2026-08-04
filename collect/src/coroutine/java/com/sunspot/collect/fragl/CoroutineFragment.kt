package com.sunspot.collect.fragl

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.lifecycle.lifecycleScope
import com.sunspot.base.BaseFragment
import com.sunspot.collect.databinding.CoroutineFragmentCorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayDeque

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

    private val handler = Handler(Looper.getMainLooper())
    override fun createViewBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): CoroutineFragmentCorBinding = CoroutineFragmentCorBinding.inflate(inflater, container, false)

    override fun initView(binding: CoroutineFragmentCorBinding) {
        binding.corBtnLaunch.setOnClickListener { launchCoroutine() }
        binding.corBtnKillCallback.setOnClickListener { killCallback() }
        binding.corBtnClearLog.setOnClickListener { clearLog() }
        binding.corBtnSwitchThread.setOnClickListener { withContextSwitchThread() }
        binding.corBtnUiOnIo.setOnClickListener { touchUiOnIoThread() }
    }

    /**
     * android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
     *                                                       	at android.view.ViewRootImpl.checkThread(ViewRootImpl.java:11177)
     *                                                       	at android.view.ViewRootImpl.requestLayout(ViewRootImpl.java:2482)
     *                                                       	at android.view.View.requestLayout(View.java:27053)
     *                                                       	at android.view.View.requestLayout(View.java:27053)
     *                                                       	at android.view.View.requestLayout(View.java:27053)
     *                                                       	at android.view.View.requestLayout(View.java:27053)
     *                                                       	at android.view.View.requestLayout(View.java:27053)
     */
    private fun touchUiOnIoThread() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                log("IO 线程 设置tv text")
                try {
                    binding.tvUser.text = "touchUiOnIoThread"
                } catch (e: Exception) {
                    log("崩了：${e.javaClass.simpleName} —— ${e.message}")
                }
            }
        }
    }

    /**
     * （2）withContext(IO) 切线程，默认协程跑在主线程上，不能执行Thread.sleep，现在切到子线程，可以放心做耗时操作啦。且能用return 直接拿回耗时结果,不用callback。
     * 例子：真实网络请求，子线程耗时，主线程刷新
     * review
     * 1. 协程里优先用delay而不是sleep。
     *          delay 挂起，线程可以瞬间去干其他协程的活；
     *          sleep阻塞，线程被占死2s，不能去干其他协程的活。
     */
    private fun withContextSwitchThread() {
        lifecycleScope.launch {
            //这里还是main线程
            log("1️⃣协程起点Main，准备切IO")//准备挂起，挂起后协程打标记，withContext(IO) 协程内部分配线程池处理执行。
            val result = withContext(Dispatchers.IO) {//DefaultDispatcher-worker-1 协程内部的默认线程池
                //这里就是子线程了
                log("withContext(IO) 切完成线程后")
                Thread.sleep(2000)
                log("thread sleep 2s，模拟网络请求")
                //注意 这里就是返回值，不用return。最后一行是block块的返回值，最终变成withContext的返回值，即 result=DataUserInfo。比 Handler 回调优雅。
                DataUserInfo("vitta", 2)
            }
            log("跳出withContext(IO)块，自动切回Main，更新UI")//切回线程是自动的，不用操作handler post
            binding.tvUser.text = result.toString()
        }
    }

    //协程是一段可以原地暂停再原地继续执行的代码，他不是线程
    //线程 是系统创建调度的，数量多了系统吃不消
    //协程就是一个封装类 无数个数量都没事，可以成千上万个协程跑在一个线程上。它暂停是不占用线程，那条线程可以立刻去干别的活。
    private fun launchCoroutine() {
        lifecycleScope.launch {
            log("启动一个协程 线程名：${Thread.currentThread().name} ")//main
        }
    }

    // ==================== （1）Android经典场景，子线程请求网络，主线程更新UI ====================
    /**
     * （1）Android经典场景，子线程请求网络，主线程更新UI
     */
    private fun killCallback() {
        log("=========killCallback=========")
        //老写法
        requestUserInfo(onGetDataCallback = { userInfo ->
            log("Java initData: 调用方取到了网络耗时请求到的数据：$userInfo")
        })

        //协程写法
        lifecycleScope.launch {
            delay(1500)//等java执行完再执行kt，日志看的清楚点
            log("------------KT-----------")
            val userInfo = requestUserInfo()
            log("KT initData: 调用方直接拿到了return的返回值数据 不用callback $userInfo")
            //            tv.setText(userInfo.name)
        }
    }

    /**
     * 主线程调用 期望抛回主线程 然后更新UI
     */
    private fun requestUserInfo(onGetDataCallback: (DataUserInfo) -> Unit) {
        Thread(Runnable() {
            //模拟网络请求
            log("Java requestUserInfo: 网络请求UserInfo")
            Thread.sleep(1000)
            val userInfo = DataUserInfo(name = "vitta", age = 18)
            log("Java requestUserInfo: 请求到User信息 $userInfo")
            //🎯抛回主线程
            handler.post {
                log("Java requestUserInfo: handler post 到：${Thread.currentThread().name} 再callback 给调用方")//main
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
        log("KT requestUserInfo: 模拟网络请求，线程名 ${Thread.currentThread().name}") //在主线程！
        delay(5000) //模拟耗时 主线程delay1000 不会ANR吗？不会！- 原因在协程的delay是挂起，相当于给协程封装类加了标记，现在暂停执行方法，5s后原地恢复执行，主线程打完标记立马可以执行别的UI刷新。
//        Thread.sleep(10000)//把这句打开，在10s内点击其他的UI，因为主线程遭阻塞了，点击操作无法在5s内dispatch完毕，就会ANR；10s内不点其他UI，不会ANR，主线程被占死10s，这期间不能刷新UI。刷就会ANR。
        //线程的阻塞状态？广义上讲，线程的Runnable 中run方法不执行了，不占CPU，包括三个线程模型中状态 SLEEP进入TIME_WAITING，锁竞争synchronized进入BLOCKED，wait进入WAITING。不执行runnable意味着代码都不执行了呗。
        log("KT requestUserInfo: 网络请求成功，耗时5s")
        return DataUserInfo(name = "vitta", age = 18)
    }


    data class DataUserInfo(val name: String, val age: Int) {
        companion object {
            fun default() = DataUserInfo(name = "vitta", age = 18)
        }
    }

    // ==================== 日志模块 ====================
    // 屏幕日志框：任何线程都可调用 log()，内部自动切回主线程刷新，并附带线程名前缀。
    private val logLines = ArrayDeque<String>()

    private fun log(message: String) {
        val line = "[${Thread.currentThread().name}] $message"
        Log.e(TAG, line)
        appendLog(line)
    }

    private fun appendLog(line: String) {
        handler.post {
            if (view == null) return@post
            logLines.addLast(line)
            while (logLines.size > MAX_LOG_LINES) {
                logLines.removeFirst()
            }
            binding.corTvLog.text = logLines.joinToString(separator = "\n")
            binding.corSvLog.post {
                if (view != null) {
                    binding.corSvLog.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }
        }
    }

    private fun clearLog() {
        logLines.clear()
        binding.corTvLog.text = ""
    }


    private companion object {
        private const val TAG = "CoroutineFragment"
        private const val MAX_LOG_LINES = 180
    }
}
