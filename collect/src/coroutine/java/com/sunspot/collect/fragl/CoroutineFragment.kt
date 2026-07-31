package com.sunspot.collect.fragl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.sunspot.base.BaseFragment
import com.sunspot.collect.databinding.CoroutineFragmentCorBinding

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
class CoroutineFragment : BaseFragment<CoroutineFragmentCorBinding>() {
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): CoroutineFragmentCorBinding = CoroutineFragmentCorBinding.inflate(inflater, container, false)

    override fun initView(binding: CoroutineFragmentCorBinding) {
    }


    private companion object {
        private const val TAG = "CoroutineFragment"
    }
}