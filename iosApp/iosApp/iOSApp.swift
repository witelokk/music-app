import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        IosMediaDownloadBridgeRegistry.shared.register(bridge: NativeMediaDownloadBridge.shared)
        KoinInitIosKt.koin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
