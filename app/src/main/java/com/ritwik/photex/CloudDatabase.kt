package com.ritwik.photex

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CloudDatabase {
    companion object {
        private const val TAG = "CloudDatabase"

        fun isLoggedIn(): Boolean {
            val auth = FirebaseAuth.getInstance()
            return auth != null
        }
        fun getUid(): String? {
            return FirebaseAuth.getInstance().currentUser?.uid
        }
        fun incrementNoOfSaves()
        {
            val uid = getUid()?:return
            getNoOfSaves {saves->
                FirebaseDatabase.getInstance().reference.child("USERS").child(uid).child("SAVES").setValue(saves+1)

            }
        }
        private fun getNoOfSaves(function: (Long) -> Unit)
        {
            val uid = getUid()?:null
            FirebaseDatabase.getInstance().reference.child("USERS").child(uid!!).child("SAVES")
                .addListenerForSingleValueEvent(
                    object :ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(!snapshot.exists())
                            {

                                function(0)
                                return
                            }
                            val saves = snapshot.value as Long
                            function(saves)

                        }

                        override fun onCancelled(error: DatabaseError) {
                            function(-1)
                        }

                    }
                )

        }

    }

    fun createUserDatabase() {
        checkIfDatabaseExists { status->
            if (!status)
            {
                Log.d(TAG, "createUserDatabase: status = $status")
                val uid = getUid()?:return@checkIfDatabaseExists
                val database = FirebaseDatabase.getInstance().reference.child("USERS").child(uid)
                database.child("UID").setValue(uid)

            }
        }
    }


    fun checkIfDatabaseExists(function: (Boolean) -> Unit) {
        Log.d(TAG, "checkIfDatabaseExists: ")
        val UID = getUid() ?: return
        FirebaseDatabase.getInstance().reference.child("USERS").child(UID)
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Log.d(TAG, "onDataChange: snapshot exists")
                            function(true)
                        } else {
                            Log.d(TAG, "onDataChange: snapshot does not exist")
                            function(false)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        function(false)
                    }

                }
            )
    }


}