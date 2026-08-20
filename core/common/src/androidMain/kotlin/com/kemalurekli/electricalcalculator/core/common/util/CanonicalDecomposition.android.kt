package com.kemalurekli.electricalcalculator.core.common.util

import java.text.Normalizer

actual fun String.decomposeCanonically(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
