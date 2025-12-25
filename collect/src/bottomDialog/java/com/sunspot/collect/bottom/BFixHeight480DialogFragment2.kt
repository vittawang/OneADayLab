package com.sunspot.collect.bottom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sunspot.collect.R
import com.sunspot.collect.databinding.BoFragmentFixHeight2Binding
import com.sunspot.libext.dp2px

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2025/12/19 17:16
 * -------------------------------------
 * 描述：固定高度2:代码控制XML根标签LayoutParams
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */
class BFixHeight480DialogFragment2 : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return BoFragmentFixHeight2Binding.inflate(inflater).root
    }

    override fun onStart() {
        super.onStart()
        //改根布局的高度
        view?.updateLayoutParams {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = dp2px(480)
        }
    }


    override fun getTheme(): Int {
        return R.style.TransparentBottomSheetDialog
    }

}