package com.ritwik.photex

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class FontDatabase(val context: Context) {
    fun getOfflineFonts(): ArrayList<String> {
        var arrayList = arrayListOf<String>()
        val file = context.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/Fonts")
        file?.let {
            for (sf in it.listFiles()) {
                // Log.d(TAG, "getOfflineFonts: ${sf.name}")
                arrayList.add(sf.name)
            }
        }
        arrayList.sort()
        return arrayList

    }

    fun checkForUpdate(function: (updateAvailable: Boolean) -> Unit) {

        var offlineCount = getOfflineFonts().size
        Log.d(TAG, "checkForUpdate: Offline Count = $offlineCount")
        var onlineCount = 0
        getOnlineFontNumber {
            onlineCount = it
            Log.d(TAG, "checkForUpdate: online Count Number = $onlineCount")
            if (offlineCount != onlineCount) {
                Log.d(TAG, "checkForUpdate: font update available")
                function(true)
            } else {
                function(false)
                Log.d(TAG, "checkForUpdate: Fonts up to date")
            }
        }
        Log.d(TAG, "checkForUpdate: online Count Number = $onlineCount")

    }

    fun update(function: (Int) -> Unit) {
        var onlineArray = mapOf<String, String>()
        val offlineArray = getOfflineFonts()
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("FONTS").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Log.d(TAG, "onDataChange: Snapshot does not exist")
                        return
                    }
                    if (snapshot.hasChildren()) {
                        var count = 0
                        val array = arrayListOf<OnlineFontDetails>()
                        for (child in snapshot.children) {
                            count += 1
                            val key = child.key
                            Log.d(TAG, "onDataChange: $key")
                            if (offlineArray.contains("${key}.ttf")) {
                                //Log.d(TAG, "onDataChange: already downloaded = $key")
                                //Log.d(TAG, "onDataChange: Offline fonts contain key")


                            } else {
                                if (key != null) {
                                    onlineArray += onlineArray.plus(
                                        Pair(
                                            key,
                                            child.value.toString()
                                        )
                                    )
                                    //downloadFont(key, child.value.toString())
                                    val temp = OnlineFontDetails()
                                    temp.setFontName(key)
                                    temp.setLink(child.value.toString())
                                    array.add(temp)


                                }
                            }
                        }
                        iterate(array,0,function)


                    } else {
                        //Log.d(TAG, "onDataChange: Snapshot does not have children")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: ${error.message}")
                }

            }
        )
    }

    fun downloadFont(name: String, url: String, function: () -> Unit) {
        val file = context.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/Fonts")
        val newFont = File(file, "${name}.ttf")
        if (newFont.exists()) {
            newFont.delete()
        }
        FirebaseStorage.getInstance().getReferenceFromUrl(url).getFile(newFont)
            .addOnSuccessListener {
                Log.d(TAG, "downloadFont: Font downloaded = $name")
                function()

            }
    }

    private fun getOnlineFontNumber(function: (Int) -> Unit) {
        var count = 0
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("FONTS").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Log.d(TAG, "onDataChange: Snapshot does not exist")
                        return
                    }
                    if (snapshot.hasChildren()) {
                        count = snapshot.childrenCount.toInt()
                        function(count)
                        return
                    } else {
                        function(0)
                        return
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: cancelled")
                    return
                }

            })
    }

    inner class OnlineFontDetails
    {
       private lateinit var fontName:String
        private lateinit var downLink:String
        fun getFontName():String
        {
            return if(!this::fontName.isInitialized)
            {
                ""
            }
            else
            {
                fontName
            }
        }
        fun getDownloadLink():String
        {
            return if(!this::downLink.isInitialized)
            {
                ""
            }
            else {
                downLink
            }
        }
        fun setFontName(name:String)
        {

            fontName = name
        }
        fun setLink(link:String)
        {
            downLink = link
        }
    }
    private fun iterate(array:ArrayList<OnlineFontDetails>,index:Int,function: (Int) -> Unit)
    {
        if(index<array.size)
        {
            download(array,index,function)
            function(1)
        }
        else
        {
            function(0)
        }
    }
    private fun download(array:ArrayList<OnlineFontDetails>,index:Int,function: (Int) -> Unit)
    {
        val name = array[index].getFontName()
        val url = array[index].getDownloadLink()
        val file = context.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/Fonts")
        val newFont = File(file, "${name}.ttf")
        if (newFont.exists()) {
            newFont.delete()
        }
        FirebaseStorage.getInstance().getReferenceFromUrl(url).getFile(newFont)
            .addOnSuccessListener {
                Log.d(TAG, "downloadFont: Font downloaded = $name")
                iterate(array,index+1,function)

            }
    }
    companion object {
        private const val TAG = "FontDatabase"
    }
}