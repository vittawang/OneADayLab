package com.sunspot.log.logan

import android.content.Intent
import android.text.TextUtils
import com.dianping.logan.Logan
import com.dianping.logan.LoganConfig
import com.sunspot.libext.AppContext
import com.sunspot.libext.ProcessUtil
import com.sunspot.log.DLog
import com.sunspot.log.config.DLogConfig
import com.sunspot.log.config.DLogTag
import com.sunspot.log.config.IDLogger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class LoggerLoganImpl : IDLogger {

    companion object {
        private const val LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
        private const val LOG_MESSAGE_FORMAT = "%s --> %s %s"
        private const val LOG_MAX_FILE_SIZE = 10L
        private const val LOG_MAX_BACKUP_SIZE = 5

        /**
         * 日志类型主目录名称
         */
        fun getLogTypeDirName() = DLogConfig.LOG_NAME_PREFIX + DLogConfig.LOG_TYPE_LOGAN
    }

    private var isLogcat = false

    /**
     * 当前日志文件路径
     */
    private var filePath = ""

    /**
     * 日志文件类型，区分进程
     */
    private var processTag = DLogTag.MAIN_PROCESS

    /**
     * 缓存文件通用路径
     */
    private val logCachePathPrefix by lazy {
        getLogCacheDirPath() + File.separator + getLogTypeDirName() + File.separator + getLogNamePrefix()
    }

    /**
     * 日志文件通用路径
     */
    private val logFilePathPrefix by lazy {
        DLog.getLogDirPath() + File.separator + getLogTypeDirName() + File.separator + getLogNamePrefix()
    }

    override fun initConfig(isLogcat: Boolean) {
        this.isLogcat = isLogcat
        processTag = DLogConfig.getInstance().getLogProcessTag()

        // 多进程后缀目录格式： log_xxx
        val cachePath = logCachePathPrefix + processTag.tagName
        val filePath = logFilePathPrefix + processTag.tagName
        this@LoggerLoganImpl.filePath = filePath

        val maxFileSize = DLogConfig.getInstance().getLoganMaxFileSize()
            .takeIf { it > 0 } ?: LOG_MAX_FILE_SIZE
        val maxBackupSize = DLogConfig.getInstance().getMaxBackupSize()
            .takeIf { it > 0 } ?: LOG_MAX_BACKUP_SIZE
        val config = LoganConfig.Builder()
            .setCachePath(cachePath)
            .setPath(filePath)
            .setMaxFile(maxFileSize)
            .setMaxBackupSize(maxBackupSize)
            .setEncryptKey16(DLogConfig.getInstance().getLogAesKey().toByteArray())
            .setEncryptIV16(DLogConfig.getInstance().getLogAesIv().toByteArray())
            .build()
        Logan.setDebug(isLogcat)
        Logan.init(config)
    }

    override fun getProcessInfo(tag: DLogTag): String? {
        return if (filterPrintInfo(tag)) "" else super.getProcessInfo(tag)
    }

    override fun getThreadInfo(tag: DLogTag): String? {
        return if (filterPrintInfo(tag)) "" else super.getThreadInfo(tag)
    }

    override fun getStackTraceInfo(tag: DLogTag): String? {
        return if (filterPrintInfo(tag)) "" else super.getStackTraceInfo(tag)
    }

    /**
     * 指定类型过滤进程、线程、堆栈信息
     */
    private fun filterPrintInfo(tag: DLogTag) = when (tag) {
        DLogTag.TAG_REQUEST_TIME,
        DLogTag.TAG_HTTP,
        DLogTag.TAG_VISIT_PATH -> true

        else -> false
    }

    override fun isLogcatPrint() = isLogcat

    override fun getFilePath() = filePath

    override fun writeLog(tag: DLogTag, message: String, t: Throwable?) {
        val timeMillis = getCurrentTimeStamp()
        val date = getTimeStampInFormat(timeMillis, LOG_DATE_FORMAT, null) ?: timeMillis
        val traceInfo = getThrowableTrace(t)
        Logan.w(
            LOG_MESSAGE_FORMAT.format(date, message, traceInfo),
            tag.getType(processTag.tagType)
        )
    }

    /**
     * 解析获取异常堆栈信息
     */
    private fun getThrowableTrace(t: Throwable?): String {
        t ?: return ""
        return runCatching {
            // 将堆栈跟踪信息捕获到字符串
            val bos = ByteArrayOutputStream()
            t.printStackTrace(PrintStream(bos))
            bos.toString()
        }.getOrDefault("")
    }

    override fun flush() {
        runCatching {
            // 1.同步当前进程日志
            Logan.f()

            // 2.主进程发送广播通知子进程同步日志文件
            if (ProcessUtil.isMainProcess()
                && DLogConfig.getInstance().isMainBroadcast()
            ) {
                val context = AppContext.instance.context
                val intent = Intent(DLogConfig.getInstance().getLoganFlushAction())
                context.sendBroadcast(intent)
            }
        }.onFailure {
            logThrowable(it)
        }
    }

    /**
     * 缓存文件存储路径
     *
     * 内部存储：data/data/$pageName/cache/ulog/
     */
    private fun getLogCacheDirPath(): String {
        val logDirName = DLogConfig.LOG_ROOT_PATH_NAME
        val file = File(AppContext.instance.context.cacheDir, logDirName)
        return file.absolutePath
    }

    private fun getCurrentTimeStamp(): Long {
        val calendar = Calendar.getInstance()
        // calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis()
    }

    fun getTimeStampInFormat(time: Long, format: String, timeZoneId: String?): String? {
        try {
            val formatter = SimpleDateFormat(format)
            if (!TextUtils.isEmpty(timeZoneId)) {
                formatter.setTimeZone(TimeZone.getTimeZone(timeZoneId))
            }
            val date = Date(time)
            val timeStamp = formatter.format(date)
            return timeStamp
        } catch (e: Exception) {
        }
        return null
    }
}