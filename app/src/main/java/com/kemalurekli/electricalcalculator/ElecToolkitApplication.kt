package com.kemalurekli.electricalcalculator

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point and Hilt dependency graph root.
 *
 * Deliberately does no work in `onCreate`: every dependency is lazily
 * constructed by Hilt on first injection, which is what keeps cold start off
 * the critical path.
 */
@HiltAndroidApp
class ElecToolkitApplication : Application()
