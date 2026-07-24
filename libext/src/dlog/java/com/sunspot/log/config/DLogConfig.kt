package com.sunspot.log.config

import android.os.Process
import android.text.TextUtils
import com.sunspot.libext.AppContext
import com.sunspot.libext.BuildConfig
import com.sunspot.libext.PackageUtils
import com.sunspot.libext.ProcessUtil
import com.sunspot.libext.R
import com.sunspot.libext.ResourceUtil
import com.sunspot.libext.SharedPreferencesUtils
import com.sunspot.log.log4j.LoggerLog4jImpl
import com.sunspot.log.logan.LoggerLoganImpl

/**
 * Created by buluke on 2024/9/9.
 *
 * 业务日志配置，支持多进程
 */
class DLogConfig private constructor() {

    companion object {
        const val LOG_TYPE_LOG4J = 1
        const val LOG_TYPE_LOGAN = 2

        /**
         * 日志路径前缀
         */
        const val LOG_NAME_PREFIX = "log"

        /**
         * 日志根路径名
         */
        const val LOG_ROOT_PATH_NAME = "u$LOG_NAME_PREFIX"

        /**
         * 播放进程的日志路径后缀
         */
        const val LOG_PATH_SUFFIX_PLAYER = "_player"

        /**
         * 字幕进程的日志路径后缀
         */
        const val LOG_PATH_SUFFIX_CAPTION = "_caption"

        /**
         * 进程名: 播放进程
         */
        private const val PROCESS_NAME_PLAYER = ":player"
        /**
         * 进程名: 字幕进程
         */
        private const val PROCESS_NAME_CAPTION= ":captionService"

        /**
         * 主进程日志类型
         */
        private const val KEY_MAIN_LOG_TYPE = "main-log-switch-type"

        /**
         * 子进程使用不同日志类型场景下使用
         */
        private const val KEY_CHILD_LOG_TYPE = "child-log-switch-type"

        /**
         * 多进程使用相同日志类型时使用
         */
        private const val KEY_CURRENT_LOG_TYPE = "current-log-switch-type"
        private const val DEFAULT_LOG_TYPE = LOG_TYPE_LOGAN

        /**
         * 日志加密 aes key
         */
        private const val KEY_LOG_AES_KEY = "log-aes-key"

        /**
         * 日志加密 aes iv
         */
        private const val KEY_LOG_AES_IV = "log-aes-iv"

        private const val LOG_KEY = "7bc97"
        private const val LOG_KEY_IV = "c2c9h8km"

        /**
         * Logan 子进程同步缓存至文件广播后缀
         */
        private const val ACTION_PROCESS_LOGAN_FLUSH_SUFFIX = ".action_process_log_flush"

        @JvmStatic
        fun getInstance() = Holder.holder
    }

    private object Holder {
        val holder = DLogConfig()
    }

    /**
     * 日志类型，默认值：[DEFAULT_LOG_TYPE]
     *
     * 使用场景：
     * 1. 日志初始化依据此参数配置
     * 2. App启动配置接口动态获取，不存在远程配置，则使用代码配置项
     */
    private var logType = DEFAULT_LOG_TYPE
    private var isLoadCache = false

    /**
     * 是否删除当天日志
     */
    private var isDeleteToday = true

    /**
     * logan日志文件大小，单位M
     */
    private var loganMaxFileSize = -1L

    /**
     * 日志文件备份切分数量，默认Logan:5、log4j:7
     */
    private var maxBackupSize = -1

    /**
     * 日志秘钥key
     */
    private var logAesKey = ""
    private var isLoadAesKeyCache = false

    /**
     * 日志秘钥iv
     */
    private var logAesIv  = ""
    private var isLoadAesIvCache = false

    /**
     * 是否发送主进程同步广播
     */
    private var isMainFlushBroadcast = false

