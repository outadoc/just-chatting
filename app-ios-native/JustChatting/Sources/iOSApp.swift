import JCShared
import SwiftUI

@main
struct iOSApp: App {
    init() {
        // Perform dependency injection
        SharedKoinKt.startSharedKoin { _ in }

        #if DEBUG
            Logger.shared.logStrategy = CompositeLogStrategy(strategies: [AppleLogStrategy(), KoinHelper().getLogStrategy()])
        #else
            Logger.shared.logStrategy = KoinHelper().getLogStrategy()
        #endif
    }

    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
