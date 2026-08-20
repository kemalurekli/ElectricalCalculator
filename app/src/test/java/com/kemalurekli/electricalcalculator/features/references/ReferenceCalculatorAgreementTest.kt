package com.kemalurekli.electricalcalculator.features.references

import com.kemalurekli.electricalcalculator.features.calculators.earthfault.domain.ProtectiveDeviceType
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceCatalog
import com.kemalurekli.electricalcalculator.features.references.domain.ReferenceText
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Where the reference tables and the calculators must agree.
 *
 * Left in `:app` when the rest of `ReferenceCatalogTest` moved to
 * `:feature:references`: it reads a constant out of the earth fault
 * calculator, and that has not moved yet. The third of these — see
 * `GlossaryOutboundLinkTest` and `FieldNoteOutboundLinkTest` — and the last, if
 * the calculators are next.
 */
class ReferenceCalculatorAgreementTest {

    @Test
    fun `the breaker curve multipliers agree with the earth fault calculator`() {
        // The reference and the calculator must not disagree about what a
        // Type C does. The upper bound of each band is what the calculator uses.
        val topic = requireNotNull(ReferenceCatalog.topicOrNull("breaker_curves"))
        val upperBounds = topic.sections.first().rows
            .map { (it.value as ReferenceText.Symbol).text }
            .map { it.substringAfter("– ").removeSuffix(" × In").trim().toDouble() }

        assertEquals(
            listOf(
                ProtectiveDeviceType.MCB_TYPE_B.instantaneousMultiplier,
                ProtectiveDeviceType.MCB_TYPE_C.instantaneousMultiplier,
                ProtectiveDeviceType.MCB_TYPE_D.instantaneousMultiplier,
            ),
            upperBounds,
        )
    }
}
