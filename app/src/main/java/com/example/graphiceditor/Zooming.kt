package com.example.graphiceditor

class Zooming() {

    companion object {
        fun zoom(currentPicture: PixelArray, scale: Double): PixelArray {
            val transMatrix = arrayOf(
                doubleArrayOf(scale, 0.0, 0.0),
                doubleArrayOf(0.0, scale, 0.0),
                doubleArrayOf(0.0, 0.0, 1.0)
            )

            val zoomTransformations = AffineTransformations(transMatrix)
            val newPicture: PixelArray

            newPicture = if (scale > 1)
                zoomTransformations.transformWithBilinearFiltering(currentPicture)
            else
                zoomTransformations.transformWithTrilinearFiltering(currentPicture)

            return newPicture
        }
    }
}