package com.kemalurekli.electricalcalculator

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Test runner that swaps in [HiltTestApplication].
 *
 * Instrumented tests need an application annotated for Hilt testing so modules
 * can be replaced per test. Without this, every test would boot the production
 * graph, including the real database and DataStore.
 *
 * Referenced from `build.gradle.kts` as the `testInstrumentationRunner`.
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(classLoader, HiltTestApplication::class.java.name, context)
}
