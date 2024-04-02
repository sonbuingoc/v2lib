package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.sonbn.admobutilslibrary.R
import com.sonbn.admobutilslibrary.databinding.ShimmerNativeMediumBinding
import com.sonbn.admobutilslibrary.databinding.ShimmerNativeSmallBinding


private const val TAG = "AdNative"

object AdNative {
    private val map = mutableMapOf<String, NativeAd>()
    interface Callback{
        fun onAdLoaded(nativeAd: NativeAd?)
    }
    fun loadNative(activity: Activity, id: String) {
        var nativeId = id
        if (AdmobUtils.isDebug) {
            nativeId = AdmobUtils.NATIVE
        }
        if (!AdmobUtils.isShowAds || !AdmobUtils.isNetworkAvailable(activity)) {
            return
        }
        val builder =
            AdLoader.Builder(activity, nativeId).forNativeAd { p0 -> map[id] = p0 }.build()
        builder.loadAd(AdRequest.Builder().build())
    }

    fun loadAndShowNativeSmall(
        activity: Activity,
        id: String,
        viewGroup: ViewGroup,
        idLayout: Int? = null,
        callback: Callback? = null
    ) {
        var nativeId = id
        if (AdmobUtils.isDebug) {
            nativeId = AdmobUtils.NATIVE
        }
        if (!AdmobUtils.isShowAds || !AdmobUtils.isNetworkAvailable(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        val layoutShimmer = ShimmerNativeSmallBinding.inflate(LayoutInflater.from(activity)).root
        layoutShimmer.startShimmer()
        viewGroup.removeAllViews()
        viewGroup.addView(layoutShimmer)
        var layout = idLayout
        if (layout == null) layout = R.layout.gnt_small_template_view
        val builder = AdLoader.Builder(activity, nativeId).forNativeAd { p0 ->
            layoutShimmer.stopShimmer()
            showNative(activity, id, p0, viewGroup, layout)
            callback?.onAdLoaded(p0)
        }.build()
        builder.loadAd(AdRequest.Builder().build())
    }

    fun loadAndShowNativeMedium(
        activity: Activity,
        id: String,
        viewGroup: ViewGroup,
        idLayout: Int? = null,
        callback: Callback?
    ) {
        var nativeId = id
        if (AdmobUtils.isDebug) {
            nativeId = AdmobUtils.NATIVE
        }
        if (!AdmobUtils.isShowAds || !AdmobUtils.isNetworkAvailable(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        val layoutShimmer = ShimmerNativeMediumBinding.inflate(LayoutInflater.from(activity)).root
        layoutShimmer.startShimmer()
        viewGroup.removeAllViews()
        viewGroup.addView(layoutShimmer)
        var layout = idLayout
        if (layout == null) layout = R.layout.gnt_medium_template_view
        val builder = AdLoader.Builder(activity, nativeId).forNativeAd { p0 ->
            layoutShimmer.stopShimmer()
            showNative(activity, id, p0, viewGroup, layout)
            callback?.onAdLoaded(p0)
        }.build()
        builder.loadAd(AdRequest.Builder().build())
    }

    private fun showNative(
        activity: Activity,
        id: String,
        nativeAd: NativeAd? = null,
        parent: ViewGroup,
        idLayout: Int
    ) {
        if (!AdmobUtils.isShowAds && !AdmobUtils.isNetworkAvailable(activity)) {
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