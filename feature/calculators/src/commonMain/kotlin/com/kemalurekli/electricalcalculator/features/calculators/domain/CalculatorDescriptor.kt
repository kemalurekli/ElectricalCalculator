package com.kemalurekli.electricalcalculator.features.calculators.domain

import org.jetbrains.compose.resources.StringResource
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon


/**
 * Everything the app knows about a calculator without running it.
 *
 * Drives the calculator list, home dashboard, search index and favourites, so
 * that adding a calculator is a single entry in [
 *   com.kemalurekli.electricalcalculator.core.domain.catalog.CalculatorCatalog
 * ] rather than an edit across several screens.
 *
 * @param title user-visible name, as a string resource for translation.
 * @param description one-line summary shown beneath the title.
 * @param searchKeywords extra terms matched during search, letting a user find
 *   "Voltage Drop" by typing "cable loss" or "IR".
 */
data class CalculatorDescriptor(
    val id: CalculatorId,
    val title: StringResource,
    val description: StringResource,
    val category: CalculatorCategory,
    val icon: CalculatorIcon,
    val searchKeywords: List<String> = emptyList(),
)
