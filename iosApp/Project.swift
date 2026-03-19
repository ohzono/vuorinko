import ProjectDescription

// MARK: - Configuration

private let developmentTeam: String? = nil

private func codeSigningSettings() -> SettingsDictionary {
    var settings = SettingsDictionary()
    if let teamId = developmentTeam {
        settings = settings.automaticCodeSigning(devTeam: teamId)
    }
    return settings
}

// MARK: - KMP Framework

/// KMP shared framework path (built by Gradle)
private let sharedFrameworkPath = "../shared/build/bin/iosSimulatorArm64/releaseFramework"

// MARK: - Project

let project = Project(
    name: "Vuorinko",
    options: .options(
        automaticSchemesOptions: .enabled()
    ),
    targets: [
        // MARK: App Target
        .target(
            name: "Vuorinko",
            destinations: .iOS,
            product: .app,
            bundleId: "com.komakoma.vuorinko",
            deploymentTargets: .iOS("17.0"),
            infoPlist: .extendingDefault(with: [
                "UILaunchScreen": [:],
                "CFBundleDisplayName": "Vuorinko",
                "NSPhotoLibraryUsageDescription": "お子様に見せたい写真をアルバムに追加するために、フォトライブラリへのアクセスが必要です。",
                "NSFaceIDUsageDescription": "親モードへの復帰に生体認証を使用します。",
            ]),
            sources: ["App/Sources/**"],
            resources: ["App/Resources/**"],
            dependencies: [],
            settings: .settings(base: codeSigningSettings())
        ),

        // MARK: Unit Tests
        .target(
            name: "VuorinkoTests",
            destinations: .iOS,
            product: .unitTests,
            bundleId: "com.komakoma.vuorinko.tests",
            deploymentTargets: .iOS("17.0"),
            sources: ["Tests/AppTests/**"],
            dependencies: [
                .target(name: "Vuorinko"),
            ],
            settings: .settings(base: codeSigningSettings())
        ),
    ]
)
