package com.coooldoggy.swipelogger

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.Lifecycle
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

abstract class GestureRecognizer (context: Context): OnGestureRecognizerStateChangeListener{

    private val alStateListeners = Collections.synchronizedList(ArrayList<OnGestureRecognizerStateChangeListener>())
    private var beginEvent: Boolean = false
    private val context: WeakReference<Context> = WeakReference(context)

    protected val currentLocation = PointF()
    protected val prevLocation = PointF()
    protected val downLocation = PointF()
    protected var downTime: Long = 0L
    protected var prevdownTime: Long = 0L
    protected val gestureRecognizerHandler: GestureHandler = GestureHandler(Looper.getMainLooper())

    var actionListener: ((GestureRecognizer) -> Any?)? = null
    var stateChangeListener : ((GestureRecognizer, State? ,State?) -> Unit)? = null

    val downLocationX: Float
    get() = downLocation.x

    val downLocationY: Float
    get() = downLocation.y

    val currentLocationX: Float
    get() = currentLocation.x

    val currentLocationY: Float
    get() = currentLocation.y


    enum class State {
        Available,
        Began,
        Changed,
        Failed,
        Cancelled,
        Ended
    }

    override fun onStateChanged(recognizer: GestureRecognizer) {
        TODO("Not yet implemented")
    }

    protected inner class GestureHandler(mainLooper: Looper) : Handler(mainLooper){
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

        if (changed){
            stateChangeListener?.invoke(this, oldVal, value)
            val iterator = alStateListeners.listIterator()
            while (iterator.hasNext()){
                iterator.next().onStateChanged(this)
            }
        }
    }

    var isEnabled: Boolean = true
    set(value) {
        if (field != value){
            field = value
            if (!value){
                reset()
            }
        }
    }


    open fun reset(){
        state = null
    }

}