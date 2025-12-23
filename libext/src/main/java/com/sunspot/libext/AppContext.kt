package com.sunspot.libext

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.StringRes

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2023/11/9 15:21
 * -------------------------------------
 * 描述：饿汉式单例
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */
class AppContext {

    companion object {
        @JvmStatic
        val instance: AppContext by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AppContext()
        }
    }

    lateinit var context: Context

    fun initContext(context: Context) {
        this.context = context
    }

    fun string(@StringRes strId: Int) = context.resources.getString(strId)

    fun stringArray(@ArrayRes strArrayId: Int) = context.resources.getStringArray(strArrayId)
    fun intArray(@ArrayRes intArray: Int) = context.resources.getIntArray(intArray)

    fun color(@ColorRes colorId: Int) = context.resources.getColor(colorId, null)

}