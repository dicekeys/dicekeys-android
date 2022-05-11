package org.dicekeys.app

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import org.dicekeys.app.repositories.DiceKeyRepository
import java.util.*
import kotlin.concurrent.schedule

class AppLifecycleObserver constructor(val sharedPreferences: SharedPreferences, val diceKeyRepository: DiceKeyRepository) : DefaultLifecycleObserver{
    private val timer = Timer()
    private  var timerTask: TimerTask? = null

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        timerTask?.cancel()
    }

    override fun onPause(owner: LifecycleOwner) {
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
        // 60 seconds
        const val DELAY = 60 * 1000L
    }
}