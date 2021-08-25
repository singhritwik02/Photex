package com.ritwik.photex

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class TwitterAccountDatabase(context: Context,
) {
    companion object
    {
        private const val TAG = "TwitterAccountDatabase"
        fun addToSharedPref(name:String,username:String,bitmap: Bitmap,context: Context)
        {
            val sharedPreferences = context.getSharedPreferences("TWITTER_SHARED",MODE_PRIVATE)
            with(sharedPreferences.edit())
            {
                putString("NAME",name)
                putString("USERNAME",username)
                commit()
            }
            saveBitmap(bitmap,context)
        }
        private fun saveBitmap(bitmap: Bitmap,context: Context)
        {
            try {


                val fileParent = File("${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/Twitter")
                if(!fileParent.exists())
                {
                    fileParent.mkdir()
                }
                val file = File(fileParent,"Twitter.png")
                val outStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream.flush()
                outStream.close()
            }
            catch (e:Exception)
            {
                e.printStackTrace()
            }

        }
        fun getDetails(context: Context):ArrayList<String>
        {
            val list = arrayListOf<String>()
            val sharedPreferences = context.getSharedPreferences("TWITTER_SHARED",MODE_PRIVATE)
            list.add(sharedPreferences.getString("NAME","")?:"")
            list.add(sharedPreferences.getString("USERNAME","")?:"")
            return list
        }
        fun getProfileThumbnail(context: Context):Bitmap?
        {
            val file = File("${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/Twitter/Twitter.png")
            if(!file.exists())
            {
                return null
            }
            val bitmap = BitmapFactory.decodeFile(file.path)
            return bitmap
        }
    }
}