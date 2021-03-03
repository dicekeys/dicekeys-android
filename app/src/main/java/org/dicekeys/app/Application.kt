package org.dicekeys.app

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Application : Application(){

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver
}