package com.example.graphiceditor.Fragments

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.example.graphiceditor.Filter
import com.example.graphiceditor.PixelArray
import com.example.graphiceditor.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.fragment_filter.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext


class filterFragment : Fragment() {

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

        imageView2.setImageResource(R.drawable.hippo)

        currentPicture = PixelArray((imageView2.drawable as BitmapDrawable).bitmap)
        originalImage = (imageView2.drawable as BitmapDrawable).bitmap

        initButtons()
    }


    override fun onPause() {
        super.onPause()

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
