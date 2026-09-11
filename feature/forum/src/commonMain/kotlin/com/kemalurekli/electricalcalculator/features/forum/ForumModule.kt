package com.kemalurekli.electricalcalculator.features.forum

import com.kemalurekli.electricalcalculator.features.forum.data.ForumAuthRepositoryImpl
import com.kemalurekli.electricalcalculator.features.forum.data.ForumRepositoryImpl
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumAccountViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumBlockedViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumCategoriesViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumComposeThreadViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumProfileViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumRulesViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumSessionViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadsViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * The forum's screens and the two repositories under them.
 *
 * The client is not built here any more. It moved to `:core:backend` when the
 * report button arrived, because a report has to be filed by the same session
 * that says who the reader is — see `AppBackend`.
 */
val forumModule: Module = module {
    single<ForumRepository> { ForumRepositoryImpl(get(), Dispatchers.Default) }
    single<ForumAuthRepository> { ForumAuthRepositoryImpl(get(), Dispatchers.Default) }

    viewModelOf(::ForumCategoriesViewModel)
    viewModelOf(::ForumThreadsViewModel)
    viewModelOf(::ForumThreadViewModel)
    viewModelOf(::ForumComposeThreadViewModel)
    viewModelOf(::ForumProfileViewModel)
    viewModelOf(::ForumAccountViewModel)
    viewModelOf(::ForumSessionViewModel)
    viewModelOf(::ForumBlockedViewModel)
    viewModelOf(::ForumRulesViewModel)
}
