package com.sunspot.log;

import android.text.TextUtils;
import android.util.Log;

import com.sunspot.libext.AppContext;
import com.sunspot.log.config.DLogConfig;
import com.sunspot.log.config.DLogTag;
import com.sunspot.log.config.IDLogger;

import java.io.File;

/**
 * 日志记录类
 * 日志级别从低往高: DLog.v() < DLog.d() < DLog.i() < DLog.w() < DLog.e()
 * 1、程序调试阶段的log以及不需要在正式发版后打印到文件的使用级别DLog.v()或DLog.d()
 * 2、确认是重要日志信息，需要在正式发版后抓取的使用级别DLog.i()
 * 3、DLog.w()和DLog.e()为程序执行的警告及错误信息，可以带上Throwable,以便于后续的跟踪定位
 * <p>
 * author: Jarry.Li
 * date: 16/2/22 下午3:18
 */
public class DLog {

    private static IDLogger logger = null;

    public static void logHttp(String message) {
        log(DLogTag.TAG_HTTP, message, null);
    }

    public static void logHttp(String message, Throwable t) {
        log(DLogTag.TAG_HTTP, message, t);
    }

    public static void logRequestTime(String message) {
        log(DLogTag.TAG_REQUEST_TIME, message, null);
    }

    public static void logRequestTime(String message, Throwable t) {
        log(DLogTag.TAG_REQUEST_TIME, message, t);
    }

    public static void logTcp(String message) {
        log(DLogTag.TAG_TCP, message, null);
    }

    public static void logTcp(String message, Throwable t) {
        log(DLogTag.TAG_TCP, message, t);
    }

    public static void logCalling(String message) {
        log(DLogTag.TAG_CALLING, message, null);
    }

    public static void logCalling(String message, Throwable t) {
        log(DLogTag.TAG_CALLING, message, t);
    }

    public static void logEngine(String message) {
        log(DLogTag.TAG_ENGINE, message, null);
    }

    public static void logEngine(String tag, String message) {
        log(DLogTag.TAG_ENGINE, tag + ": " + message, null);
    }

    public static void logEngineVolume(String message) {
        log(DLogTag.TAG_ENGINE_VOLUME, message, null);
    }

    public static void logCommonWithKey(String key, String tag, String message) {
        logCommon(String.format("%s_%s", key, tag), message);
    }

    /**
     * 记录引擎回调输出的trace
     */
    public static void logCallingTrace(String message) {
        log(DLogTag.TAG_CALLING_TRACE, message, null);
    }

    public static void logImMessages(String message) {
        log(DLogTag.TAG_IM_MESSAGE, message, null);
    }

    public static void logImMessages(String tag, String message) {
        log(DLogTag.TAG_IM_MESSAGE, tag + ": " + message, null);
    }

    public static void logImMessages(String message, Throwable t) {
        log(DLogTag.TAG_IM_MESSAGE, message, t);
    }

    public static void logContact(String message) {
        log(DLogTag.TAG_CONTACT, message, null);
    }

    public static void logContact(String message, Throwable t) {
        log(DLogTag.TAG_CONTACT, message, t);
    }

    public static void logReport(String message) {
        log(DLogTag.TAG_REPORT, message, null);
    }

    public static void logReport(String message, Throwable t) {
        log(DLogTag.TAG_REPORT, message, t);
    }

    public static void logCrash(String message, Throwable t) {
        log(DLogTag.TAG_CRASH, message, t);
    }

    public static void logCommon(String tag, String message) {
        log(DLogTag.TAG_COMMON, tag + ": " + message, null);
    }

    public static void logCommon(String message) {
        log(DLogTag.TAG_COMMON, message, null);
    }

    public static void logCommon(String tag, String message, Throwable t) {
        log(DLogTag.TAG_COMMON, tag + ": " + message, t);
    }

    public static void logGdx(String tag, String message) {
        log(DLogTag.TAG_GDX, tag + ": " + message, null);
    }

    public static void logImageLoad(String tag, String message) {
        log(DLogTag.TAG_IMAGE_LOAD_TIME, tag + ": " + message, null);
    }

    public static void logImageLoad(String tag, String message, Throwable t) {
        log(DLogTag.TAG_IMAGE_LOAD_TIME, tag + ": " + message, t);
    }

    public static void logImageInfo(String tag, String message) {
        log(DLogTag.TAG_IMAGE_INFO, tag + ": " + message, null);
    }

    public static void logImageDiff(String tag, String message) {
        log(DLogTag.TAG_IMAGE_DIFF, tag + ": " + message, null);
    }

    public static void logLiveInfo(String message) {
        log(DLogTag.TAG_LIVE_INFO, message, null);
    }

    public static void logLiveInfo(String tag, String message) {
        log(DLogTag.TAG_LIVE_INFO, tag + ": " + message, null);
    }

    public static void logLiveInfo(String tag, String message, Throwable t) {
        log(DLogTag.TAG_LIVE_INFO, tag + ": " + message, t);
    }

