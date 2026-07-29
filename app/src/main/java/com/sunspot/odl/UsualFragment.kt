package com.sunspot.odl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.sunspot.base.BaseFragment
import com.sunspot.odl.databinding.UsualFragmentBinding

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2026/7/29 10:55
 * -------------------------------------
 * 描述：
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */
class UsualFragment : BaseFragment<UsualFragmentBinding>() {
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): UsualFragmentBinding = UsualFragmentBinding.inflate(inflater, container, false)

    override fun initView(binding: UsualFragmentBinding) {
        binding.show.setOnClickListener { showLoadingDialog() }
        binding.dismiss.setOnClickListener { dismissLoadingDialog() }
    }

    override fun initData(binding: UsualFragmentBinding) {

    }
}