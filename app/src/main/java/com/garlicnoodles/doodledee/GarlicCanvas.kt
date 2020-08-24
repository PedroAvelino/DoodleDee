package com.garlicnoodles.doodledee

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.math.max
import kotlin.math.min


class GarlicCanvas(
    context: Context?,
    attrs: AttributeSet?
) : View(context, attrs) {

    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null
    private var paintScreen: Paint? = null
    private var paintLine: Paint? = null
    private var pathMap: HashMap<Int, Path>? = null
    private var previousPointMap: HashMap<Int, Point>? = null

    private var gestureDetector : ScaleGestureDetector? = null

    fun init() {
        paintScreen = Paint()
        paintLine = Paint()
        paintLine!!.isAntiAlias = true
        paintLine!!.color = Color.BLACK
        paintLine!!.style = Paint.Style.STROKE
        paintLine!!.strokeWidth = 7f
        paintLine!!.strokeCap = Paint.Cap.ROUND
        pathMap = HashMap()
        previousPointMap = HashMap()

        gestureDetector = ScaleGestureDetector(context, ScaleListener(this))
    }

    //Inner class to scale the canvas
    private inner class ScaleListener( view : View) :
        ScaleGestureDetector.SimpleOnScaleGestureListener()
    {
        private var scaleFactor = 1.0f

        val myView = view

        override fun onScale(detector: ScaleGestureDetector?): Boolean {

            scaleFactor *= gestureDetector!!.scaleFactor

            scaleFactor = max(0.1f, min(scaleFactor, 2.0f))

            myView.scaleX = scaleFactor
            myView.scaleY = scaleFactor

            return true
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val actionIndex = event.actionIndex
        if (action == MotionEvent.ACTION_DOWN ||
            action == MotionEvent.ACTION_POINTER_UP
        ) {
            touchStarted(
                event.getX(actionIndex),
                event.y,
                event.getPointerId(actionIndex)
            )
        } else if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_POINTER_UP
        ) {
            touchEnded(event.getPointerId(actionIndex))
        } else {

            //If we have more than one finger touching the screen
            //Scale the canvas
            if(event.pointerCount > 1)
            {
                gestureDetector?.onTouchEvent(event)
            }
            else{
                //Otherwise just draw as usual
                touchMoved(event)
            }
        }
        invalidate()
        return true
    }

    private fun touchMoved(event: MotionEvent) {
        for (i in 0 until event.pointerCount) {
            val pointerId = event.getPointerId(i)
            val pointerIndex = event.findPointerIndex(pointerId)

            if (pathMap!!.containsKey(pointerId)) {
                val newX = event.getX(pointerIndex)
                val newY = event.getY(pointerIndex)
                val path = pathMap!![pointerId]
                val point = previousPointMap!![pointerId]
                val deltaX = Math.abs(newX - point!!.x)
                val deltaY = Math.abs(newY - point.y)
                if (deltaX >= TOUCH_TOLERANCE ||
                    deltaY >= TOUCH_TOLERANCE
                ) {
                    path!!.quadTo(
                        point.x.toFloat(), point.y.toFloat(),
                        (newX + point.x) / 2,
                        (newY + point.y) / 2
                    )
                    point.x = newX.toInt()
                    point.y = newY.toInt()
                }
            }
        }
    }

    fun clear() {
        pathMap!!.clear()
        previousPointMap!!.clear()
        bitmap!!.eraseColor(Color.WHITE)
        invalidate()
    }

    private fun touchEnded(pointerId: Int) {
        val path = pathMap!![pointerId]

        if( path != null){
            bitmapCanvas!!.drawPath(path, paintLine!!)
            path.reset()
        }

    }

    private fun touchStarted(x: Float, y: Float, pointerId: Int) {
        val path: Path?
        val point: Point?
        if (pathMap!!.containsKey(pointerId)) {
            path = pathMap!![pointerId]
            point = previousPointMap!![pointerId]
        } else {
            path = Path()
            pathMap!![pointerId] = path
            point = Point()
            previousPointMap!![pointerId] = point
        }
        path!!.moveTo(x, y)
        point!!.x = x.toInt()
        point.y = y.toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap!!, 0f, 0f, paintScreen)
        for (key in pathMap!!.keys) {
            canvas.drawPath(pathMap!![key]!!, paintLine!!)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap!!)
        bitmap!!.eraseColor(Color.WHITE)
    }

    //Save the drawing here
    @SuppressLint("SdCardPath", "WrongThread")
    fun saveDrawing( name : String ){

        val cw : ContextWrapper = ContextWrapper(context)

        //val dir : File = File(Environment.getDataDirectory().toString() +"/doodledee")
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/PerracoLabs")
            }
        }

        var uri : Uri? = null
        var stream: OutputStream? = null

        try {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            uri = resolver.insert(contentUri, contentValues)
            if (uri == null) {
                Log.d("error", "Failed to create new  MediaStore record.")
                return
            }

            stream = resolver.openOutputStream(uri)

            if (stream == null) {
                Log.d("error", "Failed to get output stream.")
            }

            val saved = bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            if (!saved) {
                Log.d("error", "Failed to save bitmap.")
            }

            Toast.makeText(context, "Picture saved", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {

            Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
            if (uri != null) {
                resolver.delete(uri, null, null)
            }
        } finally {
            if (stream != null) {
                stream.close()
            }
        }

    }

    fun setDrawingColor(color : Int){
        paintLine!!.setColor(color)
    }

    fun getDrawingColor(): Int {
        return paintLine!!.color
    }

    fun setLineWidth(width : Int){
        paintLine!!.strokeWidth = width.toFloat()
    }

    fun getLineWidth() : Int{
        return paintLine!!.strokeWidth.toInt()
    }

    companion object {
        const val TOUCH_TOLERANCE = 10f
    }

    init {
        init()
    }
}