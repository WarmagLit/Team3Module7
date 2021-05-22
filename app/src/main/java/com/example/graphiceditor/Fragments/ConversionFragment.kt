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
import kotlinx.android.synthetic.main.fragment_convertion.*
import kotlinx.android.synthetic.main.fragment_convertion.imageView2
import kotlinx.android.synthetic.main.fragment_convertion.radiusInput
import kotlinx.android.synthetic.main.fragment_convertion.seekBarRadius
import kotlinx.android.synthetic.main.fragment_convertion.splineField
import kotlinx.android.synthetic.main.fragment_draw.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext


class ConvertionFragment : Fragment() {

    var currentPicture = PixelArray(1, 1)
    lateinit var originalImage: Bitmap

    private var currentSpline = Splines()
    private var isSpline = false

    private var currentPlacePoint = 0
    private var affineOldPoints = Array(3) { IntArray(2) }
    private var affineNewPoints = Array(3) { IntArray(2) }
    private val pointColor = intArrayOf(
        colorOf(255, 140, 0, 0),
        colorOf(255, 0, 140, 0),
        colorOf(255, 0, 0, 140),
        colorOf(120, 240, 60, 60),
        colorOf(120, 60, 240, 60),
        colorOf(120, 60, 60, 240)
    )


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

        initSpline()
        initTransformer()
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
    private fun initSpline() {
        seekBarRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                radiusInput.setText(((i.toDouble() / 20).toInt()).toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something

            }
        })

        lineButton.setOnClickListener {
            val splineBitmap = Bitmap.createBitmap(
                currentPicture.width,
                currentPicture.height,
                Bitmap.Config.ARGB_8888
            )

            splineField.setImageBitmap(splineBitmap)
            prohibitPlacePoints()
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

            for (x in 0 until changes.width) {
                for (y in 0 until changes.height) {
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

    private fun onTouchSplineField(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            val splineBitmap = (splineField.drawable as BitmapDrawable).bitmap

            val r = radiusInput.text.toDouble().toInt()

            if (isSpline) {
                if (currentSpline.checkSelected()) {
                    currentSpline.changeSelectedPoint(x, y)
                    deletePointButton.visibility = View.INVISIBLE
                } else if (currentSpline.select(x, y, r) == -1) {
                    currentSpline.add(x, y)
                    deletePointButton.visibility = View.INVISIBLE
                } else if (!currentSpline.checkIsSettingPoint()) {
                    deletePointButton.visibility = View.VISIBLE
                }
            } else {
                currentSpline.add(x, y)
            }

            val newSplineBitmap =
                if (isSpline) currentSpline.drawSpline(r, splineBitmap)
                else currentSpline.drawPolyline(r, splineBitmap)

            splineField.setImageBitmap(newSplineBitmap)

            return false
        }

        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initTransformer() {

        pointsField.setOnTouchListener { _, event ->
            onTouchPointsField(event)
        }

        placePointsButton.setOnClickListener {
            val newSplineBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            splineField.setImageBitmap(newSplineBitmap)
            allowPlacePoints()
        }

        affineButton.setOnClickListener {
            CoroutineScope(EmptyCoroutineContext).launch(Dispatchers.Main) { applyTransformation() }
            prohibitPlacePoints()
        }

        inverseButton.setOnClickListener {
            val oldCopy = affineOldPoints
            affineOldPoints = affineNewPoints
            affineNewPoints = oldCopy
            val pointsBitmap = (pointsField.drawable as BitmapDrawable).bitmap
            for (i in 0..2) {
                pointsBitmap.addPoint(
                    affineOldPoints[i][0],
                    affineOldPoints[i][1],
                    pointColor[i]
                )
                pointsBitmap.addPoint(
                    affineNewPoints[i][0],
                    affineNewPoints[i][1],
                    pointColor[i + 3]
                )
            }
            pointsField.setImageBitmap(pointsBitmap)
        }
    }

    private fun allowPlacePoints() {
        currentPlacePoint = 1
        val pointsBitmap = Bitmap.createBitmap(
            currentPicture.width,
            currentPicture.height,
            Bitmap.Config.ARGB_8888
        )

        pointsField.setImageBitmap(pointsBitmap)
    }

    private suspend fun applyTransformation() {
        val oldSystemX = doubleArrayOf(
            affineOldPoints[0][0].toDouble(),
            affineOldPoints[1][0].toDouble(),
            affineOldPoints[2][0].toDouble()
        )
        val oldSystemY = doubleArrayOf(
            affineOldPoints[0][1].toDouble(),
            affineOldPoints[1][1].toDouble(),
            affineOldPoints[2][1].toDouble()
        )
        val newSystemX = doubleArrayOf(
            affineNewPoints[0][0].toDouble(),
            affineNewPoints[1][0].toDouble(),
            affineNewPoints[2][0].toDouble()
        )
        val newSystemY = doubleArrayOf(
            affineNewPoints[0][1].toDouble(),
            affineNewPoints[1][1].toDouble(),
            affineNewPoints[2][1].toDouble()
        )

        val transformations = AffineTransformations(oldSystemX, oldSystemY, newSystemX, newSystemY)

        currentPicture = transformations.transformWithTrilinearFiltering(
            currentPicture,
            currentPicture.width,
            currentPicture.height
        )
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private fun prohibitPlacePoints() {
        currentPlacePoint = 0
        val pointsBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        pointsField.setImageBitmap(pointsBitmap)
    }

    private fun onTouchPointsField(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return false
        if (currentPlacePoint == 0) return false
        val pointsBitmap = (pointsField.drawable as BitmapDrawable).bitmap

        val x = event.x.toInt()
        val y = event.y.toInt()

        pointsBitmap.addPoint(x, y, pointColor[currentPlacePoint - 1])
        if (currentPlacePoint in 1..3) affineOldPoints[currentPlacePoint - 1] = intArrayOf(x, y)
        else affineNewPoints[currentPlacePoint - 4] = intArrayOf(x, y)

        currentPlacePoint = (currentPlacePoint + 1) % 7
        pointsField.setImageBitmap(pointsBitmap)
        return false
    }

    private fun Bitmap.addPoint(x: Int, y: Int, color: Int) {
        val r = 20

        for (i in x - r..x + r) {
            if (i !in 0 until width) continue
            for (j in y - r..y + r) {
                if (j !in 0 until height || (x - i) * (x - i) + (y - j) * (y - j) > r * r) continue
                setPixel(i, j, color)
            }

        }
    }

    fun reloadImage() {
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