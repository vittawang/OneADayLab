package com.sunspot.log.config

/**
 * Created by buluke on 2024/9/9.
 *
 * 日志TAG类型，详情文档：[https://wiki.uxin001.com/pages/viewpage.action?pageId=681723039]
 *
 * 定义要求：
 * 单类型间隔100，预留100位子类型占位
 */
enum class DLogTag(
    var tagName: String,
    val tagType: Int
) {

    /**
     * 主进程
     *
     * name: 日志路径后缀
     * type: 进程日志取值范围
     */
    MAIN_PROCESS("_main", 10000),

    /**
     * 播放进程
     */
    PLAYER_PROCESS("_player", 20000),

    /**
     * 字幕进程
     */
    CAPTION_PROCESS("_caption", 30000),

    /**
     * 其他进程
     */
    OTHER_PROCESS("_other", 90000),


    /**
     * 通用日志
     */
    TAG_COMMON("CommonInfo", 1000),

    /**
     * 页面访问
     */
    TAG_VISIT_PATH("VisitPath", 1100),

    /**
     * 异常日志
     */
    TAG_CRASH("CrashHandler", 1200),

    /**
     * IM消息
     */
    TAG_IM_MESSAGE("IMMessageInfo", 1300),

    /**
     * 直播相关
     */
    TAG_LIVE_INFO("LiveInfo", 1400),

    /**
     * 直播回调相关
     */
    TAG_CALLING("LiveCalling", 1500),

    /**
     * 引擎回调输出的trace
     */
    TAG_CALLING_TRACE("LiveCallingTrace", 1501),


    /**
     * 直播引擎相关
     */
    TAG_ENGINE("LiveEngine", 1600),

    /**
     * 直播引擎声音相关
     *
     */
    TAG_ENGINE_VOLUME("LiveEngineVolume", 1601),

    TAG_CONTACT("LiveContact", 1700),

    TAG_REPORT("LiveReport", 1800),

    /**
     * 直播资源
     */
    TAG_GDX("LiveGdx", 1900),

    /**
     * 声网视频直播相关
     */
    TAG_VIDEO_LIVE("WBVideoLive", 2000),

    /**
     * 广播剧相关
     */
    TAG_RADIO_INFO("RadioInfo", 2100),

    /**
     * 视频相关
     */
    TAG_VIDEO_INFO("VideoInfo", 2200),

    /**
     * 播放动效相关日志
     */
    TAG_PLAY_ANIM_INFO("PlayAnimInfo", 2300),

    /**
     * 下载相关日志
     */
    TAG_DOWNLOAD_INFO("DownloadInfo", 2400),

    /**
     * 加解密相关
     */
    TAG_ENCRYPT("Encrypt", 2401),

    /**
     * 网络请求
     */
    TAG_HTTP("HttpRequest", 2500),

    /**
     * 网络请求耗时相关
     */
    TAG_REQUEST_TIME("HttpRequestTime", 2501),


    /**
     * tcp网络请求相关
     */
    TAG_TCP("TcpRequest", 2600),

    /**
     * 图片信息相关
     */
    TAG_IMAGE_INFO("ImageInfo", 2700),

    /**
     * 网络图片加载时间
     */
    TAG_IMAGE_LOAD_TIME("ImageLoad", 2701),

    /**
     * 网络图片对比
     */
    TAG_IMAGE_DIFF("ImageDiff", 2702),

    /**
     * ui相关
     */
    TAG_UI("UI", 2800),

    /**
     * pc麦克风回调相关
     */
    TAG_PC("PC_MIC", 2900),

    /**
     * 有读相关
     */
    TAG_NOVEL("Novel", 3000),

    /**
     * 其他（日志预留位）
     */
    OTHER("other", 3100);

    /**
     * 进程type
     */
    fun getType(processType: Int): Int = processType + tagType
}