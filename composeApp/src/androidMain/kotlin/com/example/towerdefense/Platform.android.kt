package com.example.towerdefense

import android.content.Context
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