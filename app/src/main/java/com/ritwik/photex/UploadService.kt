package com.ritwik.photex

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.lang.UnsupportedOperationException
import javax.annotation.Nullable

class UploadService() : Service() {


    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet Implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Service")
        val uriString = intent!!.extras!!["URI"].toString()
        val uri = Uri.parse(uriString)
        uploadImage(uri)
        return START_STICKY

    }
    companion object
    {
        private const val TAG = "UploadService"
    }
    private fun uploadImage(imageUri: Uri) {
        val fileName = imageUri.lastPathSegment.toString()
        // creating the storage reference
        val storageRef =
            FirebaseStorage.getInstance().reference.child("UPLOADED_TEMPLATES").child(fileName)
        //uploading the image uri to the firebase storage
        storageRef.putFile(imageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnCompleteListener { downloadLinkTask ->
                val downloadLink = downloadLinkTask.result
                addUploadedTemplateToDatabase(fileName, downloadLink.toString())
                Log.d(TAG, "uploadImage: download Link = $downloadLink")
            }
        }.addOnFailureListener {
            Log.d(TAG, "uploadImage: Failed to Upload Image with Error  = ${it.toString()}")
        }

    }

    private fun addUploadedTemplateToDatabase(fileName: String, downloadLink: String) {
        val uid = FirebaseAuth.getInstance().uid!!
        // creating the database reference
        val databaseRef =
            FirebaseDatabase.getInstance().reference.child("Temp_Templates").child(fileName)
        // setting the values of the child nodes
        databaseRef.child("UPLOADER").setValue(uid)
        // setting the link of the image
        databaseRef.child("IMAGE_LINK").setValue(downloadLink)
        onDestroy()

    }

    override fun onDestroy() {
        super.onDestroy()
    }
}