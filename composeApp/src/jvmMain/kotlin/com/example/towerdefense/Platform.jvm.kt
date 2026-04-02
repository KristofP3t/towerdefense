package com.example.towerdefense

import java.io.File

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