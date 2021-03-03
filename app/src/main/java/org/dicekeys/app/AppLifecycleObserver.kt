package org.dicekeys.app

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.preference.PreferenceManager
import org.dicekeys.app.repositories.DiceKeyRepository
import java.util.*
import kotlin.concurrent.schedule

class AppLifecycleObserver(context: Context, val diceKeyRepository: DiceKeyRepository) : LifecycleObserver{
    private val timer = Timer()
    private  var timerTask: TimerTask? = null

    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        timerTask?.cancel()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        var delay = DELAY
        
        try {
            // Default value 60 seconds, keep it in synced with default value in preferences.xml
            delay = sharedPreferences.getString("autolock", "60")!!.toLong() * 1000L
        }catch (e: Exception){
            e.printStackTrace()
        }

        timerTask = timer.schedule(delay) {
            diceKeyRepository.clear()
        }
    }

    companion object{
        // 30 seconds 
        const val DELAY = 30 * 1000L
    }
}