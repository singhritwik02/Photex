package com.ritwik.photex

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.util.Log

open class Items {
    var text:String = ""
    var type: String? = null
    private lateinit var stickerBitmap: Bitmap
    private lateinit var defaultStickerBitmap: Bitmap
    private lateinit var stickerThumbnail:Bitmap
    var locationX = 0f
    var locationY = 0f
    var rotation = 0f
    var paint = Paint()
    var size = 0f
    var strokePaint:Paint? = null
    var backgroundMargins:Margins? = null
    var backgroundAlpha = 255
    var stickerDimension:String = ""
    private lateinit var originalStickerDimension:IntArray
    fun setBackground(x:Float,y:Float,color:String?)
    {

        if(x == 0f && y == 0f)
        {
            backgroundMargins = null
            return
        }
        if(backgroundMargins == null) {
            backgroundMargins = Margins()
        }
        backgroundMargins?.let {
            it.marginY = y
            it.marginX = x
            if(color!=null) {
                it.backPaint.color = Color.parseColor(color)
            }
            else
            {
                if(it.backPaint.color==0)
                {
                    it.backPaint.color = Color.WHITE
                }
            }
        }
    }
    fun setStickerBitmap(bitmap: Bitmap)
    {
        this.stickerBitmap = bitmap
        if(!::originalStickerDimension.isInitialized)
        {
            originalStickerDimension = IntArray(2)
            originalStickerDimension[0] = bitmap.width
            originalStickerDimension[1] = bitmap.height
        }
        generateStickerThumbnail(stickerBitmap)
        {
            stickerThumbnail = it
        }
        if(!this::defaultStickerBitmap.isInitialized)
        {
            defaultStickerBitmap = bitmap
        }
        stickerDimension = "ORIGINAL"
    }
    fun getOriginalStickerBitmap():Bitmap
    {
        return defaultStickerBitmap
    }
    fun getStickerBitmap():Bitmap
    {
     return stickerBitmap
    }
    fun removeBackground()
    {
        backgroundMargins = null
    }
    fun getStickerThumbail():Bitmap
    {
        if(!this::stickerThumbnail.isInitialized)
        {
            generateStickerThumbnail(
                stickerBitmap
            )
            {
                stickerThumbnail = it
            }
        }
        return stickerThumbnail
    }
    fun getDefaultStickerDimen(): IntArray {
        return originalStickerDimension
    }
    inner class Margins
    {
         var marginX = 0f
         var marginY = 0f
        var backPaint:Paint = Paint()
    }
    fun generateStickerThumbnail(bitmap: Bitmap, function: (bitmap: Bitmap) -> Unit) {
        val scaled = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
        Log.d(TAG, "generateStickerThumbnail: ")
        if (scaled == null) {
            Log.d(TAG, "generateStickerThumbnail: null")
        } else {
            function(scaled)
            Log.d(TAG, "generateStickerThumbnail: not null")
        }


    }
    companion object
    {
        private const val TAG = "Items"
    }

}