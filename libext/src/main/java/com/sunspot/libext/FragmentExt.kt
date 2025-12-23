package com.sunspot.libext

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

fun DialogFragment.addAndShow(fm: FragmentManager?, tag: String? = this@addAndShow.javaClass.simpleName) {
    fm?.apply {
        var findFragmentByTag = fm.findFragmentByTag(tag)
        var ft = beginTransaction()
        if (findFragmentByTag != null) {
            ft.remove(findFragmentByTag)
        }
        ft.add(this@addAndShow, tag)
        ft.commitAllowingStateLoss()
    }
}

