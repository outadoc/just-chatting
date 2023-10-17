import JCShared
import SwiftUI
import Swinject

@main
struct iOSApp: App {
    init() {
        // Perform dependency injection
        Container.shared.setup()

        #if DEBUG
            Logger.shared.logStrategy = AppleLogStrategy()
        #endif
    }

    var body: some Scene {
        WindowGroup {
            RootNavigationView()
        }
    }
}
