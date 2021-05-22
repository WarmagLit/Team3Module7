package com.example.graphiceditor

import android.text.Editable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val alpha = 24
const val red = 16
const val green = 8
const val blue = 0

fun Int.component(component: Int) =
    this shr component and 0xff

fun Editable.toDouble() = this.toString().toDouble()

fun colorOf(a: Int, r: Int, g: Int, b: Int): Int {
    return (a shl alpha) + (r shl red) + (g shl green) + b
}

fun colorOf(r: Int, g: Int, b: Int): Int {
    return (255 shl alpha) + (r shl red) + (g shl green) + b
}

fun multiplyMatrices(matrix1: Array<DoubleArray>, matrix2: Array<DoubleArray>): Array<DoubleArray> {
    val prod = Array(3) { DoubleArray(3) }
    for (i in 0..2) {
        for (j in 0..2) {
            prod[i][j] = 0.0
            for (k in 0..2) {
                prod[i][j] += matrix1[i][k] * matrix2[k][j]
            }
        }
    }
    return prod
}

fun transportMatrix(transportX: Double, transportY: Double) = arrayOf(
    doubleArrayOf(1.0, 0.0, transportX),
    doubleArrayOf(0.0, 1.0, transportY),
    doubleArrayOf(0.0, 0.0, 1.0)
)

fun rotateMatrix(angle: Double): Array<DoubleArray> {
    val cos = when (angle) {
        90.0 -> 0.0
        180.0 -> -1.0
        270.0 -> 0.0
        360.0 -> 1.0
        else -> cos(angle.toRad())
    }
    val sin = when (angle) {
        90.0 -> 1.0
        180.0 -> 0.0
        270.0 -> -1.0
        360.0 -> 0.0
        else -> sin(angle.toRad())
    }
    return arrayOf(
        doubleArrayOf(cos, -sin, 0.0),
        doubleArrayOf(sin, cos, 0.0),
        doubleArrayOf(0.0, 0.0, 1.0)
    )
}

fun zoomMatrix(zoomX: Double, zoomY: Double): Array<DoubleArray> = arrayOf(
    doubleArrayOf(zoomX, 0.0, 0.0),
    doubleArrayOf(0.0, zoomY, 0.0),
    doubleArrayOf(0.0, 0.0, 1.0)
)

fun Double.toRad(): Double {
    return (this * PI / 180)
}