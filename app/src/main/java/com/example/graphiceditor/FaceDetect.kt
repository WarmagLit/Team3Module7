package com.example.graphiceditor

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.graphiceditor.Retrofit.IUploadAPI
import com.example.graphiceditor.Retrofit.RetrofitClient
import com.example.graphiceditor.Utilis.Common
import com.example.graphiceditor.Utilis.IUploadCallback
import com.example.graphiceditor.Utilis.ProgressRequestBody
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_face_detect.*
import kotlinx.android.synthetic.main.fragment_filter.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.StringBuilder
import java.net.URISyntaxException
import java.util.jar.Manifest

private const val IMAGE_PICK_CODE = 1000
private const val CAMERA_REQUEST_CODE = 42

class FaceDetect : AppCompatActivity(), IUploadCallback {

    lateinit var mService: IUploadAPI
    var selectedUri:Uri?=null
    lateinit var dialog: ProgressDialog

    private val apiUpload:IUploadAPI
        get() = RetrofitClient.client.create(IUploadAPI::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detect)

        //Request permission
        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object:PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    Toast.makeText(this@FaceDetect, "You must accept permission", Toast.LENGTH_LONG).show()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {

                }

            } ).check()

        //Service
        mService = apiUpload

        //View event
        imageView3.setOnClickListener {
            pickImageFromGallery()
        }
        buttonUpload.setOnClickListener {
            uploadFile()
        }

    }

    private fun uploadFile() {
        if (selectedUri != null) {
            dialog = ProgressDialog(this@FaceDetect)
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            dialog.setMessage("Uploading...")
            dialog.isIndeterminate = false
            dialog.max = 100
            dialog.setCancelable(false)
            dialog.show()

            var file: File? = null
            try {
                file = File(Common.getFilePath(this, selectedUri!!))
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
            if (file != null) {
                val requestBody = ProgressRequestBody(file, this)
                val body = MultipartBody.Part.createFormData("image", file.name, requestBody)

                Thread(Runnable {
                    mService.uploadFile(body)
                        .enqueue(object : Callback<String> {
                            override fun onResponse(
                                call: Call<String>,
                                response: Response<String>
                            ) {
                                dialog.dismiss()
                                val image_processed_link =
                                    StringBuilder("http://10.0.2.2:5000/").append(
                                        response.body()!!.replace("\"", "")
                                    ).toString()
                                Picasso.get().load(image_processed_link).into(imageView3)
                                Toast.makeText(
                                    this@FaceDetect,
                                    "Face detected!!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Toast.makeText(this@FaceDetect, t.message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                        })
                }).start()
            }
            else {
                Toast.makeText(this@FaceDetect, "Cannot load this file!", Toast.LENGTH_SHORT).show()
            }
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

    override fun onProgressUpdate(percent: Int) {

    }
}