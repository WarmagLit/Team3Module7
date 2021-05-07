package com.example.graphiceditor

import android.R.attr.data
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_draw.*
import java.io.File


class DrawActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)

        var imageUri = intent.getStringExtra("image")
        var bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri.toString().toUri())

        //удаление временной картинки
        val file = File(imageUri.toString())
        Log.d("tag", file.delete().toString())
        //file.delete()

        imageVievSec.setImageBitmap(bitmap)
    }
}