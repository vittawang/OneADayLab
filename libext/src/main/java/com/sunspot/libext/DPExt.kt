package com.sunspot.libext


/**
 * Int dp 转 px
 */
inline val Int.dp: Int
    get() {
        return (this * AppContext.instance.context.resources.displayMetrics.density + 0.5f).toInt()
    }

/**
 * Float dp 转 px
 */
inline val Float.dp: Float
    get() {
        return this * AppContext.instance.context.resources.displayMetrics.density
    }

/**
 * Int sp 转 px
 */
inline val Int.sp: Int
    get() {
        return (this * AppContext.instance.context.resources.displayMetrics.scaledDensity + 0.5f).toInt()
    }

/**
 * Float sp 转 px
 */
inline val Float.sp: Float
    get() {
        return this * AppContext.instance.context.resources.displayMetrics.scaledDensity
    }

/**
 * Int px 转 dp
 */
inline val Int.px2dp: Int
    get() {
        return (this / AppContext.instance.context.resources.displayMetrics.density + 0.5f).toInt()
    }

/**
 * Float px 转 dp
 */
inline val Float.px2dp: Float
    get() {
        return this / AppContext.instance.context.resources.displayMetrics.density
    }

/**
 * Int px 转 sp
 */
inline val Int.px2sp: Int
    get() {
        return (this / AppContext.instance.context.resources.displayMetrics.scaledDensity + 0.5f).toInt()
    }

/**
 * Float px 转 sp
 */
inline val Float.px2sp: Float
    get() {
        return this / AppContext.instance.context.resources.displayMetrics.scaledDensity
    }

