package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G
import com.google.android.gms.ads.nativead.NativeAd

object AdmobUtils {
    var isDebug = true
    var isShowAds = false

    const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    const val BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val REWARD = "ca-app-pub-3940256099942544/5224354917"
    const val NATIVE = "ca-app-pub-3940256099942544/2247696110"

    private var aoaManager = AOAManager.getInstance()
    private var resumeManager = ResumeManager.getInstance()
    private var adInterstitial = AdInterstitial.getInstance()
    private var adBanner = AdBanner.getInstance()
    private var adNative = AdNative.getInstance()
    private var adReward = AdReward.getInstance()
    fun initMobileAds(context: Context, isShowAds: Boolean, isDebug: Boolean) {
        AdmobUtils.isDebug = isDebug
        AdmobUtils.isShowAds = isShowAds

        val conf = RequestConfiguration.Builder()
            .setMaxAdContentRating(MAX_AD_CONTENT_RATING_G)
            .build()

        MobileAds.setRequestConfiguration(conf)
        MobileAds.initialize(context) { }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!
            .isConnected
    }

    fun initAoa(
        mActivity: Activity, id: String,
        showDialogLoading: Boolean = true,
        timeOut: Long,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        aoaManager.showAdIfAvailable(
            mActivity,
            id,
            showDialogLoading,
            timeOut,
            onShowAdCompleteListener
        )
    }

    fun initResume(
        application: Application,
        id: String,
        showDialogLoading: Boolean = true,
        onShowAdCompleteListener: OnShowAdCompleteListener? = null
    ) {
        resumeManager.init(application, id, showDialogLoading, onShowAdCompleteListener)
    }

    fun insertActivityDisableResumeAd(activity: Class<*>) {
        resumeManager.insertActivityDisableAd(activity)
    }

    fun removeActivityDisableResumeAd(activity: Class<*>) {
        resumeManager.removeActivityDisableAd(activity)
    }

    fun showBanner(
        mActivity: Activity,
        id: String,
        frameLayout: FrameLayout,
        line: View,
        timeOut: Long? = null, /*time millis*/
        mAdBannerCallback: AdBanner.AdBannerCallback? = null
    ) {
        adBanner.showBanner(mActivity, id, frameLayout, line, timeOut, mAdBannerCallback)
    }

    fun showBannerCollapsible(
        mActivity: Activity,
        id: String,
        frameLayout: FrameLayout,
        line: View,
        timeOut: Long? = null, /*time millis*/
        mAdBannerCallback: AdBanner.AdBannerCallback? = null
    ) {
        adBanner.showBannerCollapsible(mActivity, id, frameLayout, line, timeOut, mAdBannerCallback)
    }

    fun loadInter(mContext: Context, id: String) {
        adInterstitial.loadInterstitial(mContext, id)
    }

    fun setCappingInter(value: Int) {
        adInterstitial.setFrequencyCapping(value)
    }

    fun setPercentShowInter(value: Int) {
        adInterstitial.setInterstitialPercent(value)
    }

    fun showInter(
        mActivity: Activity,
        id: String,
        showDialogLoading: Boolean = true,
        callback: AdInterstitial.AdInterstitialCallback
    ) {
        adInterstitial.showInterstitial(mActivity, id, showDialogLoading, callback)
    }

    fun loadReward(
        activity: Activity, id: String
    ) {
        adReward.loadReward(activity, id)
    }

    fun showReward(
        mActivity: Activity,
        id: String,
        showDialogLoading: Boolean = true,
        callback: AdReward.AdRewardCallback
    ) {
        adReward.showReward(mActivity, id, showDialogLoading, callback)
    }

    fun setCappingReward(value: Int) {
        adReward.setFrequencyCapping(value)
    }

    fun setPercentShowReward(value: Int) {
        adReward.setPercentShowReward(value)
    }

    fun loadNative(activity: Activity, id: String) {
        adNative.loadNative(activity, id)
    }

    fun showNative(
        activity: Activity,
        id: String,
        nativeAd: NativeAd? = null,
        parent: ViewGroup,
        idLayout: Int
    ) {
        adNative.showNative(activity, id, nativeAd, parent, idLayout)
    }

    fun loadAndShowNativeSmall(
        activity: Activity,
        id: String,
        viewGroup: ViewGroup,
        idLayout: Int? = null,
        timeOut: Long? = null, /*time millis*/
        adNativeCallback: AdNative.AdNativeCallback? = null
    ) {
        adNative.loadAndShowNativeSmall(
            activity,
            id,
            viewGroup,
            idLayout,
            timeOut,
            adNativeCallback
        )
    }

    fun loadAndShowNativeMedium(
        activity: Activity,
        id: String,
        viewGroup: ViewGroup,
        idLayout: Int? = null,
        timeOut: Long? = null, /*time millis*/
        adNativeCallback: AdNative.AdNativeCallback? = null
    ) {
        adNative.loadAndShowNativeMedium(
            activity,
            id,
            viewGroup,
            idLayout,
            timeOut,
            adNativeCallback
        )
    }
}