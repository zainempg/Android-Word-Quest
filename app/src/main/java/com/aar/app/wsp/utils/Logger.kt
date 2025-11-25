package com.aar.app.wsp.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
object Logger {
    fun debug(tag: String, message: String, throwable: Throwable? = null) {
        Log.d(tag, message, throwable)
        logToCrashTics(tag, message, throwable)
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        Log.d(tag, message, throwable)
        logToCrashTics(tag, message, throwable)

        if (throwable != null) {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }

    private fun logToCrashTics(tag: String, message: String, throwable: Throwable?) {
        FirebaseCrashlytics.getInstance().log(tag + ": $message: ${throwable?.message} : ${throwable?.cause}")
    }

    fun info(tag: String, message: String) {
        Log.i(tag,message)
        FirebaseCrashlytics.getInstance().log("$tag:$message")

    }
}