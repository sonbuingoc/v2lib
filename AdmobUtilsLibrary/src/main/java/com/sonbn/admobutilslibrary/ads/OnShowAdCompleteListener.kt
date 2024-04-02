package com.sonbn.admobutilslibrary.ads

import com.google.android.gms.ads.appopen.AppOpenAd

interface OnShowAdCompleteListener {
    fun onShowAdComplete(appOpenAd: AppOpenAd?)
}