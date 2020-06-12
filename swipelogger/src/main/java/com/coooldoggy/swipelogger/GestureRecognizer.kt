package com.coooldoggy.swipelogger

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

abstract class GestureRecognizer (context: Context): OnGestureRecognizerStateChangeListener{

    private val alStateListeners = Collections.synchronizedList(ArrayList<OnGestureRecognizerStateChangeListener>())
    private var beginEvent: Boolean = false
    private val context: WeakReference<Context> = WeakReference(context)

    protected val currentLocation = PointF()
    protected val prevLocation = PointF()
    protected var downTime: Long = 0L
    protected var prevdownTime: Long = 0L
    protected val gestureRecognizerHandler: GestureHandler(Looper.getMainLooper())

    override fun onStateChanged(recognizer: GestureRecognizer) {
        TODO("Not yet implemented")
    }

    protected inner class GestureHandler(mainLooper: Looper) : Handler(mainLooper){
        override fun handleMessage(msg: Message) {
            this@GestureRecognizer.handelMessage(msg)
        }
    }

    protected abstract fun handelMessage(msg: Message)

}