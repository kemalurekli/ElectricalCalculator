package com.kemalurekli.electricalcalculator.features.calculators.lighting.domain

/**
 * A validated set of lumen-method inputs.
 *
 * ### Why the utilisation factor is an input
 *
 * `UF` depends on the luminaire's photometry, the room's proportions and how
 * reflective its surfaces are. Every manufacturer publishes a table for their own
 * fitting, and no general table exists that is worth trusting — so the app asks
 * for it rather than inventing one. The notes give the range a reader can sanity-
 * check theirs against; the calculator does not pretend to know it.
 *
 * @param targetIlluminanceLux the design figure, `E`. EN 12464-1 tabulates these
 *   by task: 300 lx for general office work, 500 lx for reading and writing,
 *   750 lx for technical drawing.
 * @param roomLengthMetres,roomWidthMetres the working plane's dimensions.
 * @param mountingHeightMetres `Hm`, luminaire height *above the working plane* —
 *   not above the floor. A desk at 0,8 m under a fitting at 3,0 m gives 2,2 m,
 *   and using 3,0 is the most common way the room index comes out wrong.
 * @param luminousFluxPerLuminaireLumens `Φ`, the output of one complete fitting.
 * @param utilisationFactor `UF`, from the luminaire's own table.
 * @param maintenanceFactor `MF`, the light left after dirt and lamp ageing.
 *   0,8 is a common design value for a clean interior on a normal cleaning cycle.
 */
data class LightingInput(
    val targetIlluminanceLux: Double,
    val roomLengthMetres: Double,
    val roomWidthMetres: Double,
    val mountingHeightMetres: Double,
    val luminousFluxPerLuminaireLumens: Double,
    val utilisationFactor: Double,
    val maintenanceFactor: Double,
)

/**
 * The outcome of a lumen-method calculation.
 *
 * @param exactLuminaireCount the unrounded `N`, kept so the reader can see how
 *   close to a whole fitting the design landed. 4,05 and 4,95 both round to five
 *   and are very different conversations.
 * @param luminaireCount what to install: the exact figure rounded **up**, since
 *   rounding down is a design that misses its target.
 * @param achievedIlluminanceLux what the rounded count actually delivers.
 * @param roomIndex `K`, the shape of the room. Utilisation tables are indexed by
 *   it, so it is reported for looking `UF` up in the first place.
 * @param areaSquareMetres the working plane's area.
 * @param luminairesPerRowSuggestion a plausible grid: the two factors of the
 *   count closest to square, along the room's proportions. Null when the count
 *   is prime and no sensible grid exists.
 */
data class LightingResult(
    val exactLuminaireCount: Double,
    val luminaireCount: Int,
    val achievedIlluminanceLux: Double,
    val roomIndex: Double,
    val areaSquareMetres: Double,
    val luminairesPerRowSuggestion: Pair<Int, Int>?,
)
