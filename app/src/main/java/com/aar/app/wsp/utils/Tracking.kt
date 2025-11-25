package com.aar.app.wsp.utils

import android.os.Bundle
import androidx.core.os.bundleOf
import com.aar.app.wsp.WordSearchApp
import com.aar.app.wsp.model.Word
import com.google.firebase.analytics.FirebaseAnalytics
import kotlin.coroutines.coroutineContext

object Tracking {

    private val appContext = WordSearchApp.instance.applicationContext


    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(appContext)
    }

    fun trackPlay(
        showResumeDialog: Boolean, intentResume: Boolean,
        video: String,

        audioTrack: Int, videoTrack: Int,
        spuTrack: Int
    ) {
       }



    fun trackAdLoaded() {
        FirebaseAnalytics.getInstance(appContext).logEvent(Event.AdLoaded.name, Bundle())
    }

    fun adClicked() {
        FirebaseAnalytics.getInstance(appContext).logEvent(Event.AdClicked.name, Bundle())

    }

    fun adImpression() {
        FirebaseAnalytics.getInstance(appContext)
            .logEvent(Event.AdImpression.name, Bundle())
    }

    fun adFailedToLoad(code: Int, message: String) {
        FirebaseAnalytics.getInstance(appContext)
            .logEvent(
                Event.AdFailedToLoad.name,
                bundleOf(Pair("ErrorCode", code), Pair("Error Message", message))
            )
    }


    enum class Event {

        AdLoaded,

        AdClicked,
        AdImpression,
        AdFailedToLoad
    }
}