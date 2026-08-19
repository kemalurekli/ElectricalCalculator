//
//  ContentView.swift
//  iosApp
//
//  The whole of the Swift side. Everything else the app does is shared Kotlin.
//

import SwiftUI
import UIKit
import ElecToolkitKit

/// Hosts the Compose hierarchy inside SwiftUI.
///
/// `MainViewController()` is the one function `:iosEntry` exposes — see
/// `iosEntry/src/iosMain/.../MainViewController.kt`. Wrapping a
/// `UIViewController` is all SwiftUI needs to display it, and it is why the
/// boundary between platforms is a single call rather than a parallel UI.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            // Compose draws its own insets, the way it does on Android. Letting
            // SwiftUI inset it as well would indent the whole app by a status
            // bar's height.
            .ignoresSafeArea(.all)
            // Compose handles the keyboard itself; without this, SwiftUI moves
            // the entire hierarchy up as well and the layout is pushed twice.
            .ignoresSafeArea(.keyboard)
    }
}
