//
//  SettingsSectionAbout.swift
//  JustChatting
//

import JCShared
import SwiftUI

struct SettingsSectionAbout: View {
    let viewModel: SettingsViewModel

    @State private var showLogsCopied = false
    @State private var sharedLogs: SharedLogs?

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
                        SettingsSectionDependencies()
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
                sharedLogs = URL(string: (e.uri as AnyObject).description).map(SharedLogs.init)
            case .navigateToDetail:
                break
            }
        }
        .alert("Logs copied to clipboard", isPresented: $showLogsCopied) {
            Button("OK", role: .cancel) {}
        }
        .sheet(item: $sharedLogs) { logs in
            ActivityView(items: [logs.url])
                .presentationDetents([.medium, .large])
        }
    }

    private func externalLink(url: URL, title: LocalizedStringKey, subtitle: LocalizedStringKey) -> some View {
        ExternalLink(destination: url) {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                Text(subtitle)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
    }
}

/// Exported log file waiting to be shared.
private struct SharedLogs: Identifiable {
    let url: URL
    var id: URL { url }
}
