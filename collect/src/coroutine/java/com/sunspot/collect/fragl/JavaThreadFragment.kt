package com.sunspot.collect.fragl

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ScrollView
import com.sunspot.base.BaseFragment
import com.sunspot.collect.databinding.CoroutineFragmentJavaBinding
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
class JavaThreadFragment : BaseFragment<CoroutineFragmentJavaBinding>() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val logLines = ArrayDeque<String>()
    private val logger = JavaThreadEx.Logger(::appendLog)

    private var threadExamples: JavaThreadEx? = null

    override fun createViewBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): CoroutineFragmentJavaBinding =
        CoroutineFragmentJavaBinding.inflate(inflater, container, false)

    override fun initView(binding: CoroutineFragmentJavaBinding) {
        threadExamples = JavaThreadEx(logger, mainHandler)

        binding.corBtnJavaThread.setOnClickListener {
            runDemo(
                title = "Thread：下载头像",
                action = JavaThreadEx::thread
            )
        }
        binding.corBtnJavaRunnable.setOnClickListener {
            runDemo(
                title = "Runnable：复用下载任务",
                action = JavaThreadEx::runnable
            )
        }
        binding.corBtnJavaStartRun.setOnClickListener {
            runDemo(
                title = "图片压缩：start() / run()",
                action = JavaThreadEx::startVersusRun
            )
        }
        binding.corBtnJavaThreadState.setOnClickListener {
            runDemo("观察下载线程状态", JavaThreadEx::threadStates)
        }
        binding.corBtnJavaSleepRetry.setOnClickListener {
            runDemo("下载失败后重试：sleep()", JavaThreadEx::sleepRetry)
        }
        binding.corBtnJavaJoin.setOnClickListener {
            runDemo("下载后解压：join()", JavaThreadEx::joinDownloadThenUnzip)
        }
        binding.corBtnJavaInterrupt.setOnClickListener {
            runDemo("取消下载：interrupt()", JavaThreadEx::interruptDownload)
        }
        binding.corBtnJavaRace.setOnClickListener {
            runDemo("多窗口卖票：竞态条件", JavaThreadEx::unsafeTicketSale)
        }
        binding.corBtnJavaSynchronized.setOnClickListener {
            runDemo("安全卖票：synchronized", JavaThreadEx::synchronizedTicketSale)
        }
        binding.corBtnJavaVolatile.setOnClickListener {
            runDemo("停止数据同步：volatile", JavaThreadEx::volatileStopFlag)
        }
        binding.corBtnJavaAtomic.setOnClickListener {
            runDemo("统计下载数量：AtomicInteger", JavaThreadEx::atomicDownloadCount)
        }
        binding.corBtnJavaLock.setOnClickListener {
            runDemo("银行取款：ReentrantLock", JavaThreadEx::lockedBankWithdraw)
        }
        binding.corBtnJavaWaitNotify.setOnClickListener {
            runDemo("等待商家出餐：wait() / notifyAll()", JavaThreadEx::waitForMeal)
        }
        binding.corBtnJavaLatch.setOnClickListener {
            runDemo("首页等待三个接口：CountDownLatch", JavaThreadEx::loadHomePage)
        }
        binding.corBtnJavaFuture.setOnClickListener {
            runDemo("计算订单价格：Callable / Future", JavaThreadEx::calculateOrderPrice)
        }
        binding.corBtnJavaThreadPool.setOnClickListener {
            runDemo("批量上传图片：ThreadPoolExecutor", JavaThreadEx::uploadImages)
        }
        binding.corBtnJavaHandler.setOnClickListener {
            runDemo(
                title = "接口结果更新页面：Handler",
                action = {
                    it.updateUiWithHandler()
                }
            )
        }
        binding.corBtnJavaStop.setOnClickListener {
            stopCurrentDemo()
        }
        binding.corBtnJavaClearLog.setOnClickListener {
            logLines.clear()
            binding.corTvJavaLog.text = ""
        }
    }

    private fun runDemo(
        title: String,
        action: (JavaThreadEx) -> Unit
    ) {
        val examples = threadExamples ?: return
        appendLog("\n========== $title ==========")
        try {
            action(examples)
        } catch (throwable: Throwable) {
            appendLog(
                "[${Thread.currentThread().name}] ${throwable.javaClass.simpleName}: " +
                    (throwable.message ?: "未知错误")
            )
        }
    }

    private fun stopCurrentDemo() {
        val stopped = threadExamples?.cancelAll() == true
        appendLog(if (stopped) "[main] 已发送中断请求" else "[main] 当前没有运行中的实验")
    }

    private fun appendLog(message: String) {
        mainHandler.post {
            if (view == null) return@post
            logLines.addLast(message)
            while (logLines.size > MAX_LOG_LINES) {
                logLines.removeFirst()
            }
            binding.corTvJavaLog.text = logLines.joinToString(separator = "\n")
            binding.corSvJavaLog.post {
                if (view != null) {
                    binding.corSvJavaLog.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }
        }
    }

    override fun onDestroyView() {
        threadExamples?.cancelAll()
        threadExamples = null
        mainHandler.removeCallbacksAndMessages(null)
        logLines.clear()
        super.onDestroyView()
    }

    private companion object {
        const val TAG = "JavaThreadFragment"
        const val MAX_LOG_LINES = 180
    }
}
