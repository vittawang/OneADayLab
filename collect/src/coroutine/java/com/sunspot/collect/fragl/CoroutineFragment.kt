package com.sunspot.collect.fragl

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate: " )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e(TAG, "onCreateView: " )
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart: " )
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume: " )
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: " )
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: " )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: " )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e(TAG, "onDestroyView: " )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG, "onAttach: " )
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(TAG, "onDetach: " )
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Log.e(TAG, "onHiddenChanged: $hidden" )
    }

    private companion object {
        private const val TAG = "CoroutineFragment"
    }
}