package com.example.graphiceditor.Fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.example.graphiceditor.*
import com.example.graphiceditor.ImageStorageManager.Companion.deleteImageFromInternalStorage
import com.example.graphiceditor.ImageStorageManager.Companion.getImageFromInternalStorage
import com.example.graphiceditor.ImageStorageManager.Companion.saveToInternalStorage
import kotlinx.android.synthetic.main.fragment_draw.*
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.fragment_filter.imageView2

class DrawFragment : Fragment() {

    var currentPicture = PixelArray(1, 1)
    lateinit var originalImage : Bitmap

    private var currentBrush = "red"
    private var currentSpline = Splines()
    private var isSpline = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_draw, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        originalImage = getImageFromInternalStorage(
            getActivity()!!.applicationContext,
            "myImage"
        )!!
        currentPicture = PixelArray(originalImage)

        imageView2.setImageBitmap(originalImage)
        deleteImageFromInternalStorage(
            getActivity()!!.applicationContext,
            "myImage"
        )

        initBrush()
        initSpline()

    }

    override fun onPause() {
        super.onPause()

        val bit = (imageView2.drawable as BitmapDrawable).bitmap

        saveToInternalStorage(
            getActivity()!!.applicationContext,
            bit,
            "myImage"
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initBrush(){

        btnClearBrush.setOnClickListener {
            val drawingBitmap = Bitmap.createBitmap(
                currentPicture.width,
                currentPicture.height,
                Bitmap.Config.ARGB_8888
            )

            drawingField.setImageBitmap(drawingBitmap)
        }

        val arrayBtn = arrayOf(
            btnBlurBrush,
            btnRedBrush,
            btnGreenBrush,
            btnBlueBrush,
            btnGrayBrush
        )

        val arrayStrings = arrayOf(
            "blur",
            "red",
            "green",
            "blue",
            "gray"
        )

        for (i in arrayBtn.indices){
            arrayBtn[i].setOnClickListener {
                currentBrush = arrayStrings[i]
            }
        }

        drawingField.setOnTouchListener { _, event ->
            onTouchDrawingField(event)
        }

        drawButton.setOnClickListener {
            val drawingBitmap = Bitmap.createBitmap(
                currentPicture.width,
                currentPicture.height,
                Bitmap.Config.ARGB_8888
            )

            drawingField.setImageBitmap(drawingBitmap)
        }

        applyDrawingButton.setOnClickListener {
            val changes = PixelArray((drawingField.drawable as BitmapDrawable).bitmap)
            for (x in 0 until changes.width){
                for (y in 0 until changes.height){
                    if (changes[x, y] != 0) {
                        currentPicture[x, y] = changes[x, y]
                    }
                }
            }
            val drawingBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

            drawingField.setImageBitmap(drawingBitmap)
            imageView2.setImageBitmap(currentPicture.bitmap)
        }

        seekBarRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                radiusInput.setText((i.toDouble() / 100).toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something

            }
        })

        seekBarCentring.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                centeringInput.setText((i.toDouble() / 100).toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something

            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSpline(){
        lineButton.setOnClickListener {
            val splineBitmap = Bitmap.createBitmap(
                currentPicture.width,
                currentPicture.height,
                Bitmap.Config.ARGB_8888
            )

            splineField.setImageBitmap(splineBitmap)
            currentSpline = Splines()
            isSpline = false
        }

        splineField.setOnTouchListener { _, event ->
            onTouchSplineField(event)
        }

        splineButton.setOnClickListener {
            if (isSpline) return@setOnClickListener

            val splineBitmap = (splineField.drawable as BitmapDrawable).bitmap
            val r = radiusInput.text.toDouble().toInt()

            val newSplineBitmap = currentSpline.drawSpline(r, splineBitmap)
            splineField.setImageBitmap(newSplineBitmap)

            isSpline = true
        }

        applySplineButton.setOnClickListener {
            val splineBitmap = (splineField.drawable as BitmapDrawable).bitmap
            val r = radiusInput.text.toDouble().toInt()
            val changes = PixelArray(currentSpline.drawSplineWithoutSettings(r, splineBitmap))

            for (x in 0 until changes.width){
                for (y in 0 until changes.height){
                    val k = changes[x, y].component(alpha)
                    fun newColor(component: Int) =
                        (k * changes[x, y].component(component) +
                                (255 - k) * currentPicture[x, y].component(component)) / 255
                    currentPicture[x, y] = colorOf(
                        currentPicture[x, y].component(alpha),
                        newColor(red),
                        newColor(green),
                        newColor(blue)
                    )
                }
            }
            val newSplineBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

            splineField.setImageBitmap(newSplineBitmap)
            imageView2.setImageBitmap(currentPicture.bitmap)
        }

        deletePointButton.setOnClickListener {
            val splineBitmap = (splineField.drawable as BitmapDrawable).bitmap
            val r = radiusInput.text.toDouble().toInt()

            currentSpline.removeSelectedPoint()

            val newSplineBitmap = currentSpline.drawSpline(r, splineBitmap)
            splineField.setImageBitmap(newSplineBitmap)

            deletePointButton.visibility = View.INVISIBLE
        }
    }


    private fun onTouchDrawingField(event: MotionEvent): Boolean{
        if (event.action != MotionEvent.ACTION_MOVE) return false
        val x = event.x.toInt()
        val y = event.y.toInt()
        val drawingBitmap = (drawingField.drawable as BitmapDrawable).bitmap

        val r = radiusInput.text.toDouble().toInt()
        val centering = centeringInput.text.toDouble()

        Paintbrush.draw(currentPicture, drawingBitmap, x, y, r, centering, currentBrush)
        drawingField.setImageBitmap(drawingBitmap)

        return false
    }

    private fun onTouchSplineField(event: MotionEvent): Boolean{
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            val splineBitmap = (splineField.drawable as BitmapDrawable).bitmap

            val r = radiusInput.text.toDouble().toInt()

            if (isSpline) {
                if (currentSpline.checkSelected()){
                    currentSpline.changeSelectedPoint(x, y)
                    deletePointButton.visibility = View.INVISIBLE
                }
                else if (currentSpline.select(x, y, r) == -1){
                    currentSpline.add(x, y)
                    deletePointButton.visibility = View.INVISIBLE
                }
                else if (currentSpline.getSelectedListNumber() == 0){
                    deletePointButton.visibility = View.VISIBLE
                }
            }
            else {
                currentSpline.add(x, y)
            }

            val newSplineBitmap =
                if(isSpline) currentSpline.drawSpline(r, splineBitmap)
                else currentSpline.drawPolyline(r, splineBitmap)

            splineField.setImageBitmap(newSplineBitmap)

            return false
        }

        return false
    }

    fun reloadImage () {
        originalImage = getImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")!!
        currentPicture = PixelArray(originalImage)
        deleteImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")
    }



}