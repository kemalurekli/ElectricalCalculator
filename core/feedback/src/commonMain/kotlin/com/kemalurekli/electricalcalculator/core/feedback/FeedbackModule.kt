package com.kemalurekli.electricalcalculator.core.feedback

import com.kemalurekli.electricalcalculator.core.feedback.data.FeedbackRepositoryImpl
import com.kemalurekli.electricalcalculator.core.feedback.domain.FeedbackRepository
import com.kemalurekli.electricalcalculator.core.feedback.domain.appBuild
import com.kemalurekli.electricalcalculator.core.feedback.presentation.ReportIssueViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * The report button's repository and the dialog behind it.
 *
 * [appBuild] is resolved once, at graph construction: it reads the package
 * manager on Android and the bundle on iOS, and neither answer changes while
 * the app is running.
 */
val feedbackModule: Module = module {
    single<FeedbackRepository> {
        FeedbackRepositoryImpl(
            backend = get(),
            preferences = get(),
            build = appBuild(),
            ioDispatcher = Dispatchers.Default,
        )
    }

    viewModelOf(::ReportIssueViewModel)
}
