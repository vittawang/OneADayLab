package com.sunspot.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.sunspot.base.loading.LoadingDialog

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2026/7/24 14:37
 * -------------------------------------
 * 描述：
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */
abstract class BaseFragment<V : ViewBinding> : Fragment() {

    protected lateinit var binding: V

    private var loadingDialog: LoadingDialog? = null
    private var hasCreatedView = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = createViewBinding(inflater, container, savedInstanceState)
        hasCreatedView = true
        initView(binding)
        initData(binding)
        return binding.root
    }

    abstract fun createViewBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): V

    /**
     * 配置属性 setListener等
     */
    abstract fun initView(binding: V)

    /**
     * 绑定数据 D4
     */
    abstract fun initData(binding: V)

    @JvmOverloads
    fun showLoadingDialog(message: CharSequence = getString(R.string.base_loading_default_message)) {
        if (!hasCreatedView ||
            !isAdded ||
            !lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
        ) {
            return
        }
        val currentContext = context ?: return
        val dialog = loadingDialog ?: LoadingDialog(currentContext).also {
            loadingDialog = it
        }
        dialog.setMessage(message).showSafely()
    }

    fun showLoadingDialog(@StringRes messageRes: Int) {
        showLoadingDialog(getString(messageRes))
    }

    fun dismissLoadingDialog() {
        loadingDialog?.dismissSafely()
        loadingDialog = null
    }

    override fun onDestroyView() {
        dismissLoadingDialog()
        hasCreatedView = false
        binding = null
        super.onDestroyView()
    }
}
