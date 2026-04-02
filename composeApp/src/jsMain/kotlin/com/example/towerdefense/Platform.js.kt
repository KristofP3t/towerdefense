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