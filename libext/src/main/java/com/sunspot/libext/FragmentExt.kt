package com.sunspot.libext

import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun DialogFragment.addAndCommit(
    fm: FragmentManager?, tag: String? = this@addAndCommit.javaClass.simpleName
) {
    fm?.apply {
        val findFragmentByTag = fm.findFragmentByTag(tag)
        val ft = beginTransaction()
        if (findFragmentByTag != null) {
            ft.remove(findFragmentByTag)
        }
        ft.add(this@addAndCommit, tag)
        ft.commitAllowingStateLoss()
    }
}

fun Fragment.addAndCommit(fm: FragmentManager?, tag: String? = this@addAndCommit.javaClass.simpleName) {
    fm?.apply {
        val findFragmentByTag = fm.findFragmentByTag(tag)
        val ft = beginTransaction()
        if (findFragmentByTag != null) {
            ft.remove(findFragmentByTag)
        }
        ft.add(this@addAndCommit, tag)
        ft.commitAllowingStateLoss()
    }
}

fun Fragment.addAndCommit(
    fm: FragmentManager?, tag: String? = this@addAndCommit.javaClass.simpleName, @IdRes container: Int
) {
    fm?.apply {
        val findFragmentByTag = fm.findFragmentByTag(tag)
        val ft = beginTransaction()
        if (findFragmentByTag != null) {
            ft.remove(findFragmentByTag)
        }
        ft.add(container, this@addAndCommit, tag)
        ft.commitAllowingStateLoss()
    }
}

