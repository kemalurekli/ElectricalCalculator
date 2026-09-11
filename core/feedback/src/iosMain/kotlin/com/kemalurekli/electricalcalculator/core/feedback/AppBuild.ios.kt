package com.kemalurekli.electricalcalculator.core.feedback.domain

import platform.Foundation.NSBundle

/**
 * `CFBundleShortVersionString` is the one the store shows and the one a
 * reporter would quote; `CFBundleVersion` is the build number, which changes
 * every archive and means nothing to anybody outside Xcode.
 */
actual fun appBuild(): AppBuild = AppBuild(
    version = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString")
        as? String ?: "",
    platform = "ios",
)
