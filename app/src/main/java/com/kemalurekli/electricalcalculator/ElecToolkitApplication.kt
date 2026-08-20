package com.kemalurekli.electricalcalculator

import com.kemalurekli.electricalcalculator.core.common.util.regionContext
import com.kemalurekli.electricalcalculator.core.common.util.appInfoContext
import com.kemalurekli.electricalcalculator.core.datastore.preferencesContext
import com.kemalurekli.electricalcalculator.core.database.databaseContext
import android.app.Application
import com.kemalurekli.electricalcalculator.core.common.di.coreCommonModule
import com.kemalurekli.electricalcalculator.core.data.di.coreDataModule
import com.kemalurekli.electricalcalculator.core.di.androidAppModule
import com.kemalurekli.electricalcalculator.features.calculators.calculatorsModule
import com.kemalurekli.electricalcalculator.features.converter.converterModule
import com.kemalurekli.electricalcalculator.features.favorites.favoritesModule
import com.kemalurekli.electricalcalculator.features.fieldnotes.fieldNotesModule
import com.kemalurekli.electricalcalculator.features.forum.forumModule
import com.kemalurekli.electricalcalculator.features.glossary.glossaryModule
import com.kemalurekli.electricalcalculator.features.home.homeModule
import com.kemalurekli.electricalcalculator.features.projects.projectsModule
import com.kemalurekli.electricalcalculator.features.references.referencesModule
import com.kemalurekli.electricalcalculator.features.settings.settingsModule
import com.kemalurekli.electricalcalculator.features.theory.theoryModule
import com.kemalurekli.electricalcalculator.features.history.historyModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Application entry point and dependency graph root.
 *
 * There used to be two graphs here: Hilt for what lived in `:app`, Koin for the
 * modules that had left it. Hilt is gone. What is registered below is what iOS
 * registers too, plus `androidAppModule` for the two things that genuinely need
 * Android — the device region and the per-app language.
 *
 * `startKoin` is eager but only registers definitions; it constructs nothing
 * until something is resolved, so cold start is unaffected.
 */
class ElecToolkitApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Before Koin, not after: the first screen resolves a repository, which
        // opens the database, which reads this. The Hilt modules that used to
        // set these are gone, and nothing else runs early enough.
        databaseContext = this
        preferencesContext = this
        appInfoContext = this
        regionContext = this
        startKoin {
            androidContext(this@ElecToolkitApplication)
            modules(
                coreCommonModule,
                coreDataModule,
                androidAppModule,
                converterModule,
                historyModule,
                glossaryModule,
                fieldNotesModule,
                referencesModule,
                calculatorsModule,
                theoryModule,
                favoritesModule,
                homeModule,
                projectsModule,
                forumModule,
                settingsModule,
            )
        }
    }
}
