import SwiftUI
import Swinject
import JCShared

@main
struct iOSApp: App {
    init() {
        // Perform dependency injection
        Container.shared.setup()
    }

    var body: some Scene {
        WindowGroup {
            HomeView(
                viewModel: Container.shared.resolve(MainRouterViewModel.self)!
            )
        }
    }
}
