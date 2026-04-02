package com.example.towerdefense

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

var androidAppContext: Context? = null

actual fun storageWrite(key: String, value: String) {
    androidAppContext
        ?.getSharedPreferences("towerdefense", Context.MODE_PRIVATE)
        ?.edit()?.putString(key, value)?.apply()
}

actual fun storageRead(key: String): String? =
    androidAppContext
        ?.getSharedPreferences("towerdefense", Context.MODE_PRIVATE)
        ?.getString(key, null)

actual fun playSound(id: String) {
    try {
        val (tone, ms) = when (id) {
            "shoot"     -> ToneGenerator.TONE_PROP_BEEP   to 40
            "kill"      -> ToneGenerator.TONE_PROP_BEEP2  to 80
            "boss_kill" -> ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE to 250
            "game_over" -> ToneGenerator.TONE_CDMA_NETWORK_BUSY    to 500
            "victory"   -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD to 300
            else        -> return
        }
        ToneGenerator(AudioManager.STREAM_MUSIC, 60).startTone(tone, ms)
    } catch (_: Exception) {}
}