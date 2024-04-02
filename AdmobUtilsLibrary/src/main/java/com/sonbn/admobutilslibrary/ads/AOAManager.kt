package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.os.Looper
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.sonbn.admobutilslibrary.dialog.DialogLoadingAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "AOAManager"

object AppOpenManager {
    private var appOpenAd: AppOpenAd? = null
    private var TIME_OUT = 10 * 1000L //10s
    private val exception = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.toString())
    }

    fun showAdIfAvailable(
        mActivity: Activity,
        id: String,
        showDialogLoading: Boolean = true,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        if (!AdmobUtils.isShowAds) {
            onShowAdCompleteListener.onShowAdComplete(appOpenAd)
            return
        }
        val dialogLoadingAd = DialogLoadingAd()
        val fragmentActivity = mActivity as FragmentActivity

        /**/
        val job = CoroutineScope(Dispatchers.Main + exception).launch {
            delay(TIME_OUT)
            onShowAdCompleteListener.onShowAdComplete(appOpenAd)
        }
        val fullScreenContentCallback: FullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onShowAdCompleteListener.onShowAdComplete(appOpenAd)
                    Log.d(TAG, "onAdDismissedFullScreenContent")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    dismissDialog(dialogLoadingAd)
                    onShowAdCompleteListener.onShowAdComplete(appOpenAd)
                    job.cancel()
                    Log.d(TAG, "onAdFailedToShowFullScreenContent: ${adError.message}")
                }

                override fun onAdShowedFullScreenContent() {
                    dismissDialog(dialogLoadingAd)
                    job.cancel()
                    Log.d(TAG, "onAdShowedFullScreenContent")
                }
            }

        val loadCallback = object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                ad.fullScreenContentCallback = fullScreenContentCallback
                if (showDialogLoading) {
                    dialogLoadingAd.show(
                        fragmentActivity.supportFragmentManager,
                        DialogLoadingAd::class.simpleName
                    )
                    android.os.Handler(Looper.getMainLooper()).postDelayed({
                        ad.show(mActivity)
                    }, 3000)
                } else {
                    ad.show(mActivity)
                }
                Log.d(TAG, "onAdLoaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                onShowAdCompleteListener.onShowAdComplete(appOpenAd)
                job.cancel()
                Log.d(TAG, "onAdFailedToLoad: ${loadAdError.message}")
            }
        }
        var idAd = id
        if (AdmobUtils.isDebug) {
            idAd = AdmobUtils.APP_OPEN
        }
        if (AdInterstitial.isShowedFullScreen) {
            onShowAdCompleteListener.onShowAdComplete(appOpenAd)
        } else {
            AppOpenAd.load(mActivity, idAd, adRequest, loadCallback)
        }

    }

    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

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