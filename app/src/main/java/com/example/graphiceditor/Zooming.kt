package com.example.graphiceditor

class Zooming() {

    companion object {
        suspend fun zoom(currentPicture: PixelArray, scale: Double): PixelArray {
            val newWidth = (currentPicture.width * scale).toInt()
            val newHeight = (currentPicture.height * scale).toInt()

            val transMatrix = zoomMatrix(scale, scale)

            val zoomTransformations = AffineTransformations(transMatrix)

            return if (scale >= 0.5)
                zoomTransformations.transformWithBilinearFiltering(currentPicture, newWidth, newHeight)
            else
                zoomTransformations.transformWithTrilinearFiltering(currentPicture, newWidth, newHeight)
        }
    }
}