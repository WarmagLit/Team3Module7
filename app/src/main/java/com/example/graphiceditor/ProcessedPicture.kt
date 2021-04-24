package com.example.graphiceditor

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

class ProcessedPicture(currentBitmap: Bitmap) {
    var bitmap = currentBitmap
    var pixelsArray = getPixelsArray(bitmap)

    fun getPixelsArray(bitmap: Bitmap) : Array<Array<PixelARGB>>{
        var pixels = arrayOf<Array<PixelARGB>>()
        for (x in 0..bitmap.width-1){
            var pixelsLine = arrayOf<PixelARGB>()
            for (y in 0..bitmap.height-1){
                var pixel = bitmap.getPixel(x, y)
                pixelsLine += PixelARGB(Color.alpha(pixel), Color.red(pixel), Color.green(pixel), Color.blue(pixel))
            }
            pixels += pixelsLine
        }
        return pixels
    }

    fun updateBitmap(){
        var bitmap2 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        for (x in 0..bitmap2.width - 1) {
            for (y in 0..bitmap2.height - 1) {
                bitmap2.setPixel(
                    x,
                    y,
                    Color.argb(pixelsArray[x][y].a, pixelsArray[x][y].r, pixelsArray[x][y].g, pixelsArray[x][y].b)
                )
            }
        }
        bitmap = bitmap2;
    }
}