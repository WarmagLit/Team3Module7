package com.example.graphiceditor

import android.graphics.Bitmap

class PixelArray : Cloneable{
    val bitmap: Bitmap
        get() {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (x in 0 until width)
                for(y in 0 until height)
                    bitmap.setPixel(x, y, get(x, y))
            return bitmap
        }

    private var pixels: Array<IntArray>
    val width: Int
    val height: Int

    operator fun get(x: Int, y: Int) = pixels[x][y]
    operator fun set(x: Int, y: Int, value: Int){
        pixels[x][y] = value
    }

    constructor(currentBitmap: Bitmap){
        width = currentBitmap.width
        height = currentBitmap.height
        pixels = Array(currentBitmap.width){ x ->
            IntArray(currentBitmap.height){ y ->
                currentBitmap.getPixel(x, y)
            }
        }
    }

    constructor(pixels: Array<IntArray>) {
        width = pixels.size
        height = pixels[0].size
        this.pixels = Array(pixels.size){ x ->
            pixels[x].copyOf()
        }
    }

    constructor(width: Int, height: Int) {
        this.width = width
        this.height = height
        pixels = Array(width){ IntArray(height){ 0 } }
    }

    constructor(pixelArray: PixelArray, width: Int, height: Int) {
        this.width = width
        this.height = height
        this.pixels = Array(width){ x ->
            IntArray(height) { y ->
                if(x < width && y < height) pixelArray[x, y] else 0
            }
        }
    }

    public override fun clone() = PixelArray(pixels)

    fun getMipmap(): PixelArray{
        val mipmap = Array(width*2){ x ->
            IntArray(height) { y ->
                if(x < width && y < height) pixels[x][y] else 0
            }
        }

        var leftDistance = this.width
        var width = this.width / 2
        var height = this.height / 2

        while (width > 0){
            for (x in leftDistance until leftDistance + width){
                for (y in 0 until height){
                    val topLeftX = 2*x - 2*width - leftDistance
                    val topLeftY = 2*y

                    val topLeft =
                        mipmap[topLeftX][topLeftY]

                    val topRight =
                        if (topLeftX + 1 < leftDistance)
                            mipmap[topLeftX + 1][topLeftY]
                        else topLeft

                    val bottomLeft =
                        if (topLeftY + 1 < 2*height)
                            mipmap[topLeftX][topLeftY + 1]
                        else topLeft

                    val bottomRight =
                        if (topLeftY + 1 < 2*height)
                            mipmap[topLeftX + 1][topLeftY + 1]
                        else topRight

                    fun average(component: Int) = (topLeft.component(component) +
                            topRight.component(component) +
                            bottomLeft.component(component) +
                            bottomRight.component(component)) / 4

                    mipmap[x][y] = colorOf(
                        average(alpha),
                        average(red),
                        average(green),
                        average(blue)
                    )
                }
            }
            leftDistance += width
            width /= 2
            height /= 2
        }
        return PixelArray(mipmap)
    }
}