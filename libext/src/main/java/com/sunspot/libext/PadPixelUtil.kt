package com.sunspot.libext

import android.content.Context
import android.graphics.Point
import android.view.ViewGroup
import android.view.WindowManager

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2024/2/6 15:24
 * -------------------------------------
 * 描述：pad横屏尺寸工具管理
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */
object PadPixelUtil {

    /**
     * 屏幕宽高比
     */
    private var SCREEN_RATIO = 0f

    /**
     * 第二层玩法宽度、背景区宽度、弹幕宽度
     */
    private var padLevelTwoWidth = 0

    /**
     * 第二层玩法宽度、背景区宽度、弹幕宽度
     */
    private var bgWidth: Int = 0

    /**
     * 第二层玩法宽度、背景区宽度、弹幕宽度
     */
    private var chatWidth: Int = 0

    @JvmStatic
    fun landGiftScaledWidth(context: Context?): Int {
        return (CommonUtils.getScreenHeight(context) * 720f / 1360f).toInt()
    }

    @JvmStatic
    fun getPadDialogWidth(context: Context?): Int {
        var width = dp2px(310)
        var maxWidth = CommonUtils.getScreenWidth(context) - dp2px(80)
        if (width > maxWidth) {
            width = maxWidth
        }
        return width
    }

    /**
     * Pad上半弹层的宽度
     * @param context
     * @return
     */
    @JvmStatic
    fun landPanelWidth(context: Context?): Int {
        if (context == null) {
            return ViewGroup.LayoutParams.MATCH_PARENT
        }
        var screenWidth = CommonUtils.getScreenWidth(context)
        if (DeviceUtil.isPad(context)) {
            //PAD限制最大宽度
            val maxScreenWidth = CommonUtils.dip2px(context, 375f)
            if (screenWidth > maxScreenWidth) {
                screenWidth = maxScreenWidth
            }
        }
        return screenWidth
    }

    /**
     * Pad上半弹层的高度
     * @param context
     * @param heightPercent pad 上弹层高度 取屏幕高度的百分比 默认 0.8f
     * @return
     */
    @JvmStatic
    fun landPadPanelHeight(context: Context?, heightPercent: Float, defaultHeight: Int): Int {
        if (context == null) {
            return defaultHeight
        }
        return if (DeviceUtil.isPad(context)) {
            //PAD 上固定高度
            val screenWidth = CommonUtils.getScreenWidth(context)
            val screenHeight = CommonUtils.getScreenHeight(context)

            if (isMagicOrMultiWindowMode(context)) {
                if (screenWidth / (screenHeight * 1.0f) < 0.32f) {
                    (screenWidth * 1.55f).toInt()
                } else {
                    (screenHeight * heightPercent).toInt()
                }
            } else {
                (screenWidth.coerceAtMost(screenHeight) * heightPercent).toInt()
            }
        } else {
            //非PAD上使用默认传入的高度
            defaultHeight
        }
    }

    private fun initPadScale(context: Context) {
        val screenRatio = getScreenRatio(context)
        val sizePoint = Point()
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (manager != null) {
            val display = manager.defaultDisplay
            display.getSize(sizePoint)
        }
        //横屏下宽高
        val width: Int
        val height: Int
        if (sizePoint.x > sizePoint.y) {
            width = sizePoint.x
            height = sizePoint.y
        } else {
            width = sizePoint.y
            height = sizePoint.x
        }
        if (screenRatio >= 0.6f && screenRatio <= 0.7f) {
            //中屏比例(主流机型 1600/2560 长方形)
            bgWidth = width - dp2px(280) * 2
            chatWidth = dp2px(360) //280 + 80(padding)
            padLevelTwoWidth = (height * 0.65f).toInt() //840f / 1280f
        } else if (screenRatio > 0.7f) {
            //大屏比例(最少机型 更正方形)
            bgWidth = (height * 0.56f).toInt() //720f / 1280f
            chatWidth =
                (width - bgWidth) / 2 + dp2px(80) //280 + 80(padding)
            padLevelTwoWidth = bgWidth
        } else {
            //<0.6f 小屏比例（次主流机型 720/1280 更长方形）
            bgWidth = dp2px(375)
            chatWidth = (width - bgWidth) / 2 + dp2px(80)
            padLevelTwoWidth = dp2px(340)
        }
    }

    /**
     * 第一层竖屏视频直播宽度
     * 第二层玩法（连麦、PK）宽度
     */
    @JvmStatic
    fun getPadScaledWidth(context: Context): Int {
        if (padLevelTwoWidth <= 0 || isMagicOrMultiWindowMode(context)) {
            initPadScale(context)
        }
        return padLevelTwoWidth
    }

    /**
     * pad背景宽度
     */
    @JvmStatic
    fun getPadBgWidth(context: Context): Int {
        if (bgWidth <= 0 || isMagicOrMultiWindowMode(context)) {
            initPadScale(context)
        }
        return bgWidth
    }

    /**
     * 弹幕区宽度
     */
    @JvmStatic
    fun getPadChatListWidth(context: Context): Int {
        if (chatWidth <= 0 || isMagicOrMultiWindowMode(context)) {
            initPadScale(context)
        }
        return chatWidth
    }

    private fun isMagicOrMultiWindowMode(context: Context): Boolean {
        return false
    }

    /**
     * 获取设备宽高比
     *
     * @return (0, 1]
     */
    private fun getScreenRatio(context: Context): Float {
        if (SCREEN_RATIO > 0 || !isMagicOrMultiWindowMode(context)) {
            return SCREEN_RATIO
        }
        val sizePoint = Point()
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        display.getSize(sizePoint)
        if (sizePoint.x > sizePoint.y && sizePoint.x > 0) {
            SCREEN_RATIO = sizePoint.y.toFloat() / sizePoint.x.toFloat()
            return SCREEN_RATIO
        } else if (sizePoint.y > sizePoint.x && sizePoint.y > 0) {
            SCREEN_RATIO = sizePoint.x.toFloat() / sizePoint.y.toFloat()
            return SCREEN_RATIO
        }
        return 1f
    }
}