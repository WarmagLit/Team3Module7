package com.example.graphiceditor.Fragments

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
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


class transformFragment : Fragment() {

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


    private suspend fun apply(filter: Filter) {
        currentPicture = filter.process(currentPicture)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private suspend fun applyZoom(zoomFactor: Double){
        currentPicture = Zooming.zoom(currentPicture, zoomFactor)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private suspend fun applyRotate(angle: Double){
        currentPicture = Rotation.rotate(currentPicture, angle)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

}