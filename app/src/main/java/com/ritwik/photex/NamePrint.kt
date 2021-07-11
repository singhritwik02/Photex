package com.ritwik.photex

import android.graphics.Bitmap

class NamePrint: Items() {
    private var displayText = ""
    private lateinit var displayIcon: Bitmap
    fun setDisplayText(text:String)
    {
        displayText = text
    }
    fun getDisplayText():String
    {
        return displayText
    }
    fun setDisplayIcon(icon:Bitmap)
    {
        displayIcon = icon
    }
    fun getDisplayIcon():Bitmap
    {
        return displayIcon
    }


}