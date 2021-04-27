package com.example.graphiceditor

import android.graphics.Bitmap

class Zooming() {

    companion object {
        fun zoom(currentPicture: ProcessedPicture, scale: Double): Bitmap {
            val transMatrix = arrayOf(
                doubleArrayOf(scale, 0.0, 0.0),
                doubleArrayOf(0.0, scale, 0.0),
                doubleArrayOf(0.0, 0.0, 1.0)
            )

            val zoomTransformations = AffineTransformations(transMatrix)
            val newBitmap = Bitmap.createBitmap(currentPicture.bitmap.width, currentPicture.bitmap.height, Bitmap.Config.ARGB_8888)
            val biasX = ((1.0 - scale) * newBitmap.width / 2).toInt()
            val biasY = ((1.0 - scale) * newBitmap.height / 2).toInt()

            for (x in 0..newBitmap.width - 1) {
                for (y in 0..newBitmap.height - 1) {
                    val oldCoordinates = zoomTransformations.inverseTransition(intArrayOf(x, y, 1))
                    var oldX = oldCoordinates[0]
                    var oldY = oldCoordinates[1]
                    var newX = x
                    var newY = y

                    if (scale < 1.0){
                        newX += biasX
                        newY += biasY
                    }
                    else{
                        oldX -= (biasX/scale).toInt()
                        oldY -= (biasY/scale).toInt()
                    }

                    if (oldX in 0 .. currentPicture.bitmap.width - 1 && oldY in 0 .. currentPicture.bitmap.height - 1) {
                        if (newX in 0 .. newBitmap.width - 1 && newY in 0 .. newBitmap.height - 1) {
                            newBitmap.setPixel(newX, newY, currentPicture.bitmap.getPixel(oldX, oldY))
                        }
                    }
                }
            }

            return newBitmap
        }
    }
}