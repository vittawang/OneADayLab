package com.sunspot.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
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
abstract class BaseActivity<V : ViewBinding> : FragmentActivity() {

    protected lateinit var binding: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = createViewBinding(layoutInflater)
        setContentView(binding.root)
        initView(binding)
        initData(binding, savedInstanceState)
    }

    abstract fun createViewBinding(layoutInflater: LayoutInflater): V

    abstract fun initView(binding: V)

    open fun initData(binding: V, savedInstanceState: Bundle?) {}
}