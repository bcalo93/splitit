# SplitIt Agent Notes

## Project Boundaries

- This is a Kotlin Multiplatform project with two Gradle modules: `:composeApp` (KMP shared library) and `:androidApp` (Android application). `:composeApp` targets Android (library), `iosArm64`, and `iosSimulatorArm64`.
- Put shared UI, domain, data, DI, and presentation changes under `composeApp/src/commonMain`; platform integrations belong in `composeApp/src/androidMain`, `composeApp/src/iosMain`, or `:androidApp` for Android-specific app wiring.
- Android starts Koin and Compose from `MainActivity` in `:androidApp`; iOS does the equivalent from `MainViewController` in `:composeApp`, hosted by the `iosApp` Xcode project.
- The main Compose navigation and screen wiring is in `composeApp/src/commonMain/kotlin/com/splitit/App.kt`; business behavior is organized into domain use cases/repositories and presentation view models.

## Commands

- Use the checked-in Gradle wrapper (`./gradlew`); it uses Gradle 9.7.0 and the project compiles Kotlin/Java for JVM 11.
- Build the Android debug app with `./gradlew :androidApp:assembleDebug`.
- Run the JVM-backed Android/common unit tests with `./gradlew :composeApp:testAndroidHostTest`.
- Run one test class with `./gradlew :composeApp:testAndroidHostTest --tests 'com.splitit.domain.service.BalanceCalculatorTest'`; replace the class name as needed.
- Run Android lint with `./gradlew :androidApp:lint :composeApp:lint`; use `./gradlew :androidApp:check :composeApp:check` for the full Gradle verification task.
- Run the iOS app from `iosApp/iosApp.xcodeproj` in Xcode; the Kotlin framework is embedded through the generated Xcode/Gradle integration rather than a separate root app task.

## Data And Tests

- SQLDelight schema and queries live in `composeApp/src/commonMain/sqldelight`; edit the `.sq` source, not generated files under `build/`.
- SQLDelight generates `SplitItDatabase` during the normal build. The explicit interface task is `./gradlew :composeApp:generateCommonMainSplitItDatabaseInterface`; migration verification is `./gradlew :composeApp:verifyCommonMainSplitItDatabaseMigration`.
- Shared tests are in `commonTest` and Android/JDBC SQLDelight repository tests are in `androidHostTest`; the latter uses an in-memory SQLite database and needs no external service.
- Android SDK settings are local (`local.properties` is ignored); do not commit that file or other generated/build output.

## Known Warnings

- SQLDelight `2.3.2` logs a JDK warning about `sun.misc.Unsafe::objectFieldOffset` when generating the database interface. This originates in the IntelliJ platform code bundled inside `app.cash.sqldelight:compiler-env` and cannot be suppressed on JDK 21. It will be silenced automatically when running on JDK 23+ with `--sun-misc-unsafe-memory-access=allow`.
