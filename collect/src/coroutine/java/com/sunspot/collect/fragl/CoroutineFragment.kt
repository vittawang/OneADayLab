package com.sunspot.collect.fragl

import android.annotation.SuppressLint
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
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
        binding.corBtnKillCallback.setOnClickListener { killCallback() }//1
        binding.corBtnClearLog.setOnClickListener { clearLog() }
        binding.corBtnSwitchThread.setOnClickListener { withContextSwitchThread() }//2
        binding.corBtnUiOnIo.setOnClickListener { touchUiOnIoThread() }//3
        binding.corBtnSync.setOnClickListener { serialRequest() }
        binding.corBtnAsync.setOnClickListener { parallelRequest() }//4
        binding.corBtnJoin.setOnClickListener { jobJoin() }//5
        binding.corBtnCancelStart.setOnClickListener { startCancelableJob() }//6
        binding.corBtnCancelDo.setOnClickListener { cancelJob6() }//6
        binding.corBtnWithTimeout.setOnClickListener { timeoutDemo() }//7
    }

    /**
     * withTimeout：到点自动 cancel。withTimeoutOrNull 超时返回 null（业务友好）。
     */
    private fun timeoutDemo() {
        lifecycleScope.launch {
            //挂起函数，返回值是block块的返回值。最多等2s 否则返回null
            log("withTimeoutOrNull 2s")
            val datePage = withTimeoutOrNull(2000) {
                log("开始请求（模拟耗时 3 秒，会超时）")
                delay(3000)
                DataPage("没网啦！")
            }
            if (datePage == null) {
                log("请求超时！withTimeoutOrNull 返回 null")
                binding.tvUser.text = "网络超时，请重试"
            } else {
                log("请求成功：$datePage")
                binding.tvUser.text = datePage.content
            }
        }
    }

    private var job6: Job? = null

    /**
     * （6）job cancel: 立即标记协程为取消状态，执行到下一个挂起点(如delay)自动检查状态，而后取消。没有下一个挂起点，可以手动判断 isActive 实现。
     * 业务场景：开启下载任务，每1s(delay 挂起点)加5，过程中点cancel,看协程是如何被取消的
     * 疑问🤔：那在接口请求的例子中，我这个接口正在请求着，退出页面了，此时cancel掉协程也不管用啊，接口正在执行中，已经没有挂起点了
     *      —— 1. retrofit 在coroutine cancel中把OkHttp的Call cancel掉了，协程友好的网络库！
     *      —— 2. 在withContext往主线程切的时候，就是挂起点，至少能保证不乱刷UI，不过线程确实会执行完，会占用些资源。
     *
     */
    private fun startCancelableJob() {
        job6 = lifecycleScope.launch(Dispatchers.IO) {
            log("开始下载任务")
            var progress = 0
            while (progress < 100) {
                delay(1000) //挂起点：会响应cancel取消
                progress += 10
                log("当前进度：$progress%")
            }
            log("任务完成")//若中途取消，这句不会走到
        }


//        //改成sleep 没有挂起点了，cancel是停不了协程的
//        job6 = lifecycleScope.launch(Dispatchers.IO) {
//            log("开始下载任务")
//            var progress = 0
//            while (progress < 100) {
//                Thread.sleep(1200)
//                progress += 10
//                log("当前进度：$progress%")
//            }
//            log("任务完成")
//        }


//        //手动判断isActive
//        job6 = lifecycleScope.launch(Dispatchers.IO) {
//            log("开始下载任务")
//            var progress = 0
//            //⬇️ 主动判断标记位状态
//            while (progress < 100) {
//                if (!isActive) {
//                    log("任务取消，已返回")
//                    return@launch
//                }
//                Thread.sleep(1200)
//                progress += 10
//                log("当前进度：$progress%")
//            }
//            log("任务完成")
//        }

    }

    /**
     * 协作式取消：cancel() 发信号，挂起点(delay) 检查到后抛 CancellationException 停止
     */
    private fun cancelJob6() {
        log("点了取消，调用 job.cancel()")
        job6?.cancel()
        log("已给协程打cancel标记，下一个挂起点(delay)会报CancellationException，协程停止。这个Exception被协程框架捕获处理了，认为是正常的取消收尾 静默吞掉。")
    }


    /**
     * （5）协程的返回值 是Job
     *      挂起函数的返回值是T-Data，或者空
     *  业务场景：等下载任务（子协程执行 → fork 分叉）完成时（子协程结束 → join 汇合），告诉UI 任务完成了。
     */
    private fun jobJoin() {
        lifecycleScope.launch {//父协程
            val job = launch(Dispatchers.IO) { //子协程
                log("下载任务，子协程开始")
                delay(3000)
                log("下载任务，子协程结束，耗时3s")
            }
            //1. 这样直接执行的话 会不等job 执行完，立刻执行这里
//            binding.tvUser.text = "🎉任务完成！"
//            log("☹️ 根本没等3s耗时，直接就完成了，肯定不对！")

            //2. 这样执行的话，会等job执行完，再更新UI，注意这里是main线程
            job.join()
            binding.tvUser.text = "🎉任务完成！"
            log("🎉任务完成！")
        }
    }

    /**
     * （4）并行请求 async()
     * 翻译：async 异步；await 等待；defer 延迟
     * 日志信息：
     * 1. 串行时，两个方法分开写withContext(IO) 竟然走的是同一个线程Worker-1 ？就串行了？如果他走的不是同一个线程 不就并行了吗？
     *      —— 涉及到两个挂起函数在一个协程里，他就是模拟代码执行顺序，上一个完全执行完，下一个才开跑
     * 2. 真实场景经常是这样的 异步请求两个接口，两个接口都回来才更新UI，换Java写法，要两个callback 两个标记，都判断回来才能更新UI。
     *
     */
    @SuppressLint("SetTextI18n")
    private fun parallelRequest() {
        //异步请求 并行
        lifecycleScope.launch {
            log("=======两个挂起并行（async/await）=======")
            val start = System.currentTimeMillis()
            val pageDeferred = async { queryHomePage() }//立刻开跑 1.5s
            //async 返回值是 Deferred<T> 延迟的Data
            val userDeferred = async { queryUser() }//立刻开跑 1s，不等上面的挂起结束，才叫两个挂起 并行执行嘛！
            //await 返回值是 T，就是Data
            val dataP = pageDeferred.await()
            val dataU = userDeferred.await()
            log("并行：两个接口下面调用，耗时 ${(System.currentTimeMillis() - start) / 1000f}s,开始更新UI")
            binding.tvUser.text = "async/await并行： ${dataU.name} - ${dataP.content}"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun serialRequest() {
        //同步请求 串行
        lifecycleScope.launch {
            log("=======两个挂起串行=======")
            val start = System.currentTimeMillis()
            val dataUser = queryUser() //协程挂起，直到完全结束了，返回值。 1s
            val dataPage = queryHomePage()//再执行下一个挂起1.5s，所以才叫串行，不是分两个线程执行下去了
            log("串行：两个接口下面调用，耗时 ${(System.currentTimeMillis() - start) / 1000f}s,开始更新UI")//2.532s
            binding.tvUser.text = "串行 ${dataUser.name} - ${dataPage.content}"
        }
    }


    /**
     * 🎯这个写法 是Google范式写法，保证调用方 Main安全，方法调用完 自动切回Main了，很安全！
     */
    private suspend fun queryUser() = withContext(Dispatchers.IO) {
        Thread.sleep(1000)
        log("请求到user 信息，耗时1s")
        DataUserInfo("vitta", 4)
    }

    private suspend fun queryHomePage() = withContext(Dispatchers.IO) {
        Thread.sleep(1500)
        log("请求到homePage信息，耗时1.5s")
        DataPage("页面内容")
    }


    /**
     * （3）子线程touch ui
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


    data class DataUserInfo(val name: String, val age: Int)
    data class DataPage(val content: String)

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
