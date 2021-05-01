package com.example.graphiceditor

class Zooming() {

    companion object {
        suspend fun zoom(currentPicture: PixelArray, scale: Double): PixelArray {

            val newPicture: PixelArray

            val transMatrix = arrayOf(
                doubleArrayOf(
                    scale,
                    0.0,
                    0.0
                ),
                doubleArrayOf(
                    0.0,
                    scale,
                    0.0
                ),
                doubleArrayOf(
                    (1 - scale) * currentPicture.width / 2,
                    (1 - scale) * currentPicture.height / 2,
                    1.0
                )
            )

            val zoomTransformations = AffineTransformations(transMatrix)

            newPicture = if (scale > 1)
                zoomTransformations.transformWithBilinearFiltering(currentPicture)
            else
                zoomTransformations.transformWithTrilinearFiltering(currentPicture)

            return newPicture
        }
    }
}