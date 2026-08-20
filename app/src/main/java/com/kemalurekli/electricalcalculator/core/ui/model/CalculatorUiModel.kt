package com.kemalurekli.electricalcalculator.core.ui.model

import com.kemalurekli.electricalcalculator.R
import com.kemalurekli.electricalcalculator.core.common.util.ResourceIdResolver
import com.kemalurekli.electricalcalculator.core.domain.catalog.SearchableCalculator
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorCategory
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorDescriptor
import com.kemalurekli.electricalcalculator.core.common.model.CalculatorIcon
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorId

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
    stringResolver: ResourceIdResolver,
    isFavorite: Boolean,
) = CalculatorUiModel(
    id = id,
    title = stringResolver.get(titleRes),
    description = stringResolver.get(descriptionRes),
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
fun List<CalculatorDescriptor>.toSearchable(stringResolver: ResourceIdResolver) =
    map { descriptor ->
        SearchableCalculator(
            descriptor = descriptor,
            title = stringResolver.get(descriptor.titleRes),
            description = stringResolver.get(descriptor.descriptionRes),
        )
    }

/** The localised section heading for a category. */
fun CalculatorCategory.titleRes(): Int = when (this) {
    CalculatorCategory.POWER_AND_LOAD -> R.string.category_power_and_load
    CalculatorCategory.PROTECTION -> R.string.category_protection
    CalculatorCategory.CABLE_AND_CONDUIT -> R.string.category_cable_and_conduit
    CalculatorCategory.MACHINES -> R.string.category_machines
    CalculatorCategory.ENERGY_STORAGE -> R.string.category_energy_storage
    CalculatorCategory.LIGHTING -> R.string.category_lighting
    CalculatorCategory.RENEWABLES -> R.string.category_renewables
}
