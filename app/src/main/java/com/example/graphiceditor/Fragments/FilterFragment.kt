package com.example.graphiceditor.Fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.graphiceditor.*
import com.example.graphiceditor.ImageStorageManager.Companion.deleteImageFromInternalStorage
import com.example.graphiceditor.ImageStorageManager.Companion.getImageFromInternalStorage
import com.example.graphiceditor.ImageStorageManager.Companion.saveToInternalStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.fragment_filter.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import kotlin.coroutines.EmptyCoroutineContext


class FilterFragment : Fragment() {

    var currentPicture = PixelArray(1, 1)
    lateinit var originalImage : Bitmap


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalImage = getImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")!!
        currentPicture = PixelArray(originalImage)

        imageView2.setImageBitmap(originalImage)
        deleteImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")

        initButtons()
        initUnsharp()
    }

    fun reloadImage () {
        originalImage = getImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")!!
        currentPicture = PixelArray(originalImage)
        imageView2.setImageBitmap(originalImage)
        deleteImageFromInternalStorage(getActivity()!!.applicationContext, "myImage")
    }


    override fun onPause() {
        super.onPause()

        val bit = (imageView2.drawable as BitmapDrawable).bitmap
        saveToInternalStorage(getActivity()!!.applicationContext, bit, "myImage")
    }



    private fun initButtons() {
        filtersLayoutFilt.btnMain.setOnClickListener {
            imageView2.setImageBitmap(originalImage)
            currentPicture = PixelArray((imageView2.drawable as BitmapDrawable).bitmap)
        }

        val arrayBtn = arrayOf(
            filtersLayoutFilt.btnRed,
            filtersLayoutFilt.btnGreen,
            filtersLayoutFilt.btnBlue,
            filtersLayoutFilt.btnGray,
            filtersLayoutFilt.btnDiagonal,
            filtersLayoutFilt.btnSwap,
            filtersLayoutFilt.btnNegative,
            filtersLayoutFilt.btnBlur,
            filtersLayoutFilt.btnEdge,
            filtersLayoutFilt.btnEmboss
        )

        val arrayStrings = getStrings()

        for (i in arrayBtn.indices){
            arrayBtn[i].setOnClickListener {
                val filter = of(arrayStrings[i])
                Log.d("TAG", filter.toString())
                if (filter != Filter.NONE) {
                    CoroutineScope(EmptyCoroutineContext).launch(Dispatchers.Main) { apply(filter) }

                }
            }
        }

    }

    private fun initUnsharp() {
        unsharpButton.setOnClickListener {
            currentPicture = Unsharp.unsharpFilter(currentPicture,unsharpSigma.text.toDouble(),koefK.text.toDouble())
            imageView2.setImageBitmap(currentPicture.bitmap)
        }
    }

    fun createImageFromBitmap(bitmap: Bitmap): String? {
        var fileName: String? = "myImage" //no .png or .jpg needed
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val fo: FileOutputStream = view!!.getContext().openFileOutput(fileName, Context.MODE_PRIVATE)
            fo.write(bytes.toByteArray())
            // remember close file output
            fo.close()
        } catch (e: Exception) {
            e.printStackTrace()
            fileName = null
        }
        return fileName
    }

    private fun of(string: String): Filter{
        Filter.values().forEach {
            if(getString(it) == string)
                return it
        }
        return Filter.NONE
    }

    private suspend fun apply(filter: Filter) {
        currentPicture = filter.process(currentPicture)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }


    private fun getStrings(): List<String> = Filter.values().map { getString(it) }
    private fun getString(filter: Filter) = getString(filter.code)
}
