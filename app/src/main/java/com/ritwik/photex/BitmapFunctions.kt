package com.ritwik.photex

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt

class BitmapFunctions {
    companion object {
        private const val TAG = "BitmapFunctions"
        fun getResizedBitmap(
            toModifyBitmap: Bitmap,
            referenceBitmap: Bitmap,
            percent: Float,
            resizeAlong: String
        ): Bitmap {
//            resize along  => H = height/W = Width/L = Larger Side
            val scale = toModifyBitmap.width / toModifyBitmap.height
            var width = 0f
            var height = 0f
            when (resizeAlong) {
                "H" -> {
                    height = (percent * referenceBitmap.height).toFloat()
                    width = height * scale
                }
                "W" -> {
                    width = (percent * referenceBitmap.width)
                    height = width / scale
                }
                "L" -> {
                    if (toModifyBitmap.width >= toModifyBitmap.height) {
                        width = (percent * referenceBitmap.width)
                        height = width / scale
                    } else {

                        height = (percent * referenceBitmap.height).toFloat()
                        width = height * scale
                    }
                }
            }
            Log.d(TAG, "getResizedBitmap: Final width = $width")
            Log.d(TAG, "getResizedBitmap: Final Height = $height")
            // creating the scaled bitmap
            val toReturn = Bitmap.createScaledBitmap(
                toModifyBitmap, width.toInt(),
                height.toInt(), false
            )
            return toReturn
        }

        fun getBitmapFromAssets(context: Context, fileName: String): Bitmap? {
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

        fun createWatermark(
            context: Context,
            platform: String,
            text: String,
            referenceBitmap: Bitmap,
            scale: Float
        ): Bitmap? {
            val paint = Paint()
            // setting the typeFace
            paint.typeface = ResourcesCompat.getFont(context, R.font.avenir_next_bold)

            // getting the size of font
            val newScale = 0.03f * scale
            Log.d(TAG, "createWatermark: new scale = $newScale")
            val newHeight = referenceBitmap.height * newScale
            paint.textSize = newHeight
            var fileName = ""
            when (platform) {
                "Instagram" -> {
                    fileName = "instagram.png"
                }
                "Twitter" -> {
                    fileName = "twitter.png"
                }
                "Youtube" -> {
                    fileName = "youtube.png"
                }
                "Facebook" -> {
                    fileName = "facebook.png"
                }
            }
            val tempIcon = getBitmapFromAssets(context, fileName)
            // resizing the icon
            var icon = tempIcon
            if (tempIcon != null) {
                icon = BitmapFunctions.getResizedBitmap(tempIcon, referenceBitmap, newScale, "H")

            } else {
                return null
            }
            val finalIcon = icon!!
            // calculating the dimensions of the canvas Bitmap
            val margin = newHeight / 2

            val canvasHeight = newHeight + (2 * margin)
            val canvasWidth = (finalIcon.width) + (3 * margin) + paint.measureText(text)
            // creating a bitmap with the above dimensions
            val mainBitmap = Bitmap.createBitmap(
                canvasWidth.toInt(),
                canvasHeight.toInt(),
                Bitmap.Config.ARGB_8888
            )
            // drawing the icon to the bitmap
            val canvas = Canvas(mainBitmap)
            val rectPaint = Paint()
            paint.color = Color.WHITE
            canvas.drawRoundRect(
                0f, 0f,
                mainBitmap.width.toFloat(), mainBitmap.height.toFloat(), 15f, 15f, rectPaint
            )
            val bitmapY = (mainBitmap.height / 2) - (finalIcon.height / 2)
            val bitmapX = margin
            // drawing the icon bitmap
            canvas.drawBitmap(finalIcon, bitmapX.toFloat(), bitmapY.toFloat(), null)
            val textX = (finalIcon.width) + margin
            // getting the height of text
            val fontMetrics = Paint.FontMetrics()
            paint.getFontMetrics(fontMetrics)
            val textHeight = fontMetrics.bottom - fontMetrics.top
            val textY = mainBitmap.height - (textHeight / 2)

            // drawing the text on canvas
            canvas.drawText(text, textX.toFloat(), textY.toFloat(), paint)
            return mainBitmap
        }

        fun setBorders(
            referenceBitmap: Bitmap,
            borderPercent: Float,
            colorString: String?
        ): Bitmap {
            val width = referenceBitmap.width + 2 * (referenceBitmap.width * borderPercent)
            val height = referenceBitmap.height + 2 * (referenceBitmap.height * borderPercent)
            Log.d(TAG, "setBorders: bitmap width with borders = $width")
            Log.d(TAG, "setBorders: bitmap height with borders = $height")
            // creating a new bitmap
            val bitmap = Bitmap.createBitmap(
                width.roundToInt(),
                height.roundToInt(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            // drawing the main bitmap onto the plain canvas
            val color =
                if (colorString != null) {
                    var color = Color.WHITE
                    try {
                        color = Color.parseColor(colorString)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        color = Color.WHITE

                    }
                    color
                } else {
                    Color.WHITE
                }
            val canvasPaint = Paint()
            canvasPaint.color = color
            canvas.drawPaint(canvasPaint)

            // drawing the bitmap over the canvas,leaving the respective borders
            val left = (bitmap.width - referenceBitmap.width).toFloat()/2
            val top = (bitmap.height - referenceBitmap.height).toFloat()/2

            canvas.drawBitmap(referenceBitmap,left,top,null)
            return bitmap
        }
        fun increaseLeftBorder(referenceBitmap: Bitmap,borderPercent: Float,colorString: String?):Bitmap
        {
            Log.d(TAG, "increaseLeftBorder: border percent = $borderPercent")
            val width = referenceBitmap.width + (referenceBitmap.width * borderPercent)
            val height = referenceBitmap.height
            Log.d(TAG, "setBorders: bitmap width with borders = $width")
            Log.d(TAG, "setBorders: bitmap height with borders = $height")
            // creating a new bitmap
            val bitmap = Bitmap.createBitmap(
                width.roundToInt(),
                height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            // drawing the main bitmap onto the plain canvas
            val color =
                if (colorString != null) {
                    var color = Color.WHITE
                    try {
                        color = Color.parseColor(colorString)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        color = Color.WHITE

                    }
                    color
                } else {
                    Color.WHITE
                }
            val canvasPaint = Paint()
            canvasPaint.color = color
            canvas.drawPaint(canvasPaint)

            // drawing the bitmap over the canvas,leaving the respective borders
            val left = (bitmap.width - referenceBitmap.width.toFloat())
            val top = 0f

            canvas.drawBitmap(referenceBitmap,left,top,null)
            return bitmap
        }
        private fun setMarginZero(referenceBitmap: Bitmap,side:String)
        {
            when(side)
            {
                "L"->
                {

                }
            }
        }

    }
}