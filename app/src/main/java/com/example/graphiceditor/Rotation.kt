package com.example.graphiceditor

class Rotation {

    companion object {
        suspend fun rotate(currentPicture: PixelArray, angle: Double): PixelArray {

            val transMatrix = multiplyMatrices(
                multiplyMatrices(
                    transportMatrix(
                        currentPicture.width.toDouble() / 2,
                        currentPicture.height.toDouble() / 2
                    ),
                    rotateMatrix(angle)
                ),
                transportMatrix(
                    -currentPicture.width.toDouble() / 2,
                    -currentPicture.height.toDouble() / 2
                )
            )

            val rotateTransformations = AffineTransformations(transMatrix)

            return rotateTransformations.transformWithoutFiltering(currentPicture)
        }
    }

}