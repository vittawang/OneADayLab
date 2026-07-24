/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.sunspot.libext;

import android.content.Context;
import android.content.SharedPreferences;


import java.util.Set;

public class SharedPreferencesUtils {

    public static final String PREFS_ABOUT_LIVE = "PrefsFileAboutLive"; // 直播数据缓存文件

    private static String FILE_NAME = PREFS_ABOUT_LIVE;

    public static String String = "String";

    public static String Integer = "Integer";

    public static String Boolean = "Boolean";

    public static String Float = "Float";

    public static String Long = "Long";

    public static String HashSet = "HashSet";

    /**
     * 可以临时切换目录
     */
    public static void changeFileName(String fileName) {
        FILE_NAME = fileName;
    }

    /**
     * 恢复存放目录至默认
     */
    public static void resetDefaultFileName() {
        FILE_NAME = PREFS_ABOUT_LIVE;
    }

    @SuppressWarnings("unchecked")
    public static void setParam(Context context, String key, Object object) {
        String type = object.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (String.equals(type)) {
            editor.putString(key, (String) object);
        } else if (Integer.equals(type)) {
            editor.putInt(key, (Integer) object);
        } else if (Boolean.equals(type)) {
            editor.putBoolean(key, (Boolean) object);
        } else if (Float.equals(type)) {
            editor.putFloat(key, (Float) object);
        } else if (Long.equals(type)) {
            editor.putLong(key, (Long) object);
        } else if (HashSet.equals(type)) {
            editor.putStringSet(key, (Set<String>) object);
        }

        editor.apply();
    }

    public static void setParamWithCommit(Context context, String key, Object object) {
        String type = object.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (String.equals(type)) {
            editor.putString(key, (String) object);
        } else if (Integer.equals(type)) {
            editor.putInt(key, (Integer) object);
        } else if (Boolean.equals(type)) {
            editor.putBoolean(key, (Boolean) object);
        } else if (Float.equals(type)) {
            editor.putFloat(key, (Float) object);
        } else if (Long.equals(type)) {
            editor.putLong(key, (Long) object);
        }

        editor.commit();
    }

    @SuppressWarnings("unchecked")
    public static Object getParam(Context context, String key, Object defaultObject) {
        String type = defaultObject.getClass().getSimpleName();
        if (context != null){
            SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE); // 有信数据缓存文件

            if (String.equals(type)) {
                return sp.getString(key, (String) defaultObject);
            } else if (Integer.equals(type)) {
                return sp.getInt(key, (Integer) defaultObject);
            } else if (Boolean.equals(type)) {
                return sp.getBoolean(key, (Boolean) defaultObject);
            } else if (Float.equals(type)) {
                return sp.getFloat(key, (Float) defaultObject);
            } else if (Long.equals(type)) {
                return sp.getLong(key, (Long) defaultObject);
            } else if (HashSet.equals(type)) {
                return sp.getStringSet(key, (Set<String>) defaultObject);
            }
        }

        return null;
    }

    public static void setParam(Context context, String name, String key, Object object) {
        String type = object.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (String.equals(type)) {
            editor.putString(key, (String) object);
        } else if (Integer.equals(type)) {
            editor.putInt(key, (Integer) object);
        } else if (Boolean.equals(type)) {
            editor.putBoolean(key, (Boolean) object);
        } else if (Float.equals(type)) {
            editor.putFloat(key, (Float) object);
        } else if (Long.equals(type)) {
            editor.putLong(key, (Long) object);
        }

        editor.apply();
    }

    public static Object getParam(Context context, String name, String key, Object defaultObject) {
        String type = defaultObject.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE); // 有信数据缓存文件

        if (String.equals(type)) {
            return sp.getString(key, (String) defaultObject);
        } else if (Integer.equals(type)) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (Boolean.equals(type)) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (Float.equals(type)) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (Long.equals(type)) {
            return sp.getLong(key, (Long) defaultObject);
        }

        return null;
    }

    /**
     * 移除SharedPreference中对应Key的记录信息
     *
     * @param context Context
     * @param key     SharedPreference中记录值的key
     */
    public static void removeParms(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * 清除全部SP
     */
    public static void clearParms(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    public static void removeDeprecatedSPCache(long accountId) {
        SharedPreferences sp = AppContext.getInstance().context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (!sp.getBoolean("hasDeletedDeprecatedParams", false)) {
            SharedPreferences.Editor editor = sp.edit();
            //首页直播流
            editor.remove("PrefsDataHomeTagList");
            //首页推荐流
            editor.remove("PrefsHomeRecommendFeedList");
            //消息箱
            editor.remove("message_box_data" + accountId);
            //首页直播流列表Banner
            editor.remove("PrefsAdvList");
            //访客推荐流
            editor.remove("PrefsVisitorHomeRecommendFeedList");
            //首页直播流 全部
            editor.remove("PrefsHomeTagFeedList_0_0");
            editor.remove("PrefsHomeTagFeedList_107_0");
            editor.remove("PrefsHomeTagFeedList_112_0");
            editor.remove("PrefsHomeTagFeedList_113_0");
            editor.remove("PrefsHomeTagFeedList_114_0");
            //男
            editor.remove("PrefsHomeTagFeedList_0_1");
            editor.remove("PrefsHomeTagFeedList_107_1");
            editor.remove("PrefsHomeTagFeedList_112_1");
            editor.remove("PrefsHomeTagFeedList_113_1");
            editor.remove("PrefsHomeTagFeedList_114_1");
            //女
            editor.remove("PrefsHomeTagFeedList_0_2");
            editor.remove("PrefsHomeTagFeedList_107_2");
            editor.remove("PrefsHomeTagFeedList_112_2");
            editor.remove("PrefsHomeTagFeedList_113_2");
            editor.remove("PrefsHomeTagFeedList_114_2");
            editor.remove("publish_recommend_list");
            //我的粉丝关注列表
            editor.remove("PrefsFansInterestList_1" + accountId);
            editor.remove("PrefsFansInterestList_0" + accountId);
            //MediaScanner
            editor.remove("PrefsLocalVideoInfo");
            //老首页
            editor.remove("sp_cache_personal_models");
            //老的不用的SP
            editor.remove("PrefsVoiceConnectCountDown");
            editor.remove("PrefsHelpNotifiSetting");
            editor.remove("PrefsCreateRoomTagId");
            editor.remove("PrefsHomeDailySpecial");
            editor.remove("PrefsHomeAnchorRank");
            editor.remove("PrefsHomeTopics");
            editor.remove("PrefsNovelLeaderBoardList");
            editor.remove("PrefsFindHotGroupData");
            editor.remove("PrefsFindHotGroupTitleData");
            editor.remove("PrefsNovelPayGuide");
            editor.remove("PrefsDiscoveryList");
            editor.remove("PrefsGiftPendantList");
            editor.remove("PrefsGiftPanelTopTabList");
            editor.remove("PrefsReGiftPendantUserList");
            editor.remove("PrefsReGiftPendantList");
            editor.remove("PrefsSdkLoginSuccess");
            editor.remove("PrefsCategoryItem");
            editor.remove("PrefsLiveRoomeGuideKey");
            editor.remove("PrefsPersonalCenterGuideKey");
            editor.remove("PrefsHomeGuideKey");
            //删除标识
            editor.putBoolean("hasDeletedDeprecatedParams", true);
            editor.apply();
        }
    }
}
