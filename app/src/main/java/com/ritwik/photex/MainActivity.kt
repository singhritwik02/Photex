package com.ritwik.photex

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.AudienceNetworkAds.InitListener
import com.facebook.ads.AudienceNetworkAds.InitResult
import com.ritwik.photex.BuildConfig.DEBUG
import com.ritwik.photex.databinding.ActivityMainBinding
import com.ritwik.photex.databinding.PopupExitConfirmationBinding


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
        AudienceNetworkAds.initialize(this);

    }
    class AudienceNetworkInitializeHelper : InitListener {
        override fun onInitialized(result: InitResult) {
            Log.d(AudienceNetworkAds.TAG, result.message)
        }

        companion object {
            /**
             * It's recommended to call this method from Application.onCreate().
             * Otherwise you can call it from all Activity.onCreate()
             * methods for Activities that contain ads.
             *
             * @param context Application or Activity.
             */
            fun initialize(context: Context?) {
                if (!AudienceNetworkAds.isInitialized(context)) {
                    if (DEBUG) {
                        AdSettings.turnOnSDKDebugger(context)
                    }
                    AudienceNetworkAds
                        .buildInitSettings(context)
                        .withInitListener(AudienceNetworkInitializeHelper())
                        .initialize()
                }
            }
        }
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

                pecPreview.setImageBitmap(fragment.getPreview())

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

}