    /**
     * 远程配置的主进程日志类型
     */
    private val remoteLogTypeCache by lazy {
        getLogTypeCacheSp(KEY_MAIN_LOG_TYPE, logType)
    }

    /**
     * 远程配置的子进程日志类型
     */
    private val childRemoteLogTypeCache by lazy {
        getLogTypeCacheSp(KEY_CHILD_LOG_TYPE, logType)
    }

    /**
     * logan 同步日志文件广播
     */
    private val loganFlushProcessAction by lazy {
        val context = AppContext.instance.context
        PackageUtils.getPackageName(context) + ACTION_PROCESS_LOGAN_FLUSH_SUFFIX
    }

    /**
     * 获取相应的 log 实现类
     *
     * 获取规则：
     * 1. 本地 sp 缓存有值
     * 2. 缓存无值获取已设置的 type 或默认值
     */
    fun getLogger(): IDLogger {
        return when (getLogTypeCache()) {
            LOG_TYPE_LOG4J -> LoggerLog4jImpl()
            else -> LoggerLoganImpl()
        }
    }

    /**
     * 获取支持的日志类型目录名称
     *
     * 1. log4j --> 日志主目录：log1
     * 3. logan --> 日志主目录：log2
     */
    fun getAllLogTypeDirName() = arrayOf(
        LoggerLog4jImpl.getLogTypeDirName(),
        LoggerLoganImpl.getLogTypeDirName()
    )

    /**
     * 配置远程sp缓存日志秘钥
     *
     * 1. 冷启生效
     */
    fun setRemoteLogAesKey(value: String?) = setLogAesCacheSp(KEY_LOG_AES_KEY, value)

    /**
     * 配置远程sp缓存日志秘钥偏移量
     *
     * 1. 冷启生效
     */
    fun setRemoteLogAesIv(value: String?) = setLogAesCacheSp(KEY_LOG_AES_IV, value)

    /**
     * 配置远程sp缓存日志类型
     *
     * 1. 冷启生效
     */
    fun setRemoteLogType(type: Int?) = setLogTypeCacheSp(KEY_MAIN_LOG_TYPE, type)

    /**
     * 配置子进程远程sp缓存日志类型
     *
     * 1. 冷启生效
     */
    fun setRemoteChildLogType(type: Int?) = setLogTypeCacheSp(KEY_CHILD_LOG_TYPE, type)

    /**
     * 获取 Logan 子进程同步缓存内容至文件的广播名
     */
    fun getLoganFlushAction() = loganFlushProcessAction

    /**
     * 日志文件类型TAG，区分进程
     */
    fun getLogProcessTag(): DLogTag {
        if (ProcessUtil.isMainProcess()) {
            return DLogTag.MAIN_PROCESS
        }
        val processName = ProcessUtil.getProcessName() ?: ""
        return when {
            processName.endsWith(PROCESS_NAME_PLAYER) -> DLogTag.PLAYER_PROCESS
            processName.endsWith(PROCESS_NAME_CAPTION) -> DLogTag.CAPTION_PROCESS
            // 未知进程日志文件名格式: log_yyy (yyy值为pid)
            else -> DLogTag.OTHER_PROCESS.apply {
                tagName = "_${Process.myPid()}"
            }
        }
    }

    fun setLogType(logType: Int): DLogConfig {
        this.logType = logType
        return this
    }

    fun setDeleteToday(isDeleteToday: Boolean): DLogConfig {
        this.isDeleteToday = isDeleteToday
        return this
    }

    fun isDeleteToday() = isDeleteToday

    /**
     * 主进程是否发送同步广播
     */
    fun setMainBroadcast(isMainFlushBroadcast: Boolean) {
        this.isMainFlushBroadcast = isMainFlushBroadcast
    }

    fun setLoganMaxFileSize(loganMaxFileSize: Long): DLogConfig {
        if (loganMaxFileSize < 0) {
            // 默认配置
            this.loganMaxFileSize = -1L
            return this
        }
        this.loganMaxFileSize = loganMaxFileSize
        return this
    }

