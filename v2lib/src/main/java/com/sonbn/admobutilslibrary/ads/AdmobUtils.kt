package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G
import com.google.android.gms.ads.nativead.NativeAd
import com.sonbn.admobutilslibrary.BuildConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdmobUtils {
    var isDebug = true
    var isShowAds = false
    var isForeground = false
    const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    const val BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val BANNER_COLLAPSIBLE = "ca-app-pub-3940256099942544/2014213617"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val REWARD = "ca-app-pub-3940256099942544/5224354917"
    const val NATIVE = "ca-app-pub-3940256099942544/2247696110"

    fun initMobileAds(context: Context, isShowAds: Boolean, isDebug: Boolean) {
        CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, throwable -> if (BuildConfig.DEBUG) throwable.printStackTrace() }).launch {
            AdmobUtils.isDebug = isDebug
            AdmobUtils.isShowAds = isShowAds
            val conf = RequestConfiguration.Builder()
                .setMaxAdContentRating(
                    MAX_AD_CONTENT_RATING_G
                )
                .build()

            MobileAds.setRequestConfiguration(conf)
            MobileAds.initialize(context) { }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                isForeground = true
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                isForeground = false
            }
        })
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!
            .isConnected
    }
}