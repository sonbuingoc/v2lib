package com.sonbn.admobutilslibrary.ads

import android.app.Activity
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import android.util.Log
import com.facebook.shimmer.BuildConfig
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform


enum class AdsStatus {
    NotInitialized,
    NonPersonalized,
    Personalized,
    Denied,
    Error
}
class GDPRManager {
    private lateinit var prefs: SharedPreferences
    private lateinit var consentInformation: ConsentInformation
    private var mCallback: Callback? = null

    companion object {
        private const val TAG = "GDPRManager"
        private var instance: GDPRManager? = null
        fun getInstance(): GDPRManager {
            if (instance == null) {
                instance = GDPRManager()
            }
            return instance!!
        }
    }


    fun init(activity: Activity, callback: Callback) {
        this.mCallback = callback
        prefs = PreferenceManager.getDefaultSharedPreferences(activity.application)
        // Set tag for underage of consent. false means users are not underage.
        if (!isGDPR()){
            mCallback?.initializeMobileAdsSdk(true)
            return
        }
        val params = ConsentRequestParameters.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    setConsentDebugSettings(
                        ConsentDebugSettings.Builder(activity)
                            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                            // .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA)
                            .build()
                    )
                }
            }
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        printLogs("showConsentFormIfNeeded 1")
        if (isGDPR() && !canShowAds()) {
            consentInformation.reset()
        }
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                if (consentInformation.isConsentFormAvailable) {
                    loadForm(activity)
                }else{
                    mCallback?.initializeMobileAdsSdk(false)
                }
                printLogs("requestConsentInfoUpdate")
            },
            {
                mCallback?.initializeMobileAdsSdk(false)
                printLogs("requestConsentInfoUpdate error")
            }
        )
    }


    private fun getState(): AdsStatus {
        return if (isGDPR()) {
            when {
                canShowPersonalizedAds() -> {
                    AdsStatus.Personalized
                }

                canShowAds() -> {
                    AdsStatus.NonPersonalized
                }

                else -> {
                    AdsStatus.Denied
                }
            }
        } else {
            AdsStatus.Personalized
        }
    }


    private fun loadForm(activity: Activity) {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { consentForm ->
            printLogs("loadConsentForm")
            mCallback?.initializeMobileAdsSdk(canShowAds() || canShowPersonalizedAds())
        }
    }

    private fun isGDPR(): Boolean {
        val gdpr = prefs.getInt("IABTCF_gdprApplies", 0)
        return gdpr == 1
    }

    private fun canShowAds(): Boolean {
        // https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details
        // https://support.google.com/admob/answer/9760862?hl=en&ref_topic=9756841

        val purposeConsent = prefs.getString("IABTCF_PurposeConsents", "") ?: ""
        val vendorConsent = prefs.getString("IABTCF_VendorConsents", "") ?: ""
        val vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "") ?: ""
        val purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "") ?: ""

        val googleId = 755
        val hasGoogleVendorConsent = hasAttribute(vendorConsent, index = googleId)
        val hasGoogleVendorLI = hasAttribute(vendorLI, index = googleId)

        // Minimum required for at least non-personalized ads
        return hasConsentFor(listOf(1), purposeConsent, hasGoogleVendorConsent) &&
                hasConsentOrLegitimateInterestFor(
                    listOf(2, 7, 9, 10),
                    purposeConsent,
                    purposeLI,
                    hasGoogleVendorConsent,
                    hasGoogleVendorLI
                )
    }

    private fun canShowPersonalizedAds(): Boolean {
        // https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details
        // https://support.google.com/admob/answer/9760862?hl=en&ref_topic=9756841

        val purposeConsent = prefs.getString("IABTCF_PurposeConsents", "") ?: ""
        val vendorConsent = prefs.getString("IABTCF_VendorConsents", "") ?: ""
        val vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "") ?: ""
        val purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "") ?: ""

        val googleId = 755
        val hasGoogleVendorConsent = hasAttribute(vendorConsent, index = googleId)
        val hasGoogleVendorLI = hasAttribute(vendorLI, index = googleId)

        return hasConsentFor(listOf(1, 3, 4), purposeConsent, hasGoogleVendorConsent) &&
                hasConsentOrLegitimateInterestFor(
                    listOf(2, 7, 9, 10),
                    purposeConsent,
                    purposeLI,
                    hasGoogleVendorConsent,
                    hasGoogleVendorLI
                )
    }

    // Check if a binary string has a "1" at position "index" (1-based)
    private fun hasAttribute(input: String, index: Int): Boolean {
        return input.length >= index && input[index - 1] == '1'
    }

    // Check if consent is given for a list of purposes
    private fun hasConsentFor(
        purposes: List<Int>,
        purposeConsent: String,
        hasVendorConsent: Boolean
    ): Boolean {
        return purposes.all { p -> hasAttribute(purposeConsent, p) } && hasVendorConsent
    }

    // Check if a vendor either has consent or legitimate interest for a list of purposes
    private fun hasConsentOrLegitimateInterestFor(
        purposes: List<Int>,
        purposeConsent: String,
        purposeLI: String,
        hasVendorConsent: Boolean,
        hasVendorLI: Boolean
    ): Boolean {
        return purposes.all { p ->
            (hasAttribute(purposeLI, p) && hasVendorLI) ||
                    (hasAttribute(purposeConsent, p) && hasVendorConsent)
        }
    }

    private fun printLogs(msg: String) {
        val massage =
            """
----------------------
    msg: $msg
    isGDPR: ${isGDPR()}
    adsState: ${getState()}
    canShowAds: ${canShowAds()}
    canShowPersonalizedAds: ${canShowPersonalizedAds()}
    consentStatus: ${consentInformation.consentStatus}
    isConsentFormAvailable: ${consentInformation.isConsentFormAvailable}
----------------------
            """

        Log.d(TAG, massage)
    }

    interface Callback {
        fun initializeMobileAdsSdk(value: Boolean)
    }
}