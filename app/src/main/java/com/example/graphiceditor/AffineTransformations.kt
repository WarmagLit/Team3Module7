package com.example.graphiceditor

import android.graphics.Bitmap
import kotlin.math.*

class AffineTransformations {
    private var matrix: Array<DoubleArray>
    private var inverseMatrix: Array<DoubleArray>

    constructor(transMatrix: Array<DoubleArray>) {
        matrix = transMatrix
        inverseMatrix = calculateInverseMatrix(matrix)
    }

    constructor(
        oldSystemX: DoubleArray, oldSystemY: DoubleArray,
        newSystemX: DoubleArray, newSystemY: DoubleArray
    ) {
        val oldSystemInverseMatrix = calculateInverseMatrix(
            arrayOf(
                doubleArrayOf(oldSystemX[0], oldSystemX[1], oldSystemX[2]),
                doubleArrayOf(oldSystemY[0], oldSystemY[1], oldSystemY[2]),
                doubleArrayOf(1.0, 1.0, 1.0)
            )
        )

        val newSystemMatrix = arrayOf(
            doubleArrayOf(newSystemX[0], newSystemX[1], newSystemX[2]),
            doubleArrayOf(newSystemY[0], newSystemY[1], newSystemY[2]),
            doubleArrayOf(1.0, 1.0, 1.0)
        )

        matrix = multiplyMatrices(
            newSystemMatrix,
            oldSystemInverseMatrix
        )
        inverseMatrix = calculateInverseMatrix(matrix)
    }

