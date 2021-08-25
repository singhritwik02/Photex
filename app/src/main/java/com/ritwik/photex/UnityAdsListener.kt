package com.ritwik.photex

import android.app.Activity
import android.content.Context
import android.util.Log
import com.unity3d.ads.IUnityAdsListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAds.FinishState
import com.unity3d.ads.UnityAds.UnityAdsError


 class UnityInterstital (val context: Context,val activity: Activity): IUnityAdsListener {
     fun initialiseAd()
     {
         Log.d(TAG, "initialiseAd: ")
         UnityAds.addListener(this)

         if(!UnityAds.isInitialized()) {
             UnityAds.initialize(context, "4218265", false)
         }
     }
     fun displayAd()
     {
         if(UnityAds.isReady("Interstitial_Android"))
         {
             UnityAds.show(activity,"Interstitial_Android",object:IUnityAdsShowListener
             {
                 override fun onUnityAdsShowFailure(
                     p0: String?,
                     p1: UnityAds.UnityAdsShowError?,
                     p2: String?
                 ) {
                     Log.d(TAG, "onUnityAdsShowFailure: ")
                 }

                 override fun onUnityAdsShowStart(p0: String?) {
                     Log.d(TAG, "onUnityAdsShowStart: ")
                 }

                 override fun onUnityAdsShowClick(p0: String?) {
                     Log.d(TAG, "onUnityAdsShowClick: ")
                 }

                 override fun onUnityAdsShowComplete(
                     p0: String?,
                     p1: UnityAds.UnityAdsShowCompletionState?
                 ) {
                     Log.d(TAG, "onUnityAdsShowComplete: ")

                 }

             })

         }
     }
    override fun onUnityAdsReady(surfacingId: String) {
        Log.d(TAG, "onUnityAdsReady: ")

    }

    override fun onUnityAdsStart(surfacingId: String) {
        Log.d(TAG, "onUnityAdsStart: ")
    }

    override fun onUnityAdsFinish(surfacingId: String, finishState: FinishState) {
        Log.d(TAG, "onUnityAdsFinish: ")
    }

    override fun onUnityAdsError(error: UnityAdsError, message: String) {
        Log.d(TAG, "onUnityAdsError: ")
    }
    companion object
    {
        private const val TAG = "UnityAdsListener"
    }
}