package com.coooldoggy.swipelogger

import android.util.Log

class LogUtil {
    private val TAG = LogUtil::class.java.simpleName

    companion object{
        const val LOG_LEVEL_V = 0
        const val LOG_LEVEL_D = 1
        const val LOG_LEVEL_I = 2
        const val LOG_LEVEL_W = 3
        const val LOG_LEVEL_E = 4
        const val LOG_LEVEL_A = 5
        const val LOG_LEVEL_N = 6
    }

    var LOG_LEVEL = LOG_LEVEL_V

    constructor(level: Int){
        LOG_LEVEL = level
    }






}