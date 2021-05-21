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
import android.widget.SeekBar
import com.example.graphiceditor.*
import com.example.graphiceditor.ImageStorageManager.Companion.deleteImageFromInternalStorage
import com.example.graphiceditor.ImageStorageManager.Companion.getImageFromInternalStorage
import com.example.graphiceditor.ImageStorageManager.Companion.saveToInternalStorage
import kotlinx.android.synthetic.main.fragment_convertion.*
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.fragment_filter.imageView2
import kotlinx.android.synthetic.main.fragment_transform.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Math.abs
import kotlin.coroutines.EmptyCoroutineContext


class TransformFragment : Fragment() {

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


        originalImages = getImageFromInternalStorage(activity!!.applicationContext, "myImage")!!
        currentPicture = PixelArray(originalImages)

        imageView2.setImageBitmap(originalImages)
        deleteImageFromInternalStorage(activity!!.applicationContext, "myImage")

        initRotater()
        initZoomer()
    }

    override fun onPause() {
        super.onPause()

        val bit = (imageView2.drawable as BitmapDrawable).bitmap

        saveToInternalStorage(activity!!.applicationContext, bit, "myImage")
    }

    private fun initZoomer() {

        seekBarZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                zoomingInput.setText((i.toDouble() / 50).toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }

    private fun initRotater() {
        seekBarRotate.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                rotationInput.setText((i.toDouble()).toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        applyChangeButton.setOnClickListener {
            if (!rotationInput.text.isEmpty()) {
                val angle = rotationInput.text.toDouble()
                if (!(Math.abs(angle - 0.0) < 0.01)) {

                    CoroutineScope(EmptyCoroutineContext).launch(Dispatchers.Main) { applyRotate(angle) }
                    rotationInput.setText("0.0")
                }
            }

            if (!zoomingInput.text.isEmpty()) {
                val zoomFactor = zoomingInput.text.toDouble()
                if (!(Math.abs(zoomFactor - 1.0) < 0.01)) {

                    CoroutineScope(EmptyCoroutineContext).launch(Dispatchers.Main) { applyZoom(zoomFactor) }
                    zoomingInput.setText("1.0")
                }
            }

        }
    }

    private suspend fun applyZoom(zoomFactor: Double){
        currentPicture = Zooming.zoom(currentPicture, zoomFactor)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private suspend fun applyRotate(angle: Double){
        currentPicture = Rotation.rotate(currentPicture, angle)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    fun reloadImage () {
        originalImages = getImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")!!
        currentPicture = PixelArray(originalImages)
        deleteImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")
    }

}