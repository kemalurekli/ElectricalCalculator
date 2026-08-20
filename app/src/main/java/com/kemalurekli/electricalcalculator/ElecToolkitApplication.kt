package com.kemalurekli.electricalcalculator

import android.app.Application
import com.kemalurekli.electricalcalculator.core.di.hiltBridgeModule
import com.kemalurekli.electricalcalculator.features.calculators.calculatorsModule
import com.kemalurekli.electricalcalculator.features.converter.converterModule
import com.kemalurekli.electricalcalculator.features.fieldnotes.fieldNotesModule
import com.kemalurekli.electricalcalculator.features.glossary.glossaryModule
import com.kemalurekli.electricalcalculator.features.references.referencesModule
import com.kemalurekli.electricalcalculator.features.theory.theoryModule
import com.kemalurekli.electricalcalculator.features.history.historyModule
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Application entry point and dependency graph root — currently both graphs.
 *
 * Hilt still wires everything that lives in `:app`. Koin wires the modules that
 * have left it, because Hilt is Android-only and those modules also build for
 * iOS. The two run side by side, each owning a disjoint set of types, until the
 * migration finishes and Hilt goes.
 *
 * Hilt does no work in `onCreate` by design: every dependency is lazily
 * constructed on first injection, which keeps cold start off the critical path.
 * Koin's `startKoin` is eager but only registers definitions — it constructs
 * nothing until something is resolved.
 */
@HiltAndroidApp
class ElecToolkitApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ElecToolkitApplication)
            // `hiltBridgeModule` comes first only for readability; Koin resolves
            // by type, not by declaration order.
            modules(
                hiltBridgeModule,
                converterModule,
                historyModule,
                glossaryModule,
                fieldNotesModule,
                referencesModule,
                calculatorsModule,
                theoryModule,
            )
        }
    }
}
