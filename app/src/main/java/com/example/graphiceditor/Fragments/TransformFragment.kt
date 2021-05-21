package com.example.graphiceditor.Fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.graphiceditor.*
import com.example.graphiceditor.ImageStorageManager.Companion.deleteImageFromInternalStorage
import com.example.graphiceditor.ImageStorageManager.Companion.getImageFromInternalStorage
import com.example.graphiceditor.ImageStorageManager.Companion.saveToInternalStorage
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.fragment_filter.imageView2
import kotlinx.android.synthetic.main.fragment_transform.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import java.lang.Math.abs
import kotlin.coroutines.EmptyCoroutineContext


class TransformFragment : Fragment() {

    private var currentPlacePoint = 0
    private var affineOldPoints = Array(3){ IntArray(2) }
    private var affineNewPoints = Array(3){ IntArray(2) }
    private val pointColor = intArrayOf(
        colorOf(255, 140, 0, 0),
        colorOf(255, 0, 140, 0),
        colorOf(255, 0, 0, 140),
        colorOf(120, 240, 60, 60),
        colorOf(120, 60, 240, 60),
        colorOf(120, 60, 60, 240)
    )


    var currentPicture = PixelArray(1, 1)
    lateinit var originalImages : Bitmap


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transform, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        originalImages = getImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")!!
        currentPicture = PixelArray(originalImages)

        imageView2.setImageBitmap(originalImages)
        deleteImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")

        initRotater()
        initZoomer()
        initTransformer()
    }

    override fun onPause() {
        super.onPause()

        val bit = (imageView2.drawable as BitmapDrawable).bitmap

        saveToInternalStorage(getActivity()!!.applicationContext, bit, "myImage")
    }

    private fun initZoomer() {
        zoomingInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (zoomingInput.text.isEmpty()) return@setOnFocusChangeListener

                val zoomFactor = zoomingInput.text.toDouble()
                if (abs(zoomFactor - 1.0) < 0.01) return@setOnFocusChangeListener

                CoroutineScope(EmptyCoroutineContext).async { applyZoom(zoomFactor) }
                zoomingInput.setText("1.0")
            }
        }
    }

    private fun initRotater() {
        rotationInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (rotationInput.text.isEmpty()) return@setOnFocusChangeListener

                val angle = rotationInput.text.toDouble()
                if (abs(angle - 0.0) < 0.01) return@setOnFocusChangeListener

                CoroutineScope(EmptyCoroutineContext).async { applyRotate(angle) }
                rotationInput.setText("0.0")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initTransformer() {

        pointsField.setOnTouchListener{ _, event ->
            onTouchPointsField(event)
        }

        placePointsButton.setOnClickListener {
            allowPlacePoints()
        }

        affineButton.setOnClickListener {
            CoroutineScope(EmptyCoroutineContext).async { applyTransformation() }
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

    private fun allowPlacePoints(){
        currentPlacePoint = 1
        val pointsBitmap = Bitmap.createBitmap(
            currentPicture.width,
            currentPicture.height,
            Bitmap.Config.ARGB_8888
        )

        pointsField.setImageBitmap(pointsBitmap)
    }

    private suspend fun applyTransformation(){
        val oldSystemX = doubleArrayOf(affineOldPoints[0][0].toDouble(), affineOldPoints[1][0].toDouble(), affineOldPoints[2][0].toDouble())
        val oldSystemY = doubleArrayOf(affineOldPoints[0][1].toDouble(), affineOldPoints[1][1].toDouble(), affineOldPoints[2][1].toDouble())
        val newSystemX = doubleArrayOf(affineNewPoints[0][0].toDouble(), affineNewPoints[1][0].toDouble(), affineNewPoints[2][0].toDouble())
        val newSystemY = doubleArrayOf(affineNewPoints[0][1].toDouble(), affineNewPoints[1][1].toDouble(), affineNewPoints[2][1].toDouble())

        val transformations = AffineTransformations(oldSystemX, oldSystemY, newSystemX, newSystemY)

        currentPicture = transformations.transformWithTrilinearFiltering(currentPicture, currentPicture.width, currentPicture.height)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private fun prohibitPlacePoints(){
        currentPlacePoint = 0
        val pointsBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        pointsField.setImageBitmap(pointsBitmap)
    }

    private suspend fun applyZoom(zoomFactor: Double){
        currentPicture = Zooming.zoom(currentPicture, zoomFactor)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private suspend fun applyRotate(angle: Double){
        currentPicture = Rotation.rotate(currentPicture, angle)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private fun onTouchPointsField(event: MotionEvent): Boolean{
        if (event.action != MotionEvent.ACTION_DOWN) return false
        if (currentPlacePoint == 0) return false
        val pointsBitmap = (pointsField.drawable as BitmapDrawable).bitmap

        /*val isHorizontal = (pointsBitmap.height < pointsBitmap.width)
        val verticalDifference =
            if(isHorizontal) 0
            else (pointsBitmap.height - pointsField.height * pointsBitmap.width / pointsField.width) / 2
        val horizontalDifference =
            if(isHorizontal) (pointsBitmap.width - pointsField.width * pointsBitmap.height / pointsField.height) / 2
            else 0*/


        val x = event.x.toInt() * pointsBitmap.width / pointsField.width// - verticalDifference
        val y = event.y.toInt() * pointsBitmap.height / pointsField.height// - horizontalDifference

        pointsBitmap.addPoint(x, y, pointColor[currentPlacePoint - 1])
        if (currentPlacePoint in 1..3) affineOldPoints[currentPlacePoint - 1] = intArrayOf(x, y)
        else affineNewPoints[currentPlacePoint - 4] = intArrayOf(x, y)

        currentPlacePoint = (currentPlacePoint + 1) % 7
        pointsField.setImageBitmap(pointsBitmap)
        return false
    }

    private fun Bitmap.addPoint(x: Int, y: Int, color: Int){
        val r = 20

        for (i in x-r..x+r){
            if (i !in 0 until width) continue
            for (j in y-r..y+r){
                if (j !in 0 until height || (x - i) * (x - i) + (y - j) * (y - j) > r * r) continue
                setPixel(i, j, color)
            }

        }
    }


    fun reloadImage () {
        originalImages = getImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")!!
        currentPicture = PixelArray(originalImages)
        deleteImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")
    }

}