package com.example.towerdefense

import kotlinx.browser.localStorage

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun storageWrite(key: String, value: String) {
    localStorage.setItem("towerdefense_$key", value)
}

actual fun storageRead(key: String): String? =
    localStorage.getItem("towerdefense_$key")