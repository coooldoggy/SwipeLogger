package com.coooldoggy.swipelogger

import android.view.MotionEvent
import android.view.View

class GestureRecongnizerDelegate {
    private var view: View? = null

    var isEnabled = true
    set(value) {
        field = value
        set.forEach{it.isEnabled = value}
    }

    private val set = LinkedHashSet<GestureRecognizer>()
    var shouldBegin: ((recognizer: GestureRecognizer) -> Boolean) = { true }
    var shouldRecieveTouch : ((recognizer: GestureRecognizer) -> Boolean) = { true }

    var shouldRecognizeSimultaneouslyWithGestureRecognizer: (recognizer: GestureRecognizer, other: GestureRecognizer) -> Boolean = {_, _ -> true}

    fun addGestureRecongnizer(recognizer: GestureRecognizer){
        recognizer.delegate = this
        set.add(recognizer)
    }

    fun removeGestureRecognizer(recognizer: GestureRecognizer): Boolean{
        if (set.remove(recognizer)){
            recognizer.delegate = null
            recognizer.clearStateListeners()
            return true
        }
        return false
    }

    fun size() = set.size

    fun clear(){
        for (item in set){
            item.delegate  = null
            item.clearStateListeners()
        }
        set.clear()
    }

    fun onTouchEvent(view: View, event: MotionEvent): Boolean{
        if (!isEnabled) return false
        var handled = false

        for (recognizer in set){
            handled = handled or recognizer.onTouchEvent(event)
        }
        return handled
    }

    internal fun shouldRecognizeSimultaneouslyWithGestureRecognizer(recognizer: GestureRecognizer): Boolean{
        if (set.size == 1){
            return true
        }
        var result = true
        for (other in set){
            if (other != recognizer){
                if (other.hasBeganFiringEvents()){
                    result = result and (shouldRecognizeSimultaneouslyWithGestureRecognizer(recognizer, other))
                }
            }
        }
        return result
    }
}