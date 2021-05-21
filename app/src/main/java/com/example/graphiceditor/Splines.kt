package com.example.graphiceditor

import android.graphics.Bitmap
import android.graphics.Picture
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class Splines {
    private val pointsList: MutableList<IntArray>
    private val additionPointsList1: MutableList<IntArray>
    private val additionPointsList2: MutableList<IntArray>
    private var selectedIndex = -1
    private var selectedList = 0

    constructor(){
        pointsList = mutableListOf(intArrayOf(0, 0), intArrayOf(0, 0))
        additionPointsList1 = MutableList(0){
            intArrayOf(0, 0)
        }
        additionPointsList2 = MutableList(0){
            intArrayOf(0, 0)
        }
    }

    constructor(x0: Int, y0: Int){
        pointsList = mutableListOf(intArrayOf(x0, y0), intArrayOf(0, 0))
        additionPointsList1 = MutableList(0){
            intArrayOf(0, 0)
        }
        additionPointsList2 = MutableList(0){
            intArrayOf(0, 0)
        }
    }

    fun add(x: Int, y: Int){
        pointsList.remove(pointsList.last())
        pointsList.add(intArrayOf(x, y))
        pointsList.add(intArrayOf(0, 0))

        if (pointsList.size < 3) return

        additionPointsList1.add(intArrayOf(0, 0))
        additionPointsList2.add(intArrayOf(0, 0))
        calculateAdditionPoints(pointsList.size - 4)
        calculateAdditionPoints(pointsList.size - 3)
        calculateAdditionPoints(pointsList.size - 2)
    }

    fun calculateAdditionPoints(index: Int){
        if (index < 0 || index > pointsList.size - 3) return

        val p0 = pointsList[index]
        val p1 = pointsList[index + 1]
        val p2 = pointsList[index + 2]

        val mx1 = (p0[0] + p1[0]) / 2
        val my1 = (p0[1] + p1[1]) / 2
        val mx2 = (p1[0] + p2[0]) / 2
        val my2 = (p1[1] + p2[1]) / 2

        val l01 = sqrt(((p0[0] - p1[0]) * (p0[0] - p1[0]) + (p0[1] - p1[1]) * (p0[1] - p1[1])).toDouble())
        val l12 = sqrt(((p1[0] - p2[0]) * (p1[0] - p2[0]) + (p1[1] - p2[1]) * (p1[1] - p2[1])).toDouble())

        val mx12 = (mx1 + (mx2 - mx1) * l01 / (l01 + l12)).toInt()
        val my12 = (my1 + (my2 - my1) * l01 / (l01 + l12)).toInt()

        additionPointsList1[index] = intArrayOf(mx1 + p1[0] - mx12, my1 + p1[1] - my12)
        additionPointsList2[index] = intArrayOf(mx2 + p1[0] - mx12, my2 + p1[1] - my12)
    }

    fun select(x: Int, y: Int, r: Int): Int{
        selectedList = 0
        selectedIndex = selectInList(x, y, 2 * r, pointsList)
        if (selectedIndex != -1) return selectedIndex

        selectedList = 1
        selectedIndex = selectInList(x, y, 3 * r / 2, additionPointsList1)
        if (selectedIndex != -1) return selectedIndex

        selectedList = 2
        selectedIndex = selectInList(x, y, 3 * r / 2, additionPointsList2)
        return selectedIndex
    }

    private fun selectInList(x: Int, y: Int, r: Int, list: MutableList<IntArray>): Int{
        var i = -1
        for (point in list){
            i++
            if (abs(point[0] - x) <= r && abs(point[1] - y) <= r){
                return i
            }
        }
        return -1
    }

    fun checkSelected() = (selectedIndex != -1)

    fun getSelectedListNumber() = selectedList

    private fun getSelectedList() = when(selectedList){
        1 -> additionPointsList1
        2 -> additionPointsList2
        else -> pointsList
    }

    fun changeSelectedPoint(x: Int, y: Int){
        val list = getSelectedList()
        if (selectedList == 0) {
            list[selectedIndex] = intArrayOf(x, y)
            calculateAdditionPoints(selectedIndex - 2)
            calculateAdditionPoints(selectedIndex - 1)
            calculateAdditionPoints(selectedIndex)
        }
        else {
            val point = pointsList[selectedIndex + 1]
            val selectedPoint = list[selectedIndex]
            val currentDistance = sqrt((
                    (point[0] - selectedPoint[0]) * (point[0] - selectedPoint[0]) +
                            (point[1] - selectedPoint[1]) * (point[1] - selectedPoint[1])
                    ).toDouble()
            )
            val newDistance = sqrt((
                    (point[0] - x) * (point[0] - x) +
                            (point[1] - y) * (point[1] - y)
                    ).toDouble()
            )
            changeLength(selectedIndex + 1, newDistance / currentDistance)
        }

        selectedIndex = -1
    }

    private fun changeLength(index: Int, k: Double){
        additionPointsList1[index - 1] = intArrayOf(
            (pointsList[index][0] + k * (additionPointsList1[index - 1][0] - pointsList[index][0])).toInt(),
            (pointsList[index][1] + k * (additionPointsList1[index - 1][1] - pointsList[index][1])).toInt()
        )
        additionPointsList2[index - 1] = intArrayOf(
            (pointsList[index][0] + k * (additionPointsList2[index - 1][0] - pointsList[index][0])).toInt(),
            (pointsList[index][1] + k * (additionPointsList2[index - 1][1] - pointsList[index][1])).toInt()
        )
    }

    fun removeSelectedPoint(){
        if (selectedList != 0) return
        pointsList.removeAt(selectedIndex)
        if (additionPointsList1.isNotEmpty()){
            additionPointsList1.remove(additionPointsList1.last())
            additionPointsList2.remove(additionPointsList2.last())
        }
        for (i in selectedIndex - 2 until additionPointsList1.size){
            calculateAdditionPoints(i)
        }

        selectedIndex = -1
    }


    private fun getLine(x0: Int, y0: Int, x1: Int, y1: Int): Array<IntArray> {
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

    private fun Bitmap.drawPoint(x: Int, y: Int, r: Int, color: Int){
        for (i in x - r..x + r) {
            if (i !in 0 until this.width) continue
            for (j in y - r..y + r) {
                if (j !in 0 until this.height || (x - i) * (x - i) + (y - j) * (y - j) > r * r) continue
                this.setPixel(i, j, color)
            }
        }
    }

    private fun Bitmap.drawNodePoint(x: Int, y: Int, r: Int) {
        this.drawPoint(x, y, r * 3 / 2, colorOf(255, 0, 0))
    }

    private fun Bitmap.drawAddPoint(x: Int, y: Int, r: Int) {
        this.drawPoint(x, y, r, colorOf(0, 255, 0))
    }

    private fun Bitmap.drawSelectedNode(x: Int, y: Int, r: Int){
        if (selectedList == 0) this.drawPoint(x, y, r * 3 / 2, colorOf(125, 0, 0))
        else this.drawPoint(x, y, r, colorOf(0, 125, 0))
    }

    fun Bitmap.drawLine(x0: Int, y0: Int, x1: Int, y1: Int, r: Int){
        val pointsArray = getLine(x0, y0, x1, y1)
        for (i in pointsArray.indices){
            this.drawPoint(pointsArray[i][0], pointsArray[i][1], r, colorOf(0, 0, 0))
        }
    }

    private fun Bitmap.drawThinLine(x0: Int, y0: Int, x1: Int, y1: Int, r: Int){
        this.drawLine(x0, y0, x1, y1, r * 3 / 7)
    }

    private fun Bitmap.drawBezierLine(
        begin: IntArray,
        p1: IntArray,
        p2: IntArray,
        end: IntArray,
        r: Int
    ){
        if(abs(begin[0] - end[0]) <= 3 && abs(begin[1] - end[1]) <= 3){
            this.drawLine(begin[0], begin[1], end[0], end[1], r)
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


    fun drawPolyline(r: Int, bitmap: Bitmap): Bitmap{
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        bitmap.recycle()

        for (i in 1 .. pointsList.size - 3){

            newBitmap.drawLine(pointsList[i][0], pointsList[i][1], pointsList[i + 1][0], pointsList[i + 1][1], r)
            newBitmap.drawNodePoint(pointsList[i][0], pointsList[i][1], r)

        }
        newBitmap.drawNodePoint(pointsList[pointsList.size - 2][0], pointsList[pointsList.size - 2][1], r)
        if (checkSelected()){
            val list = getSelectedList()
            newBitmap.drawSelectedNode(
                list[selectedIndex][0],
                list[selectedIndex][1],
                r
            )
        }
        return newBitmap
    }

    fun drawSpline(r: Int, bitmap: Bitmap): Bitmap{
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        bitmap.recycle()

        for (i in 1 .. pointsList.size - 3) {
            newBitmap.drawBezierLine(
                pointsList[i],
                additionPointsList2[i - 1],
                additionPointsList1[i],
                pointsList[i + 1],
                r
            )
        }

        for (i in 1 .. pointsList.size - 2) {
            newBitmap.drawThinLine(
                additionPointsList1[i - 1][0],
                additionPointsList1[i - 1][1],
                additionPointsList2[i - 1][0],
                additionPointsList2[i - 1][1],
                r
            )

            newBitmap.drawAddPoint(additionPointsList1[i - 1][0], additionPointsList1[i - 1][1], r)
            newBitmap.drawAddPoint(additionPointsList2[i - 1][0], additionPointsList2[i - 1][1], r)
            newBitmap.drawNodePoint(pointsList[i][0], pointsList[i][1], r)
        }

        if (checkSelected()){
            val list = getSelectedList()
            newBitmap.drawSelectedNode(
                list[selectedIndex][0],
                list[selectedIndex][1],
                r
            )
        }
        return newBitmap
    }

    fun drawSplineWithoutSettings(r: Int, bitmap: Bitmap): Bitmap {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        bitmap.recycle()

        for (i in 1..pointsList.size - 3) {
            newBitmap.drawBezierLine(
                pointsList[i],
                additionPointsList2[i - 1],
                additionPointsList1[i],
                pointsList[i + 1],
                r
            )
        }
        return newBitmap
    }
}