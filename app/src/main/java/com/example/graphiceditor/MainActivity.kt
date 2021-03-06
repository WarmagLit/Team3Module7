package com.example.graphiceditor

import android.Manifest.permission.*
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_PICTURES
import android.os.SystemClock
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.*
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.example.graphiceditor.Fragments.*
import com.example.graphiceditor.ImageStorageManager.Companion.saveToInternalStorage
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_filter.*
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream


private const val CAMERA_REQUEST_CODE = 42
private const val IMAGE_PICK_CODE = 1000
private const val PERMISSION_CAMERA_CODE = 1002
private const val IMAGE_SAVE_CODE = 1001

class MainActivity : AppCompatActivity() {
    var mainCurrentPicture = PixelArray(1, 1)
    var mainOriginalImage = mainCurrentPicture.bitmap

    val filterFrag = FilterFragment()
    val transformFrag = TransformFragment()
    val drawFrag = DrawFragment()
    val convertionFrag = ConvertionFragment()
    val otherFrag = OtherFragment()

    var currentFragment = "filterFragment"

    private var currentPicture = PixelArray(1, 1)
    private var affineOldPoints = Array(3) { IntArray(2) }
    private var affineNewPoints = Array(3) { IntArray(2) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appSettingPrefs: SharedPreferences = getSharedPreferences("AppSettingPrefs", 0)
        val isNightModeOn: Boolean = appSettingPrefs.getBoolean("NightMode", false)
        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        makeCurrentFragment(filterFrag)

        var myDrawable = ContextCompat.getDrawable(this, R.drawable.hippo)
        // convert the drawable to a bitmap
        val bitmap = myDrawable!!.toBitmap()
        currentPicture = PixelArray(bitmap)
        saveToInternalStorage(this, bitmap, "myImage")

        makeCurrentFragment(filterFrag)

        setupNavigation()

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            //Toast.makeText(this, "Menu selected", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbarmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var itemview = item.itemId

        when (itemview) {
            R.id.addGallery -> {
                if (checkPermission(READ_EXTERNAL_STORAGE, IMAGE_PICK_CODE)) {
                    pickImageFromGallery()
                }
                true
            }
            R.id.addCamera -> {
                if (checkPermission(CAMERA, CAMERA_REQUEST_CODE)) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA_REQUEST_CODE)
                }
                true
            }
            R.id.saveImg -> {
                if (checkPermission(WRITE_EXTERNAL_STORAGE, IMAGE_SAVE_CODE)) {
                    saveImage(true)
                }
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
                    currentFragment = "filterFragment"
                    makeCurrentFragment(filterFrag)
                    true
                }
                R.id.chooseTransform -> {
                    currentFragment = "transformFragment"
                    makeCurrentFragment(transformFrag)
                    true
                }
                R.id.chooseDraw -> {
                    currentFragment = "drawFragment"
                    makeCurrentFragment(drawFrag)
                    true
                }
                R.id.choose??onversions -> {
                    currentFragment = "conversionFragment"
                    makeCurrentFragment(convertionFrag)
                    true
                }
                R.id.chooseOther -> {
                    currentFragment = "otherFragment"
                    makeCurrentFragment(otherFrag)
                    true
                }
                else -> true
            }
        }
    }

    private fun checkPermission(permission: String, requestCode: Int): Boolean {

        if (checkSelfPermission(permission) == PERMISSION_GRANTED)
            return true

        val permissions = arrayOf(permission)
        requestPermissions(permissions, requestCode)
        return false
    }


    // is called after checkPermission gets the result of asking the permission.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CAMERA_CODE) {
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

    private fun saveImage(withText: Boolean): Uri? {
        var bitmap = (imageView2.drawable as BitmapDrawable).bitmap
        //val bitmap = getImageFromInternalStorage(this, "myImage")!!
        if (currentFragment == "filterFragment") {
            bitmap = filterFrag.currentPicture.bitmap
        }
        if (currentFragment == "drawFragment") {
            bitmap = drawFrag.currentPicture.bitmap
        }
        if (currentFragment == "transformFragment") {
            bitmap = transformFrag.currentPicture.bitmap
        }
        saveToInternalStorage(this.applicationContext, bitmap, "myImage")
        val imageUri: Uri? = bitmap.saveImage(this)
        Toast.makeText(
            this,
            getString(R.string.savedImage, imageUri),
            Toast.LENGTH_SHORT
        ).show()
        return imageUri
    }


    private fun Bitmap.saveImage(context: Context): Uri? {
        return if (Build.VERSION.SDK_INT >= 29) {
            saveFileNewSDK(context)
        } else {
            saveFileOldSDK(context)
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
            File(
                context.getExternalFilesDir(DIRECTORY_PICTURES).toString() +
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


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun getStrings(): List<String> = Filter.values().map { getString(it) }
    private fun getString(filter: Filter) = getString(filter.code)

    private suspend fun apply(filter: Filter) {
        currentPicture = filter.process(currentPicture)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private suspend fun applyZoom(zoomFactor: Double) {
        currentPicture = Zooming.zoom(currentPicture, zoomFactor)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private suspend fun applyRotate(angle: Double) {
        currentPicture = Rotation.rotate(currentPicture, angle)
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    private suspend fun applyTransformation() {
        val oldSystemX = doubleArrayOf(
            affineOldPoints[0][0].toDouble(),
            affineOldPoints[1][0].toDouble(),
            affineOldPoints[2][0].toDouble()
        )
        val oldSystemY = doubleArrayOf(
            affineOldPoints[0][1].toDouble(),
            affineOldPoints[1][1].toDouble(),
            affineOldPoints[2][1].toDouble()
        )
        val newSystemX = doubleArrayOf(
            affineNewPoints[0][0].toDouble(),
            affineNewPoints[1][0].toDouble(),
            affineNewPoints[2][0].toDouble()
        )
        val newSystemY = doubleArrayOf(
            affineNewPoints[0][1].toDouble(),
            affineNewPoints[1][1].toDouble(),
            affineNewPoints[2][1].toDouble()
        )

        val transformations = AffineTransformations(oldSystemX, oldSystemY, newSystemX, newSystemY)

        currentPicture = transformations.transformWithTrilinearFiltering(
            currentPicture,
            currentPicture.width,
            currentPicture.height
        )
        imageView2.setImageBitmap(currentPicture.bitmap)
    }

    /*
    private fun allowPlacePoints(){
        currentPlacePoint = 1
        val pointsBitmap = Bitmap.createBitmap(
            currentPicture.width,
            currentPicture.height,
            Bitmap.Config.ARGB_8888
        )

        pointsField.setImageBitmap(pointsBitmap)
    }



    private fun prohibitPlacePoints(){
        currentPlacePoint = 0
        val pointsBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        pointsField.setImageBitmap(pointsBitmap)
    }
*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data)
            val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap
            imageView2.setImageBitmap(thumbNail)
            mainCurrentPicture = PixelArray((imageView2.drawable as BitmapDrawable).bitmap)
            mainOriginalImage = (imageView2.drawable as BitmapDrawable).bitmap
            saveToInternalStorage(this, mainOriginalImage, "myImage")
            if (currentFragment == "filterFragment") {
                filterFrag.reloadImage()
            }
            if (currentFragment == "drawFragment") {
                drawFrag.reloadImage()
            }
            if (currentFragment == "transformFragment") {
                transformFrag.reloadImage()
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageView2.setImageURI(data?.data)
            mainCurrentPicture = PixelArray((imageView2.drawable as BitmapDrawable).bitmap)
            mainOriginalImage = (imageView2.drawable as BitmapDrawable).bitmap
            saveToInternalStorage(this, mainOriginalImage, "myImage")
            if (currentFragment == "filterFragment") {
                filterFrag.reloadImage()
            }
            if (currentFragment == "drawFragment") {
                drawFrag.reloadImage()
            }
            if (currentFragment == "transformFragment") {
                transformFrag.reloadImage()
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun Bitmap.addPoint(x: Int, y: Int, color: Int) {
        val r = 20

        for (i in x - r..x + r) {
            if (i !in 0 until width) continue
            for (j in y - r..y + r) {
                if (j !in 0 until height || (x - i) * (x - i) + (y - j) * (y - j) > r * r) continue
                setPixel(i, j, color)
            }

        }
    }


}