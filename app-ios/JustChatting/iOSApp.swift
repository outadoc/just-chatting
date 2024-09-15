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
        let receiver = DeeplinkReceiverHelper().getInstance()
        WindowGroup {
            MainView()
                .onOpenURL { url in
                    receiver.onReceiveIntent(uri: url.absoluteString)
                }
        }
    }
}
