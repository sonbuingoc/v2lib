[![](https://jitpack.io/v/sonbuingoc/AdmobUtilsLibrary.svg)](https://jitpack.io/#sonbuingoc/AdmobUtilsLibrary)


# Step1. 
```
repositories {
	maven { url 'https://jitpack.io' }
}
```

  

# Step2.
```
dependencies {
	implementation 'com.github.sonbuingoc:AdmobUtilsLibrary:Tag'
}
```

# EU Consent
```
val gdprManager = GDPRManager.getInstance(this)
        gdprManager.init(this)
        gdprManager.mCallback = object : GDPRManager.Callback {
            override fun initializeMobileAdsSdk(value: Boolean) {
                if (value) {
                   //
                } else {
                   //
                }
            }

        }
```

# Ads
## Init
```
AdmobUtils.initMobileAds(this@SplashActivity, true, false)
```

## Show ads
```
        <View
            android:id="@+id/line"
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:background="@color/gray" />

        <FrameLayout
            android:id="@+id/flBanner"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@color/black"
            android:minHeight="50dp" />

AdBanner.showBannerCollapsible(this, AdsManager.BANNER_COLLAPSIBLE, binding.flBanner, binding.line)

//Native small min height = 120dp, medium = 200dp
```
