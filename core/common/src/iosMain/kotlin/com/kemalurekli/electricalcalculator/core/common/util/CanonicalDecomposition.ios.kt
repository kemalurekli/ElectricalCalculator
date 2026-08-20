package com.kemalurekli.electricalcalculator.core.common.util

import platform.Foundation.NSString
import platform.Foundation.decomposedStringWithCanonicalMapping

actual fun String.decomposeCanonically(): String =
    (this as NSString).decomposedStringWithCanonicalMapping