    private fun calculateInverseMatrix(matrix: Array<DoubleArray>): Array<DoubleArray> {
        val inverseMatrix = arrayOf(DoubleArray(3), DoubleArray(3), DoubleArray(3))
        val det = calculateDet(matrix)

        inverseMatrix[0][0] = (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1]) / det
        inverseMatrix[1][0] = -(matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0]) / det
        inverseMatrix[2][0] = (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]) / det

        inverseMatrix[0][1] = -(matrix[0][1] * matrix[2][2] - matrix[0][2] * matrix[2][1]) / det
        inverseMatrix[1][1] = (matrix[0][0] * matrix[2][2] - matrix[0][2] * matrix[2][0]) / det
        inverseMatrix[2][1] = -(matrix[0][0] * matrix[2][1] - matrix[0][1] * matrix[2][0]) / det

        inverseMatrix[0][2] = (matrix[0][1] * matrix[1][2] - matrix[0][2] * matrix[1][1]) / det
        inverseMatrix[1][2] = -(matrix[0][0] * matrix[1][2] - matrix[0][2] * matrix[1][0]) / det
        inverseMatrix[2][2] = (matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]) / det

        return inverseMatrix
    }

    private fun calculateDet(matrix: Array<DoubleArray>): Double {
        var det = 0.0
        det += matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1])
        det -= matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0])
        det += matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0])

        return det
    }

    private fun makeTransition(oldSystem: IntArray): DoubleArray {
        val newSystem = DoubleArray(3)
        for (i in 0..2) {
            for (j in 0..2) {
                newSystem[i] += matrix[i][j] * oldSystem[j]
            }
        }
        return newSystem
    }

    private fun inverseTransition(newSystem: IntArray): DoubleArray {
        val oldSystem = DoubleArray(3)
        for (i in 0..2) {
            for (j in 0..2) {
                oldSystem[i] += inverseMatrix[i][j] * newSystem[j]
            }
        }
        return oldSystem
    }

    fun transformWithoutFiltering(currentPicture: PixelArray, width: Int, height: Int): PixelArray {
        val newPicture = PixelArray(width, height)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val oldCoordinates = inverseTransition(intArrayOf(x, y, 1))
                val oldX = oldCoordinates[0].toInt()
                val oldY = oldCoordinates[1].toInt()

                if (oldX < 0 || oldY < 0 || oldX > currentPicture.width - 1 || oldY > currentPicture.height - 1) {
                    continue
                }

                newPicture[x, y] = currentPicture[oldX, oldY]
            }
        }

        return newPicture
    }

    fun transformWithBilinearFiltering(
        currentPicture: PixelArray,
        width: Int,
        height: Int
    ): PixelArray {
        val newPicture = PixelArray(width, height)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val oldCoordinates = inverseTransition(intArrayOf(x, y, 1))
                val oldX = oldCoordinates[0]
                val oldY = oldCoordinates[1]

                if (oldX < 0 || oldY < 0 || oldX > currentPicture.width - 1 || oldY > currentPicture.height - 1) {
                    continue
                }

                val oldXFloor = floor(oldX).toInt()
                val oldYFloor = floor(oldY).toInt()
                val oldXCeil = ceil(oldX).toInt()
                val oldYCeil = ceil(oldY).toInt()

                val leftDif = if (oldXCeil != oldXFloor) oldX - oldXFloor else 1.0
                val topDif = if (oldYCeil != oldYFloor) oldY - oldYFloor else 1.0
                val rightDif = if (oldXCeil != oldXFloor) oldXCeil - oldX else 0.0
                val bottomDif = if (oldYCeil != oldYFloor) oldYCeil - oldY else 0.0

                val topLeft = currentPicture[oldXFloor, oldYFloor]
                val topRight = currentPicture[oldXCeil, oldYFloor]
                val bottomLeft = currentPicture[oldXFloor, oldYCeil]
                val bottomRight = currentPicture[oldXCeil, oldYCeil]

                fun average(component: Int): Int {
                    return (
                            topDif *
                                    (leftDif * topLeft.component(component) + rightDif * topRight.component(
                                        component
                                    ))
                                    + bottomDif *
                                    (leftDif * bottomLeft.component(component) + rightDif * bottomRight.component(
                                        component
                                    ))
                            ).toInt()
                }

                newPicture[x, y] = colorOf(
                    average(alpha),
                    average(red),
                    average(green),
                    average(blue)
                )
            }
        }

        return newPicture
    }

    fun transformWithTrilinearFiltering(
        currentPicture: PixelArray,
        width: Int,
        height: Int
    ): PixelArray {
        val newPicture = PixelArray(width, height)
        val mipmapPicture = currentPicture.getMipmap()

        for (x in 0 until width) {
            for (y in 0 until height) {
                val oldCoordinatesNearbyX =
                    if (x != 0) inverseTransition(intArrayOf(x - 1, y, 1))
                    else inverseTransition(intArrayOf(x + 1, y, 1))
                val oldCoordinatesNearbyY =
                    if (x != 0) inverseTransition(intArrayOf(x, y - 1, 1))
                    else inverseTransition(intArrayOf(x, y + 1, 1))
                val oldCoordinates = inverseTransition(intArrayOf(x, y, 1))

                val oldX = oldCoordinates[0]
                val oldY = oldCoordinates[1]

                val kX = abs(oldCoordinatesNearbyX[0] - oldX)
                val kY = abs(oldCoordinatesNearbyY[1] - oldY)
                val k = if ((kX + kY) / 2 > 1.0) ((kX + kY) / 2) else 1.0

                val lod = log2(k)
                val lowLod = floor(lod).toInt()

                var leftDistance1 = 0
                var lodWidth = currentPicture.width

                for (i in 0 until lowLod) {
                    leftDistance1 += lodWidth
                    lodWidth /= 2
                }

                val leftDistance2 = leftDistance1 + lodWidth

                val zoomingFactor1 = 2.0.pow(lowLod)
                val zoomingFactor2 = zoomingFactor1 * 2

                val oldX1 = leftDistance1 + oldX / zoomingFactor1
                val oldY1 = oldY / zoomingFactor1
                val oldX2 = leftDistance2 + oldX / zoomingFactor2
                val oldY2 = oldY / zoomingFactor2

                if (oldX2 > leftDistance2 + lodWidth / 2 ||
                    oldY1 > currentPicture.height / zoomingFactor1 - 1 ||
                    oldX1 < leftDistance1 ||
                    oldY2 < 0
                ) {
                    currentPicture[x, y] = 0
                    continue
                }

                val oldXFloor = intArrayOf(floor(oldX1).toInt(), floor(oldX2).toInt())
                val oldYFloor = intArrayOf(floor(oldY1).toInt(), floor(oldY2).toInt())
                val oldXCeil = intArrayOf(ceil(oldX1).toInt(), ceil(oldX2).toInt())
                val oldYCeil = intArrayOf(ceil(oldY1).toInt(), ceil(oldY2).toInt())

                val leftDif = doubleArrayOf(
                    if (oldXCeil[0] != oldXFloor[0]) oldX1 - oldXFloor[0] else 1.0,
                    if (oldXCeil[1] != oldXFloor[1]) oldX2 - oldXFloor[1] else 1.0
                )
                val topDif = doubleArrayOf(
                    if (oldYCeil[0] != oldYFloor[0]) oldY1 - oldYFloor[0] else 1.0,
                    if (oldYCeil[1] != oldYFloor[1]) oldY2 - oldYFloor[1] else 1.0
                )
                val rightDif = doubleArrayOf(
                    if (oldXCeil[0] != oldXFloor[0]) oldXCeil[0] - oldX1 else 0.0,
                    if (oldXCeil[1] != oldXFloor[1]) oldXCeil[1] - oldX2 else 0.0
                )
                val bottomDif = doubleArrayOf(
                    if (oldYCeil[0] != oldYFloor[0]) oldYCeil[0] - oldY1 else 0.0,
                    if (oldYCeil[1] != oldYFloor[1]) oldYCeil[1] - oldY2 else 0.0
                )

                val topLeft = intArrayOf(
                    mipmapPicture[oldXFloor[0], oldYFloor[0]],
                    mipmapPicture[oldXFloor[1], oldYFloor[1]]
                )
                val topRight = intArrayOf(
                    mipmapPicture[oldXCeil[0], oldYFloor[0]],
                    mipmapPicture[oldXCeil[1], oldYFloor[1]]
                )
                val bottomLeft = intArrayOf(
                    mipmapPicture[oldXFloor[0], oldYCeil[0]],
                    mipmapPicture[oldXFloor[1], oldYCeil[1]]
                )
                val bottomRight = intArrayOf(
                    mipmapPicture[oldXCeil[0], oldYCeil[0]],
                    mipmapPicture[oldXCeil[1], oldYCeil[1]]
                )

                fun average(component: Int): Int {
                    var middle = 0.0
                    middle += (zoomingFactor2 - k) * (
                            topDif[0] *
                                    (leftDif[0] * topLeft[0].component(component) +
                                            rightDif[0] * topRight[0].component(component)) +
                                    bottomDif[0] *
                                    (leftDif[0] * bottomLeft[0].component(component) +
                                            rightDif[0] * bottomRight[0].component(component))
                            )

                    middle += (k - zoomingFactor1) * (
                            topDif[1] *
                                    (leftDif[1] * topLeft[1].component(component) +
                                            rightDif[1] * topRight[1].component(component)) +
                                    bottomDif[1] *
                                    (leftDif[1] * bottomLeft[1].component(component) +
                                            rightDif[1] * bottomRight[1].component(component))
                            )

                    middle /= zoomingFactor1

                    return middle.toInt()
                }

                newPicture[x, y] = colorOf(
                    average(alpha),
                    average(red),
                    average(green),
                    average(blue)
                )
            }
        }

        return newPicture
    }
}