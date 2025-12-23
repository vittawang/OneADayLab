package com.sunspot.libext

import android.util.TypedValue

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2023/11/10 11:18
 * -------------------------------------
 * 描述：屏幕尺寸工具类
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */

fun screenWidth(): Int {
    return AppContext.instance.context.resources.displayMetrics.widthPixels
}

fun screenHeight(): Int {
    return AppContext.instance.context.resources.displayMetrics.heightPixels
}

fun dp2px(dp: Int): Int {
    AppContext.instance.context.apply {
        val density = resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }
}

fun dp2px(dp: Float): Int {
    AppContext.instance.context.apply {
        val density = resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }
}

fun px2dp(px: Int): Int {
    AppContext.instance.context.apply {
        val density = resources.displayMetrics.density
        return (px / density + 0.5f).toInt()
    }
}

fun sp2px(sp: Float): Float {
    AppContext.instance.context.apply {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics
        )
    }
}

fun sp2px(sp: Int): Int {
    AppContext.instance.context.apply {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), resources.displayMetrics
        ).toInt()
    }
}

