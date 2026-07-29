package com.sunspot.base.loading

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.sunspot.base.R
import com.sunspot.libext.dp

/**
 * 用于短时接口请求的阻塞式 Loading 弹窗。
 *
 * 适合保存、提交等短操作；耗时较长的任务应使用可取消或可后台运行的进度页面。
 */
class LoadingDialog(
    context: Context
) : Dialog(context, R.style.BaseLoadingDialog), DefaultLifecycleObserver {

    private companion object {
        const val BACKGROUND_DIM_AMOUNT = 0.48f
        const val WIDTH = 240
    }

    private val hostContext = context
    private val lifecycleOwner = hostContext as? LifecycleOwner
    private val contentView = LayoutInflater.from(hostContext)
        .inflate(R.layout.base_loading_dialog, null, false)
    private val messageView: TextView = contentView.findViewById(R.id.base_loading_tv_message)

    private var lifecycleAttached = false

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Dialog>.onCreate(savedInstanceState)
        setContentView(contentView)
    }

    override fun onStart() {
        super<Dialog>.onStart()
        window?.apply {
            setLayout(
                WIDTH.dp,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setDimAmount(BACKGROUND_DIM_AMOUNT)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    fun setMessage(message: CharSequence?): LoadingDialog {
        messageView.text = message?.takeIf { it.isNotBlank() }
            ?: hostContext.getText(R.string.base_loading_default_message)
        return this
    }

    fun setMessage(@StringRes messageRes: Int): LoadingDialog {
        return setMessage(hostContext.getText(messageRes))
    }

    /**
     * 页面有效时显示；重复调用不会重复 show。
     */
    @MainThread
    fun showSafely() {
        if (isShowing || !canShow()) {
            return
        }
        attachLifecycle()
        try {
            show()
        } catch (_: WindowManager.BadTokenException) {
            detachLifecycle()
        }
    }

    /**
     * 可重复调用的安全关闭方法。
     */
    @MainThread
    fun dismissSafely() {
        if (!isShowing) {
            detachLifecycle()
            return
        }
        dismiss()
    }

    override fun dismiss() {
        try {
            super.dismiss()
        } finally {
            detachLifecycle()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        dismissSafely()
    }

    private fun canShow(): Boolean {
        val activity = hostContext as? Activity
        if (activity != null && (activity.isFinishing || activity.isDestroyed)) {
            return false
        }
        val lifecycle = lifecycleOwner?.lifecycle ?: return true
        return lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
    }

    private fun attachLifecycle() {
        if (lifecycleAttached) {
            return
        }
        lifecycleOwner?.lifecycle?.addObserver(this)
        lifecycleAttached = lifecycleOwner != null
    }

    private fun detachLifecycle() {
        if (!lifecycleAttached) {
            return
        }
        lifecycleOwner?.lifecycle?.removeObserver(this)
        lifecycleAttached = false
    }
}
