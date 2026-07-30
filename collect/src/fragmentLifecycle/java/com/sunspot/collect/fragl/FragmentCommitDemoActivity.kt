package com.sunspot.collect.fragl

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sunspot.base.BaseActivity
import com.sunspot.collect.R
import com.sunspot.collect.databinding.FraglCommitDemoActivityBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 对比 FragmentTransaction 四种 commit 方法的执行时机和状态丢失行为。
 */
class FragmentCommitDemoActivity : BaseActivity<FraglCommitDemoActivityBinding>() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private var sequence = 0

    private val fragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentCreated(
                fragmentManager: FragmentManager,
                fragment: Fragment,
                savedInstanceState: Bundle?
            ) {
                logFragment(fragment, "onCreate")
            }

            override fun onFragmentStarted(
                fragmentManager: FragmentManager,
                fragment: Fragment
            ) {
                logFragment(fragment, "onStart")
            }

            override fun onFragmentResumed(
                fragmentManager: FragmentManager,
                fragment: Fragment
            ) {
                logFragment(fragment, "onResume")
            }

            override fun onFragmentPaused(
                fragmentManager: FragmentManager,
                fragment: Fragment
            ) {
                logFragment(fragment, "onPause")
            }

            override fun onFragmentStopped(
                fragmentManager: FragmentManager,
                fragment: Fragment
            ) {
                logFragment(fragment, "onStop")
            }

            override fun onFragmentDestroyed(
                fragmentManager: FragmentManager,
                fragment: Fragment
            ) {
                logFragment(fragment, "onDestroy")
            }
        }

    override fun createViewBinding(
        layoutInflater: LayoutInflater
    ): FraglCommitDemoActivityBinding =
        FraglCommitDemoActivityBinding.inflate(layoutInflater)

    override fun initView(binding: FraglCommitDemoActivityBinding) {
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            fragmentLifecycleCallbacks,
            false
        )

        binding.fraglCommit.setOnClickListener {
            executeTransaction(CommitMethod.COMMIT)
        }
        binding.fraglCommitAllowing.setOnClickListener {
            executeTransaction(CommitMethod.COMMIT_ALLOWING_STATE_LOSS)
        }
        binding.fraglCommitNow.setOnClickListener {
            executeTransaction(CommitMethod.COMMIT_NOW)
        }
        binding.fraglCommitNowAllowing.setOnClickListener {
            executeTransaction(CommitMethod.COMMIT_NOW_ALLOWING_STATE_LOSS)
        }
        binding.fraglDelayedCommit.setOnClickListener {
            scheduleTransaction(CommitMethod.COMMIT)
        }
        binding.fraglDelayedAllowing.setOnClickListener {
            scheduleTransaction(CommitMethod.COMMIT_ALLOWING_STATE_LOSS)
        }
        binding.fraglClearLog.setOnClickListener {
            binding.fraglCommitLog.text = ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appendLog(
            "Activity.onCreate(saved=${savedInstanceState != null}), " +
                "isStateSaved=${supportFragmentManager.isStateSaved}"
        )
    }

    override fun onStart() {
        super.onStart()
        appendLog("Activity.onStart, isStateSaved=${supportFragmentManager.isStateSaved}")
    }

    override fun onResume() {
        super.onResume()
        appendLog("Activity.onResume, isStateSaved=${supportFragmentManager.isStateSaved}")
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        appendLog(
            "Activity.onResumeFragments, " +
                "isStateSaved=${supportFragmentManager.isStateSaved}（可安全提交）"
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        appendLog(
            "Activity.onSaveInstanceState BEFORE super, " +
                "isStateSaved=${supportFragmentManager.isStateSaved}"
        )
        super.onSaveInstanceState(outState)
        appendLog(
            "Activity.onSaveInstanceState AFTER super, " +
                "isStateSaved=${supportFragmentManager.isStateSaved}"
        )
    }

    override fun onStop() {
        appendLog("Activity.onStop, isStateSaved=${supportFragmentManager.isStateSaved}")
        super.onStop()
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(
            fragmentLifecycleCallbacks
        )
        super.onDestroy()
    }

    private fun executeTransaction(method: CommitMethod) {
        val fragmentManager = supportFragmentManager
        val instanceNumber = ++sequence
        val tag = "commit_demo:$instanceNumber"
        val fragment = CommitDemoFragment.newInstance(
            method.label,
            instanceNumber,
            currentTime()
        )

        appendLog(
            "${method.label} 调用前：isStateSaved=${fragmentManager.isStateSaved}"
        )

        try {
            val transaction = fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragl_commit_container, fragment, tag)
                .runOnCommit {
                    appendLog(
                        "${method.label} runOnCommit：" +
                            "findFragmentByTag=${fragmentManager.findFragmentByTag(tag) != null}"
                    )
                }

            when (method) {
                CommitMethod.COMMIT -> transaction.commit()
                CommitMethod.COMMIT_ALLOWING_STATE_LOSS ->
                    transaction.commitAllowingStateLoss()

                CommitMethod.COMMIT_NOW -> transaction.commitNow()
                CommitMethod.COMMIT_NOW_ALLOWING_STATE_LOSS ->
                    transaction.commitNowAllowingStateLoss()
            }

            appendLog(
                "${method.label} 返回后立即查询：" +
                    "findFragmentByTag=${fragmentManager.findFragmentByTag(tag) != null}"
            )
        } catch (exception: IllegalStateException) {
            appendLog(
                "${method.label} 捕获 ${exception.javaClass.simpleName}：" +
                    exception.message
            )
        }
    }

    private fun scheduleTransaction(method: CommitMethod) {
        appendLog(
            "已安排 3 秒后执行 ${method.label}；" +
                "请在 3 秒内按 Home 键，再返回查看结果。"
        )
        mainHandler.postDelayed(
            {
                appendLog(
                    "延迟任务触发：Activity=${lifecycle.currentState}，" +
                        "isStateSaved=${supportFragmentManager.isStateSaved}"
                )
                executeTransaction(method)
            },
            DELAY_MILLIS
        )
    }

    private fun logFragment(fragment: Fragment, event: String) {
        if (fragment is CommitDemoFragment) {
            appendLog("${fragment.demoLabel}.$event")
        }
    }

    private fun appendLog(message: String) {
        binding.fraglCommitLog.append("${currentTime()}  $message\n")
        binding.fraglCommitLogScroll.post {
            binding.fraglCommitLogScroll.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    private fun currentTime(): String = timeFormat.format(Date())

    private enum class CommitMethod(val label: String) {
        COMMIT("commit()"),
        COMMIT_ALLOWING_STATE_LOSS("commitAllowingStateLoss()"),
        COMMIT_NOW("commitNow()"),
        COMMIT_NOW_ALLOWING_STATE_LOSS("commitNowAllowingStateLoss()")
    }

    private companion object {
        const val DELAY_MILLIS = 3_000L
    }
}
