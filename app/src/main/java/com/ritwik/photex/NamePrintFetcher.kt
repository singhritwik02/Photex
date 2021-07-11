package com.ritwik.photex

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap

class NamePrintFetcher {
    companion object {
        private var namePrintList = arrayListOf<NamePrint>()
        fun addNamePrint(context: Context,namePrint: NamePrint) {
            val sharedPreferences = context.getSharedPreferences("NAMES",MODE_PRIVATE)
            with(sharedPreferences.edit())
            {
                putString(namePrint.getDisplayTitle(),namePrint.getDisplayText())
                commit()
            }
        }
        fun retrieveNames(context:Context,mainBitmap: Bitmap):ArrayList<NamePrint> {
            val sharedPreferences = context.getSharedPreferences("NAMES",MODE_PRIVATE)
            val names = sharedPreferences.all
            namePrintList.clear()
            for(n in names)
            {//
                val temp = NamePrint()
                temp.setDisplayTitle(n.key)
                temp.setDisplayText(n.value.toString())
                val bmp = getBitmap(context,temp.getDisplayText(),mainBitmap)
                if(bmp!=null) {
                    temp.setDisplayIcon(bmp)
                }
                namePrintList.add(temp)
            }
            return namePrintList
        }
        fun getBitmap(context: Context,fileName:String,mainBitmap: Bitmap):Bitmap?
        {
            val temp = BitmapFunctions.getBitmapFromAssets(context,fileName)
            var bitmap:Bitmap? = null
            if(temp!=null)
            {
                bitmap = BitmapFunctions.getResizedBitmap(temp,mainBitmap,0.01f,"L")
            }
            return bitmap
        }
    }
}