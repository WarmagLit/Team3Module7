package com.example.graphiceditor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.internal.ContextUtils.getActivity
import kotlinx.android.synthetic.main.activity_cubik.*

class CubeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cubik)

        goBack.setOnClickListener {
            val intent = Intent(this@CubeActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}