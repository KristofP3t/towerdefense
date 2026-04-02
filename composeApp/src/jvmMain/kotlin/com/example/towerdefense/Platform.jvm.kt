package com.example.towerdefense

import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.math.*

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

private fun storageFile(key: String): File {
    val dir = File(System.getProperty("user.home"), ".towerdefense")
    dir.mkdirs()
    return File(dir, "$key.txt")
}

actual fun storageWrite(key: String, value: String) {
    try { storageFile(key).writeText(value) } catch (_: Exception) {}
}

actual fun storageRead(key: String): String? = try {
    storageFile(key).takeIf { it.exists() }?.readText()
} catch (_: Exception) { null }

actual fun playSound(id: String) {
    val (freq, durationMs, vol) = when (id) {
        "shoot"     -> Triple(620.0,  35,  0.18f)
        "kill"      -> Triple(360.0,  90,  0.35f)
        "boss_kill" -> Triple(180.0, 280,  0.55f)
        "game_over" -> Triple(140.0, 700,  0.50f)
        "victory"   -> Triple(740.0, 450,  0.45f)
        else        -> return
    }
    Thread {
        try {
            val sampleRate = 44100f
            val samples    = (sampleRate * durationMs / 1000).toInt()
            val buf        = ByteArray(samples * 2)
            for (i in 0 until samples) {
                val t        = i.toDouble() / sampleRate
                val attack   = minOf(1.0, i / (sampleRate * 0.01))
                val decay    = exp(-4.0 * i / samples)
                val envelope = attack * decay
                val value    = (vol * 32767 * envelope * sin(2 * PI * freq * t)).toInt().toShort()
                buf[i * 2]     = (value.toInt() and 0xFF).toByte()
                buf[i * 2 + 1] = (value.toInt() shr 8).toByte()
            }
            val format = AudioFormat(sampleRate, 16, 1, true, false)
            val info   = DataLine.Info(SourceDataLine::class.java, format)
            val line   = AudioSystem.getLine(info) as SourceDataLine
            line.open(format, 4096)
            line.start()
            line.write(buf, 0, buf.size)
            line.drain()
            line.close()
        } catch (_: Exception) {}
    }.also { it.isDaemon = true }.start()
}