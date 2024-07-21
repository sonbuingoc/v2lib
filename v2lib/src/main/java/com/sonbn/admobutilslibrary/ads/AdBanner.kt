package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.os.Bundle
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
import com.google.android.gms.ads.LoadAdError
import com.sonbn.admobutilslibrary.databinding.ShimmerBannerBinding
import com.sonbn.admobutilslibrary.utils.gone


object AdBanner {
    private var mAdBannerListener: AdBannerListener? = null

    interface AdBannerListener {
        fun onFetchAd()
        fun onAdLoaded(adView: AdView)
        fun onAdFailedToLoad(loadAdError: LoadAdError)
    }

    fun setAdBannerListener(adBannerListener: AdBannerListener) {
        if (this.mAdBannerListener != null) return
        this.mAdBannerListener = adBannerListener
    }

    fun showBanner(
        mActivity: Activity,
        id: String,
        frameLayout: FrameLayout,
        line: View,
        adBannerListener: AdBannerListener? = null
    ) {
        if (!AdmobUtils.isShowAds) {
            line.visibility = View.GONE
            frameLayout.visibility = View.GONE
            return
        }
        mAdBannerListener?.onFetchAd()

        val idBanner = if (AdmobUtils.isDebug) AdmobUtils.BANNER else id
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
                adBannerListener?.onAdLoaded(mAdView)
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                frameLayout.gone()
                adBannerListener?.onAdFailedToLoad(p0)
            }
        }
    }

    fun showBannerCollapsible(
        mActivity: Activity,
        id: String,
        frameLayout: FrameLayout,
        line: View,
        collapsibleType: BannerCollapsibleType = BannerCollapsibleType.TOP,
        adBannerListener: AdBannerListener? = null
    ) {
        if (!AdmobUtils.isShowAds) {
            line.visibility = View.GONE
            frameLayout.visibility = View.GONE
            return
        }
        val idBanner = if (AdmobUtils.isDebug) AdmobUtils.BANNER else id
        val mAdView = AdView(mActivity)
        mAdView.setAdSize(getAdSize(mActivity))
        mAdView.adUnitId = idBanner
        val extras = Bundle()
        extras.putString("collapsible", collapsibleType.value)
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
                adBannerListener?.onAdLoaded(mAdView)
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                frameLayout.gone()
                adBannerListener?.onAdFailedToLoad(p0)
            }
        }
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

    enum class BannerCollapsibleType(val value: String) {
        TOP("top"),
        BOTTOM("bottom")
    }
}