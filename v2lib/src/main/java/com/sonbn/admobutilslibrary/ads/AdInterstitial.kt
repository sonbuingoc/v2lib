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
    private var onAdInterstitialListener: OnAdInterstitialListener? = null

    interface OnAdInterstitialListener {
        fun onFetchAd()
        fun onAdLoaded(interstitialAd: InterstitialAd)
        fun onAdFailedToLoad(loadAdError: LoadAdError)
        fun onAdShowedFullScreenContent()
        fun onAdFailedToShowFullScreenContent(adError: AdError)
    }


    fun setOnAdInterstitialListener(onAdInterstitialListener: OnAdInterstitialListener) {
        if (this.onAdInterstitialListener != null) return
        this.onAdInterstitialListener = onAdInterstitialListener
    }

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
        onAdInterstitialListener?.onFetchAd()
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
                onAdInterstitialListener?.onAdLoaded(interstitialAd)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error
                isLoading = false
                Log.i(TAG, "onAdFailedToLoad: $loadAdError")
                loadCallback?.onAdFailedToLoad(loadAdError)
                onAdInterstitialListener?.onAdFailedToLoad(loadAdError)
            }

        }
        InterstitialAd.load(activity, idInter, adRequest, interstitialAdLoadCallback)
    }

    fun showInterstitial(
        mActivity: Activity,
        id: String,
        showCallback: InterShowCallback? = null,
        isCappingDurationMet: (Boolean) -> Unit = {}
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
                onAdInterstitialListener?.onAdFailedToShowFullScreenContent(p0)
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
                onAdInterstitialListener?.onAdShowedFullScreenContent()
            }
        }

        if (SystemClock.elapsedRealtime() - startTimeShow < frequencyCapping * 1000L) {
            isCappingDurationMet(false)
            return
        }
        isCappingDurationMet(true)
        map[id]?.show(mActivity)
    }

    fun loadAndShowInterstitial(
        activity: Activity,
        id: String,
        onCompleteListener: () -> Unit = {}
    ) {
        if (!AdmobUtils.isShowAds) {
            onCompleteListener.invoke()
            return
        }
        val idInter: String = if (AdmobUtils.isDebug) AdmobUtils.INTERSTITIAL else id
        val adRequest: AdRequest = AdRequest.Builder().build()
        InterstitialAd.load(activity, idInter, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                super.onAdLoaded(p0)
                p0.apply {
                    fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            onCompleteListener()
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            onCompleteListener()
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                        }
                    }
                    show(activity)
                }
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                onCompleteListener()
            }
        })
    }
}
