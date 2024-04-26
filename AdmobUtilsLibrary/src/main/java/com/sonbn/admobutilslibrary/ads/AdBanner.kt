package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.sonbn.admobutilslibrary.databinding.ShimmerBannerBinding
import com.sonbn.admobutilslibrary.gone
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object AdBanner {
    private const val TIME_OUT = 60 * 1000L
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    interface Callback {
        fun onAdLoaded(adView: AdView?)
    }

    fun showBanner(
        mActivity: Activity,
        id: String,
        frameLayout: FrameLayout,
        line: View,
        mCallback: Callback? = null
    ) {
        var isLoaded = false
        if (!AdmobUtils.isNetworkAvailable(mActivity) || !AdmobUtils.isShowAds) {
            line.gone()
            frameLayout.gone()
            return
        }
        var idBanner = id
        if (AdmobUtils.isDebug) {
            idBanner = AdmobUtils.BANNER
        }
        val mAdView = AdView(mActivity)
        mAdView.setAdSize(getAdSize(mActivity))
        mAdView.adUnitId = idBanner
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        val layoutShimmer = ShimmerBannerBinding.inflate(LayoutInflater.from(mActivity)).shimmer
        layoutShimmer.startShimmer()

        frameLayout.addView(layoutShimmer)
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                layoutShimmer.stopShimmer()
                frameLayout.removeAllViews()
                frameLayout.addView(mAdView)
                isLoaded = true
                mCallback?.onAdLoaded(mAdView)
            }
        }
//        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
//            delay(TIME_OUT)
//            if (!isLoaded) {
//                withContext(Dispatchers.Main + exceptionHandler) {
//                    frameLayout.gone()
//                    line.gone()
//                }
//            }
//        }
    }

    fun showBannerCollapsible(
        mActivity: Activity,
        id: String,
        frameLayout: FrameLayout,
        line: View
    ) {
        var isLoaded = false
        if (!AdmobUtils.isNetworkAvailable(mActivity) || !AdmobUtils.isShowAds) {
            line.gone()
            frameLayout.gone()
            return
        }
        var idBanner = id
        if (AdmobUtils.isDebug) {
            idBanner = AdmobUtils.BANNER
        }
        val mAdView = AdView(mActivity)
        mAdView.setAdSize(getAdSize(mActivity))
        mAdView.adUnitId = idBanner
        val extras = Bundle()
        extras.putString("collapsible", "top")
        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
        mAdView.loadAd(adRequest)
        val layoutShimmer: ShimmerFrameLayout =
            ShimmerBannerBinding.inflate(LayoutInflater.from(mActivity)).root
        layoutShimmer.startShimmer()
        frameLayout.addView(layoutShimmer)
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                layoutShimmer.stopShimmer()
                frameLayout.removeAllViews()
                frameLayout.addView(mAdView)
                isLoaded = true
            }
        }
//        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
//            delay(TIME_OUT)
//            if (!isLoaded) {
//                withContext(Dispatchers.Main + exceptionHandler) {
//                    frameLayout.gone()
//                    line.gone()
//                }
//            }
//        }
    }

    private fun getAdSize(mActivity: Activity): AdSize {
        val display = mActivity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth)
    }
}