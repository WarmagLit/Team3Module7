package com.example.graphiceditor

import android.text.Editable

const val alpha = 24
const val red = 16
const val green = 8
const val blue = 0

fun Int.component(component: Int) =
    this shr component and 0xff

fun Editable.toDouble() = this.toString().toDouble()

fun colorOf(a: Int, r: Int, g: Int, b: Int): Int{
    return (a shl alpha) + (r shl red) + (g shl green) + b
}

fun colorOf(r: Int, g: Int, b: Int): Int{
    return (255 shl alpha) + (r shl red) + (g shl green) + b
}