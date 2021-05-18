package com.example.graphiceditor

import kotlin.math.ceil
import kotlin.math.exp

class Unsharp() {
    companion object {
        fun unsharpFilter(image: PixelArray, sigma: Double, k: Double) :PixelArray {
            val blurImage = Array(image.width){ x ->
                Array(image.height){ y ->
                    intArrayOf(image[x,y].component(red),
                        image[x,y].component(green),
                        image[x,y].component(blue))
                }
            }
            var sum: Double
            var pix_r: Int
            var pix_g: Int
            var pix_b: Int
            var s2: Double = 2 * sigma * sigma
            var N: Int = ceil(3 * sigma).toInt()

            var window = DoubleArray(N * 2 + 1) { 0.0 }
            var tmp = Array(image.width) { intArrayOf(0,0,0) }
            window[N] = 1.0
            for (i in 1..N) {
                window[N+i] = exp(-i * i / s2)
                window[N-i] = window[N+i]
            }
            for (j in 0..image.height - 1) {
                for (i in 0..image.width - 1) {
                    sum = 0.0
                    pix_b = 0
                    pix_g = 0
                    pix_r = 0
                    for (k in -N..N) {
                        var l = i + k
                        if (l >= 0 && l < image.width) {
                            pix_r = (pix_r + image[l,j].component(red) * window[N+k]).toInt()
                            pix_g = (pix_g + image[l,j].component(green) * window[N+k]).toInt()
                            pix_b = (pix_b + image[l,j].component(blue) * window[N+k]).toInt()
                            sum += window[N+k]
                        }
                    }

                    pix_r = (pix_r/sum).toInt()
                    pix_g = (pix_g/sum).toInt()
                    pix_b = (pix_b/sum).toInt()
                    tmp[i][0] = pix_r
                    tmp[i][1] = pix_g
                    tmp[i][2] = pix_b
                }
                for (i in 0 until image.width) {
                    blurImage[i][j][0] = tmp[i][0]
                    blurImage[i][j][1] = tmp[i][1]
                    blurImage[i][j][2] = tmp[i][2]
                }
            }

            window = DoubleArray(N * 2 + 1) { 0.0 }
            tmp = Array(image.height) { intArrayOf(0,0,0) }
            window[N] = 1.0
            for (i in 1..N) {
                window[N+i] = exp(-i * i / s2)
                window[N-i] = window[N+i]
            }
            for (i in 0 until image.width) {
                for (j in 0 until image.height) {
                    sum = 0.0
                    pix_b = 0
                    pix_g = 0
                    pix_r = 0
                    for (k in -N..N) {
                        val l = j + k
                        if (l >= 0 && l < image.height) {
                            pix_r = (pix_r + image[i,l].component(red) * window[N+k]).toInt()
                            pix_g = (pix_g + image[i,l].component(green) * window[N+k]).toInt()
                            pix_b = (pix_b + image[i,l].component(blue) * window[N+k]).toInt()
                            sum += window[N+k]
                        }
                    }
                    pix_r = (pix_r/sum).toInt()
                    pix_g = (pix_g/sum).toInt()
                    pix_b = (pix_b/sum).toInt()
                    tmp[j][0] = pix_r
                    tmp[j][1] = pix_g
                    tmp[j][2] = pix_b

                }
                for (j in 0 until image.height) {
                    blurImage[i][j][0] = tmp[j][0]
                    blurImage[i][j][1] = tmp[j][1]
                    blurImage[i][j][2] = tmp[j][2]
                }
            }

            val zak = Array(image.width){ x ->
                Array(image.height){ y ->
                    intArrayOf(image[x,y].component(red),
                        image[x,y].component(green),
                        image[x,y].component(blue))
                }
            }
            for (j in 0 until image.height) {
                for (i in 0 until image.width) {
                    zak[i][j][0] = image[i,j].component(red) - blurImage[i][j][0]
                    zak[i][j][1] = image[i,j].component(green) - blurImage[i][j][1]
                    zak[i][j][2] = image[i,j].component(blue) - blurImage[i][j][2]
                }
            }

            val sharp = Array(image.width){ x ->
                Array(image.height){ y ->
                    intArrayOf(image[x,y].component(red),
                        image[x,y].component(green),
                        image[x,y].component(blue))
                }
            }
            for (j in 0 until image.height) {
                for (i in 0 until image.width) {
                    sharp[i][j][0] = (image[i,j].component(red) + zak[i][j][0] * k).toInt()
                    sharp[i][j][1] = (image[i,j].component(green) + zak[i][j][1] * k).toInt()
                    sharp[i][j][2] = (image[i,j].component(blue) + zak[i][j][2] * k).toInt()

                    if (sharp[i][j][0] > 255)
                        sharp[i][j][0] = 255

                    if (sharp[i][j][1] > 255)
                        sharp[i][j][1] = 255

                    if (sharp[i][j][2] > 255)
                        sharp[i][j][2] = 255

                    if (sharp[i][j][0] < 0)
                        sharp[i][j][0] = 0

                    if (sharp[i][j][1] < 0)
                        sharp[i][j][1] = 0

                    if (sharp[i][j][2] < 0)
                        sharp[i][j][2] = 0
                    image[i,j] = colorOf(sharp[i][j][0],sharp[i][j][1],sharp[i][j][2])
                }
            }

            return image
        }
    }
}