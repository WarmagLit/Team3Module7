package com.example.graphiceditor

class Filters() {
    fun Check(told: ProcessedPicture, As: String)
    {
        when (As){
            "DiagonalSepia" -> diagonalSepia(told)
            "Grey" -> grey(told)
            "Sepia" -> sepia(told)
            "Blue" -> blue(told)
            "Red" -> red(told)
            "Green" -> green(told)
            "SwapColors" -> swapColors(told)
            "Negative" -> negative(told)
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
                if (image.pixelsArray[i][j].b < 200) {
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
                if (image.pixelsArray[i][j].r < 200) {
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
                if (image.pixelsArray[i][j].g < 200) {
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
}
