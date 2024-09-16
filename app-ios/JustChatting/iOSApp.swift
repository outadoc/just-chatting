import JCShared
import SwiftUI

@main
struct iOSApp: App {

    init() {
        // Perform dependency injection
        SharedKoinKt.startSharedKoin { _ in }

        #if DEBUG
            Logger.shared.logStrategy = AppleLogStrategy()
        #endif
    }

    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
