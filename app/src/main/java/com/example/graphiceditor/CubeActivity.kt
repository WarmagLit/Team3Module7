package com.example.graphiceditor

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.internal.ContextUtils.getActivity
import kotlinx.android.synthetic.main.activity_cubik.*
import kotlinx.android.synthetic.main.activity_settings.*
import java.lang.Math.PI

class CubeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cubik)

        RotateZ.setOnClickListener {
            canvasBox.rotateZ3D(30 * PI / 180)
            Toast.makeText(
                this,
                "Нажмите на кубик, чтобы увидеть изменения",
                Toast.LENGTH_SHORT
            ).show()
        }

        goBack.setOnClickListener {
            val intent = Intent(this@CubeActivity, MainActivity::class.java)
            startActivity(intent)
        }

        val appSettingPrefs: SharedPreferences = getSharedPreferences("AppSettingPrefs", 0)
        val isNightModeOn: Boolean = appSettingPrefs.getBoolean("NightMode", false)
        var colorBackground = Color.WHITE
        if (isNightModeOn) {
            colorBackground = Color.BLACK
        }

    }


}