package com.example.towerdefense

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform