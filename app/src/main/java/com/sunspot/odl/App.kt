package com.sunspot.odl

import android.app.Application
import com.sunspot.libext.AppContext
import com.sunspot.libext.BuildConfig
import com.sunspot.log.DLog
import com.sunspot.log.config.DLogConfig

/**
 * -------------------------------------
 * 作者：vitta
 * -------------------------------------
 * 时间：2025/12/19 20:19
 * -------------------------------------
 * 描述：
 * -------------------------------------
 * 备注：
 * -------------------------------------
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext.instance.initContext(this)
        DLogConfig.getInstance().setMainBroadcast(true)
        DLog.config(BuildConfig.DEBUG)
    }

}