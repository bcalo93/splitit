# SplitIt

SplitIt is a Kotlin Multiplatform mobile app for managing shared expenses among groups of people — trips, dinners, roommates, or any situation where money needs to be split fairly.

Built with **Compose Multiplatform**, it shares UI, domain logic, persistence, and navigation across **Android** and **iOS** while keeping each platform's entry point minimal.

---

## 📖 Documentation

- **[`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md)** — complete architecture overview: layers, package responsibilities, patterns, design decisions, and testing strategy.
- **[`docs/DESIGN.md`](./docs/DESIGN.md)** — visual design system and per-screen specifications ("Cuentas claras" concept).
- **[`AGENTS.md`](./AGENTS.md)** — project conventions, build commands, and agent-specific notes.

---

## 📸 Screenshots

> _App screenshots will be added here._

| Groups                            | Group Detail                      | Expenses                              | Settlement                                |
|-----------------------------------|-----------------------------------|---------------------------------------|-------------------------------------------|
| ![Groups](docs/assets/groups.png) | ![Detail](docs/assets/detail.png) | ![Expenses](docs/assets/expenses.png) | ![Settlement](docs/assets/settlement.png) |

---

## 🏗️ Project Structure

* [`/composeApp`](./composeApp/src) is for code shared across Compose Multiplatform applications.
  * [`commonMain`](./composeApp/src/commonMain/kotlin) is for code common to all targets.
  * [`androidMain`](./composeApp/src/androidMain/kotlin) and [`iosMain`](./composeApp/src/iosMain/kotlin) hold platform-specific integrations such as the SQLDelight driver.
* [`/androidApp`](./androidApp/src/main/kotlin) contains the Android application entry point (`MainActivity`).
* [`/iosApp`](./iosApp/iosApp) contains the iOS SwiftUI host and the bridge to the Kotlin/Native `MainViewController`.

---

## 🚀 Build and Run

### Android Application

Build and run the development version from your IDE or directly from the terminal:

```shell
./gradlew :composeApp:assembleDebug
```

### iOS Application

Open the [`/iosApp`](./iosApp) directory in Xcode and run it from there, or use the run configuration in your IDE.

---

## 🧪 Running Tests

```shell
# Run JVM-backed unit tests
./gradlew :composeApp:testAndroidHostTest

# Run a specific test class
./gradlew :composeApp:testAndroidHostTest --tests 'com.splitit.domain.service.BalanceCalculatorTest'

# Run lint
./gradlew :androidApp:lint :composeApp:lint

# Full verification
./gradlew :androidApp:check :composeApp:check
```

---

## 📦 Release

SplitIt uses **SemVer** (`MAJOR.MINOR.PATCH`) with annotated git tags `vX.Y.Z`. Each release is built and signed by a GitHub Actions workflow (`.github/workflows/release.yml`) and published as a GitHub Release with the signed APK attached. The version comes from the tag, so no version-bump commit is required (and `main` stays protected).

### Signing setup

Release APKs are signed with a personal keystore, never committed to the repo.

- **Local builds**: create `keystore.properties` at the repo root (git-ignored):

  ```properties
  storeFile=splitit-release.jks
  storePassword=<store password>
  keyAlias=<key alias>
  keyPassword=<key password>
  ```

  `storeFile` is resolved relative to the repo root, so place the keystore at the repo root and use the path above.

- **CI builds**: the following GitHub Actions secrets must be set on the repository:
  - `KEYSTORE_BASE64` — `base64 < splitit-release.jks | pbpaste` (the encoded keystore).
  - `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

### Releasing

Both paths produce the same result: a tagged GitHub Release with the signed APK.

**A. Push a tag**

```shell
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

The workflow builds, signs and publishes the APK to the release `v1.0.0`.

**B. Create a release from the web UI**

Go to GitHub → *Releases* → *Draft a new release*, set the tag to `v1.0.0`, and publish. The workflow builds, signs and attaches the APK to that release.

### Installing on your device

Download the APK from the release page and install it, or use ADB:

```shell
adb install app-release.apk
```

### Installing on an emulator (step by step)

1. **Build the signed release APK locally** (requires `keystore.properties`, see [Signing setup](#signing-setup)):

   ```shell
   ./gradlew :androidApp:assembleRelease
   ```

   The signed APK is produced at `androidApp/build/outputs/apk/release/androidApp-release.apk`.

2. **Start an emulator.** List your available AVDs and launch one:

   ```shell
   emulator -list-avds          # e.g. Medium_Phone_API_36.1
   emulator -avd <AVD_NAME> &   # replace with your AVD name
   ```

   Or start it from Android Studio's Device Manager.

3. **Verify the emulator is running**:

   ```shell
   adb devices
   # List of devices attached
   # emulator-5554   device
   ```

4. **Install the APK**:

   ```shell
   adb install androidApp/build/outputs/apk/release/androidApp-release.apk
   ```

   If you get `INSTALL_FAILED_UPDATE_INCOMPATIBLE: Existing package com.splitit signatures do not match`, a previous build is signed with a different key (e.g. the debug keystore). Uninstall it first and retry:

   ```shell
   adb uninstall com.splitit
   adb install androidApp/build/outputs/apk/release/androidApp-release.apk
   ```

5. **Launch the app**:

   ```shell
   adb shell monkey -p com.splitit -c android.intent.category.LAUNCHER 1
   ```

> **Note:** debug (`assembleDebug`) and release builds use different signing keys. Alternating between them on the same emulator/device requires `adb uninstall com.splitit` before installing the other flavor.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
