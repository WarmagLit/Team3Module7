package com.example.graphiceditor

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.internal.ContextUtils.getActivity
import kotlinx.android.synthetic.main.activity_cubik.*
import kotlinx.android.synthetic.main.activity_settings.*
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