package com.kemalurekli.electricalcalculator.core.data.repository

import com.kemalurekli.electricalcalculator.core.common.di.IoDispatcher
import com.kemalurekli.electricalcalculator.core.common.util.TimeProvider
import com.kemalurekli.electricalcalculator.core.database.dao.FavoriteCalculatorDao
import com.kemalurekli.electricalcalculator.core.database.entity.FavoriteCalculatorEntity
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
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
    private val dao: FavoriteCalculatorDao,
    private val timeProvider: TimeProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<CalculatorId>> =
        dao.observeAll()
            // Unknown keys are skipped rather than crashing: they occur after a
            // downgrade or once a calculator is retired from the catalog.
            .map { entities -> entities.mapNotNull { CalculatorId.fromKeyOrNull(it.calculatorId) } }
            .distinctUntilChanged()

    override fun observeIsFavorite(calculatorId: CalculatorId): Flow<Boolean> =
        dao.observeIsFavorite(calculatorId.key).distinctUntilChanged()

    override suspend fun toggle(calculatorId: CalculatorId): Boolean =
        withContext(ioDispatcher) {
            val nowFavorite = !dao.isFavorite(calculatorId.key)
            writeFavorite(calculatorId, nowFavorite)
            nowFavorite
        }

    override suspend fun setFavorite(calculatorId: CalculatorId, isFavorite: Boolean) =
        withContext(ioDispatcher) {
            writeFavorite(calculatorId, isFavorite)
        }

    private suspend fun writeFavorite(calculatorId: CalculatorId, isFavorite: Boolean) {
        if (isFavorite) {
            dao.insert(
                FavoriteCalculatorEntity(
                    calculatorId = calculatorId.key,
                    pinnedAtEpochMillis = timeProvider.now().toEpochMilli(),
                ),
            )
        } else {
            dao.deleteById(calculatorId.key)
        }
    }
}
