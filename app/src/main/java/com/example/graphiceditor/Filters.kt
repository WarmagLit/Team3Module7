package com.example.graphiceditor

import kotlinx.android.synthetic.main.activity_main.view.*
import kotlin.math.ceil
import kotlin.math.exp

class Filters() {
    fun Check(told: ProcessedPicture, As: String)
    {
        when (As){
            "DiagonalSepia" -> diagonalSepia(told)
            "Grey" -> grey(told)
            "Sepia" -> sepia(told)
            "Blue" -> blue(told)
            "SwapColors" -> swapColors(told)
            "Negative" -> negative(told)
            "Blurring" -> blurring(told)
            "EdgeDetection" -> edgeDetection(told)
            "Emboss" -> emboss(told)
            "SomeFilter" -> mynkFilter(told)
            "UnsharpFilter" -> unsharpFilter(told,5.0)
        }
    }

    fun diagonalSepia(image: ProcessedPicture) {
        for (i in 0..image.bitmap.width - 1) {
            for (j in 0..image.bitmap.height - 1) {
                var newRed =
                    (0.393 * image.pixelsArray[i][j].r + 0.769 * image.pixelsArray[i][j].g + 0.189 * image.pixelsArray[i][j].b).toInt()
                var newGreen =
                    (0.349 * image.pixelsArray[i][j].r + 0.686 * image.pixelsArray[i][j].g + 0.168 * image.pixelsArray[i][j].b).toInt()
                var newBlue =
                    (0.272 * image.pixelsArray[i][j].r + 0.534 * image.pixelsArray[i][j].g + 0.131 * image.pixelsArray[i][j].b).toInt()

                if (newRed > 255) {
                    newRed = 255
                }
                if (newGreen > 255) {
                    newGreen = 255
                }
                if (newBlue > 255) {
                    newBlue = 255
                }

                if (i < j - image.bitmap.height / 2) {
                    // apply sepia at lower
                    image.pixelsArray[i][j].a = image.pixelsArray[i][j].a
                    image.pixelsArray[i][j].r = newRed
                    image.pixelsArray[i][j].g = newGreen
                    image.pixelsArray[i][j].b = newBlue


                } else if ((i - (image.bitmap.height / 2)) > j) {
                    // apply sepia upper
                    image.pixelsArray[i][j].a = image.pixelsArray[i][j].a
                    image.pixelsArray[i][j].r = newRed
                    image.pixelsArray[i][j].g = newGreen
                    image.pixelsArray[i][j].b = newBlue

                } else {
                    //  don't apply sepia
                    image.pixelsArray[i][j] = image.pixelsArray[i][j]
                }
            }
        }
    }


    fun grey(image: ProcessedPicture)
    {
        for (i in 0..image.bitmap.width - 1) {
            for (j in 0..image.bitmap.height - 1) {
                var intensity = ((image.pixelsArray[i][j].r + image.pixelsArray[i][j].g + image.pixelsArray[i][j].b) / 3)
                if (intensity > 255)
                    intensity = 255
                image.pixelsArray[i][j].r = intensity
                image.pixelsArray[i][j].g = intensity
                image.pixelsArray[i][j].b = intensity
            }
        }
    }

    fun sepia(image: ProcessedPicture) {
        for (i in 0..image.bitmap.width - 1) {
            for (j in 0..image.bitmap.height - 1) {
                var newRed =
                    (0.393 * image.pixelsArray[i][j].r + 0.769 * image.pixelsArray[i][j].g + 0.189 * image.pixelsArray[i][j].b).toInt()
                var newGreen =
                    (0.349 * image.pixelsArray[i][j].r + 0.686 * image.pixelsArray[i][j].g + 0.168 * image.pixelsArray[i][j].b).toInt()
                var newBlue =
                    (0.272 * image.pixelsArray[i][j].r + 0.534 * image.pixelsArray[i][j].g + 0.131 * image.pixelsArray[i][j].b).toInt()

                if (newRed > 255) {
                    newRed = 255
                }
                if (newGreen > 255) {
                    newGreen = 255
                }
                if (newBlue > 255) {
                    newBlue = 255
                }
                image.pixelsArray[i][j].r = newRed
                image.pixelsArray[i][j].g = newGreen
                image.pixelsArray[i][j].b = newBlue

            }
        }
    }

