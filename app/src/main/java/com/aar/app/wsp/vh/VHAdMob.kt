package com.aar.app.wsp.vh

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.aar.app.wsp.databinding.AdmobRowBinding
import com.aar.app.wsp.utils.Logger
import com.aar.app.wsp.utils.Tracking
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import d.d.pvp.model.AdModel


class VHAdMob(val binding: AdmobRowBinding, val adUnitId: String) : VHBase(binding.root, {}) {

    private val TAG = "VHAdMob"

    private fun setupNativeAd(listModel: AdModel? = null) {
        binding.nativeAdView.layoutParams = binding.nativeAdView.layoutParams.apply {
            height = 0
        }
        if (listModel?.nativeAd == null) {
            var adLoader: AdLoader? = null
            val context = binding.root.context
            adLoader = AdLoader.Builder(
                context,
                adUnitId
            ).forNativeAd { ad: NativeAd ->
                // Show the ad.
                Logger.debug(TAG, "forNativeAd : is Loading ${adLoader?.isLoading}")
                listModel?.let {
                    it.nativeAd = ad
                }
                populateUnifiedNativeAdView(ad, binding)

            }
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setRequestCustomMuteThisAd(true)
                        .setVideoOptions(
                            VideoOptions.Builder()
                                .setStartMuted(true)
                                .setClickToExpandRequested(true)
                                .build()
                        )
                        .build()
                )
                .withAdListener(object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Logger.info(TAG, "onAdLoaded")
                        Tracking.trackAdLoaded()
                        binding.nativeAdView.isVisible = true
                        binding.nativeAdView.layoutParams =
                            binding.nativeAdView.layoutParams.apply {
                                height = ViewGroup.LayoutParams.WRAP_CONTENT
                            }
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        Logger.info(TAG, "onAdClicked")
                        Tracking.adClicked()


                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        Logger.info(TAG, "onAdImpression")
                        Tracking.adImpression()

                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        Logger.info(TAG, "onAdFailedToLoad ${p0.code} ${p0.message}")
                        Tracking.adFailedToLoad(p0.code, p0.message)
                        binding.nativeAdView.isVisible = false
                        binding.nativeAdView.layoutParams =
                            binding.nativeAdView.layoutParams.apply {
                                height = 0
                            }
                    }
                })
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        } else {
            listModel.nativeAd?.let { populateUnifiedNativeAdView(it, binding) }
        }
    }

    private fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: AdmobRowBinding) {

        binding.nativeAdView.isVisible = true
        binding.nativeAdView.layoutParams =
            binding.nativeAdView.layoutParams.apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

        // Set the media view.
        adView.nativeAdView.mediaView = adView.adMedia
        adView.nativeAdView.setOnHierarchyChangeListener(object :
            ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) {
                val scale: Float = adView.root.resources.displayMetrics.density

                val maxHeightPixels = 175
                val maxHeightDp = (maxHeightPixels * scale + 0.5f).toInt()

                if (child is ImageView) { //Images
                    child.adjustViewBounds = true
                    child.maxHeight = maxHeightDp
                } else { //Videos
                    val params = child?.layoutParams
                    params?.height = maxHeightDp
                    child?.layoutParams = params
                }
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {
            }
        })

        // Set other ad assets.
        adView.nativeAdView.headlineView = adView.adHeadline
        adView.nativeAdView.bodyView = adView.adBody
        adView.nativeAdView.callToActionView = adView.adCallToAction
        adView.nativeAdView.iconView = adView.adAppIcon
        adView.nativeAdView.priceView = adView.adPrice
        adView.nativeAdView.starRatingView = adView.adStars
        adView.nativeAdView.storeView = adView.adStore
        adView.nativeAdView.advertiserView = adView.adAdvertiser

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.

        adView.adBody.isVisible = nativeAd.body != null
        adView.adBody.text = nativeAd.body

        adView.adHeadline.text = nativeAd.headline
        adView.adHeadline.isVisible = nativeAd.headline != null

        adView.adCallToAction.isVisible = nativeAd.callToAction != null
        adView.adCallToAction.text = nativeAd.callToAction

        adView.adAppIcon.isVisible = nativeAd.icon != null


        adView.adAppIcon.isVisible = nativeAd.icon.let {
            if (it != null) {
                adView.adAppIcon.setImageDrawable(it.drawable)
            }
            it != null
        }


        if (nativeAd.price == null) {
            adView.adPrice.visibility = View.GONE
        } else {
            adView.adPrice.visibility = View.VISIBLE
            adView.adPrice.text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.adStore.visibility = View.GONE
        } else {
            adView.adStore.visibility = View.VISIBLE
            adView.adStore.text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.adStars.visibility = View.GONE
        } else {
            adView.adStars.rating = nativeAd.starRating!!.toFloat()
            adView.adStars.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.adAdvertiser.visibility = View.GONE
        } else {
            adView.adAdvertiser.text = nativeAd.advertiser
            adView.adAdvertiser.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.nativeAdView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.mediaContent?.videoController

        // Updates the UI to say whether or not this ad has a video asset.
//        if (vc != null) {
//            if (vc.hasVideoContent()) {
//                binding?.videostatusText?.text = String.format(
//                    Locale.getDefault(),
//                    "Video status: Ad contains a %.2f:1 video asset.",
//                )
//
//                // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
//                // VideoController will call methods on this object when events occur in the video
//                // lifecycle.
//                vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
//                    override fun onVideoEnd() {
//                        // Publishers should allow native ads to complete video playback before
//                        // refreshing or replacing them with another ad in the same UI location.
//                        binding?.refreshButton?.isEnabled = true
//                        binding?.videostatusText?.text = "Video status: Video playback has ended."
//                        super.onVideoEnd()
//                    }
//                }
//            } else {
//                binding?.videostatusText?.text = "Video status: Ad does not contain a video asset."
//                binding?.refreshButton?.isEnabled = true
//            }
//        }
    }

    fun bind(listModel: AdModel) {
        binding.nativeAdView.isVisible = false
        setupNativeAd(listModel)
    }
}
