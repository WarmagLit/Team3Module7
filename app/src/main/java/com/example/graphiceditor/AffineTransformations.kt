package com.example.graphiceditor

class AffineTransformations(transMatrix: Array<DoubleArray>) {
    var matrix = transMatrix
    var inverseMatrix = calculateInverseMatrix()

    fun calculateInverseMatrix(): Array<DoubleArray>{
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

    fun calculateDet(): Double{
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

    fun makeTransition(oldSystem: IntArray): IntArray{
        val newSystem = IntArray(3)
        for (i in 0..2){
            var value = 0.0
            for (j in 0..2){
                value += matrix[i][j] * oldSystem[j]
            }
            newSystem[i] = value.toInt()
        }
        return newSystem
    }

    fun inverseTransition(oldSystem: IntArray): IntArray{
        val newSystem = IntArray(3)
        for (i in 0..2){
            var value = 0.0
            for (j in 0..2){
                value += inverseMatrix[i][j] * oldSystem[j]
            }
            newSystem[i] = value.toInt()
        }
        return newSystem
    }
}