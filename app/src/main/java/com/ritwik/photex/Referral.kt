package com.ritwik.photex

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

class Referral(val context: Context) {
    fun createReferralLink(): String {
        var domainUriPrefix = ""
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()

        dynamicLink.link = Uri.parse("https://photex.page.link/j6mu")
        dynamicLink.setDomainUriPrefix("https://photex.page.link")
        val androidOptionsBuilder = DynamicLink.AndroidParameters.Builder("com.ritwik.photex")
        androidOptionsBuilder.minimumVersion = 12
        val androidOptions = androidOptionsBuilder.build()
        dynamicLink.setAndroidParameters(androidOptions)

        val dynamicLinkFinal = dynamicLink.buildDynamicLink()
        val dynamicLinkUri = dynamicLinkFinal.uri
        return dynamicLinkUri.toString()
    }

    fun getCustomLink(context: Context): String {
        val link = if (FirebaseAuth.getInstance().currentUser != null) {
            val uid = FirebaseAuth.getInstance().currentUser.uid
            "https://photex.page.link/?link=https://photex.page.link/j6mu-uid=$uid&apn=com.ritwik.photex&amv=12&st=Photex&sd=Download Photex and create your own memes"

        } else {
            Toast.makeText(context, "You need to be logged in!", Toast.LENGTH_SHORT).show()
            ""
        }


        return link

    }



     fun setFirstRun() {
        val sharedPreferences = context.getSharedPreferences("REFERAL", MODE_PRIVATE)
        with(sharedPreferences.edit())
        {
            putBoolean("FIRST_RUN", false)
            commit()
        }
         Log.d(TAG, "setFirstRun: Set referrer")
    }

    fun rewardToReferrer(UID: String) {
        getReferrerPoints(UID) { points ->
            Log.d(TAG, "rewardToReferrer: Rewarding referrer")
            FirebaseDatabase.getInstance().reference.child("USERS").child(UID).child("POINTS")
                .setValue(points + 1)
        }
    }

    fun checkReferralDuplicacy(function:(isDuplicate:Boolean)->Unit)
    {
        if(CloudDatabase.getUid()!=null) {
            FirebaseDatabase.getInstance().reference.child("USERS").child(CloudDatabase.getUid()!!).addValueEventListener(
                object :ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists())
                        {
                            function(false)
                        }
                        else
                        {
                            val isDuplicate = if(snapshot.hasChild("REFERRER"))
                            {
                                snapshot.child("REFERRER").value.toString() != "null"
                            }
                            else
                            {
                             false
                            }
                            function(isDuplicate)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "onCancelled: ")
                        function(true)
                    }

                }
            )
        }
    }
    fun setReferrer(UID:String)
    {
        Log.d(TAG, "setReferrer: Setting referrer")
        val UID = CloudDatabase.getUid()?:return
        FirebaseDatabase.getInstance().reference.child("USERS").child(UID).child("REFERRER").setValue(UID)
    }
    private fun getReferrerPoints(referrerUID: String, function: (Long) -> Unit) {
        FirebaseDatabase.getInstance().reference.child("USERS").child(referrerUID)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            function(0)
                        } else {
                            if(snapshot.hasChild("POINTS")) {
                                val count = (snapshot.child("POINTS").value) as Long
                                function(count)
                            }
                            else
                            {
                                function(0)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        function(0)
                    }

                }
            )
    }

    companion object {
        private const val TAG = "Referral"
        fun isFirstRun(context: Context): Boolean {
            val sharedPreferences = context.getSharedPreferences("REFERAL", MODE_PRIVATE)
            return if (sharedPreferences.contains("FIRST_RUN")) {
                sharedPreferences.getBoolean("FIRST_RUN", false)
            } else {
                true
            }
        }
        fun showShareMenu(context: Context)
        {
            val link = if (FirebaseAuth.getInstance().currentUser != null) {
                val uid = FirebaseAuth.getInstance().currentUser.uid
                "https://photex.page.link/?link=https://photex.page.link/j6mu-uid=$uid&apn=com.ritwik.photex&amv=12&st=Photex&sd=Download Photex and create your own memes"


            } else {
                Toast.makeText(context, "You need to be logged in!", Toast.LENGTH_SHORT).show()
                ""
            }
            if (link!="")
            {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT,link)
                intent.type = "text/plain"
                context.startActivity(intent)
            }
        }
        fun getYourPoints(context: Context,function: (Long) -> Unit)
        {
            val UID = CloudDatabase.getUid()
            if(UID == null)
            {
                Toast.makeText(context, "You need to be logged in", Toast.LENGTH_SHORT).show()
                return
            }
            FirebaseDatabase.getInstance().reference.child("USERS").child(UID)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                function(0)
                            } else {
                                if(snapshot.hasChild("POINTS")) {
                                    val count = (snapshot.child("POINTS").value) as Long
                                    function(count)
                                }
                                else
                                {
                                    function(0)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            function(0)
                        }

                    }
                )

        }
    }
    fun deductPoint()
    {
        val uid = CloudDatabase.getUid()
        if(uid == null)
        {
            return
        }
        getYourPoints(context)
        {currentPoints->
            FirebaseDatabase.getInstance().reference.child("USERS").child(uid).child("POINTS")
                .setValue(currentPoints - 1)
        }
    }
}