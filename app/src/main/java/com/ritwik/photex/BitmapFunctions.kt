package com.ritwik.photex

import android.graphics.Bitmap
import android.util.Log

class BitmapFunctions {
    companion object
    {
        private const val TAG = "BitmapFunctions"
        fun getResizedBitmap(toModifyBitmap: Bitmap, referenceBitmap: Bitmap, percent: Float, resizeAlong:String):Bitmap
        {
//            resize along  => H = height/W = Width/L = Larger Side
            val scale = toModifyBitmap.width/toModifyBitmap.height
            var width = 0f
            var height = 0f
            when(resizeAlong)
            {
                 "H"->
                 {
                     height = (percent*referenceBitmap.height).toFloat()
                     width = height*scale
                 }
                "W"->
                {
                    width = (percent*referenceBitmap.width)
                    height = width/scale
                }
                "L"->
                {
                    if(toModifyBitmap.width>=toModifyBitmap.height)
                    {
                        width = (percent*referenceBitmap.width)
                        height = width/scale
                    }
                    else
                    {

                        height = (percent*referenceBitmap.height).toFloat()
                        width = height*scale
                    }
                }
            }
            Log.d(TAG, "getResizedBitmap: Final width = $width")
            Log.d(TAG, "getResizedBitmap: Final Height = $height")
            // creating the scaled bitmap
            val toReturn = Bitmap.createScaledBitmap(toModifyBitmap, width.toInt(),
                height.toInt(),false)
            return toReturn
        }


    }
}