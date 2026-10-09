package com.cococue.fakecallfootballplayer.utils

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cococue.fakecallfootballplayer.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

import androidx.core.content.edit

object AdManager {

    // Default Official Google AdMob Test Ad Unit IDs & GitHub Remote Config URL
    const val DEFAULT_GITHUB_JSON_URL = "https://raw.githubusercontent.com/kebol97/fakecallfootballplayer/refs/heads/master/ads.json"
    private const val DEFAULT_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712x"
    private const val DEFAULT_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110x"
    private const val DEFAULT_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917x"

    private const val PREF_NAME = "ad_remote_pref"
    private const val KEY_SHOW_ADS = "show_ads"
    private const val KEY_INTERSTITIAL_ID = "interstitial_id"
    private const val KEY_NATIVE_ID = "native_id"
    private const val KEY_REWARDED_ID = "rewarded_id"
    private const val KEY_MIN_INTERVAL = "min_interval"

    var showAds = true
    var interstitialAdId = DEFAULT_INTERSTITIAL_ID
    var nativeAdId = DEFAULT_NATIVE_ID
    var rewardedAdId = DEFAULT_REWARDED_ID
    var minIntervalSec = 30L

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var lastAdShowTime = 0L
    private var isInitialized = false

    /**
     * Official Google UMP GDPR Consent & AdMob Initialization Flow
     */
    fun init(activity: Activity, githubUrl: String = DEFAULT_GITHUB_JSON_URL) {
        loadCachedConfig(activity)
        if (!isInitialized) {
            isInitialized = true
            requestConsentAndInit(activity, githubUrl)
        }
    }

    fun loadCachedConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        showAds = prefs.getBoolean(KEY_SHOW_ADS, true)
        interstitialAdId = prefs.getString(KEY_INTERSTITIAL_ID, null)?.ifBlank { null } ?: DEFAULT_INTERSTITIAL_ID
        nativeAdId = prefs.getString(KEY_NATIVE_ID, null)?.ifBlank { null } ?: DEFAULT_NATIVE_ID
        rewardedAdId = prefs.getString(KEY_REWARDED_ID, null)?.ifBlank { null } ?: DEFAULT_REWARDED_ID
        minIntervalSec = prefs.getLong(KEY_MIN_INTERVAL, 30L)
    }

    private fun saveCachedConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_SHOW_ADS, showAds)
            putString(KEY_INTERSTITIAL_ID, interstitialAdId)
            putString(KEY_NATIVE_ID, nativeAdId)
            putString(KEY_REWARDED_ID, rewardedAdId)
            putLong(KEY_MIN_INTERVAL, minIntervalSec)
        }
    }

    private fun requestConsentAndInit(activity: Activity, githubUrl: String) {
        val params = ConsentRequestParameters.Builder().build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    if (consentInformation.canRequestAds()) {
                        initAdMob(activity, githubUrl)
                    }
                }
            },
            { _ ->
                if (consentInformation.canRequestAds()) {
                    initAdMob(activity, githubUrl)
                }
            }
        )
    }

    private fun initAdMob(context: Context, githubUrl: String) {
        MobileAds.initialize(context) {}
        fetchRemoteConfig(context, githubUrl)
        loadInterstitialAd(context)
        loadRewardedAd(context)
    }

    /**
     * Fetches Ad Unit IDs & Ad Settings from GitHub Raw JSON URL
     */
    fun fetchRemoteConfig(context: Context, githubUrl: String = DEFAULT_GITHUB_JSON_URL) {
        thread {
            try {
                val url = URL(githubUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.requestMethod = "GET"

                if (conn.responseCode == 200) {
                    val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonStr)

                    showAds = json.optBoolean("show_ads", true)

                    val fetchedInter = json.optString("interstitial_id").trim()
                    if (fetchedInter.isNotEmpty()) {
                        interstitialAdId = fetchedInter
                    }

                    val fetchedNative = json.optString("native_id").trim()
                    if (fetchedNative.isNotEmpty()) {
                        nativeAdId = fetchedNative
                    }

                    val fetchedRewarded = json.optString("rewarded_id").trim()
                    if (fetchedRewarded.isNotEmpty()) {
                        rewardedAdId = fetchedRewarded
                    }

                    minIntervalSec = json.optLong("interstitial_interval_sec", 30L)

                    saveCachedConfig(context)

                    if (showAds) {
                        (context as? Activity)?.runOnUiThread {
                            loadInterstitialAd(context)
                            loadRewardedAd(context)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadInterstitialAd(context: Context) {
        if (!showAds || interstitialAdId.isBlank()) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            interstitialAdId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun loadRewardedAd(context: Context) {
        if (!showAds || rewardedAdId.isBlank()) return

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            rewardedAdId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    /**
     * Shows Rewarded Ad. Executes onRewardGranted callback when user completes watching video!
     */
    fun showRewardedAd(activity: Activity, onRewardGranted: () -> Unit) {
        if (showAds && rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    rewardedAd = null
                    onRewardGranted()
                }
            }
            rewardedAd?.show(activity) { _ ->
                onRewardGranted()
            }
        } else {
            onRewardGranted()
        }
    }

    /**
     * Shows Interstitial Ad adhering to Google AdMob Policies
     */
    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        val timeDiffSec = (currentTime - lastAdShowTime) / 1000

        if (showAds && interstitialAd != null && timeDiffSec >= minIntervalSec) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastAdShowTime = System.currentTimeMillis()
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            onAdDismissed()
        }
    }

    /**
     * Loads Native Ad and populates NativeAdView inside container
     */
    fun loadNativeAd(context: Context, container: ViewGroup) {
        if (!showAds || nativeAdId.isBlank()) {
            container.visibility = View.GONE
            return
        }

        val adLoader = AdLoader.Builder(context, nativeAdId)
            .forNativeAd { nativeAd ->
                val layoutInflater = LayoutInflater.from(context)
                val adView = layoutInflater.inflate(R.layout.item_native_ad, container, false) as NativeAdView
                populateNativeAdView(nativeAd, adView)
                container.removeAllViews()
                container.addView(adView)
                container.visibility = View.VISIBLE
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    container.visibility = View.GONE
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val adHeadline = adView.findViewById<TextView>(R.id.adHeadline)
        val adBody = adView.findViewById<TextView>(R.id.adBody)
        val adCallToAction = adView.findViewById<View>(R.id.adCallToAction)
        val adAppIcon = adView.findViewById<ImageView>(R.id.adAppIcon)
        val adMedia = adView.findViewById<MediaView>(R.id.adMedia)

        adView.headlineView = adHeadline
        adView.bodyView = adBody
        adView.callToActionView = adCallToAction
        adView.iconView = adAppIcon
        adView.mediaView = adMedia

        adHeadline.text = nativeAd.headline

        if (nativeAd.body == null) {
            adBody.visibility = View.GONE
        } else {
            adBody.visibility = View.VISIBLE
            adBody.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adCallToAction.visibility = View.GONE
        } else {
            adCallToAction.visibility = View.VISIBLE
            (adCallToAction as? TextView)?.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adAppIcon.visibility = View.GONE
        } else {
            adAppIcon.visibility = View.VISIBLE
            adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
        }

        adView.setNativeAd(nativeAd)
    }
}
