package com.sunspot.libext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.core.content.ContextCompat;


import com.sunspot.log.DLog;

import java.io.FileInputStream;

/**
 * -------------------------------------
 * 时间：2022/11/10 10:59
 * -------------------------------------
 * 描述：
 * -------------------------------------
 * 备注：
 * -------------------------------------
 *
 * @author leizhao
 */
public class ResourceUtil {

    /**
     * 获取颜色
     *
     * @param colorId 颜色id
     */
    public static int getColor(int colorId) {
        Context context = AppContext.getInstance().getContext();
        if (context == null) {
            return 0;
        }
        return ContextCompat.getColor(context, colorId);
    }

    /**
     * 获取图片
     *
     * @param drawableId 颜色id
     */
    public static Drawable getDrawable(int drawableId) {
        Context context = AppContext.getInstance().getContext();
        if (context == null) {
            return null;
        }
        return ContextCompat.getDrawable(context, drawableId);
    }


    /**
     * 获取文本
     *
     * @param strId 文本id
     */
    public static String getString(int strId) {
        Context context = AppContext.getInstance().getContext();
        if (context == null) {
            return null;
        }
        return context.getResources().getString(strId);
    }

    /**
     * 获取图片
     *
     * @param path 图片路径
     */
    public static Drawable getDrawable(String path) {
        Context context = AppContext.getInstance().getContext();
        if (context == null) {
            return null;
        }
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(path), null, options);
        } catch (Exception e) {
            e.printStackTrace();
            DLog.logCommon("SKIN_TAG", "ResourceUtil getDrawable by path Exception , path = " + path + " , e = " + e);
        }
        if (bitmap != null) {
            return new BitmapDrawable(context.getResources(), bitmap);
        }
        return null;
    }

    /**
     * 把头像Drawable和背景Drawable粘成一张
     */
    public static Drawable stickAvatarDrawable(Drawable borderDrawable, Drawable avatarDrawable, float padding) {
        if (borderDrawable == null || avatarDrawable == null) {
            return null;
        }
        Drawable[] drawables = {borderDrawable, avatarDrawable};
        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        // 内间距
        int paddingDp = PixelUtilKt.dp2px(padding);
        layerDrawable.setLayerInset(1, paddingDp, paddingDp, paddingDp, paddingDp);
        return layerDrawable;
    }
}

