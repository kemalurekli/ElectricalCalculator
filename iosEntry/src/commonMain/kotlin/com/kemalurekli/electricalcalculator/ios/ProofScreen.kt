package com.kemalurekli.electricalcalculator.ios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kemalurekli.electricalcalculator.core.common.util.NumberFormatter
import com.kemalurekli.electricalcalculator.core.common.util.NumberSymbols

/**
 * Proof that the pipeline works, and nothing more.
 *
 * There is a difference between "the shared code compiles for iOS" — which the
 * toolchain check already established — and "the shared code runs on iOS". This
 * screen is the second one. It calls [NumberFormatter], the module whose rewrite
 * was the riskiest part of the port, and shows what it produced.
 *
 * If these three lines read correctly on a simulator then Kotlin, Compose, the
 * Gradle-to-Xcode framework handoff and the shared module are all working, and
 * the remaining work is porting screens rather than proving the approach.
 *
 * It is deleted the moment the Converter lands here.
 */
@Composable
fun ProofScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "ElecToolkit", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Shared Kotlin, running on iOS",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Grouping, locale separators, significant-digit rounding and the
            // SI prefix table — every part of the rewrite, on screen.
            Text(
                text = NumberFormatter.format(1234.5, decimals = 2, symbols = NumberSymbols.Point),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = NumberFormatter.format(1234.5, decimals = 2, symbols = NumberSymbols.Comma),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = NumberFormatter.formatWithSiPrefix(
                    value = 1500.0,
                    unit = "W",
                    symbols = NumberSymbols.Point,
                ),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}
