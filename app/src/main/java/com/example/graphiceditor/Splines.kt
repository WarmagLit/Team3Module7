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

    fun Bitmap.drawBezierLine(
        begin: IntArray,
        p1: IntArray,
        p2: IntArray,
        end: IntArray,
        r: Int
    ){
        if(abs(begin[0] - end[0]) <= 3 && abs(begin[1] - end[1]) <= 3){
            this.drawPoint(begin[0], begin[1], r)
            this.drawPoint(end[0], end[1], r)
            return
        }
        val p = Array(4){
            Array(4) {
                IntArray(4)
            }
        }
        p[0] = arrayOf(begin, p1, p2, end)
        for (lvl in 1..3){
            for (i in 0..3-lvl){
                p[lvl][i] = intArrayOf(
                    (p[lvl - 1][i][0] + p[lvl - 1][i + 1][0])/2,
                    (p[lvl - 1][i][1] + p[lvl - 1][i + 1][1])/2
                )
            }
        }
        drawBezierLine(p[0][0], p[1][0], p[2][0], p[3][0], r)
        drawBezierLine(p[3][0], p[2][1], p[1][2], p[0][3], r)
    }


    fun drawPoliline(r: Int, bitmap: Bitmap): Bitmap{
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        bitmap.recycle()

        for (i in 0 until pointsList.size - 1){

            newBitmap.drawBezierLine(
                pointsList[i],
                intArrayOf(
                    (pointsList[i][0] + pointsList[i + 1][0])/2,
                    (pointsList[i][1] + pointsList[i + 1][1])/2
                ),
                intArrayOf(
                    pointsList[i][0] + (pointsList[i][0] - pointsList[i + 1][0])/2,
                    pointsList[i + 1][1] + (pointsList[i][1] - pointsList[i + 1][1])/2
                ),
                pointsList[i + 1],
                r
            )
            newBitmap.drawNodePoint(pointsList[i][0], pointsList[i][1], r)

            //newBitmap.drawLine(pointsList[i][0], pointsList[i][1], pointsList[i + 1][0], pointsList[i + 1][1], r)

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