package com.example.towerdefense

import platform.Foundation.NSUserDefaults
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun storageWrite(key: String, value: String) {
    NSUserDefaults.standardUserDefaults.setObject(value, forKey = "towerdefense_$key")
    NSUserDefaults.standardUserDefaults.synchronize()
}

actual fun storageRead(key: String): String? =
    NSUserDefaults.standardUserDefaults.stringForKey("towerdefense_$key")