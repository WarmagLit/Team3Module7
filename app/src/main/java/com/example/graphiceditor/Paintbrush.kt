package com.example.graphiceditor

import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.sqrt

class Paintbrush {

    companion object {
        fun draw(
            currentPicture: PixelArray,
            bitmap: Bitmap,
            x: Int,
            y: Int,
            r: Int,
            centering: Double,
            style: String
        ) {
            var newColor = when (style) {
                "blur" -> blur(currentPicture, x, y, r)
                else -> 0
            }
            val newColorIsVariable = style != "blur"
            for (i in x - r..x + r) {
                if (i !in 0 until bitmap.width) continue
                for (j in y - r..y + r) {
                    if (j !in 0 until bitmap.height || (x - i) * (x - i) + (y - j) * (y - j) > r * r) continue
                    if (newColorIsVariable) {
                        newColor = when (style) {
                            "red" -> redStyle(currentPicture[i, j])
                            "green" -> greenStyle(currentPicture[i, j])
                            "blue" -> blueStyle(currentPicture[i, j])
                            "gray" -> grayStyle(currentPicture[i, j])
                            else -> 0
                        }
                    }

                    val k =
                        (centering * sqrt(((x - i) * (x - i) + (y - j) * (y - j)).toDouble())).toInt()

                    fun newValue(component: Int): Int {
                        val newValue =
                            ((r - k) * newColor.component(component) + k * currentPicture[i, j].component(
                                component
                            )) / r
                        return max(newValue, bitmap.getPixel(i, j).component(component))
                    }

                    val newRed = newValue(red)
                    val newGreen = newValue(green)
                    val newBlue = newValue(blue)
                    bitmap.setPixel(
                        i, j, colorOf(
                            currentPicture[i, j].component(alpha),
                            newRed,
                            newGreen,
                            newBlue
                        )
                    )
                }
            }
        }

        private fun redStyle(color: Int) = colorOf(
            if (color.component(red) * 10 / 7 > 255) 255 else color.component(red) * 10 / 7,
            color.component(green),
            color.component(blue)
        )

        private fun greenStyle(color: Int) = colorOf(
            color.component(red),
            if (color.component(green) * 10 / 7 > 255) 255 else color.component(green) * 10 / 7,
            color.component(blue)
        )

        private fun blueStyle(color: Int) = colorOf(
            color.component(red),
            color.component(green),
            if (color.component(blue) * 10 / 7 > 255) 255 else color.component(blue) * 10 / 7
        )

        private fun grayStyle(color: Int): Int {
            val newColor =
                (color.component(red) + color.component(green) + color.component(blue)) / 3
            return colorOf(
                newColor,
                newColor,
                newColor
            )
        }

        private fun blur(currentPicture: PixelArray, x: Int, y: Int, r: Int): Int {
            fun calculateMiddle(component: Int): Int {
                var color = 0
                var quantity = 0
                for (i in x - r..x + r) {
                    if (i !in 0 until currentPicture.width) continue
                    for (j in y - r..y + r) {
                        if (j !in 0 until currentPicture.height || (x - i) * (x - i) + (y - j) * (y - j) > r * r) continue
                        color += currentPicture[i, j].component(component)
                        quantity++
                    }
                }
                return if (quantity > 0) color / quantity else 0
            }
            return colorOf(
                calculateMiddle(red),
                calculateMiddle(green),
                calculateMiddle(blue)
            )
        }
    }
}