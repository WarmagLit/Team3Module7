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

            val zoomingFactor1 = a / (a * abs(cos(angle.toRad())) + b * abs(sin(angle.toRad())))
            val zoomingFactor2 = b / (a * abs(sin(angle.toRad())) + b * abs(cos(angle.toRad())))

            val transMatrix = multiplyMatrices(
                multiplyMatrices(
                    multiplyMatrices(
                        transportMatrix(
                            currentPicture.width.toDouble() / 2,
                            currentPicture.height.toDouble() / 2
                        ),
                        zoomMatrix(
                            min(zoomingFactor1, zoomingFactor2),
                            min(zoomingFactor1, zoomingFactor2)
                        )
                    ),
                    rotateMatrix(angle)
                ),
                transportMatrix(
                    -currentPicture.width.toDouble() / 2,
                    -currentPicture.height.toDouble() / 2
                )
            )

            val rotateTransformations = AffineTransformations(transMatrix)

            return rotateTransformations.transformWithBilinearFiltering(currentPicture)
        }
    }

}