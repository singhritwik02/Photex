package com.ritwik.photex

import android.util.Log
import com.unity3d.ads.IUnityAdsListener
import com.unity3d.ads.UnityAds.FinishState
import com.unity3d.ads.UnityAds.UnityAdsError


 class UnityAdsListener : IUnityAdsListener {
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