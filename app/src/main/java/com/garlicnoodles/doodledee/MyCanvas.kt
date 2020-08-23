package com.garlicnoodles.doodledee

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import java.util.*

class MyCanvas(context: Context?, attrs: AttributeSet?) :
    View(context, attrs), View.OnTouchListener {

    companion object{
        var paints: Stack<Paint> = Stack()

        //Make array of paths
        var path: Path = Path()

        //CurrentPath



    }

    fun createNewPaint(strokeWidth : Float)
    {
        var paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.GREEN
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth


        //Add current path to the paths
        //Restart current path = Path()

        paints.push(paint)
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        //for (var path in paths)
        //canvas.drawPath(path, paint)

        canvas.drawPath(path, paints.peek())
    }

    init {
        createNewPaint( 5f )
    }





    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        val xPos = event.x
        val yPos = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(xPos, yPos)
                return true
            }
            MotionEvent.ACTION_MOVE -> path.lineTo(xPos, yPos)
            MotionEvent.ACTION_UP -> {
            }
            else -> return false
        }
        invalidate()
        return true
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }

}