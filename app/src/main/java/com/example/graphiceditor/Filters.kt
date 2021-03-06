package com.example.graphiceditor

import android.util.Log
import androidx.core.graphics.component1

enum class Filter(val code: Int, val process: suspend (PixelArray) -> PixelArray) {
    RED(R.string.redFilter, ::red),
    GREEN(R.string.greenFilter, ::green),
    BLUE(R.string.blueFilter, ::blue),
    GRAY(R.string.grayFilter, ::grey),
    DIAGONAL_SEPIA(R.string.diagonalSepia, ::diagonalSepia),
    SWAP_COLORS(R.string.swapColors, ::swapColors),
    NEGATIVE(R.string.negative, ::negative),
    BLUR(R.string.blur, ::blur),
    EDGE_DETECTION(R.string.edgeDetection, ::edgeDetection),
    EMBOSS(R.string.emboss, ::emboss),

    NONE(R.string.none, { TODO() }),
    UNSHARP(R.string.unsharp, ::unsharpFilter),
    SEPIA(R.string.sepia, ::sepia),
    SOME_FILTER(R.string.someFilter, ::someFilter),
}

private suspend fun diagonalSepia(image: PixelArray): PixelArray {
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            var newRed =
                (0.393 * image[i, j].component(red) + 0.769 * image[i, j].component(green) + 0.189 * image[i, j].component(
                    blue
                )).toInt()
            var newGreen =
                (0.349 * image[i, j].component(red) + 0.686 * image[i, j].component(green) + 0.168 * image[i, j].component(
                    blue
                )).toInt()
            var newBlue =
                (0.272 * image[i, j].component(red) + 0.534 * image[i, j].component(green) + 0.131 * image[i, j].component(
                    blue
                )).toInt()

            if (newRed > 255) newRed = 255
            if (newGreen > 255) newGreen = 255
            if (newBlue > 255) newBlue = 255

            if (i < j - image.height / 2 || (i - (image.height / 2)) > j)
                image[i, j] = colorOf(image[i, j].component(alpha), newRed, newGreen, newBlue)
        }
    }

    return image
}

private suspend fun grey(image: PixelArray): PixelArray {
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            val intensity =
                ((image[i, j].component(red) + image[i, j].component(green) + image[i, j].component(
                    blue
                )) / 3)
            image[i, j] = colorOf(image[i, j].component(alpha), intensity, intensity, intensity)
        }
    }

    return image
}

private suspend fun sepia(image: PixelArray): PixelArray {
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            var newRed =
                (0.393 * image[i, j].component(red) + 0.769 * image[i, j].component(green) + 0.189 * image[i, j].component(
                    blue
                )).toInt()
            var newGreen =
                (0.349 * image[i, j].component(red) + 0.686 * image[i, j].component(green) + 0.168 * image[i, j].component(
                    blue
                )).toInt()
            var newBlue =
                (0.272 * image[i, j].component(red) + 0.534 * image[i, j].component(green) + 0.131 * image[i, j].component(
                    blue
                )).toInt()

            if (newRed > 255) newRed = 255
            if (newGreen > 255) newGreen = 255
            if (newBlue > 255) newBlue = 255
            image[i, j] = colorOf(image[i, j].component(alpha), newRed, newGreen, newBlue)

        }
    }

    return image
}

private suspend fun blue(image: PixelArray): PixelArray {
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            image[i, j] = colorOf(
                image[i, j].component(alpha),
                image[i, j].component(red) * 7 / 10,
                image[i, j].component(green) * 7 / 10,
                image[i, j].component(blue)
            )
        }
    }

    return image
}

private suspend fun red(image: PixelArray): PixelArray {
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            image[i, j] = colorOf(
                image[i, j].component(alpha),
                image[i, j].component(red),
                image[i, j].component(green) * 7 / 10,
                image[i, j].component(blue) * 7 / 10
            )
        }
    }

    return image
}

private suspend fun green(image: PixelArray): PixelArray {
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            image[i, j] = colorOf(
                image[i, j].component(alpha),
                image[i, j].component(red) * 7 / 10,
                image[i, j].component(green),
                image[i, j].component(blue) * 7 / 10
            )
        }
    }

    return image
}

private suspend fun swapColors(image: PixelArray): PixelArray {
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            image[i, j] = colorOf(
                image[i, j].component(alpha),
                image[i, j].component(blue),
                image[i, j].component(red),
                image[i, j].component(green)
            )
        }
    }

    return image
}

