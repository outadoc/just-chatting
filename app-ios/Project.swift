import ProjectDescription

import ProjectDescription

let project = Project(
    name: "JustChatting",
    settings: .settings(base: [
        "ENABLE_USER_SCRIPT_SANDBOXING": "NO",
        "ASSETCATALOG_COMPILER_GENERATE_SWIFT_ASSET_SYMBOL_EXTENSIONS": "NO",
        "FRAMEWORK_SEARCH_PATHS": "$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)",
        "SWIFT_VERSION": "6.0",
    ]),
    targets: [
        .target(
            name: "JustChatting",
            destinations: .iOS,
            product: .app,
            bundleId: "fr.outadoc.justchatting",
            deploymentTargets: .iOS("17.6"),
            infoPlist: .extendingDefault(
                with: [
                    "CFBundleDisplayName": "Just Chatting",
                    "CFBundleVersion": Plist.Value.string(
                        Environment.versionCode.getString(default: "1")
                    ),
                    "CFBundleShortVersionString": Plist.Value.string(
                        Environment.versionName.getString(default: "0.1.0")
                    ),
                    "CFBundleLocalizations": ["en", "fr"],
                    "CFBundleURLTypes": [
                        Plist.Value.dictionary(
                            [
                                "CFBundleTypeRole": "Editor",
                                "CFBundleURLName": "fr.outadoc.justchatting",
                                "CFBundleURLSchemes": ["justchatting"],
                            ]
                        ),
                    ],
                    "UILaunchStoryboardName": "Launch Screen",
                    "CADisableMinimumFrameDurationOnPhone": true,
                    "LSApplicationCategoryType": "public.app-category.social-networking",
                ]
            ),
            sources: ["JustChatting/Sources/**"],
            resources: .resources(["JustChatting/Resources/**"]),
            scripts: [
                .pre(
                    script: "cd \"$SRCROOT/..\" && ./gradlew :shared:embedAndSignAppleFrameworkForXcode",
                    name: "Generate shared framework",
                    basedOnDependencyAnalysis: false
                ),
            ],
            dependencies: [],
            settings: .settings(
                base: ["OTHER_LDFLAGS": "$(inherited) -lsqlite3"]
            )
        ),
    ]
)
