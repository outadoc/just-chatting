import ProjectDescription

import ProjectDescription

let project = Project(
    name: "JustChatting",
    settings: .settings(base: [
        "FRAMEWORK_SEARCH_PATHS": "$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)",
    ]),
    targets: [
        .target(
            name: "JustChatting",
            destinations: .iOS,
            product: .app,
            bundleId: "fr.outadoc.justchatting",
            deploymentTargets: .iOS("17.0.0"),
            infoPlist: .extendingDefault(
                with: [
                    "CFBundleShortVersionString": "0.1.0",
                    "CFBundleVersion": "0.1.0",
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
                    "UILaunchStoryboardName": "Launch Screen.storyboard",
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
            dependencies: []
        ),
    ]
)
