package com.example.towerdefense

import kotlinx.browser.localStorage

class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun storageWrite(key: String, value: String) {
    localStorage.setItem("towerdefense_$key", value)
}

actual fun storageRead(key: String): String? =
    localStorage.getItem("towerdefense_$key")

actual fun playSound(id: String) {
    val freq = when (id) {
        "shoot"     -> 620
        "kill"      -> 360
        "boss_kill" -> 180
        "game_over" -> 140
        "victory"   -> 740
        else        -> return
    }
    val dur = when (id) {
        "game_over" -> 0.7
        "victory"   -> 0.45
        "boss_kill" -> 0.28
        "kill"      -> 0.09
        else        -> 0.04
    }
    playWebAudioTone(freq, dur)
}

private fun playWebAudioTone(freq: Int, duration: Double) {
    @Suppress("UNUSED_VARIABLE") val f = freq
    @Suppress("UNUSED_VARIABLE") val d = duration
    js("""
        try {
            var ctx = new (window.AudioContext || window.webkitAudioContext)();
            var osc = ctx.createOscillator();
            var gain = ctx.createGain();
            osc.connect(gain);
            gain.connect(ctx.destination);
            osc.frequency.value = f;
            osc.type = 'sine';
            gain.gain.setValueAtTime(0.25, ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + d);
            osc.start(ctx.currentTime);
            osc.stop(ctx.currentTime + d);
            osc.onended = function() { ctx.close(); };
        } catch(e) {}
    """)
}