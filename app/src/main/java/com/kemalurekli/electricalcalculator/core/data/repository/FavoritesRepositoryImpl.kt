package com.kemalurekli.electricalcalculator.core.data.repository

import com.kemalurekli.electricalcalculator.core.common.di.IoDispatcher
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteItemDao
import com.kemalurekli.electricalcalculator.core.database.entity.FavoriteItemEntity
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteItem
import com.kemalurekli.electricalcalculator.core.domain.model.FavoriteKind
import com.kemalurekli.electricalcalculator.core.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val dao: FavoriteItemDao,
    private val timeProvider: TimeProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FavoritesRepository {

    override fun observeAll(): Flow<List<FavoriteItem>> =
        dao.observeAll()
            // A kind this build does not know is dropped rather than crashing —
            // the case after a downgrade, or once a shelf is retired.
            .map { rows -> rows.mapNotNull { row -> row.kindOrNull()?.let { FavoriteItem(it, row.key) } } }
            .distinctUntilChanged()

    override fun observeIsFavorite(item: FavoriteItem): Flow<Boolean> =
        dao.observeIsFavorite(item.kind.name, item.key).distinctUntilChanged()

    override suspend fun toggle(item: FavoriteItem): Boolean = withContext(ioDispatcher) {
        val nowFavorite = !dao.isFavorite(item.kind.name, item.key)
        write(item, nowFavorite)
        nowFavorite
    }

    override suspend fun setFavorite(item: FavoriteItem, isFavorite: Boolean) =
        withContext(ioDispatcher) { write(item, isFavorite) }

    private suspend fun write(item: FavoriteItem, isFavorite: Boolean) {
        if (isFavorite) {
            dao.insert(
                FavoriteItemEntity(
                    kind = item.kind.name,
                    key = item.key,
                    pinnedAtEpochMillis = timeProvider.now().toEpochMilli(),
                ),
            )
        } else {
            dao.delete(item.kind.name, item.key)
        }
    }

    private fun FavoriteItemEntity.kindOrNull(): FavoriteKind? =
        FavoriteKind.entries.firstOrNull { it.name == kind }
}
