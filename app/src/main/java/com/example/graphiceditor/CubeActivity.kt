package com.example.graphiceditor

import android.content.Intent
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_cubik.*
import java.lang.Math.PI

class CubeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cubik)

        RotateZ.setOnClickListener{
            canvasBox.rotateZ3D(30 * PI/180)
        }



        goBack.setOnClickListener {
            val intent = Intent(this@CubeActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

}

