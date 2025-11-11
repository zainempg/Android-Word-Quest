package com.aar.app.wsp

import android.app.Application
import com.aar.app.wsp.di.component.AppComponent
import com.aar.app.wsp.di.component.DaggerAppComponent
import com.aar.app.wsp.di.modules.AppModule

/**
 * Created by abdularis on 18/07/17.
 */
class WordSearchApp : Application() {
    lateinit var appComponent: AppComponent
    override fun onCreate() {
        super.onCreate()
        instance= this
        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()




    }
    companion object {
        @get:Synchronized
        lateinit var instance: WordSearchApp
            private set // Make the setter private

        // Optional: A more explicit getter for context if you prefer
        // fun getAppContext(): Context {
        //     return instance.applicationContext
        // }
    }

}