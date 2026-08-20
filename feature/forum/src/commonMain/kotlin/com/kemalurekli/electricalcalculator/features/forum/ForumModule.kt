package com.kemalurekli.electricalcalculator.features.forum

import com.kemalurekli.electricalcalculator.features.forum.data.ForumAuthRepositoryImpl
import com.kemalurekli.electricalcalculator.features.forum.data.ForumRepositoryImpl
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumAuthRepository
import com.kemalurekli.electricalcalculator.features.forum.domain.ForumRepository
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumAccountViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumCategoriesViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumComposeThreadViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumProfileViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadViewModel
import com.kemalurekli.electricalcalculator.features.forum.presentation.ForumThreadsViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * The forum's screens and the two repositories under them.
 *
 * `ForumBackend` is not registered here: whether there is a backend at all
 * depends on configuration the platform supplies, so the entry point declares
 * it. On Android that is the Hilt bridge; on iOS nothing declares it yet, which
 * is consistent with the Forum tab being absent there.
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
}