private suspend fun negative(image: PixelArray): PixelArray {
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            image[i, j] = colorOf(
                image[i, j].component(alpha),
                255 - image[i, j].component(red),
                255 - image[i, j].component(green),
                255 - image[i, j].component(blue)
            )
        }
    }

    return image
}

private suspend fun blur(image: PixelArray): PixelArray {
    val arrCopy = image.clone()
    Log.d("TAG", "Entered blur")

    for (x in 1..image.width - 2) {
        for (y in 1..image.height - 2) {

            fun average(component: Int): Int {
                var result = 0
                for (i in -1..1) {
                    for (j in -1..1) {
                        result += image[x + i, y + j].component(component)
                    }
                }
                return result / 9
            }

            arrCopy[x, y] =
                colorOf(image[x, y].component(alpha), average(red), average(green), average(blue))
        }
    }

    return arrCopy
}

private suspend fun edgeDetection(image: PixelArray): PixelArray {
    val arrCopy = image.clone();

    for (x in 1..image.width - 2) {
        for (y in 1..image.height - 2) {

            fun average(component: Int): Int {
                val result = image[x, y - 1].component(component) +
                        image[x, y + 1].component(component) +
                        image[x - 1, y].component(component) +
                        image[x + 1, y].component(component) -
                        4 * image[x, y].component(component)
                return when {
                    result < 0 -> 0
                    result > 255 -> 255
                    else -> result
                }
            }

            arrCopy[x, y] =
                colorOf(image[x, y].component(alpha), average(red), average(green), average(blue))
        }
    }

    return arrCopy
}

private suspend fun emboss(image: PixelArray): PixelArray {
    val arrCopy = image.clone();
    for (x in 1..image.width - 2) {
        for (y in 1..image.height - 2) {

            fun average(component: Int): Int {
                val result = image[x, y - 1].component(component) -
                        image[x, y + 1].component(component) +
                        image[x - 1, y].component(component) -
                        image[x + 1, y].component(component) + 128
                return when {
                    result < 0 -> 0
                    result > 255 -> 255
                    else -> result
                }
            }

            arrCopy[x, y] =
                colorOf(image[x, y].component(alpha), average(red), average(green), average(blue))
        }
    }

    return arrCopy
}

private suspend fun someFilter(image: PixelArray): PixelArray {
    val arrCopy = image.clone();

    for (x in 1..image.width - 2) {
        for (y in 1..image.height - 2) {
            val k1 = x + y
            val k2 = x
            val k3 = y
            val k4 = 0

            fun average(component: Int): Int {
                val result = k1 * image[x - 1, y - 1].component(component) -
                        k1 * image[x + 1, y - 1].component(component) +
                        k1 * image[x - 1, y + 1].component(component) -
                        k1 * image[x + 1, y + 1].component(component) +
                        k2 * image[x, y - 1].component(component) -
                        k2 * image[x, y + 1].component(component) +
                        k3 * image[x - 1, y].component(component) -
                        k3 * image[x + 1, y].component(component) +
                        k4 * image[x + 1, y].component(component)
                return when {
                    result < 0 -> 0
                    result > 255 -> 255
                    else -> result
                }
            }

            arrCopy[x, y] =
                colorOf(image[x, y].component(alpha), average(red), average(green), average(blue))

        }
    }
    return arrCopy
}

private suspend fun unsharpFilter(image: PixelArray): PixelArray {
    val arrCopy = image.clone();

    for (x in 1..image.width - 2) {
        for (y in 1..image.height - 2) {
            val k1 = -1
            val k2 = -2
            val k3 = -2
            val k4 = 13

            fun average(component: Int): Int {
                val result = k1 * image[x - 1, y - 1].component(component) +
                        k1 * image[x + 1, y - 1].component(component) +
                        k1 * image[x - 1, y + 1].component(component) +
                        k1 * image[x + 1, y + 1].component(component) +
                        k2 * image[x, y - 1].component(component) +
                        k2 * image[x, y + 1].component(component) +
                        k3 * image[x - 1, y].component(component) +
                        k3 * image[x + 1, y].component(component) +
                        k4 * image[x, y].component(component)
                return when {
                    result < 0 -> 0
                    result > 255 -> 255
                    else -> result
                }
            }

            arrCopy[x, y] =
                colorOf(image[x, y].component(alpha), average(red), average(green), average(blue))

        }
    }
    return arrCopy
}