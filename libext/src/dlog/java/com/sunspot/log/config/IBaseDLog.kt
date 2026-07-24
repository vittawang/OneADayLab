package com.sunspot.log.config

interface IBaseDLog {

    /**
     * 日志初始化
     *
     * @param isLogcat logcat 打印日志
     */
    fun initConfig(isLogcat: Boolean)

    /**
     * logcat 是否打印
     */
    fun isLogcatPrint() = false

    /**
     * 获取当前日志文件路径
     */
    fun getFilePath(): String

    /**
     * logcat 日志输出
     */
    fun logThrowable(throwable: Throwable?) {
        throwable ?: return
        if (isLogcatPrint()) {
            throwable.printStackTrace()
        }
    }
}