package com.sunspot.log.log4j

import android.text.TextUtils
import com.sunspot.log.DLog
import com.sunspot.log.config.DLogConfig
import com.sunspot.log.config.DLogTag
import com.sunspot.log.config.IDLogger
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.io.File

/**
 * Created by buluke on 2024/9/10.
 */
class LoggerLog4jImpl : IDLogger {

    companion object {
        private const val TAG = "LoggerLog4jImpl"
        private const val LOG_FILE_SUFFIX = ".log"
        private const val DEFAULT_LOG_FILE_NAME = "uxin-record$LOG_FILE_SUFFIX"

        /**
         * 日志类型主目录
         */
        fun getLogTypeDirName() = DLogConfig.LOG_NAME_PREFIX + DLogConfig.LOG_TYPE_LOG4J
    }

    /**
     * 当前日志文件路径
     */
    private var filePath = ""

    /**
     * 日志文件类型，区分进程
     */
    private var processTag = DLogTag.MAIN_PROCESS
    private var isLogcat = false
    private var sLogConfigurator: LogConfigurator? = null

    /**
     * 日志类型主目录
     */
    private val logDirPath by lazy {
        DLog.getLogDirPath() + File.separator + getLogTypeDirName()
    }

    override fun initConfig(isLogcat: Boolean) {
        this.isLogcat = isLogcat
        processTag = DLogConfig.getInstance().getLogProcessTag()
        val defaultLog = File(getLogPath(), DEFAULT_LOG_FILE_NAME)
        val pattern = LogConfigurator.LOG_FILE_PATTERN_MULTI
        val maxBackup = DLogConfig.getInstance().getMaxBackupSize()
        sLogConfigurator = LogConfigurator(defaultLog.absolutePath, getLevel(), pattern).apply {
            //清除掉旧配置
            isResetConfiguration = true
            isUseRootLogger = false
            // logcat日志已在基类统一处理，固不使用具体实现的本地日志，避免多次打印log
            isUseLogCatAppender = false
            isUseFileAppender = true
            maxBackupSize = if (maxBackup > 0) maxBackup else 7
            configure()
        }
    }

    override fun isLogcatPrint() = isLogcat

    override fun getFilePath() = filePath

    override fun writeLog(tag: DLogTag, message: String, t: Throwable?) {
        if (TextUtils.isEmpty(tag.tagName)) {
            return
        }
        val level = Level.INFO
        val logger = Logger.getLogger(tag.tagName)
        val local = getLevel().toInt()
        if (local > level.toInt() || logger == null) {
            return
        }
        config(logger)
        when (level) {
            Level.TRACE -> logger.trace(message, t)
            Level.DEBUG -> logger.debug(message, t)
            Level.INFO -> logger.info(message, t)
            Level.WARN -> logger.trace(message, t)
            Level.ERROR -> logger.error(message, t)
            Level.FATAL -> logger.fatal(message, t)
            else -> {
                // nothing...
            }
        }
    }

    /**
     * log4j内部已实现，此处默认取消
     */
    override fun getThreadInfo(tag: DLogTag) = ""

    private fun getLogPath(): String {
        // 目录格式：log1/log_xxx/
        val file = File(logDirPath, getLogNamePrefix() + processTag.tagName)
        mkdirs(file)
        return file.absolutePath
    }

    /**
     * 日志分级开关
     * @return
     */
    private fun getLevel() = if (isLogcat) {
        Level.DEBUG
    } else {
        Level.INFO
    }

    /**
     * 写入日志时动态配置，支持多进程多文件存储
     */
    private fun config(logger: Logger) {
        runCatching {
            val tag = logger.name
            val appender = logger.getAppender(tag)
            sLogConfigurator?.takeIf { appender == null }?.apply {
                val defaultLog = File(getLogPath(), "$tag$LOG_FILE_SUFFIX")
                val path = defaultLog.absolutePath
                this@LoggerLog4jImpl.filePath = path
                fileName = path
                isResetConfiguration = false
                configure(logger)
            }
        }.onFailure {
            logThrowable(it)
        }
    }

    private fun mkdirs(file: File?): Boolean {
        if (file == null) {
            return false
        }
        if (file.exists()) {
            return true
        }
        val mk = file.mkdirs()
        if (!mk) {
            DLog.logCommon(TAG, "mkdirs ：" + file.absolutePath + "， perform is failed!!")
        }
        return mk
    }
}