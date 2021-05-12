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
import android.widget.*
import kotlinx.android.synthetic.main.editor.*
import kotlinx.android.synthetic.main.editor_v2.*

import java.io.File.separator

import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore.Images.Media.*
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.graphiceditor.Fragments.filterFragment
import com.example.graphiceditor.Fragments.otherFragment
import com.example.graphiceditor.Fragments.transformFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_filter.*
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
    var mainCurrentPicture = PixelArray(1, 1)
    lateinit var mainOriginalImage : Bitmap

    val filterFrag = filterFragment()
    val transformFrag = transformFragment()
    val otherFrag = otherFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        makeCurrentFragment(filterFrag)

        //imageView2.setImageResource(R.drawable.hippo)

        //currentPicture = PixelArray((imageView2.drawable as BitmapDrawable).bitmap)
        //originalImage = (imageView2.drawable as BitmapDrawable).bitmap

        //initButtons()
        //initZoomer()
        //initRotater()
        //initTransformator()

        setupNavigation()

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            Toast.makeText(this, "Menu selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbarmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var itemview = item.itemId

        when(itemview) {
            R.id.addGallery -> {
                pickImageFromGallery()
                true
            }
            R.id.addCamera-> {
                if(checkPermission(CAMERA, CAMERA_REQUEST_CODE)){
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA_REQUEST_CODE)
                }
                true
            }
            R.id.saveImg-> {
                saveImage(true)
                true
            }
            else -> true
        }
        return true
    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }



    private fun setupNavigation() {
        val navView: BottomNavigationView = findViewById(R.id.bottomNavBar)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.chooseFilters -> {
                    makeCurrentFragment(filterFrag)
                    true
                }
                R.id.chooseTransform -> {
                    makeCurrentFragment(transformFrag)
                    true
                }
                R.id.chooseDraw -> {

                    true
                }
                R.id.chooseOther -> {
                    makeCurrentFragment(otherFrag)
                    true
                }
                else -> true
            }
        }
    }
/*
    private fun initButtons() {
        filtersLayout.btnMain.setOnClickListener {
            imageView2.setImageBitmap(originalImage)
            currentPicture = PixelArray((imageView2.drawable as BitmapDrawable).bitmap)
        }

        val arrayBtn = arrayOf(
            filtersLayout.btnRed,
            filtersLayout.btnGreen,
            filtersLayout.btnBlue,
            filtersLayout.btnGray,
            filtersLayout.btnDiagonal,
            filtersLayout.btnSwap,
            filtersLayout.btnNegative,
            filtersLayout.btnBlur,
            filtersLayout.btnEdge,
            filtersLayout.btnEmboss
        )

        val arrayStrings = getStrings()

        for (i in arrayBtn.indices){
            arrayBtn[i].setOnClickListener {
                val filter = of(arrayStrings[i])
                Log.d("TAG", filter.toString())
                if (filter != Filter.NONE) {
                    CoroutineScope(EmptyCoroutineContext).async { apply(filter) }
                }
            }
        }
    }

*/
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

    private fun saveImage(withText: Boolean):Uri? {
        val bitmap = (imageView2.drawable as BitmapDrawable).bitmap
        val imageUri:Uri? = bitmap.saveImage(this)
        if (withText) {

            Toast.makeText(
            this,
            getString(R.string.savedImage, imageUri),
            Toast.LENGTH_SHORT
        ).show()
        }
        return imageUri
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
/*
    private fun initZoomer() {
        zoomingInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val zoomFactor = zoomingInput.text.toDouble()
                if (abs(zoomFactor - 1.0) < 0.01) {
                    return@setOnFocusChangeListener
                }
                CoroutineScope(EmptyCoroutineContext).async { applyZoom(zoomFactor) }
                zoomingInput.setText("1.0")
            }
        }
    }

    private fun initRotater() {
        rotationInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val angle = rotationInput.text.toDouble()
                if (abs(angle - 0.0) < 0.01) {
                    return@setOnFocusChangeListener
                }
                CoroutineScope(EmptyCoroutineContext).async { applyRotate(angle) }
                rotationInput.setText("0.0")
            }
        }
    }

    private fun initTransformator() {
        affineButton.setOnClickListener {
            if (x1.text.isEmpty() || x2.text.isEmpty() || x3.text.isEmpty() ||
                x4.text.isEmpty() || x5.text.isEmpty() || x6.text.isEmpty() ||
                y1.text.isEmpty() || y2.text.isEmpty() || y3.text.isEmpty() ||
                y4.text.isEmpty() || y5.text.isEmpty() || y6.text.isEmpty()){
                return@setOnClickListener
            }

            CoroutineScope(EmptyCoroutineContext).async { applyTransformation() }
        }
    }

    private fun of(string: String): Filter{
        Filter.values().forEach {
            if(getString(it) == string)
                return it
        }
        return Filter.NONE
    }
    */
/*
    private suspend fun apply(filter: Filter) {
        currentPicture = filter.process(currentPicture)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private suspend fun applyZoom(zoomFactor: Double){
        currentPicture = Zooming.zoom(currentPicture, zoomFactor)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private suspend fun applyRotate(angle: Double){
        currentPicture = Rotation.rotate(currentPicture, angle)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private suspend fun applyTransformation(){
        val oldSystemX = doubleArrayOf(
            x1.text.toDouble(),
            x2.text.toDouble(),
            x3.text.toDouble()
        )

        val oldSystemY = doubleArrayOf(
            y1.text.toDouble(),
            y2.text.toDouble(),
            y3.text.toDouble()
        )

        val newSystemX = doubleArrayOf(
            x4.text.toDouble(),
            x5.text.toDouble(),
            x6.text.toDouble()
        )

        val newSystemY = doubleArrayOf(
            y4.text.toDouble(),
            y5.text.toDouble(),
            y6.text.toDouble()
        )

        val transformations = AffineTransformations(
            oldSystemX, oldSystemY,
            newSystemX, newSystemY
        )

        currentPicture = transformations.transformWithBilinearFiltering(currentPicture)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }
*/
    private fun getStrings(): List<String> = Filter.values().map { getString(it) }
    private fun getString(filter: Filter) = getString(filter.code)

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
            mainCurrentPicture = PixelArray((imageView2.drawable as BitmapDrawable).bitmap)
            mainOriginalImage = (imageView2.drawable as BitmapDrawable).bitmap
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageView2.setImageURI(data?.data)
            mainCurrentPicture = PixelArray((imageView2.drawable as BitmapDrawable).bitmap)
            mainOriginalImage = (imageView2.drawable as BitmapDrawable).bitmap
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}