package com.sunspot.libext

import android.app.Activity
import android.content.Intent

fun Activity.intentToActivity(activityClass: Class<*>) {
    startActivity(Intent(this, activityClass))
}