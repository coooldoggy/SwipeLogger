package com.coooldoggy.swipelogger

import android.annotation.TargetApi
import android.content.Context
import android.graphics.PointF
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.coooldoggy.swipelogger.common.OnGestureRecognizerStateChangeListener
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

abstract class GestureRecognizer(context: Context) :
    OnGestureRecognizerStateChangeListener {

    private val alStateListeners =
        Collections.synchronizedList(ArrayList<OnGestureRecognizerStateChangeListener>())
    private var beginEvent: Boolean = false
    private val contextRef: WeakReference<Context> = WeakReference(context)

    protected val currentLocation = PointF()
    protected val prevDownLocation = PointF()
    protected val downLocation = PointF()
    protected var downTime: Long = 0L
    protected var prevdownTime: Long = 0L
    private var numberOfTouches: Int = 0
    protected val gestureRecognizerHandler: GestureHandler = GestureHandler(Looper.getMainLooper())
    var actionListener: ((GestureRecognizer) -> Any?)? = null

    var stateChangeListener: ((GestureRecognizer, State?, State?) -> Unit)? = null

    internal var delegate: GestureRecongnizerDelegate? = null

    val downLocationX: Float
        get() = downLocation.x

    val downLocationY: Float
        get() = downLocation.y

    val currentLocationX: Float
        get() = currentLocation.x

    val currentLocationY: Float
        get() = currentLocation.y

    var cancleTouchInView: Boolean = true

    var tag: Any? = null
    var id: Long = generateId()
        protected set

    var requireFailureOf: GestureRecognizer? = null
        set(other) {
            field?.removeOnStateChangeListenerListener(this)
            field = other
        }

    protected val isListeningForOtherStateChanges: Boolean
        get() = null != requireFailureOf && requireFailureOf!!.hasOnStateChangeListenerListener(
            this
        )

    private fun generateId(): Long {
        return id++
    }

    enum class State {
        Available,
        Began,
        Changed,
        Failed,
        Cancelled,
        Ended
    }

    protected inner class GestureHandler(mainLooper: Looper) : Handler(mainLooper) {
        override fun handleMessage(msg: Message) {
            this@GestureRecognizer.handelMessage(msg)
        }
    }

    protected abstract fun handelMessage(msg: Message)

    var state: State? = null
        protected set(value) {
            val oldVal = field
            val changed = this.state != value || value == State.Changed
            field = value

            if (changed) {
                stateChangeListener?.invoke(this, oldVal, value)
                val iterator = alStateListeners.listIterator()
                while (iterator.hasNext()) {
                    iterator.next().onStateChanged(this)
                }
            }
        }

    var isEnabled: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                if (!value) {
                    reset()
                }
            }
        }

    var lastEvent: MotionEvent? = null
        protected set(mLastEvent) {
            mLastEvent?.recycle()
            field = mLastEvent
        }

    val context: Context? get() = contextRef.get()


    open fun reset() {
        state = null
        stopListenForOtherStateChanges()
        setBeginFiringEvents(false)
        removeMessages()
    }

    protected abstract fun removeMessages()

    protected fun removeMessages(vararg messages: Int) {
        for (message in messages) {
            gestureRecognizerHandler.removeMessages(message)
        }
    }

    protected fun hasMessages(vararg messages: Int): Boolean {
        for (message in messages) {
            if (gestureRecognizerHandler.hasMessages(message)) {
                return true
            }
        }
        return false
    }

    internal fun clearStateListeners() {
        alStateListeners.clear()
    }

    protected fun fireActionEvent() {
        actionListener?.invoke(this)
    }

    open fun hasBeganFiringEvents(): Boolean {
        return beginEvent
    }

    protected fun setBeginFiringEvents(value: Boolean) {
        beginEvent = value
    }

    protected fun addOnStateChangeListenerListener(listener: OnGestureRecognizerStateChangeListener) {
        if (!alStateListeners.contains(listener)) {
            alStateListeners.add(listener)
        }
    }

    protected fun stopListenForOtherStateChanges() {
        requireFailureOf?.removeOnStateChangeListenerListener(this)
    }

    protected fun removeOnStateChangeListenerListener(listener: OnGestureRecognizerStateChangeListener): Boolean {
        return alStateListeners.remove(listener)
    }

    protected fun hasOnStateChangeListenerListener(listener: OnGestureRecognizerStateChangeListener): Boolean {
        return alStateListeners.contains(listener)
    }

    open fun onTouchEvent(event: MotionEvent): Boolean {
        lastEvent = MotionEvent.obtain(event)

        // action down
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            prevDownLocation.set(downLocation)
            downLocation.set(event.x, event.y)
            prevdownTime = downTime
            downTime = event.downTime
        }

        // compute current location
        numberOfTouches = computeFocusPoint(event, currentLocation)
        return false
    }

    protected fun computeFocusPoint(event: MotionEvent, out: PointF): Int {
        val actionMasked = event.actionMasked
        val count = event.pointerCount
        val pointerUp = actionMasked == MotionEvent.ACTION_POINTER_UP
        val skipIndex = if (pointerUp) event.actionIndex else -1
        // Determine focal point
        var sumX = 0f
        var sumY = 0f
        for (i in 0 until count) {
            if (skipIndex == i) {
                continue
            }
            sumX += event.getX(i)
            sumY += event.getY(i)
        }

        val div = if (pointerUp) count - 1 else count
        out.x = sumX / div
        out.y = sumY / div
        return if (pointerUp) count - 1 else count
    }

    @Suppress("unused")
    @Throws(Throwable::class)
    protected fun finalize() {
    }

    protected abstract fun handleMessage(msg: Message)

    fun inState(vararg states: State): Boolean {
        return states.contains(state)
    }

    protected fun listenForOtherStateChanges() {
        requireFailureOf?.addOnStateChangeListenerListener(this)
    }

    override fun toString(): String {
        return javaClass.simpleName + "[state: " + state + ", tag:" + tag + "], touches: $numberOfTouches"
    }

    protected fun logMessage(level: Int, fmt: String) {
        if (!sDebug) {
            return
        }
        Log.println(level, LOG_TAG, "[${javaClass.simpleName}:$tag] $fmt")
    }

    @Suppress("unused")
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    companion object {

        const val VERSION = BuildConfig.VERSION_NAME

        val LOG_TAG: String = GestureRecognizer::class.java.simpleName

        /**
         * @return the instance id
         * @since 1.0.0
         */
        var id = 0
            private set

        protected var sDebug = false

        const val TIMEOUT_DELAY_MILLIS = 5
        val LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout().toLong()
        val TAP_TIMEOUT = ViewConfiguration.getTapTimeout().toLong()
        val DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout().toLong()
        const val TOUCH_SLOP = 8
        const val DOUBLE_TAP_SLOP = 100
        const val DOUBLE_TAP_TOUCH_SLOP = TOUCH_SLOP

        var logEnabled: Boolean
            get() = sDebug
            set(value) {
                sDebug = value
            }

        fun eventActionToString(action: Int): String {
            return when (action) {
                MotionEvent.ACTION_DOWN -> "ACTION_DOWN"
                MotionEvent.ACTION_UP -> "ACTION_UP"
                MotionEvent.ACTION_CANCEL -> "ACTION_CANCEL"
                MotionEvent.ACTION_MOVE -> "ACTION_MOVE"
                MotionEvent.ACTION_POINTER_DOWN -> "ACTION_POINTER_DOWN"
                MotionEvent.ACTION_POINTER_UP -> "ACTION_POINTER_UP"
                else -> "ACTION_OTHER"
            }
        }
    }
}