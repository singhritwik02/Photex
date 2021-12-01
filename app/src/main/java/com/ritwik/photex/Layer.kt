package com.ritwik.photex

import android.graphics.Typeface
import android.view.View

class Layer {
    lateinit var view: View
    var type = ""
    var backgroundColorInt = 0
    var shadowColor = 0
    var alignment = ALIGNMENT_LEFT
    var isRemovable = true
    var width = 0
    var height = 0
    var oWidth = 0
    var oHeight = 0
    var baseTypeface:Typeface = Typeface.DEFAULT
    companion object
    {
        val ALIGNMENT_LEFT = "LEFT"
        val ALIGNMENT_CENTER = "CENTER"
        val ALIGNMENT_RIGHT = "RIGHT"
        val LT_TEXT = "TEXT"
        val LT_BITMAP = "BITMAP"
    }
}