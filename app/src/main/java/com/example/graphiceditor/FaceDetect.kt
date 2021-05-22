package com.example.graphiceditor

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_face_detect.*

private const val IMAGE_PICK_CODE = 1000
private const val CAMERA_REQUEST_CODE = 42

class FaceDetect : AppCompatActivity() {

    var selectedUri: Uri? = null
    lateinit var dialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detect)


        //View event
        imageView3.setOnClickListener {
            pickImageFromGallery()
        }
        buttonUpload.setOnClickListener {

        }

    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data)
            val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap
            imageView3.setImageBitmap(thumbNail)

        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            selectedUri = data?.data
            imageView3.setImageURI(data?.data)

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}