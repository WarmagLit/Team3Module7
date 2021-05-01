package com.example.graphiceditor

class Zooming() {

    companion object {
        suspend fun zoom(currentPicture: PixelArray, scale: Double): PixelArray {

            val transMatrix = multiplyMatrices(
                transportMatrix(
                    (1 - scale) * currentPicture.width.toDouble() / 2,
                    (1 - scale) * currentPicture.height.toDouble() / 2
                ),
                zoomMatrix(scale, scale)
            )

            val zoomTransformations = AffineTransformations(transMatrix)

            return if (scale >= 0.5)
                zoomTransformations.transformWithBilinearFiltering(currentPicture)
            else
                zoomTransformations.transformWithTrilinearFiltering(currentPicture)
        }
    }
}