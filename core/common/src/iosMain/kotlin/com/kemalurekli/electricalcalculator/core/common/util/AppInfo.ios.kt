package com.kemalurekli.electricalcalculator.core.common.util

import platform.Foundation.NSBundle

actual fun appVersionName(): String =
    NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