    public static void logVideoInfo(String message) {
        log(DLogTag.TAG_VIDEO_INFO, message, null);
    }

    public static void logVideoInfo(String tag, String message) {
        log(DLogTag.TAG_VIDEO_INFO, tag + ": " + message, null);
    }

    public static void logRadioInfo(String message) {
        log(DLogTag.TAG_RADIO_INFO, message, null);
    }

    public static void logRadioInfo(String tag, String message) {
        log(DLogTag.TAG_RADIO_INFO, tag + ": " + message, null);
    }

    public static void logRadioInfo(String tag, String message, Throwable t) {
        log(DLogTag.TAG_RADIO_INFO, tag + ": " + message, t);
    }

    public static void logDownloadInfo(String message) {
        log(DLogTag.TAG_DOWNLOAD_INFO, message, null);
    }

    public static void logDownloadInfo(String tag, String message) {
        log(DLogTag.TAG_DOWNLOAD_INFO, tag + ": " + message, null);
    }

    public static void logEncryptInfo(String message) {
        log(DLogTag.TAG_ENCRYPT, message, null);
    }

    public static void logEncryptInfo(String tag, String message) {
        log(DLogTag.TAG_ENCRYPT, tag + ": " + message, null);
    }

    public static void logPCMic(String message) {
        log(DLogTag.TAG_PC, message, null);
    }

    public static void logUI(String message) {
        log(DLogTag.TAG_UI, message, null);
    }

    public static void logVisitPath(String message) {
        log(DLogTag.TAG_VISIT_PATH, message, null);
    }

    public static void logNovel(String tag, String message) {
        log(DLogTag.TAG_NOVEL, tag + ": " + message, null);
    }

    public static void logNovel(String tag, String message, Throwable throwable) {
        log(DLogTag.TAG_NOVEL, tag + ": " + message, throwable);
    }

    public static void logVideoLive(String message) {
        log(DLogTag.TAG_VIDEO_LIVE, message, null);
    }

    public static void logPlayAnimInfo(String message) {
        log(DLogTag.TAG_PLAY_ANIM_INFO, message, null);
    }

    public static void logPlayAnimInfo(String tag, String message) {
        log(DLogTag.TAG_PLAY_ANIM_INFO, tag + ": " + message, null);
    }

    public static void logPlayAnimInfo(String tag, String message, Throwable t) {
        log(DLogTag.TAG_PLAY_ANIM_INFO, tag + ": " + message, t);
    }

    public static void logOther(String message, Throwable t) {
        log(DLogTag.OTHER, message, t);
    }

    public static void logOther(String tag, String message, Throwable t) {
        log(DLogTag.OTHER, tag + ": " + message, t);
    }

    private static void log(DLogTag tag, String message, Throwable t) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        if (logger != null) {
            logger.doWrite(tag, message, t);
        }
    }

    /**
     * 日志缓存同步至文件
     */
    public static void flush() {
        if (logger != null) {
            logger.flush();
        }
    }

    private static String configLogPath = null;

    /**
     * 初始化日志的root logger
     * <p>
     * 注：子进程调用初始化需注册同步广播 {@link DLogConfig#getLoganFlushAction()} ，
     * 同时主进程初始化需调用 {@link DLogConfig#setMainBroadcast(boolean)} 支持发送广播。
     * 否则日志上传时子进程会缺失部分日志内容
     * </p>
     *
     * @param isLogcat true：logcat日志打印
     */
    public static void config(boolean isLogcat, String logPath) {
        logger = DLogConfig.getInstance().getLogger();
        logger.initConfig(isLogcat);
        Logger.isDebug = isLogcat;
        if (!TextUtils.isEmpty(logPath)) {
            configLogPath = logPath;
        }
    }

    public static void config(boolean isLogcat) {
        config(isLogcat, null);
    }

    /**
     * Dlog文件存储根路径
     *
     * <ol>
     *     <li>
     *         外部可用时：Android/data/com.uxin.live/files/AboutLive/ulog
     *     </li>
     *     <li>
     *         外部不可用时：data/data/com.uxin.live/files/AboutLive/ulog
     *     </li>
     * </ol>
     */
    public static String getLogDirPath() {
        String logDirName = configLogPath;
        if (TextUtils.isEmpty(configLogPath)) {
            logDirName = DLogConfig.LOG_ROOT_PATH_NAME;
        }
        File file = new File(AppContext.getInstance().context.getFilesDir(), logDirName);
        mkdirs(file);
        return file.getAbsolutePath();
    }

    /**
     * 日志文件父目录
     */
    public static String getLogParentPath() {
        if (logger != null) {
            return logger.getFilePath();
        }
        return "";
    }

    private static boolean mkdirs(File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        boolean mk = file.mkdirs();
        if (!mk) {
            Log.e("DLog", "mkdirs ：" + file.getAbsolutePath() + "， perform is failed!!");
        }
        return mk;
    }
}
