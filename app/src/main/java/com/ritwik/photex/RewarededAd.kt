package com.ritwik.photex

import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import com.unity3d.ads.IUnityAdsListener
import com.unity3d.ads.UnityAds

class RewarededAd(private val activity:Activity,val function:()->Unit) : IUnityAdsListener {
    private lateinit var loadingPopup: LoadingPopup
    val adUnitId = "watermark_rewarded"
    fun initialiseAd()
    {

        UnityAds.addListener(this)
            Log.d(TAG, "initialiseAd: initialising")
        if(!UnityAds.isInitialized()) {
            UnityAds.initialize(activity, "4218265", false)
        }


        loadingPopup = LoadingPopup()
        //loadingPopup.showLoading()
    }
     fun displayAd()
    {
        if (UnityAds.isReady (adUnitId)) {
            if(this::loadingPopup.isInitialized)
            {
                loadingPopup.hideLoading()
            }
            UnityAds.show(activity,adUnitId)
        }
    }
    override fun onUnityAdsReady(p0: String?) {
        Log.d(TAG, "onUnityAdsReady: ")
        if(this::loadingPopup.isInitialized)
        {
            loadingPopup.hideLoading()
        }
        displayAd()
    }

    override fun onUnityAdsStart(p0: String?) {
        Log.d(TAG, "onUnityAdsStart: ad started")
        if(this::loadingPopup.isInitialized)
        {
            loadingPopup.hideLoading()

        }

    }

    override fun onUnityAdsFinish(p0: String?, finishState: UnityAds.FinishState?) {
        if(this::loadingPopup.isInitialized)
        {
            loadingPopup.hideLoading()
        }
        finishState?.let {state->
            if (state == UnityAds.FinishState.COMPLETED)
                {
                    Log.d(TAG, "onUnityAdsFinish: completed")
                    function()
                }
            else if (state == UnityAds.FinishState.SKIPPED) {
                Log.d(TAG, "onUnityAdsFinish: Skipped")
            }
            else if (state == UnityAds.FinishState.ERROR) {
                Log.d(TAG, "onUnityAdsFinish: error")
            }
            else
            {
                Log.d(TAG, "onUnityAdsFinish")
            }
        }
    }


override fun onUnityAdsError(p0: UnityAds.UnityAdsError?, p1: String?) {
    if(this::loadingPopup.isInitialized)
    {
        loadingPopup.hideLoading()
    }
    Log.d(TAG, "onUnityAdsError: error ${p1?.toString()}" )
}
companion object
{
    private const val TAG = "RewarededAd"
}
    inner class LoadingPopup()
    {
        private lateinit var window:PopupWindow
        fun showLoading()
        {
            val view = activity.layoutInflater.inflate(R.layout.popup_loading,null)
            val dm = activity.resources.displayMetrics
            window = PopupWindow(view,(dm.widthPixels*0.9).toInt(),WindowManager.LayoutParams.WRAP_CONTENT,false)
            window.elevation = 100f
            window.showAtLocation(view,Gravity.CENTER,0,0)
        }
        fun hideLoading()
        {
            if(this::window.isInitialized)
            {
               if(window.isShowing)
               {
                   window.dismiss()
               }
            }
        }
    }
}