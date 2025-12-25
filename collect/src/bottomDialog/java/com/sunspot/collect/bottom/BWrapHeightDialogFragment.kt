package com.sunspot.collect.bottom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sunspot.collect.R
import com.sunspot.collect.databinding.BoFragmentWrapHeightBinding

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2025/12/19 17:16
 * -------------------------------------
 * 描述：自适应内容的高度
 * -------------------------------------
 * 备注：添加到dialogFragment里的xml，最外层的布局参数是不生效的。会改变成wrap_content
 * -------------------------------------
 */
class BWrapHeightDialogFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(content: String): BWrapHeightDialogFragment {
            var f = BWrapHeightDialogFragment()
            f.arguments = Bundle().apply {
                putString("content", content)
            }
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var binding = BoFragmentWrapHeightBinding.inflate(inflater)
        binding.tvContent.apply {
            text = arguments?.getString("content")
        }
        return binding.root
    }


    override fun getTheme(): Int {
        return R.style.TransparentBottomSheetDialog
    }

}