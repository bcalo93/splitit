# SplitIt Agent Notes

## Start Here

Before exploring the codebase, read the project documentation. It is kept up-to-date with the architecture and design decisions and will save you from scanning the entire project on every session:

- **[`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md)** — full architecture overview: layers, package responsibilities, patterns, and design decisions. Use this to understand where a change belongs.
- **[`docs/DESIGN.md`](./docs/DESIGN.md)** — visual design system ("Cuentas claras"), per-screen specs, component inventory, motion, and copy rules. Use this before changing UI.
- **[`README.md`](./README.md)** — project summary, documentation links, build/test commands, and screenshot placeholders.

Use these documents as the source of truth. Only dive into specific source files once the docs have oriented you.

## Documentation Maintenance

When you modify anything covered by the docs, keep the documentation consistent with the code:

- Update `docs/ARCHITECTURE.md` if you add/remove packages, change layer responsibilities, introduce new patterns, alter the navigation graph, or modify the DI wiring.
- Update `docs/DESIGN.md` if you change the design system, add/remove screens, modify components, or change user-facing copy/flow.
- Update `README.md` if you change build/test commands, project structure, or have new screenshots to add.
- Prefer editing existing docs over duplicating information. Keep summaries concise and factual.

## Project Boundaries

This is a Kotlin Multiplatform project with two Gradle modules: `:composeApp` (KMP shared library) and `:androidApp` (Android application). `:composeApp` targets Android (library), `iosArm64`, and `iosSimulatorArm64`.

- Put shared UI, domain, data, DI, and presentation changes under `composeApp/src/commonMain`.
- Put platform integrations in `composeApp/src/androidMain`, `composeApp/src/iosMain`, or `:androidApp`.
- See `docs/ARCHITECTURE.md` for the full layer breakdown, package responsibilities, navigation graph, and DI wiring.

## Design System

The visual system and component inventory are fully documented in `docs/DESIGN.md`. Prefer the existing theme tokens and reusable components in `ui/theme/` and `ui/components/` over ad-hoc styling.

## Naming: Groups

- **Visible copy** (UI strings) uses "Groups"/"Grupos" vocabulary. All user-facing strings live in `composeApp/src/commonMain/composeResources/values/strings.xml` (EN) and `values-es/strings.xml` (ES); every new string must be added to both.
- The domain and data layer use the "Group" vocabulary (`ExpenseGroup`, `GroupId`, `GroupDetails`, the SQLDelight `groups` table, and route classes `Groups`/`GroupDetails`/`GroupForm`). Legacy "Session" names have been removed from the codebase; do not reintroduce them.

## UseCase abstraction

Todos los casos de uso implementan `UseCase<in P, out R>` (`composeApp/src/commonMain/kotlin/com/splitit/domain/usecase/UseCase.kt`) con un único `suspend operator fun invoke(params: P): R`. La entrada es un DTO `data class` (o `Unit` para los sin args, p.ej. `ObserveGroupsParams`). Cada caso de uso vive con su DTO en el mismo archivo (`GroupUseCases.kt`, `ExpenseUseCases.kt`, etc.).

- En `domainModule` (Koin) el binding es mixto: `ObserveGroupDetailsUseCase` se inyecta por interfaz `UseCase<ObserveGroupDetailsParams, GroupDetails>` (consumido por 6 ViewModels); los demás, por clase concreta.
- Los ViewModels de `presentation/*/` invocan `useCase(XxxParams(...))` en lugar de `useCase(arg1, arg2, ...)`.
- Los tests de ViewModel viven en `composeApp/src/androidHostTest/kotlin/...` (no en `commonTest`) porque **MockK es JVM-only** — agregarlo a `commonTest` rompe la compilación iOS-Native. La dependencia se declara en `named("androidHostTest") { dependencies { implementation(libs.mockk) } }`. Los tests usan `coEvery { useCase.invoke(any()) } returns ...` y `coVerify { ... }`. Los tests de use case unitarios (en `commonTest`) siguen usando `InMemoryRepositories`.

## Commands

- Use the checked-in Gradle wrapper (`./gradlew`); it uses Gradle 9.7.0 and the project compiles Kotlin/Java for JVM 11.
- Build the Android debug app with `./gradlew :androidApp:assembleDebug`.
- Run the JVM-backed Android/common unit tests with `./gradlew :composeApp:testAndroidHostTest`.
- Run one test class with `./gradlew :composeApp:testAndroidHostTest --tests 'com.splitit.domain.service.BalanceCalculatorTest'`; replace the class name as needed.
- Run Android lint with `./gradlew :androidApp:lint :composeApp:lint`; use `./gradlew :androidApp:check :composeApp:check` for the full Gradle verification task.
- Run the iOS app from `iosApp/iosApp.xcodeproj` in Xcode; the Kotlin framework is embedded through the generated Xcode/Gradle integration rather than a separate root app task.

## Data And Tests

- SQLDelight schema and queries live in `composeApp/src/commonMain/sqldelight`; edit the `.sq` source, not generated files under `build/`.
- See `docs/ARCHITECTURE.md` for the testing strategy (`commonTest` vs `androidHostTest`) and the data layer overview.
- Useful SQLDelight tasks:
  - Generate interface: `./gradlew :composeApp:generateCommonMainSplitItDatabaseInterface`
  - Verify migrations: `./gradlew :composeApp:verifyCommonMainSplitItDatabaseMigration`
  - Regenerate baseline snapshot: `./gradlew :composeApp:generateCommonMainSplitItDatabaseSchema`
- Migrations live in `composeApp/src/commonMain/sqldelight/migrations/*.sqm`. The baseline schema is version 1 and already uses the `groups` table.
- Android SDK settings are local (`local.properties` is ignored); do not commit that file or other generated/build output.

## Known Warnings

- SQLDelight `2.3.2` logs a JDK warning about `sun.misc.Unsafe::objectFieldOffset` when generating the database interface. This originates in the IntelliJ platform code bundled inside `app.cash.sqldelight:compiler-env` and cannot be suppressed on JDK 21. It will be silenced automatically when running on JDK 23+ with `--sun-misc-unsafe-memory-access=allow`.
