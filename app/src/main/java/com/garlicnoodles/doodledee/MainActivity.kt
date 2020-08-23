package com.garlicnoodles.doodledee

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.garlicnoodles.doodledee.MyCanvas;
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.color_dialog.*
import java.net.Authenticator
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    lateinit var currentAlertDialog: AlertDialog.Builder
    lateinit var widthImageView : ImageView
    lateinit var widthSeekbar : SeekBar

    lateinit var alphaSeekBar : SeekBar
    lateinit var redSeekBar : SeekBar
    lateinit var greenSeekBar : SeekBar
    lateinit var blueSeekBar : SeekBar
    lateinit var colorView : View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val garlicCanvas = findViewById<View>(R.id.garlicCanvas)


        //Request for write file permission
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(permission != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }
    }

    fun makeRequest(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
    }

    fun showLineWithDialog(){

        currentAlertDialog = AlertDialog.Builder(this)

        var view = layoutInflater.inflate(R.layout.width_dialog, null)
        widthSeekbar = view.findViewById<SeekBar>(R.id.widthSeekBar)

        var setLineWidthButton = view.findViewById<Button>(R.id.widthDialogButton)
        widthImageView = view.findViewById(R.id.imageViewId)

        widthSeekbar.setOnSeekBarChangeListener( object : SeekBar.OnSeekBarChangeListener{

            var bitmap = Bitmap.createBitmap(400,100, Bitmap.Config.ARGB_8888)
            var canvas = Canvas(bitmap)


            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                var p = Paint()
                p.setColor(garlicCanvas.getDrawingColor())
                p.strokeCap = Paint.Cap.ROUND
                p.strokeWidth = progress.toFloat()

                bitmap.eraseColor(Color.DKGRAY)
                canvas.drawLine(30f, 50f, 370f, 50f, p)
                widthImageView.setImageBitmap(bitmap)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        setLineWidthButton.setOnClickListener{
            garlicCanvas.setLineWidth(widthSeekbar.progress)
        }


        currentAlertDialog.setView(view)
        currentAlertDialog.create()
        currentAlertDialog.show()


    }

    fun showColorDialog(){
        currentAlertDialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.color_dialog, null)
        alphaSeekBar = view.findViewById<SeekBar>(R.id.alphaSeek)
        redSeekBar = view.findViewById<SeekBar>(R.id.redSeek)
        greenSeekBar = view.findViewById<SeekBar>(R.id.greenSeek)
        blueSeekBar = view.findViewById<SeekBar>(R.id.blueSeek)
        colorView = view.findViewById(R.id.colorView)

        //Register the seekbar events listeners
        alphaSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged)
        redSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged)
        greenSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged)
        blueSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged)

        //Update color sliders
        var color = garlicCanvas.getDrawingColor()
        alphaSeekBar.progress = Color.alpha(color)
        redSeekBar.progress = Color.red(color)
        greenSeekBar.progress = Color.green(color)
        blueSeekBar.progress = Color.blue(color)


        //Set the Color
        var setColorButton = view.findViewById<Button>(R.id.setColorButton)
        setColorButton.setOnClickListener{
            garlicCanvas.setDrawingColor(Color.argb(
                alphaSeekBar.progress,
                redSeekBar.progress,
                greenSeekBar.progress,
                blueSeekBar.progress
            ))
        }

        currentAlertDialog.setView(view)
        currentAlertDialog.setTitle("Choose Color")
        currentAlertDialog.create().show()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }



    //Event Listener for wehen the color bar is changed
    var colorSeekBarChanged = object : SeekBar.OnSeekBarChangeListener{

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            garlicCanvas.setBackgroundColor(Color.argb(
                alphaSeekBar.progress,
                redSeekBar.progress,
                greenSeekBar.progress,
                blueSeekBar.progress
            ))


            //Display the color image
            colorView.setBackgroundColor(Color.argb(
                alphaSeekBar.progress,
                redSeekBar.progress,
                greenSeekBar.progress,
                blueSeekBar.progress
            ))

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.clearId -> {
                garlicCanvas.clear()
                return true
            }
            R.id.lineWidth -> {
                showLineWithDialog()
                return true
            }
            R.id.colorid -> {
                showColorDialog()
                return true
            }
            R.id.saveImage ->{

                garlicCanvas.saveDrawing()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
