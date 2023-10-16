import JCShared
import SwiftUI
import Swinject

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
