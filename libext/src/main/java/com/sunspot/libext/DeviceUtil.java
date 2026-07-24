package com.sunspot.libext;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.sunspot.log.DLog;

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2026/7/24 15:29
 * -------------------------------------
 * 描述：
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */
public class DeviceUtil {

    public static final String TAG = "DeviceUtil";

    /**
     * 屏幕尺寸
     */
    private static double mScreenInches = 0;

    /**
     * 大屏设备（7.0尺寸）
     */
    private final static double LARGE_SCREEN_DEVICE = 7.0;

    /**
     * 是否是pad设备
     */
    private static Boolean isPad = null;

    /**
     * 是否是大屏设备
     *
     * @param context 上下文
     * @return 尺寸超7.0英寸过则返回true，反之返回false
     */
    private static boolean isLargeScreenDevice(Context context) {
        if (mScreenInches <= 0) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm == null) {
                DLog.logCommon(TAG, "isLargeScreenDevice() WindowManager is null, return false");
                return false;
            }
            Display display = wm.getDefaultDisplay();
            if (display == null) {
                DLog.logCommon(TAG, "isLargeScreenDevice() display is null, return false");
                return false;
            }
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            // 屏幕尺寸 = 宽次方加上高次方 的 平方根
            if (dm.xdpi != 0 && dm.ydpi != 0) {
                double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
                double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
                // 大于7尺寸则认为是大屏设备
                mScreenInches = Math.sqrt(x + y);
            }
        }
        boolean isLargeScreenDevice = mScreenInches >= LARGE_SCREEN_DEVICE;
        DLog.logCommon(TAG, "isLargeScreenDevice() screen inches = " + mScreenInches + ", isLargeScreenDevice = " + isLargeScreenDevice);
        return isLargeScreenDevice;
    }

    /**
     * 是否是平板窗口
     * 部分平板设备横屏后分屏会判定是非平板
     *
     * @return
     */
    public static boolean isTabletWindow(Context context) {
        //判断是否是大屏设备：从资源管理器中获取当前设备的配置信息，过滤出屏幕信息，与大屏设备比较，大于等于大屏设备则返回true
        //SCREENLAYOUT_SIZE_LARGE 480x640个DP单位，对应于较大的资源限定符
        boolean isTabletWindow = (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        DLog.logCommon(TAG, "isTabletWindow = " + isTabletWindow);
        return isTabletWindow;
    }

    /**
     * 是否是平板设备
     *
     * @return
     */
    public static boolean isTabletDevice() {
        String characteristics = getProperty("ro.build.characteristics");
        if (characteristics == null || TextUtils.isEmpty(characteristics)) {
            DLog.logCommon(TAG, "isTabletDevice() characteristics is null, return false");
            return false;
        }
        boolean isTabletDevice = characteristics.contains("tablet");
        DLog.logCommon(TAG, "isTabletDevice = " + isTabletDevice);
        return isTabletDevice;
    }

    /**
     * 判断是否是平板设备
     *
     * @param context
     * @return
     */
    public static boolean isPad(Context context) {
        //系统属性值中含有tablet信息 或 该屏幕尺寸>=7.0 且设备信息是大屏设备
        if (isPad == null) {
            isPad = isTabletDevice() || (isLargeScreenDevice(context) && isTabletWindow(context));
        }
        return isPad;
    }

    static Class systemProperties;

    private static String getProperty(String propName) {
        String value = null;
        try {
            if (systemProperties == null) {
                systemProperties = Class.forName("android.os.SystemProperties");
            }
            Object roSecureObj = systemProperties.getMethod("get", String.class).invoke(null, propName);
            if (roSecureObj != null) {
                value = (String) roSecureObj;
            }
        } catch (Exception e) {
            value = null;
        }
        return TextUtils.isEmpty(value) ? null : value;
    }


}
