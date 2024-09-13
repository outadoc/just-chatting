import JCShared
import SwiftUI

@main
struct iOSApp: App {
    init() {
        // Perform dependency injection
        SharedKoinKt.startSharedKoin { _ in }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
