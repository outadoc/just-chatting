//
//  SettingsAboutView.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SettingsAboutView: View {
    let viewModel: SettingsViewModel

    @State private var showLogsCopied = false

    private static let repoUrl = URL(string: "https://github.com/outadoc/just-chatting")!
    private static let licenseUrl = URL(string: "https://www.gnu.org/licenses/agpl-3.0.en.html")!
    private static let xtraUrl = URL(string: "https://github.com/crackededed/Xtra")!

    var body: some View {
        Observing(viewModel.state) { state in
            Form {
                Section {
                    LabeledContent("Just Chatting") {
                        if let version = state.appVersionName {
                            Text("Version \(version)")
                        }
                    }
                }

                Section {
                    externalLink(
                        url: Self.repoUrl,
                        title: "Contribute to the project",
                        subtitle: "outadoc/just-chatting"
                    )
                    externalLink(
                        url: Self.licenseUrl,
                        title: "Licensing",
                        subtitle: "Just Chatting is provided under the terms of the GNU Affero General Public License v3.0."
                    )
                    externalLink(
                        url: Self.xtraUrl,
                        title: "Based on Xtra for Twitch",
                        subtitle: "Just Chatting is based on the Xtra for Twitch project."
                    )
                    NavigationLink("Open-source") {
                        SettingsDependenciesView()
                    }
                }

                Section {
                    Button {
                        viewModel.onExportLogsClick()
                    } label: {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Export logs")
                                .foregroundStyle(.primary)
                            Text("Export recent logs for debugging purposes.")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            }
            .navigationTitle("About")
            .navigationBarTitleDisplayMode(.inline)
        }
        .collect(flow: viewModel.events) { event in
            switch onEnum(of: event) {
            case .copyLogsToClipboard(let e):
                UIPasteboard.general.string = e.text
                showLogsCopied = true
            case .shareLogs(let e):
                if let url = URL(string: (e.uri as AnyObject).description) {
                    presentShareSheet(for: url)
                }
            case .navigateToDetail:
                break
            }
        }
        .alert("Logs copied to clipboard", isPresented: $showLogsCopied) {
            Button("OK", role: .cancel) {}
        }
    }

    private func externalLink(url: URL, title: LocalizedStringKey, subtitle: LocalizedStringKey) -> some View {
        Link(destination: url) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .foregroundStyle(.primary)
                    Text(subtitle)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Spacer()
                Image(systemName: "arrow.up.right.square")
                    .foregroundStyle(.secondary)
            }
        }
    }

    private func presentShareSheet(for url: URL) {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let root = scene.keyWindow?.rootViewController else { return }
        var presenter = root
        while let presented = presenter.presentedViewController {
            presenter = presented
        }
        let controller = UIActivityViewController(activityItems: [url], applicationActivities: nil)
        controller.popoverPresentationController?.sourceView = presenter.view
        presenter.present(controller, animated: true)
    }
}

private struct SettingsDependenciesView: View {
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
