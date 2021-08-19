package com.ritwik.photex

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ritwik.photex.databinding.ActivityMainBinding
import com.ritwik.photex.databinding.PopupExitConfirmationBinding
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity

import com.google.android.gms.tasks.OnFailureListener

import com.google.firebase.dynamiclinks.PendingDynamicLinkData

import com.google.android.gms.tasks.OnSuccessListener

import com.google.firebase.dynamiclinks.FirebaseDynamicLinks





class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val FRAGMENT_MAIN = "MAIN_MENU"
    val FRAGMENT_CHANGE_TEXT = "CHANGE_TEXT"
    val FRAGMENT_CHANGE_SIZE = "CHANGE_SIZE"
    val FRAGMENT_CHANGE_COLOR = "CHANGE_COLOR"
    val FRAGMENT_CHANGE_STROKE = "CHANGE_STROKE"
    val FRAGMENT_CHANGE_BACKGROUND = "CHANGE_BACKGROUND"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction()
            .replace(binding.amFragmentLayout.id, homeFragment, "HOME_FRAGMENT").commit()

        val view = binding.root
        setContentView(view)
        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser == null)
        {
            startActivity(Intent(this,LoginActivity::class.java))
        }
        else
        {

        }
//        FirebaseDynamicLinks.getInstance()
//            .getDynamicLink(intent)
//            .addOnSuccessListener(
//                this
//            ) { pendingDynamicLinkData ->
//                // Get deep link from result (may be null if no link is found)
//                var deepLink: Uri? = null
//                if (pendingDynamicLinkData != null) {
//                    deepLink = pendingDynamicLinkData.link
//                }
//
//                Log.e(TAG, "onCreate: Link received")
//                Log.e(TAG, "onCreate: ${deepLink?.toString()}")
//                Toast.makeText(this, "${deepLink?.toString()}", Toast.LENGTH_SHORT).show()
//                // Handle the deep link. For example, open the linked
//                // content, or apply promotional credit to the user's
//                // account.
//                // ...
//
//                // ...
//            }
//            .addOnFailureListener(
//                this
//            ) { e ->
//                Log.w(
//                    TAG,
//                    "getDynamicLink:onFailure",
//                    e
//                )
//            }

    }

    fun showExitConfirmation(fragment: CreateFragment)
    {
        val confirmationBinding = PopupExitConfirmationBinding.inflate(layoutInflater)
        val displayMetrics = resources.displayMetrics
        val window = PopupWindow(confirmationBinding.root, WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,true)
        with(window)
        {
            elevation = 100f
            showAtLocation(confirmationBinding.root, Gravity.CENTER,0,0)
        }
        with(confirmationBinding)
        {

            try {
                pecPreview.setImageBitmap(fragment.getPreview())
            }
            catch (e:Exception)
            {
                e.printStackTrace()
                super.onBackPressed()
                window.dismiss()
            }

            pecCancel.setOnClickListener {
                window.dismiss()
            }
            pecDiscardImage
                .setOnClickListener {
                    super.onBackPressed()
                    window.dismiss()
                }


        }
    }

    override fun onBackPressed() {

        val fragment = supportFragmentManager.findFragmentById(binding.amFragmentLayout.id)
        if (fragment != null) {
            val tag = fragment.tag ?: "HOME_FRAGMENT"
            Log.d(TAG, "onBackPressed: tag = $tag")
            if (tag == "CREATE_FRAGMENT") {
                val createFragment = fragment as CreateFragment
                val selectedMenu = createFragment.menuFragmentManager.getSelectedTag()
                if(selectedMenu == FRAGMENT_MAIN)
                {
                    Log.d(TAG, "onBackPressed:  main menu fragment")
                    showExitConfirmation(fragment)
                    return
                }
                else
                {
                    Log.d(TAG, "onBackPressed: Not Main menu fragment, returning to main menu")
                    createFragment.menuFragmentManager.showMainMenuFragment()
                }


            }
            else
            {
                super.onBackPressed()
            }


        } else {
            super.onBackPressed()
        }

    }
    companion object
    {
        private const val TAG = "MainActivity"
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
    }

}