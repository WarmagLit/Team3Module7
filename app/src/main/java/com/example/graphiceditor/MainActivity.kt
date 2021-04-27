package com.example.graphiceditor

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import android.graphics.Matrix as Matrix

private const val REQUEST_CODE = 42
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonGallery.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied
                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE);

                    requestPermissions(permissions, PERMISSION_CODE);

                } else {
                    //permission already granted
                    pickImageFromGallery();
                }
            } else {
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        }

        var CAMERA_REQUEST_CODE = 42
        buttonCamera.setOnClickListener {
            buttonCamera.setOnClickListener {
                Log.d("TAG", "Camera button click")
                //Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()

                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA_REQUEST_CODE)
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),
                        CAMERA_REQUEST_CODE)

                }
            }
        }

        buttonSave.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        100
                    )
                } else {
                    saveImageToStorage()
                }
            } else {
                saveImageToStorage()
            }
        }


        // Initializing a String Array
        val colors =
            arrayOf("-Не выбрано-", "KK", "Повернуть картинку на 90 град.")

        // Initializing an ArrayAdapter
        val adapter = ArrayAdapter(
            this, // Context
            android.R.layout.simple_spinner_item, // Layout
            colors // Array
        )

        // Set the drop down view resource
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        // Finally, data bind the spinner object with dapter
        spinner.adapter = adapter;

        // Set an on item selected listener for spinner object
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // Display the selected item text on text view
                text_view.text = "Spinner selected : ${parent.getItemAtPosition(position).toString()}"

                if (parent.getItemAtPosition(position).toString() == "Синий фильтр") {
                    filter("first")
                    spinner.setSelection(adapter.getPosition("-Не выбрано-"))
                }
                if (parent.getItemAtPosition(position).toString() == "Повернуть картинку на 90 град.") {
                    rotateImage()
                    spinner.setSelection(adapter.getPosition("-Не выбрано-"))
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }

        }

        var spinnerfilters: Spinner = findViewById(R.id.filterssss)
        spinnerfilters.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {

                var selected: String = spinnerfilters.getSelectedItem().toString();
                textView2.text = "Spinner selected : ${selected}"
                if (selected != "-Не выбрано-") {
                    filter(selected)
                    spinnerfilters.setSelection(adapter.getPosition("-Не выбрано-"))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        imageView2.setImageResource(R.drawable.hippo)

    }

    fun filter(told: String) {
        var currentPicture = ProcessedPicture((imageView2.getDrawable() as BitmapDrawable).bitmap)

        Filters().Check(currentPicture,told)
        currentPicture.updateBitmap()

        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    fun rotateImage() {
        var currentPicture = ProcessedPicture((imageView2.getDrawable() as BitmapDrawable).bitmap)

        val rotatedBitmap = currentPicture.bitmap.rotate(45f)
        currentPicture = ProcessedPicture(rotatedBitmap)

        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        private val IMAGE_PICK_CODE = 1000;

        private val PERMISSION_CODE = 1001;
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission from popup granted
                saveImageToStorage()
            } else {
                //permission from popup denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        } else {

            when (requestCode) {
                PERMISSION_CODE -> {
                    if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //permission from popup granted
                        pickImageFromGallery()
                    } else {
                        //permission from popup denied
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageView2.setImageURI(data?.data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun saveImageToStorage() {
        val externalStorageState = Environment.getExternalStorageState()
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
            val storageDirectory = File(getExternalFilesDir(null), "Text.txt") //not working
            val file = File(storageDirectory, "test_image.png")
            try {
                val stream: OutputStream = FileOutputStream(file)
                val drawable =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_launcher_background)
                val bitmap = (drawable as BitmapDrawable).bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
                stream.close()
                Toast.makeText(
                    this,
                    "Image saved to ${Uri.parse(file.absolutePath)}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Unable to access the storage", Toast.LENGTH_SHORT).show()
        }
    }
}
