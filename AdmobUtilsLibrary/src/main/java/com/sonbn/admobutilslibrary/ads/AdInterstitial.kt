package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


object AdInterstitial {
    private const val TAG = "AdInterstitial"
    private val map = mutableMapOf<String, InterstitialAd?>()
    var isShowedFullScreen = false
    private var startTimeShow = 0L
    private var frequencyCapping = 0
    private var interstitialPercent = 100
    private var isLoading = false
    fun setFrequencyCapping(value: Int) {
        this.frequencyCapping = value
    }

    fun setInterstitialPercent(value: Int) {
        this.interstitialPercent = value
    }

    interface InterLoadCallback {
        fun onAdLoaded(interstitialAd: InterstitialAd)
        fun onAdFailedToLoad(loadAdError: LoadAdError)
    }
    interface InterShowCallback {
        fun onAdShowedFullScreenContent()
        fun onAdFailedToShowFullScreenContent(adError: AdError)
    }
    fun loadInterstitial(
        activity: Activity,
        id: String,
        loadCallback: InterLoadCallback? = null
    ) {
        if (!AdmobUtils.isShowAds || map[id] != null || isLoading) {
            return
        }
        isLoading = true
        val idInter: String = if (AdmobUtils.isDebug) AdmobUtils.INTERSTITIAL else id
        val adRequest: AdRequest = AdRequest.Builder().build()
        val interstitialAdLoadCallback = object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                map[id] = interstitialAd
                isLoading = false
                Log.i(TAG, "onAdLoaded")
                loadCallback?.onAdLoaded(interstitialAd)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error
                isLoading = false
                Log.i(TAG, "onAdFailedToLoad: $loadAdError")
                loadCallback?.onAdFailedToLoad(loadAdError)
            }

        }
        InterstitialAd.load(activity, idInter, adRequest, interstitialAdLoadCallback)
    }

    fun showInterstitial(
        mActivity: Activity,
        id: String,
        showCallback: InterShowCallback? = null
    ) {
        if (!AdmobUtils.isShowAds) {
            return
        }
        if (map[id] == null) {
            loadInterstitial(mActivity, id)
            return
        }

        map[id]?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                isShowedFullScreen = false
                map[id] = null
                loadInterstitial(mActivity, id)
                startTimeShow = SystemClock.elapsedRealtime()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                isShowedFullScreen = false
                map[id] = null
                showCallback?.onAdFailedToShowFullScreenContent(p0)
                loadInterstitial(mActivity, id)
                startTimeShow = SystemClock.elapsedRealtime()
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                isShowedFullScreen = true
                map[id] = null
                showCallback?.onAdShowedFullScreenContent()
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }

        if (SystemClock.elapsedRealtime() - startTimeShow < frequencyCapping * 1000L) {
            return
        }

        map[id]?.show(mActivity)
    }
}
