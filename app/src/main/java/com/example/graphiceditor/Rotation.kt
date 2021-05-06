package com.example.graphiceditor

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class Rotation {

    companion object {
        suspend fun rotate(currentPicture: PixelArray, angle: Double): PixelArray {

            val a = currentPicture.width
            val b = currentPicture.height

            val cos = abs(cos(angle.toRad()))
            val sin = abs(sin(angle.toRad()))

            val newWidth = (a * cos + b * sin).toInt()
            val newHeight = (a * sin + b * cos).toInt()

            val transMatrix = multiplyMatrices(
                multiplyMatrices(
                    transportMatrix(
                        newWidth.toDouble() / 2,
                        newHeight.toDouble() / 2
                    ),
                    rotateMatrix(angle)
                ),
                transportMatrix(
                    -currentPicture.width.toDouble() / 2,
                    -currentPicture.height.toDouble() / 2
                )
            )

            val rotateTransformations = AffineTransformations(transMatrix)

            return rotateTransformations.transformWithBilinearFiltering(currentPicture, newWidth, newHeight)
        }
    }

}