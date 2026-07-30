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

    //为什么需要两个属性? 私有属性负责内部用，赋值清空等；公有属性负责外部用，默认非空
    private var _binding: V? = null
    protected val binding: V
        //对外提供一个不可赋值、非空的 Binding：
        get() = checkNotNull(_binding)//每次访问 binding 时，都去读取 _binding;相当于 if(_b==null) throw NPE,else return _b，checkNotNull 大括号里是Exception的message信息，可以自定义


    private var loadingDialog: LoadingDialog? = null
    private var hasCreatedView = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = createViewBinding(inflater, container, savedInstanceState)
        hasCreatedView = true
        initView(binding)
        initData(binding, savedInstanceState)
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
    open fun initData(binding: V, savedInstanceState: Bundle?) {}

    @JvmOverloads
    fun showLoadingDialog(message: CharSequence = getString(R.string.base_loading_default_message)) {
        if (!hasCreatedView || !isAdded || !lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
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
        super.onDestroyView()
        // 释放 Binding 对 View 树的引用
        _binding = null
    }
}
