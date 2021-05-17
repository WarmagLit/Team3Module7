package com.example.graphiceditor

import android.graphics.Bitmap
import kotlin.math.abs

class Splines {
    private val pointsList: MutableList<IntArray>
    private var selectedIndex = -1

    constructor(){
        pointsList = MutableList(0){
            intArrayOf(0, 0)
        }
    }

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

    fun select(x: Int, y: Int, r: Int): Int{
        selectedIndex = -1
        val r2 = 2 * r
        for (i in 0 until pointsList.size){
            if (abs(pointsList[i][0] - x) <= r2 && abs(pointsList[i][1] - y) <= r2){
                selectedIndex = i
                break
            }
        }
        return selectedIndex
    }

    fun checkSelected() = selectedIndex

    fun changeSelected(x: Int, y: Int){
        pointsList[selectedIndex] = intArrayOf(x, y)
        selectedIndex = -1
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
                    i, j, colorOf(0, 0, 0)
                )
            }
        }
    }

    private fun Bitmap.drawNodePoint(x: Int, y: Int, r: Int){
        val r2 = 3 * r / 2
        for (i in x - r2..x + r2) {
            if (i !in 0 until this.width) continue
            for (j in y - r2..y + r2) {
                if (j !in 0 until this.height || (x - i) * (x - i) + (y - j) * (y - j) > r2 * r2) continue
                this.setPixel(
                    i, j, colorOf(255, 0, 0)
                )
            }
        }
    }

    private fun Bitmap.drawSelectedPoint(x: Int, y: Int, r: Int){
        val r2 = 3 * r / 2
        for (i in x - r2..x + r2) {
            if (i !in 0 until this.width) continue
            for (j in y - r2..y + r2) {
                if (j !in 0 until this.height || (x - i) * (x - i) + (y - j) * (y - j) > r2 * r2) continue
                this.setPixel(
                    i, j, colorOf(125, 0, 0)
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

    fun drawPoliline(r: Int, bitmap: Bitmap): Bitmap{
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        bitmap.recycle()

        for (i in 0 until pointsList.size - 1){
            newBitmap.drawLine(pointsList[i][0], pointsList[i][1], pointsList[i + 1][0], pointsList[i + 1][1], r)
            newBitmap.drawNodePoint(pointsList[i][0], pointsList[i][1], r)
        }
        newBitmap.drawNodePoint(pointsList.last()[0], pointsList.last()[1], r)
        if (selectedIndex != -1){
            newBitmap.drawSelectedPoint(
                pointsList[selectedIndex][0],
                pointsList[selectedIndex][1],
                r
            )
        }
        return newBitmap
    }
}