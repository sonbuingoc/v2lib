package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.sonbn.admobutilslibrary.dialog.DialogLoadingAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlin.random.Random


class AdInterstitial {
    private val map = mutableMapOf<String, InterstitialAd?>()
    private var startTimeShow = 0L
    private var frequencyCapping = 0
    private var interstitialPercent = 100

    companion object {
        var showedFullScreen = false
        private const val TAG = "AdInterstitial"
        private var instance: AdInterstitial? = null
        fun getInstance(): AdInterstitial {
            if (instance == null) instance = AdInterstitial()
            return instance!!
        }
    }

    fun setFrequencyCapping(value: Int) {
        frequencyCapping = value
    }

    fun setInterstitialPercent(value: Int) {
        interstitialPercent = value
    }

    fun loadInterstitial(mContext: Context, id: String) {
        if (!AdmobUtils.isShowAds) {
            return
        }
        var idInter = id
        if (AdmobUtils.isDebug) {
            idInter = AdmobUtils.INTERSTITIAL
        }
        val adRequest: AdRequest = AdRequest.Builder().build()
        InterstitialAd.load(mContext, idInter, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    if (map[id] == null) {
                        map[id] = interstitialAd
                    }
                    Log.i(TAG, "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.d(TAG, loadAdError.toString())
                    map[id] = null
                    Log.i(TAG, "onAdFailedToLoad")
                }
            })
    }

    fun showInterstitial(
        mActivity: Activity,
        id: String,
        showDialogLoading: Boolean = true,
        callback: AdInterstitialCallback
    ) {
        if (!AdmobUtils.isShowAds) {
            callback.onCallback()
            return
        }
        if (map[id] == null) {
            callback.onCallback()
            loadInterstitial(mActivity, id)
            return
        }
        callback.onAdLoaded(map[id])
        val dialogLoadingAd = DialogLoadingAd()
        val fragmentActivity = mActivity as FragmentActivity

        map[id]?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                showedFullScreen = false
                map[id] = null
                callback.onCallback()
                loadInterstitial(mActivity, id)
                startTimeShow = SystemClock.elapsedRealtime()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                showedFullScreen = false
                map[id] = null
                callback.onCallback()
                loadInterstitial(mActivity, id)
                dismissDialog(dialogLoadingAd)
                startTimeShow = SystemClock.elapsedRealtime()
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                showedFullScreen = true
                dismissDialog(dialogLoadingAd)
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }

        val percent = Random.nextInt(100)
        if (percent < interstitialPercent || SystemClock.elapsedRealtime() - startTimeShow < frequencyCapping * 1000L) {
            callback.onCallback()
            return
        }

        if (showDialogLoading) {
            dialogLoadingAd.show(
                fragmentActivity.supportFragmentManager,
                DialogLoadingAd::class.simpleName
            )
            android.os.Handler(Looper.getMainLooper())
                .postDelayed({ map[id]?.show(mActivity) }, 2000)
        } else {
            map[id]?.show(mActivity)
        }

    }

    private fun dismissDialog(dialog: DialogFragment) {
        try {
            if (dialog.isAdded) {
                dialog.dismiss()
            }
        } catch (e: Throwable) {
            Log.e(TAG, e.message.toString())
        }
    }

    interface AdInterstitialCallback {
        fun onCallback()
        fun onAdLoaded(interstitialAd: InterstitialAd?)
    }

}
