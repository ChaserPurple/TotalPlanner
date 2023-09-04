package com.example.totalplanner.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

const val upperThreshold = 128
const val lowerThreshold = 40

class MyColor (
    var r: Float = 0f,
    var g: Float = 0f,
    var b: Float = 0f,
    var a: Float = 0f
){
    /*
     * Returns a graphics color object that can be used by composables
     */
    fun toColor(): Color{
        return Color(r,g,b,a)
    }

    /*
     * Returns whether a color is too light to contrast well with white
     */
    fun isLight(): Boolean{
        return 0.2126 * toColor().toArgb().red +
                0.7152 * toColor().toArgb().green +
                0.0722 * toColor().toArgb().blue > upperThreshold
    }
    /*
     * Returns whether a color is too dark to contrast well with black
     */
    fun isDark(): Boolean{
        return 0.2126 * toColor().toArgb().red +
                0.7152 * toColor().toArgb().green +
                0.0722 * toColor().toArgb().blue < lowerThreshold
    }
}

/*
 * Returns a MyColor object that can be stored with Room using a type converter
 */
fun Color.toMyColor(): MyColor {
    return MyColor(red,green,blue,alpha)
}