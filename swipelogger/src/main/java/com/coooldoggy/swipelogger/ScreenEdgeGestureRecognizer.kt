package com.coooldoggy.swipelogger

import android.content.Context
import android.graphics.PointF
import android.os.Message
import android.view.VelocityTracker
import android.view.ViewConfiguration
import com.coooldoggy.swipelogger.common.ContinuousRecognizer

open class ScreenEdgeGestureRecognizer(context: Context, direction: RectEdge): GestureRecognizer(context),
    ContinuousRecognizer {

    var minNumTouch: Int = 1
    var maxNumTouch: Int = Int.MAX_VALUE

    val minimumFlingVelocity: Int
    private val maximumFlingVelocity: Int

    var scrollX: Float = 0.toFloat()
    private set

    var scrollY: Float = 0.toFloat()
    private set

    var translationX: Float = 0.toFloat()
    private set

    var translationY: Float = 0.toFloat()
    private set

    var velocityX: Float = 0.toFloat()
        private set

    var velocityY: Float = 0.toFloat()
    private set

    val relativeScrollX: Float get() = -scrollX
    val relativeScrollY: Float get() = -scrollY

    //TODO("Left 이외의 것들에 대한 함수 적용 필요")
    var edge = RectEdge.LEFT

    var scaledTouchSlop: Int
    var edgeLimit: Float

    private var started: Boolean = false
    private var down: Boolean = false
    private var velocityTracker: VelocityTracker? = null
    private var lastFocusLocation = PointF()
    private var downFocusLocation = PointF()

    val isFling: Boolean
    get() = state == State.Ended && (Math.abs(velocityX) > minimumFlingVelocity || Math.abs(velocityY) > minimumFlingVelocity)

    init {
        val config = ViewConfiguration.get(context)
        scaledTouchSlop = config.scaledTouchSlop
        edgeLimit = context.resources.getDimension(R.dimen.gestures_screen_edge_limit)
        minimumFlingVelocity = config.scaledMinimumFlingVelocity
        maximumFlingVelocity = config.scaledMaximumFlingVelocity

        if (logEnabled){

        }
    }

    companion object {
        private const val MESSAGE_RESET = 4
    }



    override fun handelMessage(msg: Message) {
        when(msg.what){
            MESSAGE_RESET -> handleReset()
            else->{

            }
        }
    }

    private fun handleReset() {
        started = false
        down = false
        setBeginFiringEvents(false)
        state = State.Available
    }

    override fun reset() {
        super.reset()
        handleReset()
    }

    override fun removeMessages() {
        TODO("Not yet implemented")
    }

    override fun handleMessage(msg: Message) {
        TODO("Not yet implemented")
    }

    override fun onStateChanged(recognizer: GestureRecognizer) {
        if (recognizer.state == State.Failed && state == State.Began){
            stopListenForOtherStateChanges()
            fireActionEventIfCanRecognizeSimultaneously()
        }else if (recognizer.inState(State.Began, State.Ended) && started && down && inState(State.Available, State.Began)){
            stopListenForOtherStateChanges()
            removeMessages()
            state = State.Failed
            setBeginFiringEvents(false)
            started = false
            down = false
        }
    }

    private fun fireActionEventIfCanRecognizeSimultaneously() {
        if (inState(State.Changed, State.Ended)) {
            setBeginFiringEvents(true)
            fireActionEvent()
        } else {
            if (delegate!!.shouldRecognizeSimultaneouslyWithGestureRecognizer(this)) {
                setBeginFiringEvents(true)
                fireActionEvent()
            }
        }
    }
}