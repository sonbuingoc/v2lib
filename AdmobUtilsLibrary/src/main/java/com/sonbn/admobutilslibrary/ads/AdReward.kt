package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.sonbn.admobutilslibrary.dialog.DialogLoadingAd
import kotlin.random.Random


class AdReward {
    private val map = mutableMapOf<String, RewardedAd?>()
    private var startTimeShow = 0L
    private var frequencyCapping = 0
    private var interstitialPercent = 100

    companion object {
        var showedFullScreen = false
        private var instance: AdReward? = null
        private const val TAG = "AdReward"
        fun getInstance(): AdReward {
            if (instance == null) instance = AdReward()
            return instance!!
        }
    }

    interface AdRewardCallback {
        fun onCallback()
        fun onLoaded(ad: RewardedAd)
        fun rewardItem(rewardAmount: Int, rewardType: String)
    }

    fun setFrequencyCapping(value: Int) {
        frequencyCapping = value
    }

    fun setPercentShowReward(value: Int) {
        interstitialPercent = value
    }

    fun loadReward(activity: Activity, id: String) {
        var rewardId = id
        if (AdmobUtils.isDebug) {
            rewardId = AdmobUtils.REWARD
        }
        val adRequest: AdRequest = AdRequest.Builder().build()
        RewardedAd.load(activity, rewardId,
            adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error.
                    Log.d(TAG, loadAdError.toString())
                    map[id] = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    if (map[id] == null) {
                        map[id] = ad
                    }
                    Log.d(TAG, "Ad was loaded.")
                }
            })
    }

    fun showReward(
        mActivity: Activity,
        id: String,
        showDialogLoading: Boolean = true,
        callback: AdRewardCallback
    ) {
        if (!AdmobUtils.isShowAds) {
            callback.onCallback()
            return
        }
        if (map[id] == null) {
            callback.onCallback()
            loadReward(mActivity, id)
            return
        }
        callback.onLoaded(map[id]!!)
        val dialogLoadingAd = DialogLoadingAd()
        val fragmentActivity = mActivity as FragmentActivity

        map[id]!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                map[id] = null
                callback.onCallback()
                showedFullScreen = false
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                map[id] = null
                callback.onCallback()
                showedFullScreen = false
                dismissDialog(dialogLoadingAd)
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
                showedFullScreen = true
                dismissDialog(dialogLoadingAd)
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
                .postDelayed({
                    map[id]?.show(mActivity) { rewardItem -> // Handle the reward.
                        Log.d(TAG, "The user earned the reward.")
                        val rewardAmount = rewardItem.amount
                        val rewardType = rewardItem.type
                        callback.rewardItem(rewardAmount, rewardType)
                    }
                }, 2000)
        } else {
            map[id]?.show(mActivity) { rewardItem -> // Handle the reward.
                Log.d(TAG, "The user earned the reward.")
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                callback.rewardItem(rewardAmount, rewardType)
            }
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
}