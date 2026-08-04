package com.sunspot.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2026/7/29 17:06
 * -------------------------------------
 * 描述：
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */
abstract class BaseActivity<V : ViewBinding> : AppCompatActivity() {

    private var _binding: V? = null
    protected val binding: V
        get() = checkNotNull(_binding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = createViewBinding(layoutInflater)
        setContentView(binding.root)
        initView(binding)
        initData(binding, savedInstanceState)
    }

    abstract fun createViewBinding(layoutInflater: LayoutInflater): V

    abstract fun initView(binding: V)

    open fun initData(binding: V, savedInstanceState: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}