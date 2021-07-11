package com.ritwik.photex

import android.graphics.Bitmap

class BitmapFunctions {
    companion object
    {
        fun getResizedBitmap(toModifyBitmap: Bitmap, referenceBitmap: Bitmap, percent:Float, resizeAlong:String)
        {
//            resize along  => H = height/W = Width/L = Larger Side
            val scale = toModifyBitmap.width/toModifyBitmap.height
        }

    }
}