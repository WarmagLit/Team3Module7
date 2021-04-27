package com.example.graphiceditor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
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
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import kotlin.math.log
import android.graphics.Matrix as Matrix

private const val REQUEST_CODE = 42
private const val CAMERA_PERMISSION_CODE = 1
private const val CAMERA_REQUEST_CODE = 1
class MainActivity : AppCompatActivity() {

    var isImageVertical = true
    val PI = 3.1415926

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
                    pickImageFromGallery()

                }
            } else {
                //system OS is < Marshmallow
                pickImageFromGallery()
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

        buttonNewPage.setOnClickListener {
            setContentView(R.layout.editor)
            val image = ProcessedPicture((imageView2.getDrawable() as BitmapDrawable).bitmap)
            imageView.setImageBitmap(image.bitmap)
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
                    rotateImage(90)
                    isImageVertical = !isImageVertical
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
                if (parent.getItemAtPosition(position).toString() == "Маштабирование") {
                    //scaling(15)
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


    fun rotateImage(degree: Int) {
        var bitmap = (imageView2.getDrawable() as BitmapDrawable).bitmap

        if (degree % 90 == 0) {
            if (isImageVertical == false) {
                bitmap = verticalReflectBitmap(bitmap)
            }
            bitmap = transposeBitmap(bitmap)
        }
        else {
            bitmap = degreeRotation(bitmap, degree)
            bitmap = imageRepair(bitmap)
        }
        imageView2.setImageBitmap(bitmap)
    }

    fun transposeBitmap(image: Bitmap):Bitmap {
        val transposedImage = Bitmap.createBitmap(image.height, image.width, Bitmap.Config.ARGB_8888)

        for (i in  0..transposedImage.width-1) {
            for (j in  0..transposedImage.height-1) {
                transposedImage.setPixel(i, j, image.getPixel(j, i))
            }
        }

        return transposedImage
    }

    fun verticalReflectBitmap(image: Bitmap ):Bitmap {
        val reflectedImage = image.copy(Bitmap.Config.ARGB_8888, true)

        for (i in  0..reflectedImage.width-1) {
            for (j in  0..reflectedImage.height-1) {
                reflectedImage.setPixel(i, j, image.getPixel(reflectedImage.width - i - 1, reflectedImage.height - j - 1))
            }
        }

        return reflectedImage
    }

    fun degreeRotation(image: Bitmap, degree: Int):Bitmap {
        val rotatedImage = image.copy(Bitmap.Config.ARGB_8888, true)
        val centerX = Math.round(image.width / 2.0)
        val centerY = Math.round(image.height / 2.0)

        var x = 0
        var y = 0

        for (i in  0..rotatedImage.width-1) {
            for (j in  0..rotatedImage.height-1) {
                rotatedImage.setPixel(i, j, Color.BLACK)
            }
        }

        for (i in  0..rotatedImage.width-1) {
            for (j in  0..rotatedImage.height-1) {
                x = (Math.cos(degree*PI / 180)*(i - centerX) - Math.sin(degree*PI / 180)*(j - centerY.toDouble()) + centerX).toInt()
                y = (Math.sin(degree*PI / 180)*(i - centerX) + Math.cos(degree*PI / 180)*(j - centerY.toDouble()) + centerY).toInt()
                if (x >= 0 && x <= rotatedImage.width-1 && y >= 0 && y <= rotatedImage.height-1) {
                    rotatedImage.setPixel(x, y, image.getPixel(i, j))
                }
            }
        }

        return rotatedImage
    }

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
            val fileName =  "img_${SystemClock.uptimeMillis()}"+ ".jpeg"
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
