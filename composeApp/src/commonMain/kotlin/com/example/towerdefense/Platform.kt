package com.example.towerdefense

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun storageWrite(key: String, value: String)
expect fun storageRead(key: String): String?