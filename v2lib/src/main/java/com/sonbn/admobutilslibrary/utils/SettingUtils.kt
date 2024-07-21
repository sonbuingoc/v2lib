package com.sonbn.admobutilslibrary.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.sonbn.admobutilslibrary.BuildConfig

object SettingUtils {
    fun feedback(context: Context, subject: String, email: String) {
        runTryCatch {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.data = Uri.parse("mailto:")
            context.startActivity(Intent.createChooser(intent, subject))
        }
    }

    fun share(context: Context, label: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, label)
            val shareMessage =
                "https://play.google.com/store/apps/details?id=${context.packageName}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            context.startActivity(Intent.createChooser(shareIntent, label))
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
    }

    fun rate(context: Context) {
        val appPackageName: String = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: Throwable) {
            runTryCatch {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
        }
    }

    fun openChromeTab(context: Context, url: String) {
        try {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent: CustomTabsIntent = builder.build()
            customTabsIntent.launchUrl(
                context,
                Uri.parse(url)
            )
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
    }
}