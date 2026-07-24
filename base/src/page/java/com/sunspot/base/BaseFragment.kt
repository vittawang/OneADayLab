package com.sunspot.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sunspot.base.databinding.BaseFragmentBinding

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
abstract class BaseFragment : Fragment() {

    protected lateinit var binding: BaseFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BaseFragmentBinding.inflate(inflater, container, false)
        initView()
        initData()
        return binding.root
    }

    /**
     * 配置属性 setListener等
     */
    abstract fun initView()

    /**
     * 绑定数据 D4
     */
    abstract fun initData()

}