    fun blue(image: ProcessedPicture) {
        for (i in 0..image.bitmap.width - 1) {
            for (j in 0..image.bitmap.height - 1) {
                if (image.pixelsArray[i][j].b < 240) {
                    image.pixelsArray[i][j].r *= 7
                    image.pixelsArray[i][j].g *= 7
                    image.pixelsArray[i][j].r /= 10
                    image.pixelsArray[i][j].g /= 10
                }
            }
        }
    }

    fun red(image: ProcessedPicture) {
        for (i in 0..image.bitmap.width - 1) {
            for (j in 0..image.bitmap.height - 1) {
                if (image.pixelsArray[i][j].r < 240) {
                    image.pixelsArray[i][j].b *= 7
                    image.pixelsArray[i][j].g *= 7
                    image.pixelsArray[i][j].b /= 10
                    image.pixelsArray[i][j].g /= 10
                }
            }
        }
    }

    fun green(image: ProcessedPicture) {
        for (i in 0..image.bitmap.width - 1) {
            for (j in 0..image.bitmap.height - 1) {
                if (image.pixelsArray[i][j].g < 240) {
                    image.pixelsArray[i][j].b *= 7
                    image.pixelsArray[i][j].r *= 7
                    image.pixelsArray[i][j].b /= 10
                    image.pixelsArray[i][j].r /= 10
                }
            }
        }
    }

    fun swapColors(image: ProcessedPicture) {
        for (i in 0..image.bitmap.width - 1) {
            for (j in 0..image.bitmap.height - 1) {
                val oldBlue = image.pixelsArray[i][j].b
                image.pixelsArray[i][j].b = image.pixelsArray[i][j].r
                image.pixelsArray[i][j].r = image.pixelsArray[i][j].g
                image.pixelsArray[i][j].g = oldBlue
            }
        }
    }

    fun negative(image: ProcessedPicture) {
        for (i in 0..image.bitmap.width - 1) {
            for (j in 0..image.bitmap.height - 1) {
                image.pixelsArray[i][j].b = 255 - image.pixelsArray[i][j].b
                image.pixelsArray[i][j].r = 255 - image.pixelsArray[i][j].r
                image.pixelsArray[i][j].g = 255 - image.pixelsArray[i][j].g
            }
        }
    }

    fun blurring(image: ProcessedPicture) {
        val copyArr = image.getCopy();

        for (i in 1..image.bitmap.width - 2) {
            for (j in 1..image.bitmap.height - 2) {
                val newRed = image.pixelsArray[i-1][j-1].r + image.pixelsArray[i+1][j-1].r +
                        image.pixelsArray[i-1][j+1].r + image.pixelsArray[i+1][j+1].r +
                        image.pixelsArray[i][j-1].r + image.pixelsArray[i][j+1].r +
                        image.pixelsArray[i-1][j].r + image.pixelsArray[i+1][j].r + image.pixelsArray[i][j].r
                val newGreen = image.pixelsArray[i-1][j-1].g + image.pixelsArray[i+1][j-1].g +
                        image.pixelsArray[i-1][j+1].g + image.pixelsArray[i+1][j+1].g +
                        image.pixelsArray[i][j-1].g + image.pixelsArray[i][j+1].g +
                        image.pixelsArray[i-1][j].g + image.pixelsArray[i+1][j].g + image.pixelsArray[i][j].g
                val newBlue = image.pixelsArray[i-1][j-1].b + image.pixelsArray[i+1][j-1].b +
                        image.pixelsArray[i-1][j+1].b + image.pixelsArray[i+1][j+1].b +
                        image.pixelsArray[i][j-1].b + image.pixelsArray[i][j+1].b +
                        image.pixelsArray[i-1][j].b + image.pixelsArray[i+1][j].b + image.pixelsArray[i][j].b

                copyArr[i][j].r = newRed/9
                copyArr[i][j].g = newGreen/9
                copyArr[i][j].b = newBlue/9

            }
        }
        image.pixelsArray = copyArr
    }

