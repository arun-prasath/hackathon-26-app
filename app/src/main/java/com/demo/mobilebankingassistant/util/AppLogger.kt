package com.demo.mobilebankingassistant.util

import android.util.Log

object AppLogger {
    private const val ROOT_TAG = "SCMobileDemo"

    fun d(area: String, message: String) {
        Log.d(tag(area), message)
    }

    fun i(area: String, message: String) {
        Log.i(tag(area), message)
    }

    fun w(area: String, message: String, throwable: Throwable? = null) {
        Log.w(tag(area), message, throwable)
    }

    fun e(area: String, message: String, throwable: Throwable? = null) {
        Log.e(tag(area), message, throwable)
    }

    private fun tag(area: String): String = "$ROOT_TAG/$area"
}
