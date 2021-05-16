package com.example.graphiceditor

import android.graphics.Bitmap
import java.lang.Integer.max
import java.util.Collections.swap
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt

class Splines {
    private val pointsList: MutableList<IntArray>

    constructor(x0: Int, y0: Int){
        pointsList = mutableListOf(intArrayOf(x0, y0))
    }

    constructor(pointsList: MutableList<IntArray>){
        this.pointsList = pointsList
    }

    fun add(x: Int, y: Int){
        pointsList.add(intArrayOf(x, y))
    }

    fun add(points: MutableList<IntArray>){
        pointsList.addAll(points)
    }

    fun getLine(x0: Int, y0: Int, x1: Int, y1: Int): Array<IntArray> {
        var x = x0
        var y = y0
        var dBig = abs(x1 - x0)
        var dSmall = abs(y1 - y0)
        val sx = if (x1 - x0 > 0) 1 else -1
        val sy = if (y1 - y0 > 0) 1 else -1
        val isHorizontal = (dBig > dSmall)
        if (!isHorizontal){
            dBig += dSmall
            dSmall = dBig - dSmall
            dBig -= dSmall
        }

        val pointsArray = Array(dBig + 1){ IntArray(2) }

        var wrong = 2 * dSmall - dBig
        for (i in 0..dBig){
            pointsArray[i][0] = x
            pointsArray[i][1] = y
            if (wrong >= 0){
                if (!isHorizontal) x += sx else y += sy
                wrong -= 2 * dBig
            }
            if (!isHorizontal) y += sy else x += sx
            wrong += 2 * dSmall
        }

        return pointsArray
    }

    private fun Bitmap.drawPoint(x: Int, y: Int, r: Int){
        for (i in x - r..x + r) {
            if (i !in 0 until this.width) continue
            for (j in y - r..y + r) {
                if (j !in 0 until this.height || (x - i) * (x - i) + (y - j) * (y - j) > r * r) continue
                this.setPixel(
                    i, j, colorOf(255, 255, 255)
                )
            }
        }
    }

    fun Bitmap.drawLine(x0: Int, y0: Int, x1: Int, y1: Int, r: Int){
        val pointsArray = getLine(x0, y0, x1, y1)
        for (i in pointsArray.indices){
            this.drawPoint(pointsArray[i][0], pointsArray[i][1], r)
        }
    }

    fun drawPoliline(r: Int, bitmap: Bitmap){
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        bitmap.recycle()
        for (point in pointsList){
            newBitmap.
        }
    }
}