    fun edgeDetection(image: ProcessedPicture) {
        val copyArr = image.getCopy();

        for (i in 1..image.bitmap.width - 2) {
            for (j in 1..image.bitmap.height - 2) {
                var newRed =
                        image.pixelsArray[i][j-1].r + image.pixelsArray[i][j+1].r +
                        image.pixelsArray[i-1][j].r + image.pixelsArray[i+1][j].r - 4*image.pixelsArray[i][j].r
                var newGreen =
                        image.pixelsArray[i][j-1].g + image.pixelsArray[i][j+1].g +
                        image.pixelsArray[i-1][j].g + image.pixelsArray[i+1][j].g - 4*image.pixelsArray[i][j].g
                var newBlue =
                        image.pixelsArray[i][j-1].b + image.pixelsArray[i][j+1].b +
                        image.pixelsArray[i-1][j].b + image.pixelsArray[i+1][j].b - 4*image.pixelsArray[i][j].b

                newRed = when(newRed){
                    in -3000..0 -> 0
                    in 0..255 -> newRed
                    else -> 255
                }
                newGreen = when(newGreen){
                    in -3000..0 -> 0
                    in 0..255 -> newGreen
                    else -> 255
                }
                newBlue = when(newBlue){
                    in -3000..0 -> 0
                    in 0..255 -> newBlue
                    else -> 255
                }

                copyArr[i][j].r = newRed
                copyArr[i][j].g = newGreen
                copyArr[i][j].b = newBlue
            }
        }

        image.pixelsArray = copyArr
    }

    fun emboss(image: ProcessedPicture) {
        val copyArr = image.getCopy();
        for (i in 1..image.bitmap.width - 2) {
            for (j in 1..image.bitmap.height - 2) {
                var newRed = image.pixelsArray[i][j-1].r - image.pixelsArray[i][j+1].r +
                        image.pixelsArray[i-1][j].r - image.pixelsArray[i+1][j].r + 128
                var newGreen = image.pixelsArray[i][j-1].g - image.pixelsArray[i][j+1].g +
                        image.pixelsArray[i-1][j].g - image.pixelsArray[i+1][j].g + 128
                var newBlue = image.pixelsArray[i][j-1].b - image.pixelsArray[i][j+1].b +
                        image.pixelsArray[i-1][j].b - image.pixelsArray[i+1][j].b + 128

                newRed = when(newRed){
                    in -3000..0 -> 0
                    in 0..255 -> newRed
                    else -> 255
                }
                newGreen = when(newGreen){
                    in -3000..0 -> 0
                    in 0..255 -> newGreen
                    else -> 255
                }
                newBlue = when(newBlue){
                    in -3000..0 -> 0
                    in 0..255 -> newBlue
                    else -> 255
                }

                copyArr[i][j].r = newRed
                copyArr[i][j].g = newGreen
                copyArr[i][j].b = newBlue
            }
        }

        image.pixelsArray = copyArr
    }

    fun mynkFilter(image: ProcessedPicture) {
        val copyArr = image.getCopy()

        for (i in 1..image.bitmap.width - 2) {
            for (j in 1..image.bitmap.height - 2) {
                val k1 = i+j
                val k2 = j
                val k3 = i
                val k4 = 0

                var newRed = k1*image.pixelsArray[i-1][j-1].r - k1*image.pixelsArray[i+1][j-1].r +
                        k1*image.pixelsArray[i-1][j+1].r - k1*image.pixelsArray[i+1][j+1].r +
                        k2*image.pixelsArray[i][j-1].r - k2*image.pixelsArray[i][j+1].r +
                        k3*image.pixelsArray[i-1][j].r - k3*image.pixelsArray[i+1][j].r + k4*image.pixelsArray[i+1][j].r
                var newGreen = k1*image.pixelsArray[i-1][j-1].g - k1*image.pixelsArray[i+1][j-1].g +
                        k1*image.pixelsArray[i-1][j+1].g - k1*image.pixelsArray[i+1][j+1].g +
                        k2*image.pixelsArray[i][j-1].g - k2*image.pixelsArray[i][j+1].g +
                        k3*image.pixelsArray[i-1][j].g - k3*image.pixelsArray[i+1][j].g + k4*image.pixelsArray[i+1][j].g
                var newBlue = k1*image.pixelsArray[i-1][j-1].b - k1*image.pixelsArray[i+1][j-1].b +
                        k1*image.pixelsArray[i-1][j+1].b - k1*image.pixelsArray[i+1][j+1].b +
                        k2*image.pixelsArray[i][j-1].b - k2*image.pixelsArray[i][j+1].b +
                        k3*image.pixelsArray[i-1][j].b - k3*image.pixelsArray[i+1][j].b + k4*image.pixelsArray[i+1][j].b

                newRed = when(newRed){
                    in -100000..0 -> 0
                    in 0..255 -> newRed
                    else -> 255
                }
                newGreen = when(newGreen){
                    in -100000..0 -> 0
                    in 0..255 -> newGreen
                    else -> 255
                }
                newBlue = when(newBlue){
                    in -100000..0 -> 0
                    in 0..255 -> newBlue
                    else -> 255
                }

                copyArr[i][j].r = newRed
                copyArr[i][j].g = newGreen
                copyArr[i][j].b = newBlue
            }
        }
        image.pixelsArray = copyArr
    }

