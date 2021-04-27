package com.example.graphiceditor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.get
import androidx.core.graphics.toColor
import kotlinx.android.synthetic.main.editor.*
import java.io.File.separator

import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import kotlin.math.abs
import android.graphics.Matrix as Matrix

private const val REQUEST_CODE = 42
private const val CAMERA_PERMISSION_CODE = 1
private const val CAMERA_REQUEST_CODE = 1
class MainActivity : AppCompatActivity() {


    val PI = 3.1415926
    var currentPicture = ProcessedPicture(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView2.setImageResource(R.drawable.hippo)
        currentPicture = ProcessedPicture((imageView2.drawable as BitmapDrawable).bitmap)

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
                    pickImageFromGallery()

                }
            } else {
                //system OS is < Marshmallow
                pickImageFromGallery()
            }
        }

        var CAMERA_REQUEST_CODE = 42

        buttonCamera.setOnClickListener {
            Log.d("TAG", "Camera button click")
            //Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
            }
        }


        buttonSave.setOnClickListener {
            val bitmap = (imageView2.getDrawable() as BitmapDrawable).bitmap
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
                    //saveImageToStorage()
                    val imageUri = bitmap.saveImage(applicationContext)
                    Toast.makeText(
                        this,
                        "Image saved to ${imageUri}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val imageUri = bitmap.saveImage(applicationContext)
                Toast.makeText(
                    this,
                    "Image saved to ${imageUri}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        // Initializing a String Array
        val colors = arrayOf("-Не выбрано-","Синий фильтр","Серый фильтр","Сепия","Маштабирование","Повернуть картинку на 90 град.")



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
                text_view.text =
                    "Spinner selected : ${parent.getItemAtPosition(position).toString()}"


                if (parent.getItemAtPosition(position).toString() == "Повернуть картинку на 90 град.") {
                    //rotateImage()
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

        var zoomingInput: EditText = findViewById(R.id.zoomingInput)
        zoomingInput.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                val zoomFactor = zoomingInput.text.toString().toDouble()
                if (abs(zoomFactor - 1.0) < 0.01){
                    return@setOnFocusChangeListener
                }
                currentPicture = ProcessedPicture(Zooming.zoom(currentPicture, zoomFactor))
                zoomingInput.setText("1")
                imageView2.setImageBitmap(currentPicture.bitmap)
            }
        }

    }





    fun filter(told: String) {
        Filters().Check(currentPicture, told)
        currentPicture.updateBitmap()

        imageView2.setImageBitmap(currentPicture.bitmap)
    }

/*
    fun rotateImage() {
        val rotatedBitmap = currentPicture.bitmap.rotate(45f)

        imageView2.setImageBitmap(rotatedBitmap)
        currentPicture = ProcessedPicture(rotatedBitmap)
    }*/

    fun imageRepair(image: Bitmap):Bitmap {
        val repairImage = image.copy(Bitmap.Config.ARGB_8888, true)
        for (i in  0..repairImage.width-2) {
            for (j in  0..repairImage.height-1) {
                if (image.getPixel(i, j) == Color.BLACK) {
                    repairImage.setPixel(i, j, image.getPixel(i + 1, j))
                }
            }
        }
        return repairImage
    }

    fun scaling(degree: Int) {
        val image = (imageView2.getDrawable() as BitmapDrawable).bitmap
        val scaledImage = image.copy(Bitmap.Config.ARGB_8888, true)

        val centerX = Math.round(image.width / 2.0)
        val centerY = Math.round(image.height / 2.0)

        var a = (Math.cos(degree*PI / 180)*(- centerX) - Math.sin(degree*PI / 180)*(- centerY.toDouble()) + centerX).toInt()
        var b = (Math.sin(degree*PI / 180)*(- centerX) + Math.cos(degree*PI / 180)*(image.height - 1 - centerY.toDouble()) + centerY).toInt()
        var c = image.width - a
        var d = image.height - b
        var ratio = Math.sqrt(a*a*1.0+b*b*1.0) / image.height
        val centeringPixels = ((scaledImage.width - scaledImage.width * ratio)/2)

        Log.d("d", a.toString() + " " + b.toString() + " " + c.toString() + " "+ d.toString() + " "+ ((a*b + c*d)/1.0).toString()+ " " + ratio.toString())
        //ratio = 0.8
        for (i in  0..scaledImage.width-1) {
            for (j in  0..scaledImage.height-1) {
                if ((i * ratio + centeringPixels).toInt() >= 0 && (j * ratio + centeringPixels).toInt() >= 0 &&
                    (i * ratio + centeringPixels).toInt() < image.width && (j * ratio + centeringPixels).toInt() < image.height) {
                    scaledImage.setPixel(
                        (i * ratio + centeringPixels).toInt(),
                        (j * ratio + centeringPixels).toInt(),
                        image.getPixel(i, j)
                    )
                }
            }
        }

        imageView2.setImageBitmap(scaledImage)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        private val IMAGE_PICK_CODE = 1000
        private val PERMISSION_CODE = 1001
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission from popup granted
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }
            else {
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
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data)
            val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap
            imageView2.setImageBitmap(thumbNail)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageView2.setImageURI(data?.data)
            currentPicture = ProcessedPicture((imageView2.drawable as BitmapDrawable).bitmap)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    fun Bitmap.saveImage(context: Context): Uri? {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/test_pictures")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "img_${SystemClock.uptimeMillis()}")

            val uri: Uri? =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(this, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
                return uri
            }
        } else {
            val directory =
                File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + separator + "test_pictures")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName =  "img_${SystemClock.uptimeMillis()}"+ ".png"
            val file = File(directory, fileName)
            saveImageToStream(this, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                return Uri.fromFile(file)
            }
        }
        return null
    }


    fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
