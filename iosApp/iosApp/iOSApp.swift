import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinInitIosKt.koin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}