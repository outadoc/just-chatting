//
//  SettingsSectionDependencies.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SettingsSectionDependencies: View {
    @State private var dependencies: [Dependency]?

    var body: some View {
        Group {
            if let dependencies {
                List(dependencies, id: \.moduleName) { dependency in
                    row(dependency)
                }
            } else {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .navigationTitle("Open-source")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            dependencies = (try? await KoinHelper().getReadExternalDependenciesList().invoke()) ?? []
        }
    }

    @ViewBuilder
    private func row(_ dependency: Dependency) -> some View {
        let content = VStack(alignment: .leading, spacing: 2) {
            Text(dependency.moduleName)
                .font(.callout)
                .foregroundStyle(.primary)
            let details = [dependency.moduleVersion, dependency.moduleLicense].compactMap { $0 }
            if !details.isEmpty {
                Text(details.joined(separator: " · "))
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }

        if let urlString = dependency.moduleUrl ?? dependency.moduleLicenseUrl,
           let url = URL(string: urlString) {
            Link(destination: url) { content }
                .accessibilityHint("Shows website")
        } else {
            content
        }
    }
}
