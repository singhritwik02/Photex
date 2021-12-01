package com.ritwik.photex


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.ritwik.photex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var templateString: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        intent.extras?.let {
            if (it.get("TEMPLATE_STRING") != null) {
                templateString = it.get("TEMPLATE_STRING").toString()
            } else if (it.containsKey("SEARCH_STRING")) {
                templateString = it.get("SEARCH_STRING").toString()
            }
        }
        val homeFragment = HomeFragment()
        if (templateString != null) {
            with(Bundle())
            {
                putString("TEMPLATE", templateString)
                homeFragment.arguments = this

            }
            Log.d(TAG, "onCreate: template string is not null")
        } else {
            Log.d(TAG, "onCreate: template string is null")
        }
        supportFragmentManager.beginTransaction()
            .replace(binding.amFragmentLayout.id, homeFragment, "HOME_FRAGMENT").commit()

        val view = binding.root
        setContentView(view)
        showReviewPopup()
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {

        }

        startService(Intent(this, Notifications::class.java))
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d(TAG, "onCreate: Failed to get token")
                return@addOnCompleteListener
            }
            val token = task.getResult()
            Log.d(TAG, "onCreate: $token")


        }


    }


    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onResume() {
        super.onResume()

    }

    private fun showReviewPopup() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                    Log.d(TAG, "showReviewPopup: completed")
                }
            } else {
                // There was some problem, log or handle the error code.
                Log.d(TAG, "showReviewPopup: error getting request")
            }
        }

    }

    override fun onPause() {
        super.onPause()
    }

}