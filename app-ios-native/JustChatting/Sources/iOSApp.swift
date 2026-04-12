import JCShared
import SwiftUI

@main
struct iOSApp: App {
    init() {
        #if DEBUG
            Logger.shared.logStrategy = AppleLogStrategy()
        #endif

        // Perform dependency injection
        SharedKoinKt.startSharedKoin { _ in }
    }

    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
