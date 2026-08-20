package com.kemalurekli.electricalcalculator.features.calculators.domain

import org.jetbrains.compose.resources.StringResource
import com.kemalurekli.electricalcalculator.core.common.util.StringResolver
import com.kemalurekli.electricalcalculator.features.calculators.domain.SearchableCalculator
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.features.calculators.domain.CalculatorDescriptor
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.Res
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.category_cable_and_conduit
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.category_energy_storage
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.category_lighting
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.category_machines
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.category_power_and_load
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.category_protection
import com.kemalurekli.electricalcalculator.feature.calculators.generated.resources.category_renewables

/**
 * A calculator as the UI needs it: identity, resolved text, icon and pin state.
 *
 * Lives in `core.ui` rather than inside one feature because home, the calculator
 * list and favourites all render the same row. Keeping it here is what lets
 * those features stay independent of one another — a feature that needed to
 * import another feature's presentation package would be coupled to it.
 */
data class CalculatorUiModel(
    val id: CalculatorId,
    val title: String,
    val description: String,
    val icon: CalculatorIcon,
    val category: CalculatorCategory,
    val isFavorite: Boolean,
)

/** Resolves a catalog entry for display. */
fun CalculatorDescriptor.toUiModel(
    stringResolver: StringResolver,
    isFavorite: Boolean,
) = CalculatorUiModel(
    id = id,
    title = stringResolver.get(title),
    description = stringResolver.get(description),
    icon = icon,
    category = category,
    isFavorite = isFavorite,
)

/** Reuses text already resolved for search, avoiding a second lookup per row. */
fun SearchableCalculator.toUiModel(isFavorite: Boolean) = CalculatorUiModel(
    id = descriptor.id,
    title = title,
    description = description,
    icon = descriptor.icon,
    category = descriptor.category,
    isFavorite = isFavorite,
)

/** Builds the searchable view of the whole catalog for the active locale. */
fun List<CalculatorDescriptor>.toSearchable(stringResolver: StringResolver) =
    map { descriptor ->
        SearchableCalculator(
            descriptor = descriptor,
            title = stringResolver.get(descriptor.title),
            description = stringResolver.get(descriptor.description),
        )
    }

/** The localised section heading for a category. */
fun CalculatorCategory.title(): StringResource = when (this) {
    CalculatorCategory.POWER_AND_LOAD -> Res.string.category_power_and_load
    CalculatorCategory.PROTECTION -> Res.string.category_protection
    CalculatorCategory.CABLE_AND_CONDUIT -> Res.string.category_cable_and_conduit
    CalculatorCategory.MACHINES -> Res.string.category_machines
    CalculatorCategory.ENERGY_STORAGE -> Res.string.category_energy_storage
    CalculatorCategory.LIGHTING -> Res.string.category_lighting
    CalculatorCategory.RENEWABLES -> Res.string.category_renewables
}
