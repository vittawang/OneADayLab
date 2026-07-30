package com.sunspot.libext


/**
 * inline 关键字。此关键字向 Kotlin 编译器表明，每次使用函数时，它都应该为函数复制并粘贴（或内嵌）编译的字节码。这样可避免每次调用此函数时都为每个 action 实例化一个新类所产生的开销。
 */

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

