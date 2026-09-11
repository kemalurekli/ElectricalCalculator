package com.kemalurekli.electricalcalculator.core.vision

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSString
import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIRectFill
import platform.UIKit.drawInRect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The half of the iOS reader that a machine without a camera can prove.
 *
 * ### What this cannot test, and how that was established
 *
 * Not the recognition. **Vision's text recogniser returns nothing in the
 * simulator**, and it does so silently: given the 600 × 400 plate this test
 * draws — black bold text on white, the easiest input a recogniser will ever
 * see — `performRequests` returns true, the completion handler runs with a null
 * error, and the observation count is zero. Both `.accurate` and `.fast`
 * behave the same way, and the request will still tell you it supports
 * `en-US`. Nothing anywhere says the model is missing.
 *
 * That is the same trap the Android side already carries: ML Kit loads its
 * models on the emulator, throws nothing, and reads zero lines from clean text
 * that a real phone reads perfectly. Two platforms, two simulators, the same
 * false negative — so a green run here would have meant nothing at all, and a
 * red one would have sent somebody looking for a bug in code that is correct.
 *
 * ### What it does test
 *
 * Everything between the picture and the model: that a `UIImage` drawn by this
 * process yields a `CGImage`, that the request is built in a way Vision
 * accepts, that `performRequests` does not throw, and that the callback fires
 * exactly once. Those are this app's code. Break any of them and this fails,
 * on a laptop, in four seconds.
 *
 * The recogniser itself is verified by holding a phone in front of a motor,
 * and there is no substitute.
 */
@OptIn(ExperimentalForeignApi::class)
class NameplateRecogniseIosTest {

    @Test
    fun `a valid image goes through Vision and comes back exactly once`() {
        var calls = 0
        recognise(plate("400 V", "12.5 A")) { calls++ }

        // Once, not "at least once": the request is synchronous, so a second
        // callback would mean the handler had been attached twice — which on a
        // reader's phone is two nameplate readings racing to fill one form.
        assertEquals(1, calls)
    }

    @Test
    fun `an image with no CGImage is reported as nothing rather than thrown`() {
        var lines: List<String>? = null
        recognise(UIImage(), onLines = { lines = it })

        assertEquals(emptyList(), lines)
    }

    @Test
    fun `the drawn plate is a plate`() {
        // Guards the fixture rather than the feature. If this stops producing
        // an image with text in it, the test above starts passing for the wrong
        // reason — and the day the simulator learns to read, nobody would know
        // the picture had been blank all along.
        val image = plate("400 V")

        assertTrue(image.CGImage != null)
        assertEquals(WIDTH, image.size.useContents { width })
    }

    /** A white card with black text on it, which is what a plate is. */
    private fun plate(vararg lines: String): UIImage {
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(WIDTH, HEIGHT), opaque = true, scale = 1.0)

        UIColor.whiteColor.setFill()
        UIRectFill(CGRectMake(0.0, 0.0, WIDTH, HEIGHT))

        val attributes = mapOf<Any?, Any>(
            NSFontAttributeName to UIFont.boldSystemFontOfSize(FONT),
            NSForegroundColorAttributeName to UIColor.blackColor,
        )
        lines.forEachIndexed { index, line ->
            NSString.create(string = line).drawInRect(
                CGRectMake(MARGIN, MARGIN + index * LINE_HEIGHT, WIDTH - 2 * MARGIN, LINE_HEIGHT),
                attributes,
            )
        }

        val image = requireNotNull(UIGraphicsGetImageFromCurrentImageContext())
        UIGraphicsEndImageContext()
        return image
    }
}

private const val WIDTH = 600.0
private const val HEIGHT = 400.0
private const val MARGIN = 40.0
private const val FONT = 56.0
private const val LINE_HEIGHT = 90.0