    fun unsharpFilter(image: ProcessedPicture, sigma: Double) {
        val copyArr = image.getCopy()
        var sum: Double
        var pix_r: Int
        var pix_g: Int
        var pix_b: Int
        var red: Int
        var green: Int
        var blue: Int




        var s2: Double = 2 * sigma * sigma

        var N: Int = ceil(3 * sigma).toInt()

        var window = DoubleArray(N * 2 + 1) { 0.0 }
        var tmp = Array(image.bitmap.width) { PixelARGB() }
        window[N] = 1.0
        for (i in 1..N) {
            window[N+i] = exp(-i * i / s2)
            window[N-i] = window[N+i]
        }
        for (j in 0..image.bitmap.height - 1) {
            for (i in 0..image.bitmap.width - 1) {
                if (j == 425 && i == 319)
                {
                    print("idinahui")
                }
                sum = 0.0
                pix_b = 0
                pix_g = 0
                pix_r = 0
                for (k in -N..N) {
                    var l = i + k
                    if (l >= 0 && l < image.bitmap.width) {
                        val red = image.pixelsArray[l][j].r
                        val green = image.pixelsArray[l][j].g

                        val blue = image.pixelsArray[l][j].b

                        pix_r = (pix_r + red * window[N+k]).toInt()
                        pix_g = (pix_g + green * window[N+k]).toInt()
                        pix_b = (pix_b + blue * window[N+k]).toInt()
                        sum += window[N+k]
                    }
                }

                var sum1 = (sum + 1).toInt()

                pix_r /= sum1
                pix_g /= sum1
                pix_b /= sum1
                tmp[i].r = pix_r
                tmp[i].g = pix_g
                tmp[i].b = pix_b
            }
            for (i in 0 until image.bitmap.width) {
                copyArr[i][j].r = tmp[i].r
                copyArr[i][j].g = tmp[i].g
                copyArr[i][j].b = tmp[i].b
            }
        }

        window = DoubleArray(N * 2 + 1, {0.0})
        tmp = Array(image.bitmap.height ) { PixelARGB() }
        window[N] = 1.0
        for (i in 1..N) {
            window[N+i] = exp(-i * i / s2)
            window[N-i] = window[N+i]
        }
        for (i in 0 until image.bitmap.width) {
            for (j in 0 until image.bitmap.height) {
                sum = 0.0
                pix_b =0
                pix_g = 0
                pix_r = 0
                for (k in -N..N) {
                    var l = j + k
                    if (l >= 0 && l < image.bitmap.height) {
                        red = image.pixelsArray[i][l].r
                        green = image.pixelsArray[i][l].g
                        blue = image.pixelsArray[i][l].b

                        pix_r = (pix_r + red * window[N+k]).toInt()
                        pix_g = (pix_g + green * window[N+k]).toInt()
                        pix_b = (pix_b + blue * window[N+k]).toInt()
                        sum += window[N+k]
                    }
                }
                var sum1 = (sum + 1).toInt()
                pix_r /= sum1
                pix_g /= sum1
                pix_b /= sum1
                tmp[j].r = pix_r
                tmp[j].g = pix_g
                tmp[j].b = pix_b

            }
            for (j in 0 until image.bitmap.height) {
                copyArr[i][j].r = tmp[j].r
                copyArr[i][j].g = tmp[j].g
                copyArr[i][j].b = tmp[j].b
            }
        }

        image.pixelsArray = copyArr
    }
}
