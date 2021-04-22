package com.example.graphiceditor

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.editor.*

class MainActivity : AppCompatActivity() {


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editor)

        var sostav: Int = 0
        imageButton5.setOnClickListener {
            if (sostav == 0) {
                imageButton5.setImageResource(R.drawable.activateFilter)
                sostav = 1
            } else {
                imageButton5.setImageResource(R.drawable.Filter)
                sostav = 0
            }

        }
    }
}