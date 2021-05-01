package com.example.graphiceditor

import kotlin.math.*

class AffineTransformations {
    var matrix: Array<DoubleArray>
    var inverseMatrix: Array<DoubleArray>

    constructor(transMatrix: Array<DoubleArray>){
        matrix = transMatrix
        inverseMatrix = calculateInverseMatrix()
    }

    /*constructor(
        oldSystemX: IntArray,
        oldSystemY: IntArray,
        newSystemX: IntArray,
        newSystemY: IntArray
    ){

    }*/

    private fun calculateInverseMatrix(): Array<DoubleArray>{
        val inverseMatrix = arrayOf(DoubleArray(3), DoubleArray(3), DoubleArray(3))
        val det = calculateDet()

        inverseMatrix[0][0] = (matrix[1][1]*matrix[2][2] - matrix[1][2]*matrix[2][1])/det
        inverseMatrix[0][1] = -(matrix[1][0]*matrix[2][2] - matrix[1][2]*matrix[2][0])/det
        inverseMatrix[0][2] = (matrix[1][0]*matrix[2][1] - matrix[1][1]*matrix[2][0])/det

        inverseMatrix[1][0] = -(matrix[0][1]*matrix[2][2] - matrix[0][2]*matrix[2][1])/det
        inverseMatrix[1][1] = (matrix[0][0]*matrix[2][2] - matrix[0][2]*matrix[2][0])/det
        inverseMatrix[1][2] = -(matrix[0][0]*matrix[2][1] - matrix[0][1]*matrix[2][0])/det

        inverseMatrix[2][0] = (matrix[0][1]*matrix[1][2] - matrix[0][2]*matrix[1][1])/det
        inverseMatrix[2][1] = -(matrix[0][0]*matrix[1][2] - matrix[0][2]*matrix[1][0])/det
        inverseMatrix[2][2] = (matrix[0][0]*matrix[1][1] - matrix[0][1]*matrix[1][0])/det

        return inverseMatrix
    }

    private fun calculateDet(): Double{
        var det = 0.0
        for(i in 0..2){
            for(j in 0..2){
                if (j == i) continue
                for(k in 0..2){
                    if (k == i || k == j) continue
                    det += matrix[0][i]*matrix[1][j]*matrix[2][k]
                }
            }
        }
        return det
    }


    fun makeTransition(oldSystem: IntArray): DoubleArray{
        val newSystem = DoubleArray(3)
        for (i in 0..2){
            for (j in 0..2){
                newSystem[i] += matrix[i][j] * oldSystem[j]
            }
        }
        return newSystem
    }

    private fun inverseTransition(newSystem: IntArray): DoubleArray{
        val oldSystem = DoubleArray(3)
        for (i in 0..2){
            for (j in 0..2){
                oldSystem[j] += inverseMatrix[i][j] * newSystem[i]
            }
        }
        return oldSystem
    }

    fun transformWithoutFiltering(currentPicture: PixelArray): PixelArray {
        val newPicture = PixelArray(currentPicture.width, currentPicture.height)

        for (x in 0 until newPicture.width) {
            for (y in 0 until newPicture.height) {
                val oldCoordinates = inverseTransition(intArrayOf(x, y, 1))
                val oldX = oldCoordinates[0].toInt()
                val oldY = oldCoordinates[1].toInt()

                if (oldX < 0 || oldY < 0 || oldX > currentPicture.width - 1 || oldY > currentPicture.height - 1) {
                    continue
                }

                newPicture[x,y] = currentPicture[oldX, oldY]
            }
        }

        return newPicture
    }

