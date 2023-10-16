import SwiftUI
import Swinject

@main
struct iOSApp: App {

    init() {
        // Perform dependency injection
        Container.shared.inject()
    }

    var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
