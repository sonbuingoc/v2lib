package com.sonbn.admobutilslibrary.ads

import android.content.Context
import android.net.ConnectivityManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G
import kotlinx.coroutines.CoroutineExceptionHandler


object AdmobUtils {
    var isDebug = true
    var isShowAds = false

    const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    const val BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val REWARD = "ca-app-pub-3940256099942544/5224354917"
    const val NATIVE = "ca-app-pub-3940256099942544/2247696110"

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
}