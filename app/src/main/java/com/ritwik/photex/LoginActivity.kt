package com.ritwik.photex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ritwik.photex.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar

import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.*


class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.alSignInButton.setOnClickListener {
            loginWithGoogle()
        }
        val memeArray = intArrayOf(R.raw.meme_2,R.raw.meme_3,R.raw.meme_4,R.raw.meme_5,R.raw.meme_6)
        val memeCode = (0..4).random()
        binding.alBackArrow.setOnClickListener {
            onBackPressed()
        }
        binding.alLogo.setAnimation(memeArray[memeCode])
    }
    fun loginWithGoogle()
    {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        if (FirebaseAuth.getInstance().currentUser == null)
        {
            val signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
            startActivityForResult(signInIntent, 2)
        }

    }
    private fun authenticateWithGoogle(idToken:String)
    {
        val credentials = GoogleAuthProvider.getCredential(idToken,null)
        FirebaseAuth.getInstance().signInWithCredential(credentials).addOnSuccessListener {
            Toast.makeText(this, "Logged in !", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to Login", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 2) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                authenticateWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                e.printStackTrace()
                // ...
            }
        }
    }

    companion object
    {
        private const val TAG = "LoginActivity"
    }
}