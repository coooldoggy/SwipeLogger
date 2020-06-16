package com.coooldoggy.swipelogger.common

import com.coooldoggy.swipelogger.GestureRecognizer

interface OnGestureRecognizerStateChangeListener {
    fun onStateChanged(recognizer: GestureRecognizer)
}