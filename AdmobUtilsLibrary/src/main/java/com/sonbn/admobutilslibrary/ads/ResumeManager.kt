package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.sonbn.admobutilslibrary.dialog.DialogLoadingAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.sonbn.admobutilslibrary.runTryCatch
import java.lang.ref.WeakReference
import java.util.Date


class ResumeManager {
    companion object {
        private const val TAG = "ResumeAdManager"
        private var instance: ResumeManager? = null
        fun getInstance(): ResumeManager {
            if (instance == null) instance = ResumeManager()
            return instance!!
        }
    }

    private var mActivityRef: WeakReference<Activity>? = null
    private var set = mutableSetOf<Class<*>?>()
    private var isShowingAd = false
    private var appOpenAd: AppOpenAd? = null
    private var loadTime: Long = 0
    private var loadCallback: AppOpenAdLoadCallback? = null
    private var isLoadingAd = false
    private var mApplication: Application? = null
    private var id: String = AdmobUtils.APP_OPEN
    private var showDialogLoading = true
    private var onShowAdCompleteListener: OnShowAdCompleteListener? = null
    fun init(
        application: Application,
        id: String,
        showDialogLoading: Boolean = true,
        onShowAdCompleteListener: OnShowAdCompleteListener? = null
    ) {
        this.mApplication = application
        this.id = id
        this.showDialogLoading = showDialogLoading
        this.onShowAdCompleteListener = onShowAdCompleteListener

        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        ProcessLifecycleOwner.get().lifecycle.addObserver(defaultLifecycleObserver)
        fetchAd()
    }

    private fun setActivity(activity: Activity?) {
        mActivityRef = WeakReference(activity)
    }

    private fun getActivity(): Activity? {
        return mActivityRef?.get()
    }

    fun insertActivityDisableAd(activity: Class<*>) {
        set.add(activity)
    }

    fun removeActivityDisableAd(activity: Class<*>) {
        set.remove(activity)
    }

    private val defaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            showAdIfAvailable()
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
//            setActivity(null)
        }

    }

    private fun fetchAd() {
        if (isLoadingAd || isAdAvailable) {
            return
        }
        isLoadingAd = true
        loadCallback = object : AppOpenAdLoadCallback() {

            override fun onAdLoaded(ad: AppOpenAd) {
                isLoadingAd = false
                appOpenAd = ad
                loadTime = Date().time
                Log.d(TAG, "onAdLoaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error.
                isLoadingAd = false
                Log.d(TAG, "onAdFailedToLoad: ${loadAdError.message}")
            }
        }
        val request = adRequest
        var idAd = id
        if (AdmobUtils.isDebug) {
            idAd = AdmobUtils.APP_OPEN
        }
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

        val dialogLoadingAd = DialogLoadingAd()
        var fragmentActivity: FragmentActivity? = null
        runTryCatch {
            fragmentActivity = getActivity() as FragmentActivity
        }


        val fullScreenContentCallback: FullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    fetchAd()
                    Log.d(TAG, "onAdDismissedFullScreenContent")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    dismissDialog(dialogLoadingAd)
                    appOpenAd = null
                    isShowingAd = false
                    fetchAd()
                    Log.d(TAG, "onAdFailedToShowFullScreenContent")
                }

                override fun onAdShowedFullScreenContent() {
                    onShowAdCompleteListener?.onShowAdComplete(appOpenAd)
                    dismissDialog(dialogLoadingAd)
                    isShowingAd = true
                    Log.d(TAG, "onAdShowedFullScreenContent")
                }
            }

        if (available()) {
            appOpenAd!!.fullScreenContentCallback = fullScreenContentCallback
            if (showDialogLoading && fragmentActivity != null) {
                dialogLoadingAd.show(
                    fragmentActivity!!.supportFragmentManager,
                    DialogLoadingAd::class.simpleName
                )
                Handler(Looper.getMainLooper())
                    .postDelayed({
                        runTryCatch {
                            if (getActivity() != null && appOpenAd != null) {
                                appOpenAd!!.show(getActivity()!!)
                            }
                        }
                    }, 2000L)
            } else {
                appOpenAd!!.show(getActivity()!!)
            }
        }

    }

    private fun available(): Boolean {
        if (appOpenAd == null || getActivity() == null) return false
        if (AdInterstitial.showedFullScreen || AdReward.showedFullScreen) return false
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
        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo()

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