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
                Log.d(TAG, "getOfflineFonts: ${sf.name}")
                arrayList.add(sf.name)
            }
        }
        arrayList.sort()
        return arrayList

    }

    fun loadFonts(function: (Map<String, String>) -> Unit) {
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
                        for (child in snapshot.children) {
                            val key = child.key
                            Log.d(TAG, "onDataChange: $key")
                            if (offlineArray.contains("${key}.ttf")) {
                                Log.d(TAG, "onDataChange: already downloaded = $key")
                                Log.d(TAG, "onDataChange: Offline fonts contain key")


                            } else {
                                if (key != null) {
                                    onlineArray += onlineArray.plus(
                                        Pair(
                                            key,
                                            child.value.toString()
                                        )
                                    )
                                    downloadFont(key,child.value.toString())

                                    Log.d(TAG, "onDataChange: Adding $key = ${child.value}")
                                }
                            }
                        }
                        function(onlineArray)
                    } else {
                        Log.d(TAG, "onDataChange: Snapshot does not have children")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: ${error.message}")
                }

            }
        )
    }

    fun downloadFont(name: String, url: String) {
        val file = context.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/Fonts")
        val newFont = File(file, "${name}.ttf")
        if(newFont.exists())
        {
            newFont.delete()
        }
        FirebaseStorage.getInstance().getReferenceFromUrl(url).getFile(newFont)
            .addOnSuccessListener {
                Log.d(TAG, "downloadFont: Font downloaded = $name")

            }
    }

    companion object {
        private const val TAG = "FontDatabase"
    }
}