    fun getLoganMaxFileSize() = loganMaxFileSize

    fun setMaxBackupSize(maxBackupSize: Int): DLogConfig {
        if (maxBackupSize <= 1) {
            return this
        }
        this.maxBackupSize = maxBackupSize
        return this
    }

    fun getMaxBackupSize() = maxBackupSize

    fun isMainBroadcast() = isMainFlushBroadcast

    fun getLogAesKey(): String {
        if (isLoadAesKeyCache.not()) {
            isLoadAesKeyCache = true
            getLogAesCacheSp(KEY_LOG_AES_KEY, logAesKey)?.takeIf {
                it.isNotEmpty()
            }?.let {
                logAesKey = it
            }
        }
        if (TextUtils.isEmpty(logAesKey)) {
            logAesKey = BuildConfig.LOG_KEY + ResourceUtil.getString(R.string.log_key_2) + LOG_KEY
        }
        return logAesKey
    }

    fun getLogAesIv(): String {
        if (isLoadAesIvCache.not()) {
            isLoadAesIvCache = true
            getLogAesCacheSp(KEY_LOG_AES_IV, logAesIv)?.takeIf {
                it.isNotEmpty()
            }?.let {
                logAesIv = it
            }
        }
        if (TextUtils.isEmpty(logAesIv)) {
            logAesIv = ResourceUtil.getString(R.string.log_ke_iv) + LOG_KEY_IV
        }
        return logAesIv
    }

    /**
     * 配置日志缓存类型
     * 1:log4j
     * 2:logan
     */
    private fun setLogTypeCacheSp(key: String, type: Int?) {
        val ctx = AppContext.instance.context
        type?.let {
            SharedPreferencesUtils.setParam(ctx, key, type)
        } ?: SharedPreferencesUtils.removeParms(ctx, key)
    }

    private fun getLogTypeCacheSp(key: String, defaultValue: Int = DEFAULT_LOG_TYPE) : Int? {
        val ctx = AppContext.instance.context
        return SharedPreferencesUtils.getParam(ctx, key, defaultValue) as? Int
    }

    /**
     * 配置日志秘钥缓存
     */
    private fun setLogAesCacheSp(key: String, value: String?) {
        val ctx = AppContext.instance.context
        value?.let {
            SharedPreferencesUtils.setParam(ctx, key, value)
        } ?: SharedPreferencesUtils.removeParms(ctx, key)
    }

    private fun getLogAesCacheSp(key: String, defaultValue: String) : String? {
        val ctx = AppContext.instance.context
        return SharedPreferencesUtils.getParam(ctx, key, defaultValue) as? String
    }

    /**
     * 获取缓存日志类型，仅识别已定义类型。
     *
     * 第一次加载会获取sp缓存值，未知类型获取代码配置
     */
    private fun getLogTypeCache(): Int {
        // 第一次加载初始化
        if (isLoadCache.not()) {
            isLoadCache = true

            val isMainProcess = ProcessUtil.isMainProcess()
            // 主进程获取远程配置，子进程获取当前配置
            val key = if (isMainProcess) KEY_MAIN_LOG_TYPE else KEY_CURRENT_LOG_TYPE
            getLogTypeCacheSp(key, logType)?.takeIf {
                isValidType(it)
            }?.let {
                logType = it
            }

            // 主进程首次加载配置生效中的日志类型
            if (isMainProcess) {
                // 缓存子进程配置
                setLogTypeCacheSp(
                    KEY_CURRENT_LOG_TYPE,
                    childRemoteLogTypeCache?.takeIf { isValidType(it) } ?: logType
                )
            }
        }
        return logType
    }

    /**
     * 支持的日志类型
     */
    private fun isValidType(type: Int) = type == LOG_TYPE_LOG4J || type == LOG_TYPE_LOGAN
}