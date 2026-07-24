package com.sunspot.log.config

import android.os.Process
import android.util.Log
import com.sunspot.libext.ProcessUtil
import com.sunspot.log.DLog
import kotlin.jvm.java

/**
 * Created by buluke on 2024/9/9.
 *
 * 统一配置
 */
interface IDLogger : IBaseDLog {

    companion object {
        private const val STACK_TRACE_FORMAT = "%s.%s(L:%d):"
        private const val PROCESS_FORMAT = "[%s %d-%d]"
        private const val THREAD_FORMAT = "[%s]"
        private const val DEFAULT_SPLIT = " "
        private const val STACK_TRACE_METHOD_NAME_UNKNOWN = "unknown"
    }

    fun doWrite(tag: DLogTag, message: String, t: Throwable? = null) {
        // 添加日志通用前缀内容
        val msg = "${addHeaderMessage(tag)}$message"

        if (isLogcatPrint()) {
            t?.let { Log.e(tag.tagName, msg, it) }
                ?: Log.i(tag.tagName, msg)
        }

        runCatching {
            writeLog(tag, msg, t)
        }.onFailure {
            writeThrowable(it)
        }
    }

    /**
     * 日志写入异常
     */
    fun writeThrowable(throwable: Throwable) {
        logThrowable(throwable)
    }

    /**
     * 记录日志内容
     */
    fun writeLog(tag: DLogTag, message: String, t: Throwable? = null) {
        // nothing...
    }

    /**
     * 缓存同步至文件
     */
    fun flush() {
        // nothing...
    }

    /**
     * 日志文件名前缀
     */
    fun getLogNamePrefix() = DLogConfig.LOG_NAME_PREFIX

    /**
     * 日志通用前缀信息默认实现
     */
    fun addHeaderMessage(tag: DLogTag): String {
        val resultBuilder = StringBuilder()
        // 进程信息
        getProcessInfo(tag)?.takeIf {
            it.isNotEmpty()
        }?.let {
            resultBuilder.append(it).append(DEFAULT_SPLIT)
        }

        // 线程信息
        getThreadInfo(tag)?.takeIf {
            it.isNotEmpty()
        }?.let {
            resultBuilder.append(it).append(DEFAULT_SPLIT)
        }

        // 堆栈方法信息
        getStackTraceInfo(tag)?.takeIf {
            it.isNotEmpty()
        }?.let {
            resultBuilder.append(it).append(DEFAULT_SPLIT)
        }
        return resultBuilder.toString()
    }

    /**
     * 当前进程信息
     */
    fun getProcessInfo(tag: DLogTag): String? {
        val processName = ProcessUtil.getProcessName() ?: ""
        return PROCESS_FORMAT.format(processName, Process.myPid(), Process.myUid())
    }

    /**
     * 当前线程信息
     */
    fun getThreadInfo(tag: DLogTag): String? = THREAD_FORMAT.format(Thread.currentThread().name)

    /**
     * 添加堆栈信息
     */
    fun getStackTraceInfo(tag: DLogTag): String? {
        val stackTraces = Thread.currentThread().stackTrace
        // 动态获取DLog类的堆栈索引，避免堆栈深度变更导致堆栈信息获取失败
        val logClassName = DLog::class.java.name
        val logStackIndex = stackTraces.indexOfLast {
            it?.className == logClassName
        }
        // 未找到日志索引类
        if (logStackIndex == -1) {
            return null
        }
        // DLog堆栈索引下一个必然是调用处
        val caller = stackTraces.getOrNull(logStackIndex + 1) ?: return null
        val className = caller.className ?: return null
        // 去掉文件名后缀
        val pos = className.lastIndexOf('.').takeIf {
            it >= 0 && (it + 1) < className.length
        } ?: return null
        val classBaseName = className.substring(pos + 1)
        val methodName = caller.methodName ?: STACK_TRACE_METHOD_NAME_UNKNOWN
        return STACK_TRACE_FORMAT.format(classBaseName, methodName, caller.lineNumber)
    }
}