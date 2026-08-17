package com.kemalurekli.electricalcalculator.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cable
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.ElectricalServices
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.EvStation
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.ViewStream
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.kemalurekli.electricalcalculator.core.domain.model.CalculatorIcon

/**
 * The app's icon set.
 *
 * Centralised so the visual language can be restyled in one place, and so the
 * domain layer can refer to icons by concept ([CalculatorIcon]) without
 * depending on Compose.
 */
object ElecIcons {

    // Navigation and chrome
    val Back = Icons.AutoMirrored.Outlined.ArrowBack
    val Search = Icons.Outlined.Search
    val Clear = Icons.Outlined.Close
    val Calculators = Icons.Outlined.Calculate
    val Converter = Icons.Outlined.SwapHoriz
    val References = Icons.AutoMirrored.Outlined.MenuBook
    val Glossary = Icons.Outlined.Translate
    val FieldNotes = Icons.Outlined.TipsAndUpdates
    val Theory = Icons.Outlined.Functions

    /** A job folder: the schedule of circuits, not a single calculation. */
    val Projects = Icons.Outlined.Folder

    /** People talking, not a speech bubble with a number on it. */
    val Forum = Icons.Outlined.Forum
    val History = Icons.Outlined.History
    val Settings = Icons.Outlined.Settings

    // Actions
    val Copy = Icons.Outlined.ContentCopy
    val Share = Icons.Outlined.Share
    val FavoriteOn = Icons.Filled.Star
    val FavoriteOff = Icons.Outlined.StarBorder

    // Editable lists, where a row can be appended or taken back out
    val Add = Icons.Outlined.Add
    val Lock = Icons.Outlined.Lock
    val ThanksFilled = Icons.Filled.ThumbUp
    val Thanks = Icons.Outlined.ThumbUp
    val Remove = Icons.Outlined.RemoveCircleOutline

    // Managing saved work: an overflow menu and the three things inside it
    val More = Icons.Outlined.MoreVert
    val Rename = Icons.Outlined.DriveFileRenameOutline
    val Delete = Icons.Outlined.DeleteOutline

    /** A design stage that was satisfied, and one that was not. */
    val StagePass = Icons.Outlined.CheckCircle
    val StageFail = Icons.Outlined.ErrorOutline

    /** Resolves the icon a calculator declares in the catalog. */
    fun forCalculator(icon: CalculatorIcon): ImageVector = when (icon) {
        CalculatorIcon.VOLTAGE_DROP -> Icons.AutoMirrored.Outlined.TrendingDown
        CalculatorIcon.CABLE -> Icons.Outlined.Cable
        CalculatorIcon.TRANSFORMER -> Icons.Outlined.ElectricalServices
        CalculatorIcon.MOTOR -> Icons.Outlined.Autorenew
        CalculatorIcon.POWER -> Icons.Outlined.Bolt
        CalculatorIcon.LIGHTING -> Icons.Outlined.Lightbulb
        CalculatorIcon.SOLAR -> Icons.Outlined.WbSunny
        CalculatorIcon.NEUTRAL -> Icons.Outlined.Balance
        CalculatorIcon.COST -> Icons.Outlined.Savings
        // Two devices in series, one above the other.
        CalculatorIcon.SELECTIVITY -> Icons.Outlined.AccountTree
        // A line sagging under load.
        CalculatorIcon.MOTOR_STARTING -> Icons.AutoMirrored.Outlined.TrendingDown
        // A waveform that is no longer a sine.
        CalculatorIcon.HARMONICS -> Icons.Outlined.GraphicEq
        CalculatorIcon.EVSE -> Icons.Outlined.EvStation
        CalculatorIcon.POWER_FACTOR -> Icons.Outlined.Insights
        CalculatorIcon.BATTERY -> Icons.Outlined.BatteryChargingFull
        CalculatorIcon.WEIGHT -> Icons.Outlined.Scale
        CalculatorIcon.CONDUIT -> Icons.Outlined.Adjust
        CalculatorIcon.TRAY -> Icons.Outlined.ViewStream
        CalculatorIcon.FAULT -> Icons.Outlined.OfflineBolt
        CalculatorIcon.EARTH -> Icons.Outlined.Shield
    }
}
