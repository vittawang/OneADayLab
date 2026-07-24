package com.sunspot.libext;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2026/7/24 17:04
 * -------------------------------------
 * 描述：
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */
public class CommonUtils {

    /**
     * 获取手机屏幕宽度通用方法
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        if (null == context) {
            return 0;
        }
        Point sizePoint = new Point();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            Display display = manager.getDefaultDisplay();
            display.getSize(sizePoint);
        }
        return sizePoint.x;
    }

    /**
     * 获取手机屏幕高度通用方法
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        if (null == context) {
            return 0;
        }
        Point sizePoint = new Point();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            Display display = manager.getDefaultDisplay();
            display.getSize(sizePoint);
        }
        return sizePoint.y;
    }

    /**
     * 获取手机屏幕高度
     * 为了解决getScreenHeight（）方法，全面屏手机高度减去了StatusBar的高度
     *
     * @param context
     * @return
     */
    public static int getRealScreenHeight(Context context) {
        if (null == context) {
            return 0;
        }
        DisplayMetrics outMetrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getRealMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 屏幕尺寸dip转px换算方法
     *
     * @param context
     * @param dipValue dip值
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        if (null == context) {
            return 0;
        }
        final float scaleValue = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scaleValue + 0.5f);
    }

    public static float getDensity(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 屏幕尺寸px转dip换算方法
     *
     * @param context
     * @param pxValue px值
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        if (null == context) {
            return 0;
        }
        final float scaleValue = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scaleValue + 0.5f);
    }


}
