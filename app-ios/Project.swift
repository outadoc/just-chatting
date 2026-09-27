import ProjectDescription

/// Build settings of the app target. Signing is manual, with the certificates and profiles that
/// fastlane match installs.
func targetSettings(identity: String, provisioningProfile: String) -> SettingsDictionary {
    SettingsDictionary()
        .otherLinkerFlags(["$(inherited)", "-lsqlite3"])
        .manualCodeSigning(identity: identity, provisioningProfileSpecifier: provisioningProfile)
        .merging(["DEVELOPMENT_TEAM": "C38RDC5QNT"])
}

let project = Project(
    name: "JustChatting",
    settings: .settings(base: [
        "ENABLE_USER_SCRIPT_SANDBOXING": "NO",
        "ASSETCATALOG_COMPILER_GENERATE_SWIFT_ASSET_SYMBOL_EXTENSIONS": "NO",
        "FRAMEWORK_SEARCH_PATHS": "$(SRCROOT)/../shared-ui/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)",
    ]),
    targets: [
        .target(
            name: "JustChatting",
            destinations: .iOS,
            product: .app,
            productName: "JustChatting",
            bundleId: "fr.outadoc.justchatting",
            deploymentTargets: .iOS("26.0"),
            infoPlist: .extendingDefault(
                with: [
                    "CFBundleDisplayName": "Just Chatting",
                    "CFBundleLocalizations": ["en", "fr"],
                    // Set short version string, dynamically read from TUIST_VERSION_NAME
                    "CFBundleShortVersionString": Plist.Value.string(
                        Environment.versionName.getString(default: "0.1.0")
                    ),
                    // Set bundle version, dynamically read from TUIST_VERSION_CODE
                    "CFBundleVersion": Plist.Value.string(
                        Environment.versionCode.getString(default: "1")
                    ),
                    "CFBundleURLTypes": [
                        Plist.Value.dictionary(
                            [
                                "CFBundleTypeRole": "Editor",
                                "CFBundleURLName": "fr.outadoc.justchatting",
                                "CFBundleURLSchemes": ["justchatting"],
                            ]
                        ),
                    ],
                    // Uncap max frame rate on ProMotion devices for Compose
                    "CADisableMinimumFrameDurationOnPhone": true,
                    "ITSAppUsesNonExemptEncryption": false,
                    "LSApplicationCategoryType": "public.app-category.social-networking",
                    "UILaunchStoryboardName": "Launch Screen",
                ]
            ),
            sources: ["JustChatting/Sources/**"],
            resources: .resources(["JustChatting/Resources/**"]),
            scripts: [
                .pre(
                    script: """
                    "$SRCROOT/../gradlew" -p "$SRCROOT/../" :shared-ui:embedAndSignAppleFrameworkForXcode
                    """,
                    name: "Generate shared framework",
                    basedOnDependencyAnalysis: false
                ),
            ],
            dependencies: [],
            settings: .settings(
                configurations: [
                    .debug(
                        name: "Debug",
                        settings: targetSettings(
                            identity: "Apple Development",
                            provisioningProfile: "match Development fr.outadoc.justchatting"
                        )
                    ),
                    .release(
                        name: "Release",
                        settings: targetSettings(
                            identity: "Apple Distribution",
                            provisioningProfile: "match AppStore fr.outadoc.justchatting"
                        )
                    )
                ],
                defaultSettings: .recommended
            )
        )
    ]
)
