package com.kemalurekli.electricalcalculator.core.vision

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import platform.darwin.NSObject

/**
 * The system camera, then Vision.
 *
 * Nothing is added to the app to make this work: `VNRecognizeTextRequest` is
 * part of the operating system and runs on the device, which is the same
 * bargain the Android side makes by bundling its model — a nameplate is read
 * in a plant room, and a recogniser that needs the network fails on the day it
 * is needed.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberNameplateScanner(onRead: (NameplateReading) -> Unit): NameplateScanner {
    val callback = rememberUpdatedState(onRead)

    // Held across recompositions on purpose. UIKit keeps only a weak reference
    // to a picker's delegate, so one created inline is deallocated before the
    // reader has finished framing the plate and the callback never arrives.
    val delegate = remember {
        PickerDelegate { image ->
            if (image == null) return@PickerDelegate
            recognise(image) { lines -> callback.value(NameplateReader.read(lines)) }
        }
    }

    return remember(delegate) {
        object : NameplateScanner {
            override val isSupported: Boolean =
                UIImagePickerController.isSourceTypeAvailable(
                    UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
                )

            override fun scan() {
                val presenter = UIApplication.sharedApplication.keyWindow?.rootViewController
                    ?: return
                val picker = UIImagePickerController().apply {
                    sourceType =
                        UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                    setDelegate(delegate)
                }
                presenter.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun recognise(image: UIImage, onLines: (List<String>) -> Unit) {
    val cgImage = image.CGImage ?: return onLines(emptyList())
    val request = VNRecognizeTextRequest { request, _: NSError? ->
        val observations = request?.results.orEmpty()
            .filterIsInstance<VNRecognizedTextObservation>()
        // One candidate per line, the best one. Vision will offer several and
        // ranking them again here would be second-guessing a model with more to
        // go on than a regular expression has.
        onLines(
            observations.mapNotNull { observation ->
                (observation.topCandidates(1u).firstOrNull() as? VNRecognizedText)?.string
            },
        )
    }
    // Accurate rather than fast: a plate is photographed once, deliberately,
    // and the reader is waiting for a number rather than for a viewfinder to
    // keep up.
    request.recognitionLevel = VNRequestTextRecognitionLevelAccurate
    request.usesLanguageCorrection = false

    val handler = VNImageRequestHandler(cGImage = cgImage, options = emptyMap<Any?, Any>())
    runCatching { handler.performRequests(listOf(request), null) }
        .onFailure { onLines(emptyList()) }
}

/**
 * UIKit's own callback shape, kept out of the composable.
 *
 * Cancelling is not a failure: the picker is dismissed and nothing is reported,
 * because a reader who changed their mind does not need to be told what they
 * just did.
 */
private class PickerDelegate(
    private val onImage: (UIImage?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        picker.dismissViewControllerAnimated(true) { onImage(image) }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
    }
}
