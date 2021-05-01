package com.example.graphiceditor

import android.Manifest.permission.*
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Build
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
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
import kotlinx.android.synthetic.main.editor_v2.*

import java.io.File.separator

import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore.Images.Media.*
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.math.*

private const val REQUEST_CODE = 42
private const val CAMERA_REQUEST_CODE = 42
private const val IMAGE_PICK_CODE = 1000
private const val READ_STORAGE_CODE = 1001
private const val WRITE_STORAGE_CODE = 100
private const val PERMISSION_CAMERA_CODE = 1002

class MainActivity : AppCompatActivity() {
    var currentPicture = PixelArray(1, 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView2.setImageResource(R.drawable.hippo)
        currentPicture = PixelArray((imageView2.drawable as BitmapDrawable).bitmap)

        initButtons()
        initOptionList()
        initZoomer()
        setupNavigation()
    }


    private fun setupNavigation() {
        val navView: BottomNavigationView = findViewById(R.id.bottomNavBar)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuAbout -> {
                    Toast.makeText(this, "Menu selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.addGallery -> {
                    pickImageFromGallery()
                    true
                }
                R.id.addCamera -> {
                    Log.d("TAG", "Camera button click")

                    if(checkPermission(CAMERA, CAMERA_REQUEST_CODE)){
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, CAMERA_REQUEST_CODE)
                    }
                    true
                }
                R.id.saveImg -> {
                    saveImage()
                    true
                }
                else -> true
            }
        }
    }

    private fun initButtons() {
        filtersLayout.btnMain.setOnClickListener {
            imageView2.setImageResource(R.drawable.hippo)
            currentPicture = PixelArray((imageView2.drawable as BitmapDrawable).bitmap)
        }

        filtersLayout.btnBlue.setOnClickListener {
            val filter = of("Blue filter")
            Log.d("TAG", filter.toString())
            if (filter != Filter.NONE) {
                CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
            }
        }

        filtersLayout.btnRed.setOnClickListener {
            val filter = of("Red filter")
            Log.d("TAG", filter.toString())
            if (filter != Filter.NONE) {
                CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
            }
        }

        filtersLayout.btnGray.setOnClickListener {
            val filter = of("Gray filter")
            Log.d("TAG", filter.toString())
            if (filter != Filter.NONE) {
                CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
            }
        }

        filtersLayout.btnGreen.setOnClickListener {
            val filter = of("Green filter")
            Log.d("TAG", filter.toString())
            if (filter != Filter.NONE) {
                CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
            }
        }

        filtersLayout.btnGreen.setOnClickListener {
            val filter = of("Green filter")
            Log.d("TAG", filter.toString())
            if (filter != Filter.NONE) {
                CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
            }
        }

        filtersLayout.btnBlur.setOnClickListener {
            val filter = of("Blur")
            Log.d("TAG", filter.toString())
            if (filter != Filter.NONE) {
                CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
            }
        }

        filtersLayout.btnDiagonal.setOnClickListener {
            val filter = of("Diagonal sepia")
            Log.d("TAG", filter.toString())
            if (filter != Filter.NONE) {
                CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
            }
        }

        filtersLayout.btnSwap.setOnClickListener {
            val filter = of("Swap colors")
            Log.d("TAG", filter.toString())
            if (filter != Filter.NONE) {
                CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
            }
        }

        filtersLayout.btnNegative.setOnClickListener {
            val filter = of("Negative")
            Log.d("TAG", filter.toString())
            if (filter != Filter.NONE) {
                CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
            }
        }

        filtersLayout.btnEdge.setOnClickListener {
            val filter = of("Edge detection")
            Log.d("TAG", filter.toString())
            if (filter != Filter.NONE) {
                CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
            }
        }

        filtersLayout.btnEmboss.setOnClickListener {
            val filter = of("Emboss")
            Log.d("TAG", filter.toString())
            if (filter != Filter.NONE) {
                CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
            }
        }

    }


    private fun checkPermission(permission: String, requestCode: Int): Boolean{
        if (checkSelfPermission(permission) == PERMISSION_GRANTED)
            return true

        val permissions = arrayOf(permission)
        requestPermissions(permissions, requestCode)
        return false
    }


    // is called after checkPermission gets the result of asking the permission.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_CAMERA_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                pickImageFromGallery()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.permissionDenied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveImage() {
        val bitmap = (imageView2.drawable as BitmapDrawable).bitmap
        val imageUri:Uri? = bitmap.saveImage(this)
        Toast.makeText(
            this,
            getString(R.string.savedImage, imageUri),
            Toast.LENGTH_SHORT
        ).show()
    }
    private fun Bitmap.saveImage(context: Context): Uri? {
        if (Build.VERSION.SDK_INT >= 29) {
            return saveFileNewSDK(context)
        } else {
            return saveFileOldSDK(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun Bitmap.saveFileNewSDK(context: Context): Uri? {
        val values = fileInfo()

        val uri: Uri =
            context.contentResolver.insert(EXTERNAL_CONTENT_URI, values)
                ?: return null
        saveImageToStream(context.contentResolver.openOutputStream(uri))
        values.put(IS_PENDING, false)
        context.contentResolver.update(uri, values, null, null)
        return uri
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fileInfo(): ContentValues {
        val values = ContentValues()
        values.put(MIME_TYPE, "image/jpeg")
        values.put(DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(DATE_TAKEN, System.currentTimeMillis())
        values.put(RELATIVE_PATH, "Pictures/test_pictures")
        values.put(IS_PENDING, true)
        values.put(DISPLAY_NAME, "img_${SystemClock.uptimeMillis()}")
        return values
    }

    private fun Bitmap.saveImageToStream(outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun Bitmap.saveFileOldSDK(context: Context): Uri? {
        val directory =
            File(context.getExternalFilesDir(DIRECTORY_PICTURES).toString() +
                    separator + "test_pictures"
            )
        if (!directory.exists())
            directory.mkdirs()

        val fileName = "img_${SystemClock.uptimeMillis()}.png"
        val file = File(directory, fileName)
        saveImageToStream(FileOutputStream(file))

        val values = ContentValues()
        values.put(DATA, file.absolutePath)
        context.contentResolver.insert(EXTERNAL_CONTENT_URI, values)

        return Uri.fromFile(file)
    }



    private fun initZoomer() {
    /*
        val zoomingInput: EditText = findViewById(R.id.zoomingInput)
        zoomingInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val zoomFactor = zoomingInput.text.toDouble()
                if (abs(zoomFactor - 1.0) < 0.01) {
                    return@setOnFocusChangeListener
                }
                currentPicture = Zooming.zoom(currentPicture, zoomFactor)
                zoomingInput.setText("1")
                imageView2.setImageBitmap(currentPicture.bitmap)
            }
        }
        */
    }

    private fun initOptionList() {

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            getStrings()
        )

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)


        val spinnerFilters: Spinner = findViewById(R.id.filters)
        spinnerFilters.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                val selected: String = spinnerFilters.selectedItem.toString();

                //textView2.text = getString(R.string.spinner, selected)
                Log.d("TAG", selected)
                val filter = of(selected)
                Log.d("TAG", filter.toString())
                if (filter != Filter.NONE) {
                    CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
                    spinnerFilters.setSelection(adapter.getPosition(getString(Filter.NONE)))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }


    }

    private suspend fun apply(filter: Filter) {
        currentPicture = filter.process(currentPicture)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private fun of(string: String): Filter{
        Filter.values().forEach {
            if(getString(it) == string)
                return it
        }
        return Filter.NONE
    }

    private fun getStrings(): List<String> = Filter.values().map { getString(it) }
    private fun getString(filter: Filter) = getString(filter.code)

//    suspend fun apply(told: Filter) {
//        currentPicture = told.process(currentPicture)
//    }

//    fun imageRepair(image: Bitmap): Bitmap {
//        val repairImage = image.copy(Bitmap.Config.ARGB_8888, true)
//        for (i in  0 until repairImage.width-1) {
//            for (j in 0 until repairImage.height) {
//                if (image.getPixel(i, j) == Color.BLACK) {
//                    repairImage.setPixel(i, j, image.getPixel(i + 1, j))
//                }
//            }
//        }
//        return repairImage
//    }
//
//    fun scaling(degree: Int) {
//        val image = (imageView2.drawable as BitmapDrawable).bitmap
//        val scaledImage = image.copy(Bitmap.Config.ARGB_8888, true)
//
//        val centerX = (image.width / 2.0).roundToInt()
//        val centerY = (image.height / 2.0).roundToInt()
//
//        val a = (cos(degree*PI / 180) *(- centerX) - sin(degree*PI / 180)*(- centerY.toDouble()) + centerX).toInt()
//        val b = (sin(degree*PI / 180) *(- centerX) + cos(degree*PI / 180)*(image.height - 1 - centerY.toDouble()) + centerY).toInt()
//        val c = image.width - a
//        val d = image.height - b
//        val ratio = sqrt(a*a*1.0+b*b*1.0) / image.height
//        val centeringPixels = ((scaledImage.width - scaledImage.width * ratio)/2)
//
//        Log.d("d", a.toString() + " " + b.toString() + " " + c.toString() + " "+ d.toString() + " "+ ((a*b + c*d)/1.0).toString()+ " " + ratio.toString())
//        //ratio = 0.8
//        for (i in 0 until scaledImage.width) {
//            for (j in 0 until scaledImage.height) {
//                if ((i * ratio + centeringPixels).toInt() >= 0 && (j * ratio + centeringPixels).toInt() >= 0 &&
//                    (i * ratio + centeringPixels).toInt() < image.width && (j * ratio + centeringPixels).toInt() < image.height) {
//                    scaledImage.setPixel(
//                        (i * ratio + centeringPixels).toInt(),
//                        (j * ratio + centeringPixels).toInt(),
//                        image.getPixel(i, j)
//                    )
//                }
//            }
//        }
//
//        imageView2.setImageBitmap(scaledImage)
//    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data)
            val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap
            imageView2.setImageBitmap(thumbNail)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageView2.setImageURI(data?.data)
            currentPicture = PixelArray((imageView2.drawable as BitmapDrawable).bitmap)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}