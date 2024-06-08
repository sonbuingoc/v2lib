package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
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
import java.lang.ref.WeakReference
import java.util.Date


object ResumeManager {
    private const val TAG = "ResumeManager"

    interface ResumeListener {
        fun onFetchAd()
        fun onAdLoaded(appOpenAd: AppOpenAd)
        fun onAdFailedToLoad(adError: LoadAdError)
        fun onAdShowedFullScreenContent()
        fun onAdFailedToShowFullScreenContent(adError: AdError)
    }

    private var resumeListener: ResumeListener? = null
    fun setResumeListener(resumeListener: ResumeListener?) {
        if (this.resumeListener == null) return
        this.resumeListener = resumeListener
    }

    private var set = mutableSetOf<Class<*>?>()
    fun insertActivityDisableAd(activity: Class<*>) {
        set.add(activity)
    }

    fun removeActivityDisableAd(activity: Class<*>) {
        set.remove(activity)
    }

    private var activityReference: WeakReference<Activity>? = null
    private fun setActivity(activity: Activity) {
        activityReference = WeakReference(activity)
    }

    private fun getActivity(): Activity? {
        return activityReference?.get()
    }

    private var isShowingAd = false
    private var resumeAd: AppOpenAd? = null
    private var loadTime: Long = 0
    private var loadCallback: AppOpenAdLoadCallback? = null
    private var isLoadingAd = false
    private var mApplication: Application? = null
    private var id: String = AdmobUtils.APP_OPEN

    fun init(application: Application, id: String) {
        if (mApplication != null) {
            return
        }
        this.mApplication = application
        this.id = id

        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        ProcessLifecycleOwner.get().lifecycle.addObserver(defaultLifecycleObserver)
    }

    private val defaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            showAdIfAvailable()
        }

        //load ads when move to background
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            CoroutineScope(Dispatchers.Main + CoroutineExceptionHandler { _, _ ->  }).launch {
                //fetchAd after
                delay(1000)
                fetchAd()
            }
        }
    }
    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {

        }

        override fun onActivityStarted(p0: Activity) {
            setActivity(p0)
        }

        override fun onActivityResumed(p0: Activity) {
        }

        override fun onActivityPaused(p0: Activity) {
        }

        override fun onActivityStopped(p0: Activity) {
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        }

        override fun onActivityDestroyed(p0: Activity) {
//            mActivity = null
        }

    }

    fun fetchAd() {
        if (isLoadingAd || isAdAvailable) {
            return
        }
        resumeListener?.onFetchAd()
        isLoadingAd = true
        loadCallback = object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                isLoadingAd = false
                resumeAd = ad
                loadTime = Date().time
                Log.d(TAG, "onAdLoaded")
                resumeListener?.onAdLoaded(ad)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error.
                isLoadingAd = false
                resumeListener?.onAdFailedToLoad(loadAdError)
                Log.d(TAG, "onAdFailedToLoad: ${loadAdError.message}")

            }
        }
        val request = adRequest
        val idAd = if (AdmobUtils.isDebug) AdmobUtils.APP_OPEN else id
        mApplication?.let {
            AppOpenAd.load(it, idAd, request, loadCallback!!)
        }
    }

    private fun showAdIfAvailable() {
        if (!AdmobUtils.isShowAds) {
            return
        }
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (isShowingAd) {
            return
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable) {
            fetchAd()
            return
        }

        val fullScreenContentCallback: FullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    resumeAd = null
                    isShowingAd = false
//                    fetchAd()
                    Log.d(TAG, "onAdDismissedFullScreenContent")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    resumeAd = null
                    isShowingAd = false
//                    fetchAd()
                    Log.d(TAG, "onAdFailedToShowFullScreenContent")
                    resumeListener?.onAdFailedToShowFullScreenContent(adError)
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                    resumeListener?.onAdShowedFullScreenContent()
                    Log.d(TAG, "onAdShowedFullScreenContent")
                }
            }

        if (available()) {
            resumeAd!!.fullScreenContentCallback = fullScreenContentCallback
            resumeAd!!.show(getActivity()!!)
        }

    }

    private fun available(): Boolean {
        if (!AdmobUtils.isForeground) return false
        if (!isAdAvailable || getActivity() == null) return false
        if (AdInterstitial.isShowedFullScreen) return false
        if (set.indexOf(getActivity()!!::class.java) != -1) return false
        return true
    }

    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * 4L
    }

    private val isAdAvailable: Boolean
        get() = resumeAd != null && wasLoadTimeLessThanNHoursAgo()
}