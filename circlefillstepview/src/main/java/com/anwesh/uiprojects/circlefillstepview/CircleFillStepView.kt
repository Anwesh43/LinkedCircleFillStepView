package com.anwesh.uiprojects.circlefillstepview

/**
 * Created by anweshmishra on 14/10/18.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.graphics.Path
import android.content.Context

val nodes : Int = 5

fun Canvas.drawCFSNode(i : Int, scale : Float, paint : Paint) {
    paint.color = Color.parseColor("#283593")
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val r : Float = gap / 3
    save()
    translate(gap + i * gap, h/2)
    val path : Path = Path()
    path.addCircle(0f, 0f, r, Path.Direction.CCW)
    clipPath(path)
    for (j in 0..1) {
        val sc : Float = Math.min(0.5f, Math.max(scale - 0.5f * j, 0f)) * 2
        val sf : Float = 1f - 2 * j
        save()
        scale(1f, sf)
        drawRect(RectF(-r, 0f, r, r * sc), paint)
        restore()
    }
    restore()
}

class CircleFillStepView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.05f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CFSNode(var i : Int, val state : State = State()) {

        private var next : CFSNode? = null
        private var prev : CFSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = CFSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCFSNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CFSNode {
            var curr : CFSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class CircleFillStep(var i : Int) {

        private var curr : CFSNode = CFSNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : CircleFillStepView) {
        val animator : Animator = Animator(view)
        val cfs : CircleFillStep = CircleFillStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            cfs.draw(canvas, paint)
            animator.animate {
                cfs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            cfs.startUpdating {
                animator.start()
            }
        }
    }
}