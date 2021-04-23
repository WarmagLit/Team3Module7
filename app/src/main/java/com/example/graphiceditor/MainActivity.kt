package com.example.graphiceditor

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.app.ComponentActivity.ExtraData
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.get
import androidx.core.graphics.toColor
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import kotlin.math.log
import android.graphics.Matrix as Matrix

private const val REQUEST_CODE = 42
private const val CAMERA_PERMISSION_CODE = 1
private const val CAMERA_REQUEST_CODE = 1
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
        val colors = arrayOf("-Не выбрано-","Синий фильтр","Серый фильтр","Сепия","Повернуть картинку на 90 град.")


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
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // Display the selected item text on text view
                text_view.text =
                    "Spinner selected : ${parent.getItemAtPosition(position).toString()}"

                if (parent.getItemAtPosition(position).toString() == "Синий фильтр") {
                    BlueFilter()
                    spinner.setSelection(adapter.getPosition("-Не выбрано-"))
                }
                if (parent.getItemAtPosition(position)
                        .toString() == "Повернуть картинку на 90 град."
                ) {
                    rotateImage()
                    spinner.setSelection(adapter.getPosition("-Не выбрано-"))
                }
                if (parent.getItemAtPosition(position).toString() == "Серый фильтр") {
                    GreyFilter()
                    spinner.setSelection(adapter.getPosition("-Не выбрано-"))
                }
                if (parent.getItemAtPosition(position).toString() == "Сепия") {
                    SepiaFilter()
                    spinner.setSelection(adapter.getPosition("-Не выбрано-"))
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

    }

    class ARGB {
        var A = 0
        var R = 0
        var G = 0
        var B = 0
    }

    fun BlueFilter() {
        Log.d("TAG", "Blue Filter")
        val image = ProcessedPicture((imageView2.getDrawable() as BitmapDrawable).bitmap)

        for (i in 0..image.bitmap.width - 1) {
            for (j in 0..image.bitmap.height - 1) {
                image.pixelsArray[i][j].r /= 10
                image.pixelsArray[i][j].r *= 7
                image.pixelsArray[i][j].g /= 10
                image.pixelsArray[i][j].g *= 7
            }
        }

        image.updateBitmap()

        imageView2.setImageBitmap(image.bitmap)
    }

    fun GreyFilter() {
        Log.d("TAG", "Grey Filter")
        val image = ProcessedPicture((imageView2.getDrawable() as BitmapDrawable).bitmap)
        var mid = 0

        for (i in 0..image.bitmap.width - 1) {
            for (j in 0..image.bitmap.height - 1) {
                mid = (image.pixelsArray[i][j].r + image.pixelsArray[i][j].g + image.pixelsArray[i][j].b) / 3
                image.pixelsArray[i][j].r = mid
                image.pixelsArray[i][j].g = mid
                image.pixelsArray[i][j].b = mid
            }
        }

        image.updateBitmap()

        imageView2.setImageBitmap(image.bitmap)
    }


    fun SepiaFilter() {
        Log.d("TAG", "Sepia Filter")
        val bitmap = (imageView2.getDrawable() as BitmapDrawable).bitmap
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        var argbArray = averageARGB(mutableBitmap)


        for (i in  0..bitmap.width-1) {
            for (j in  0..bitmap.height-1) {
                    val oldR = argbArray[i][j].R
                    val oldG = argbArray[i][j].G
                    val oldB = argbArray[i][j].B
                    argbArray[i][j].R = (oldR * 0.393 + oldG * 0.769 + oldB * 0.189).toInt()
                    argbArray[i][j].G = (oldR * 0.349 + oldG * 0.686 + oldB * 0.168).toInt()
                    argbArray[i][j].B = (oldR * 0.272 + oldG * 0.534 + oldB * 0.131).toInt()

                    if (argbArray[i][j].R > 255) argbArray[i][j].R = 255
                    if (argbArray[i][j].G > 255) argbArray[i][j].G = 255
                    if (argbArray[i][j].B > 255) argbArray[i][j].B = 255

            }
        }

        val bitmap2 = ARGBtoBitmap(argbArray)

        imageView2.setImageBitmap(bitmap2)
    }


    fun rotateImage() {
        val bitmap = (imageView2.getDrawable() as BitmapDrawable).bitmap

        val rotatedBitmap = bitmap.rotate(90f)
        imageView2.setImageBitmap(rotatedBitmap)
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    fun averageARGB(bitmap: Bitmap): Array<Array<ARGB>> {
        var pixelColor = 0
        var width = bitmap.width
        var height = bitmap.height
        var size = width * height

        var A: Array<IntArray> = Array(width) { IntArray(height) { 0 } }
        var R: Array<IntArray> = Array(width) { IntArray(height) { 0 } }
        var G: Array<IntArray> = Array(width) { IntArray(height) { 0 } }
        var B: Array<IntArray> = Array(width) { IntArray(height) { 0 } }


        var argb: Array<Array<ARGB>> = Array(width) { Array(height) { ARGB() } }

        for (x in 0..width - 1) {
            for (y in 0..height - 1) {
                pixelColor = bitmap.getPixel(x, y)
                A[x][y] = Color.alpha(pixelColor)
                R[x][y] = Color.red(pixelColor)
                G[x][y] = Color.green(pixelColor)
                B[x][y] = Color.blue(pixelColor)
                argb[x][y].A = A[x][y]
                argb[x][y].R = R[x][y]
                argb[x][y].G = G[x][y]
                argb[x][y].B = B[x][y]
            }
        }
        return argb
    }

    fun ARGBtoBitmap(argb: Array<Array<ARGB>>): Bitmap {
        val bitmap = (imageView2.getDrawable() as BitmapDrawable).bitmap
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        var width = bitmap.width
        var height = bitmap.height

        for (x in 0..width - 1) {
            for (y in 0..height - 1) {
                mutableBitmap.setPixel(
                    x,
                    y,
                    Color.argb(argb[x][y].A, argb[x][y].R, argb[x][y].G, argb[x][y].B)
                )
            }
        }

        return mutableBitmap
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
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun saveImageToStorage() {
        val externalStorageState = Environment.getExternalStorageState()
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
            val storageDirectory = Environment.getExternalStorageDirectory().toString();//not working
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
