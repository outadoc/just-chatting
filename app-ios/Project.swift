import ProjectDescription

import ProjectDescription

let project = Project(
    name: "JustChatting",
    settings: .settings(base: [
        "ENABLE_USER_SCRIPT_SANDBOXING": "NO",
        "ASSETCATALOG_COMPILER_GENERATE_SWIFT_ASSET_SYMBOL_EXTENSIONS": "NO",
        "FRAMEWORK_SEARCH_PATHS": "$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)",
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
                    "LSMinimumSystemVersion": "15.0",
                    "ITSAppUsesNonExemptEncryption": false,
                ]
            ),
            sources: ["JustChatting/Sources/**"],
            resources: .resources(["JustChatting/Resources/**"]),
            scripts: [
                .pre(
                    script: """
                    "$SRCROOT/../gradlew" -p "$SRCROOT/../" :shared:embedAndSignAppleFrameworkForXcode
                    """,
                    name: "Generate shared framework",
                    basedOnDependencyAnalysis: false
                ),
                .pre(
                    script: """
                    "$SRCROOT/../gradlew" -p "$SRCROOT/../" :shared:copyFrameworkResourcesToApp \
                        -Pmoko.resources.PLATFORM_NAME="$PLATFORM_NAME" \
                        -Pmoko.resources.CONFIGURATION="$CONFIGURATION" \
                        -Pmoko.resources.ARCHS="$ARCHS" \
                        -Pmoko.resources.BUILT_PRODUCTS_DIR="$BUILT_PRODUCTS_DIR" \
                        -Pmoko.resources.CONTENTS_FOLDER_PATH="$CONTENTS_FOLDER_PATH"
                    """,
                    name: "Copy Moko resources",
                    basedOnDependencyAnalysis: false
                ),
            ],
            dependencies: [],
            settings: .settings(
                base: SettingsDictionary()
                    .automaticCodeSigning(devTeam: "C38RDC5QNT")
                    .otherLinkerFlags(["$(inherited)", "-lsqlite3"]),
                defaultSettings: .recommended
            )
        ),
    ]
)
