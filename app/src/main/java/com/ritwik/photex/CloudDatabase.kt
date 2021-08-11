package com.ritwik.photex

import com.google.firebase.auth.FirebaseAuth

class CloudDatabase {
    companion object {
        private const val TAG = "CloudDatabase"

        fun isLoggedIn(): Boolean {
            val auth = FirebaseAuth.getInstance()
            return auth!=null
        }
        fun incrementDownloads()
        {

        }
    }
}