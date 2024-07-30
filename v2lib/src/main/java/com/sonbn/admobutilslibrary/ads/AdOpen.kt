package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.util.Log
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

object AdOpen {
    private const val TAG = "AOAManager"
    private var TIME_OUT = 20 * 1000L //20s
    private val exception = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.toString())
    }
    private var isTimeOutCall = false
    private var timeoutJob = TIME_OUT
    private var aoaListener: AOAListener? = null

    /**
    * @param time in millisecond*/
    fun setTimeOut(time: Long) {
        this.timeoutJob = time
    }

    interface AOAListener {
        fun onFetchAd()
        fun onAdLoaded(adOpenAd: AppOpenAd)
        fun onAdFailedToLoad(loadAdError: LoadAdError)
        fun onShowAdComplete()
        fun onShowAdError(adError: AdError)
        fun onTimeout()
    }

    interface OnAoaShowCompleteListener {
        fun onAoaShowComplete()
    }

    fun setAOAListener(aoaListener: AOAListener) {
        if (this.aoaListener != null) return
        this.aoaListener = aoaListener
    }

    fun showAdIfAvailable(
        mActivity: Activity,
        id: String,
        onAoaShowCompleteListener: OnAoaShowCompleteListener
    ) {
        if (!AdmobUtils.isShowAds) {
            onAoaShowCompleteListener.onAoaShowComplete()
            return
        }
        /**/
        aoaListener?.onFetchAd()
        isTimeOutCall = false
        val job = CoroutineScope(Dispatchers.Main + exception).launch {
            delay(timeoutJob)
            isTimeOutCall = true
            aoaListener?.onTimeout()
            onAoaShowCompleteListener.onAoaShowComplete()
        }
        val fullScreenContentCallback: FullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAoaShowCompleteListener.onAoaShowComplete()
                    Log.d(TAG, "onAdDismissedFullScreenContent")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    onAoaShowCompleteListener.onAoaShowComplete()
                    job.cancel()
                    aoaListener?.onShowAdError(adError)
                    Log.d(TAG, "onAdFailedToShowFullScreenContent: ${adError.message}")
                }

                override fun onAdShowedFullScreenContent() {
                    job.cancel()
                    aoaListener?.onShowAdComplete()
                    Log.d(TAG, "onAdShowedFullScreenContent")
                }
            }

        val loadCallback = object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                ad.fullScreenContentCallback = fullScreenContentCallback
                if (!isTimeOutCall && AdmobUtils.isForeground) {
                    ad.show(mActivity)
                } else {
                    onAoaShowCompleteListener.onAoaShowComplete()
                }
                aoaListener?.onAdLoaded(ad)
                Log.d(TAG, "onAdLoaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                onAoaShowCompleteListener.onAoaShowComplete()
                aoaListener?.onAdFailedToLoad(loadAdError)
                job.cancel()
                Log.d(TAG, "onAdFailedToLoad: ${loadAdError.message}")
            }
        }
        val idAd = if (AdmobUtils.isDebug) AdmobUtils.APP_OPEN else id
        AppOpenAd.load(mActivity, idAd, adRequest, loadCallback)
    }

    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

}