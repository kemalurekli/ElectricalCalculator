package com.kemalurekli.electricalcalculator.core.designsystem.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

@Composable
actual fun rememberResultSharing(): ResultSharing = remember { IosResultSharing() }

private class IosResultSharing : ResultSharing {

    /**
     * iOS shows nothing when something is copied, so the caller's own
     * confirmation is the only feedback there is — always true, where Android
     * says false from 13 onwards.
     */
    override fun copy(label: String, text: String): Boolean {
        UIPasteboard.generalPasteboard.string = text
        return true
    }

    /**
     * The share sheet is presented from whatever is on screen, found by walking
     * down from the root rather than held onto. Compose runs inside one
     * `UIViewController` for the life of the app, so a cached reference would
     * usually work — but not while a sheet or an alert is already up, and
     * presenting from a controller that is not visible does nothing at all.
     */
    override fun share(subject: String, text: String) {
        val presenter = topViewController() ?: return
        val controller = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        // Required on iPad: without a source the sheet has nowhere to point
        // from and UIKit raises rather than guessing.
        controller.popoverPresentationController?.sourceView = presenter.view
        presenter.presentViewController(controller, animated = true, completion = null)
    }

    private fun topViewController(): UIViewController? {
        var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (controller?.presentedViewController != null) {
            controller = controller.presentedViewController
        }
        return controller
    }
}
