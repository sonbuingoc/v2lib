package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.sonbn.admobutilslibrary.R
import com.sonbn.admobutilslibrary.utils.gone


object AdNative {
    private val map = mutableMapOf<String, NativeAd>()
    private var onAdNativeListener: OnAdNativeListener? = null
    interface OnAdNativeListener {
        fun onFetchAd()
        fun onAdLoaded(nativeAd: NativeAd)
        fun onAdFailedToLoad(adError: LoadAdError)

    }
    interface NativeListener {
        fun onAdLoaded(nativeAd: NativeAd)
        fun onAdFailedToLoad(adError: LoadAdError)
    }

    fun setOnAdNativeListener(onAdNativeListener: OnAdNativeListener) {
        if (this.onAdNativeListener != null) return
        this.onAdNativeListener = onAdNativeListener
    }

    fun loadNative(activity: Activity, id: String) {
        var nativeId = id
        if (AdmobUtils.isDebug) {
            nativeId = AdmobUtils.NATIVE
        }
        if (!AdmobUtils.isShowAds) {
            return
        }
        val builder =
            AdLoader.Builder(activity, nativeId).forNativeAd { p0 -> map[id] = p0 }.build()
        builder.loadAd(AdRequest.Builder().build())
    }

    fun loadAndShow2NativeSmall(
        activity: Activity,
        id: String,
        viewGroup1: ViewGroup,
        viewGroup2: ViewGroup,
        idShimmer1: Int = R.layout.shimmer_native_small,
        idShimmer2: Int = R.layout.shimmer_native_small,
        idLayout1: Int = R.layout.gnt_small_template_view,
        idLayout2: Int = R.layout.gnt_small_template_view,
        nativeListener: NativeListener? = null
    ) {
        if (!AdmobUtils.isShowAds) {
            viewGroup1.visibility = View.GONE
            viewGroup2.visibility = View.GONE
            return
        }
        onAdNativeListener?.onFetchAd()
        val nativeId = if (AdmobUtils.isDebug) AdmobUtils.NATIVE else id
        val layoutShimmer1 = activity.layoutInflater.inflate(idShimmer1, null, false) as ShimmerFrameLayout
        val layoutShimmer2 = activity.layoutInflater.inflate(idShimmer2, null, false) as ShimmerFrameLayout

        layoutShimmer1.startShimmer()
        viewGroup1.removeAllViews()
        viewGroup1.addView(layoutShimmer1)

        layoutShimmer2.startShimmer()
        viewGroup2.removeAllViews()
        viewGroup2.addView(layoutShimmer2)

        var nativeAd1: NativeAd? = null
        var nativeAd2: NativeAd? = null
        lateinit var adLoader: AdLoader
        adLoader = AdLoader.Builder(activity, nativeId)
            .forNativeAd { p0 ->
                if (nativeAd1 == null) {
                    nativeAd1 = p0
                    showNative(activity, id, p0, viewGroup1, idLayout1)
                } else {
                    if (nativeAd2 == null) {
                        nativeAd2 = p0
                        showNative(activity, id, p0, viewGroup2, idLayout2)
                    }
                }

                if (!adLoader.isLoading) {
                    if (nativeAd2 == null) {
                        viewGroup2.gone()
                    }
                    nativeListener?.onAdLoaded(p0)
                    onAdNativeListener?.onAdLoaded(p0)
                }

            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    if (nativeAd1 == null) viewGroup1.gone()
                    if (nativeAd2 == null) viewGroup2.gone()
                    nativeListener?.onAdFailedToLoad(adError)
                    onAdNativeListener?.onAdFailedToLoad(adError)
                }
            })
            .build()
        adLoader.loadAds(AdRequest.Builder().build(), 2)
    }

    fun loadAndShowNativeSmall(
        activity: Activity,
        id: String,
        viewGroup: ViewGroup,
        idShimmer: Int = R.layout.shimmer_native_small,
        idLayout: Int = R.layout.gnt_small_template_view,
        nativeListener: NativeListener? = null
    ) {
        if (!AdmobUtils.isShowAds) {
            viewGroup.visibility = View.GONE
            return
        }
        onAdNativeListener?.onFetchAd()
        val nativeId = if (AdmobUtils.isDebug) AdmobUtils.NATIVE else id
        val layoutShimmer: ShimmerFrameLayout = activity.layoutInflater.inflate(idShimmer, null, false) as ShimmerFrameLayout
        layoutShimmer.startShimmer()
        viewGroup.removeAllViews()
        viewGroup.addView(layoutShimmer)
        val builder = AdLoader.Builder(activity, nativeId).forNativeAd { p0 ->
            layoutShimmer.stopShimmer()
            nativeListener?.onAdLoaded(p0)
            showNative(activity, id, p0, viewGroup, idLayout)
            onAdNativeListener?.onAdLoaded(p0)
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                viewGroup.gone()
                nativeListener?.onAdFailedToLoad(adError)
                onAdNativeListener?.onAdFailedToLoad(adError)
            }
        }).build()
        builder.loadAd(AdRequest.Builder().build())
    }

    fun loadAndShowNativeMedium(
        activity: Activity,
        id: String,
        viewGroup: ViewGroup,
        idShimmer: Int = R.layout.shimmer_native_medium,
        idLayout: Int = R.layout.gnt_medium_template_view,
        nativeListener: NativeListener? = null
    ) {
        if (!AdmobUtils.isShowAds) {
            viewGroup.visibility = View.GONE
            return
        }
        val nativeId = if (AdmobUtils.isDebug) AdmobUtils.NATIVE else id
        val layoutShimmer: ShimmerFrameLayout = activity.layoutInflater.inflate(idShimmer, null, false) as ShimmerFrameLayout
        layoutShimmer.startShimmer()
        viewGroup.removeAllViews()
        viewGroup.addView(layoutShimmer)
        val builder = AdLoader.Builder(activity, nativeId).forNativeAd { p0 ->
            layoutShimmer.stopShimmer()
            nativeListener?.onAdLoaded(p0)
            showNative(activity, id, p0, viewGroup, idLayout)
            onAdNativeListener?.onAdLoaded(p0)
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                viewGroup.gone()
                nativeListener?.onAdFailedToLoad(adError)
                onAdNativeListener?.onAdFailedToLoad(adError)
            }
        }).build()
        builder.loadAd(AdRequest.Builder().build())
    }

    private fun showNative(
        activity: Activity,
        id: String,
        nativeAd: NativeAd? = null,
        parent: ViewGroup,
        idLayout: Int
    ) {
        if (!AdmobUtils.isShowAds) {
            return
        }
        val layoutNative = activity.layoutInflater.inflate(idLayout, null, false) as NativeAdView
        parent.removeAllViews()
        if (nativeAd != null) {
            parent.addView(setNative(nativeAd, layoutNative))
        } else {
            map[id]?.let {
                parent.addView(setNative(it, layoutNative))
            }
        }
    }

    private fun setNative(nativeAd: NativeAd, layoutNative: NativeAdView): NativeAdView {
        val nativeAdView: NativeAdView? = layoutNative.findViewById(R.id.native_ad_view)
        val primaryView: TextView? = layoutNative.findViewById(R.id.primary)
        val secondaryView: TextView? = layoutNative.findViewById(R.id.secondary)
        val ratingBar: RatingBar? = layoutNative.findViewById(R.id.rating_bar)
        val tertiaryView: TextView? = layoutNative.findViewById(R.id.body)
        val iconView: ImageView? = layoutNative.findViewById(R.id.icon)
        val mediaView: MediaView? = layoutNative.findViewById(R.id.media_view)
        val callToActionView: Button? = layoutNative.findViewById(R.id.cta)
        val background: ConstraintLayout? = layoutNative.findViewById(R.id.background)

        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        val headline = nativeAd.headline
        val body = nativeAd.body
        val cta = nativeAd.callToAction
        val starRating = nativeAd.starRating
        val icon = nativeAd.icon
        val secondaryText: String?

        nativeAdView!!.callToActionView = callToActionView
        nativeAdView.headlineView = primaryView
        nativeAdView.mediaView = mediaView
        secondaryView!!.visibility = View.VISIBLE
        if (adHasOnlyStore(nativeAd)) {
            nativeAdView.storeView = secondaryView
            secondaryText = store
        } else if (!TextUtils.isEmpty(advertiser)) {
            nativeAdView.advertiserView = secondaryView
            secondaryText = advertiser
        } else {
            secondaryText = ""
        }

        primaryView!!.text = headline
        callToActionView!!.text = cta

        //  Set the secondary view to be the star rating if available.
        if (starRating != null && starRating > 0) {
            secondaryView.visibility = View.GONE
            ratingBar!!.visibility = View.VISIBLE
            ratingBar.rating = starRating.toFloat()
            nativeAdView.starRatingView = ratingBar
        } else {
            secondaryView.text = secondaryText
            secondaryView.visibility = View.VISIBLE
            ratingBar!!.visibility = View.GONE
        }

        if (icon != null) {
            iconView!!.visibility = View.VISIBLE
            iconView.setImageDrawable(icon.drawable)
        } else {
            iconView!!.visibility = View.GONE
        }

        if (tertiaryView != null) {
            tertiaryView.text = body
            nativeAdView.bodyView = tertiaryView
        }

        nativeAdView.setNativeAd(nativeAd)
        return nativeAdView
    }

    private fun adHasOnlyStore(nativeAd: NativeAd): Boolean {
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser)
    }
}