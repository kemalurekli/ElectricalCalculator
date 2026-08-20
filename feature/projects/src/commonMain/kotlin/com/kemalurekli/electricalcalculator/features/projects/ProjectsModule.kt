package com.kemalurekli.electricalcalculator.features.projects

import com.kemalurekli.electricalcalculator.features.design.domain.DesignCircuitUseCase
import com.kemalurekli.electricalcalculator.features.inspection.data.InspectionRepositoryImpl
import com.kemalurekli.electricalcalculator.features.inspection.domain.EvaluateTestUseCase
import com.kemalurekli.electricalcalculator.features.inspection.domain.InspectionRepository
import com.kemalurekli.electricalcalculator.features.projects.presentation.CircuitViewModel
import com.kemalurekli.electricalcalculator.features.projects.presentation.ScheduleReportBuilder
import com.kemalurekli.electricalcalculator.features.projects.presentation.ProjectViewModel
import com.kemalurekli.electricalcalculator.features.projects.presentation.ProjectsViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Projects, the circuits under them, and the tests recorded against those.
 *
 * `InspectionRepository` is registered here rather than in `coreDataModule`
 * because its rows only mean anything to this feature — a circuit test is not
 * something any other screen reads.
 */
val projectsModule: Module = module {
    singleOf(::DesignCircuitUseCase)
    singleOf(::EvaluateTestUseCase)
    singleOf(::ScheduleReportBuilder)
    single<InspectionRepository> { InspectionRepositoryImpl(get(), Dispatchers.Default) }

    viewModelOf(::ProjectsViewModel)
    viewModelOf(::ProjectViewModel)
    viewModelOf(::CircuitViewModel)
}
