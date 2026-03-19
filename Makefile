.PHONY: xcframework ios-generate ios-build android-build all

# Build KMP shared XCFramework for iOS
xcframework:
	./gradlew :shared:assembleSharedDebugXCFramework

# Generate iOS Xcode project (requires xcframework)
ios-generate: xcframework
	cd iosApp && tuist install && tuist generate --no-open

# Build iOS app
ios-build: ios-generate
	cd iosApp && xcodebuild -scheme Vuorinko -destination 'generic/platform=iOS Simulator' build

# Build Android app
android-build:
	./gradlew :androidApp:assembleDebug

# Build everything
all: android-build ios-build
