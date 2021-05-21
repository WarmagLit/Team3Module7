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
import kotlinx.android.synthetic.main.fragment_draw.*
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.fragment_filter.imageView2


class ConvertionFragment : Fragment() {

    var currentPicture = PixelArray(1, 1)
    lateinit var originalImage : Bitmap

    private var currentSpline = Splines()
    private var isSpline = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_convertion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        originalImage = ImageStorageManager.getImageFromInternalStorage(
            getActivity()!!.applicationContext,
            "myImage"
        )!!
        currentPicture = PixelArray(originalImage)

        imageView2.setImageBitmap(originalImage)
        ImageStorageManager.deleteImageFromInternalStorage(
            getActivity()!!.applicationContext,
            "myImage"
        )

        initSpline()

    }

    override fun onPause() {
        super.onPause()

        val bit = (imageView2.drawable as BitmapDrawable).bitmap

        ImageStorageManager.saveToInternalStorage(
            getActivity()!!.applicationContext,
            bit,
            "myImage"
        )
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
        originalImage = ImageStorageManager.getImageFromInternalStorage(
            getActivity()!!.applicationContext,
            "myImage"
        )!!
        currentPicture = PixelArray(originalImage)
        ImageStorageManager.deleteImageFromInternalStorage(
            getActivity()!!.applicationContext,
            "myImage"
        )
    }
}