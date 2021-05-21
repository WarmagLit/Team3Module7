package com.example.graphiceditor

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class Rotation {

    companion object {
        suspend fun rotate(currentPicture: PixelArray, angle: Double): PixelArray {

            val a = currentPicture.width
            val b = currentPicture.height

            val cos = abs(when(angle){
                90.0 -> 0.0
                180.0 -> -1.0
                270.0 -> 0.0
                360.0 -> 1.0
                else -> cos(angle.toRad())
            })
            val sin = abs(when(angle){
                90.0 -> 1.0
                180.0 -> 0.0
                270.0 -> -1.0
                360.0 -> 0.0
                else -> sin(angle.toRad())
            })

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

            return if(angle != 90.0 && angle != 180.0 && angle != 270.0 && angle != 360.0)
                rotateTransformations.transformWithBilinearFiltering(currentPicture, newWidth, newHeight)
            else
                rotateTransformations.transformWithoutFiltering(currentPicture, newWidth, newHeight)
        }
    }

}