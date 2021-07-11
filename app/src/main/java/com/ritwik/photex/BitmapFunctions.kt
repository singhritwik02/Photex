package com.ritwik.photex

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.IOException
import java.io.InputStream

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
        fun getBitmapFromAssets(context:Context,fileName:String):Bitmap?
        {
            val assetManager = context.assets

            val istr: InputStream
            var bitmap: Bitmap? = null
            try {
                istr = assetManager.open(fileName)
                bitmap = BitmapFactory.decodeStream(istr)
            } catch (e: IOException) {
                // handle exception
                e.printStackTrace()
            }
            return bitmap

        }

    }
}