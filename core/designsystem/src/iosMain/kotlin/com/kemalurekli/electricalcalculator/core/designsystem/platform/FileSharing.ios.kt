package com.kemalurekli.electricalcalculator.core.designsystem.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

@Composable
actual fun rememberFileSharing(): FileSharing = remember { IosFileSharing() }

private class IosFileSharing : FileSharing {

    /**
     * iOS shares a file by URL rather than by content, so the bytes go to the
     * temporary directory first. Nothing cleans up after: the system empties
     * that directory on its own schedule, and deleting the file while the share
     * sheet still holds it is how an attachment arrives empty.
     *
     * `chooserTitle` is unused. UIKit titles the sheet from the item, and
     * setting a title on a `UIActivityViewController` has done nothing since
     * iOS 13 — the parameter stays because Android needs it.
     */
    @OptIn(ExperimentalForeignApi::class)
    override fun share(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        chooserTitle: String,
    ): Boolean {
        val url = NSURL.fileURLWithPath(NSTemporaryDirectory() + fileName)
        val data = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
        if (!data.writeToURL(url, atomically = true)) return false

        val presenter = topViewController() ?: return false
        val controller = UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null,
        )
        controller.popoverPresentationController?.sourceView = presenter.view
        presenter.presentViewController(controller, animated = true, completion = null)
        return true
    }

    private fun topViewController(): UIViewController? {
        var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (controller?.presentedViewController != null) {
            controller = controller.presentedViewController
        }
        return controller
    }
}