    fun transformWithBilinearFiltering(currentPicture: PixelArray): PixelArray{
        val newPicture = PixelArray(currentPicture.width, currentPicture.height)

        for (x in 0 until newPicture.width) {
            for (y in 0 until newPicture.height) {
                val oldCoordinates = inverseTransition(intArrayOf(x, y, 1))
                val oldX = oldCoordinates[0]
                val oldY = oldCoordinates[1]

                if (oldX < 0 || oldY < 0 || oldX > currentPicture.width - 1 || oldY > currentPicture.height - 1){
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

                fun average(component: Int): Int{
                    return(
                            topDif *
                                    (leftDif * topLeft.component(component) + rightDif * topRight.component(component))
                            + bottomDif *
                                    (leftDif * bottomLeft.component(component) + rightDif * bottomRight.component(component))
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

    fun transformWithTrilinearFiltering(currentPicture: PixelArray): PixelArray{
        val mipmapPicture = currentPicture.getMipmap()

        for (x in 0 until currentPicture.width) {
            for (y in 0 until currentPicture.height) {
                val oldCoordinatesNearbyX =
                    if(x != 0) inverseTransition(intArrayOf(x - 1, y, 1))
                    else inverseTransition(intArrayOf(x + 1, y, 1))
                val oldCoordinatesNearbyY =
                    if(x != 0) inverseTransition(intArrayOf(x, y - 1, 1))
                    else inverseTransition(intArrayOf(x, y + 1, 1))
                val oldCoordinates = inverseTransition(intArrayOf(x, y, 1))

                val oldX = oldCoordinates[0]
                val oldY = oldCoordinates[1]

                val kX = abs(oldCoordinatesNearbyX[0] - oldX)
                val kY = abs(oldCoordinatesNearbyY[1] - oldY)
                val k = (kX + kY) / 2

                val lod = log2(k)
                val lowLod = floor(lod).toInt()

                var leftDistance1 = 0
                var width = currentPicture.width

                for(i in 0 until lowLod){
                    leftDistance1 += width
                    width /= 2
                }

                val leftDistance2 = leftDistance1 + width

                val zoomingFactor1 = 2.0.pow(lowLod)
                val zoomingFactor2 = zoomingFactor1 * 2

                val oldX1 = leftDistance1 + oldX / zoomingFactor1
                val oldY1 = oldY / zoomingFactor1
                val oldX2 = leftDistance2 + oldX / zoomingFactor2
                val oldY2 = oldY / zoomingFactor2

                if(oldX2 > leftDistance2 + width / 2 ||
                    oldY1 > currentPicture.height / zoomingFactor1 - 1 ||
                    oldX1 < leftDistance1 ||
                    oldY2 < 0){
                    currentPicture[x, y] = 0
                    continue
                }

                val oldXFloor = intArrayOf(floor(oldX1).toInt(), floor(oldX2).toInt())
                val oldYFloor = intArrayOf(floor(oldY1).toInt(), floor(oldY2).toInt())
                val oldXCeil = intArrayOf(ceil(oldX1).toInt(), ceil(oldX2).toInt())
                val oldYCeil = intArrayOf(ceil(oldY1).toInt(), ceil(oldY2).toInt())

                val leftDif = if (oldXCeil[0] != oldXFloor[0]) oldX1 - oldXFloor[0] else 1.0
                val topDif = if (oldYCeil[0] != oldYFloor[0]) oldY1 - oldYFloor[0] else 1.0
                val rightDif = if (oldXCeil[0] != oldXFloor[0]) oldXCeil[0] - oldX1 else 0.0
                val bottomDif = if (oldYCeil[0] != oldYFloor[0]) oldYCeil[0] - oldY1 else 0.0

                val topLeft = intArrayOf(mipmapPicture[oldXFloor[0], oldYFloor[0]],
                    mipmapPicture[oldXFloor[1], oldYFloor[1]])
                val topRight = intArrayOf(mipmapPicture[oldXCeil[0], oldYFloor[0]],
                    mipmapPicture[oldXCeil[1], oldYFloor[1]])
                val bottomLeft = intArrayOf(mipmapPicture[oldXFloor[0], oldYCeil[0]],
                    mipmapPicture[oldXFloor[1], oldYCeil[1]])
                val bottomRight = intArrayOf(mipmapPicture[oldXCeil[0], oldYCeil[0]],
                    mipmapPicture[oldXCeil[1], oldYCeil[1]])

                fun average(component: Int): Int{
                    var middle = 0.0
                    middle += (zoomingFactor2 - k) * topDif *
                            (leftDif * topLeft[0].component(component) +
                                    rightDif * topRight[0].component(component)) +
                            bottomDif *
                            (leftDif * bottomLeft[0].component(component) +
                                    rightDif * bottomRight[0].component(component))

                    middle += (k - zoomingFactor1) * topDif *
                            (leftDif * topLeft[1].component(component) +
                                    rightDif * topRight[1].component(component)) +
                            bottomDif *
                            (leftDif * bottomLeft[1].component(component) +
                                    rightDif * bottomRight[1].component(component))
                    middle /= zoomingFactor1
                    
                    return middle.toInt()
                }

                currentPicture[x, y] = colorOf(
                    average(alpha),
                    average(red),
                    average(green),
                    average(blue)
                )
            }
        }

        return currentPicture
    }
}