# SplitIt Implementation Blueprint

This document is the technical implementation blueprint for SplitIt.

The project is a Kotlin Multiplatform offline-first personal expense splitting application for Android and iOS. The existing payment optimization module is considered complete and must be treated as an external dependency. It should not be redesigned or replaced.

Current stack:

- Kotlin 2.2.20
- Compose Multiplatform 1.9
- Koin 4.1
- Android SDK 36
- Android minSdk 24

Hard constraints:

- No backend.
- No authentication.
- No cloud synchronization in the initial implementation.
- All persistence must use native device storage through Kotlin Multiplatform abstractions.
- Business logic must remain platform independent.
- Architecture should allow future cloud synchronization without major refactoring.

## 1. High-Level Architecture

Use Clean Architecture with shared business logic in `commonMain`.

Layers:

| Layer | Responsibility |
| --- | --- |
| UI | Compose screens, Material 3 components, navigation host |
| Presentation | ViewModels, screen state, user intents, validation messages |
| Domain | Entities, value objects, use cases, repository interfaces, settlement calculation |
| Data | SQLDelight database, query wrappers, repository implementations, mappers |
| Platform | Android/iOS database driver factories and app bootstrap |

Dependency direction:

```text
UI -> Presentation -> Domain <- Data
                       ^
                       |
                Optimizer adapter
```

The optimizer must remain isolated behind a domain-facing adapter:

```text
Expenses
  -> BalanceCalculator
  -> RawDebt list
  -> PaymentOptimizerAdapter
  -> existing Optimizer<Payment>
  -> SettlementTransfer list
```

This avoids coupling the app domain to optimizer-specific implementation details, including mutable `Payment` / `Participant` classes, `Int` amounts, and current equality behavior.

Recommended use cases:

- `CreateSessionUseCase`
- `UpdateSessionUseCase`
- `DeleteSessionUseCase`
- `AddParticipantUseCase`
- `UpdateParticipantUseCase`
- `RemoveParticipantUseCase`
- `CreateExpenseUseCase`
- `UpdateExpenseUseCase`
- `DeleteExpenseUseCase`
- `CalculateSessionBalancesUseCase`
- `GenerateSettlementUseCase`
- `ObserveSessionsUseCase`
- `ObserveSessionDetailsUseCase`

Future sync readiness should come from repository abstractions, stable IDs, timestamps, soft-delete metadata where appropriate, and avoiding UI/data coupling.

## 2. Domain Model

Domain models should be immutable and platform independent.

Recommended IDs:

- `SessionId`
- `ParticipantId`
- `ExpenseId`
- `SettlementId`
- `TransferId`

Use UUID strings for persistence and future synchronization compatibility.

Domain entities:

| Entity | Fields |
| --- | --- |
| `ExpenseSession` | `id`, `title`, `description?`, `createdAt`, `updatedAt`, `participantIds`, `expenseIds`, `status` |
| `Participant` | `id`, `sessionId`, `name`, `avatarColor?`, `createdAt`, `updatedAt` |
| `Expense` | `id`, `sessionId`, `title`, `amount`, `payerId`, `participantShares`, `date`, `note?`, `createdAt`, `updatedAt` |
| `ExpenseParticipantShare` | `expenseId`, `participantId`, `shareWeight` or `shareAmount` |
| `Balance` | `participantId`, `amountOwedOrReceivable` |
| `Debt` | `fromParticipantId`, `toParticipantId`, `amount` |
| `Settlement` | `id`, `sessionId`, `generatedAt`, `sourceRevision`, `transfers` |
| `SettlementTransfer` | `id`, `settlementId`, `fromParticipantId`, `toParticipantId`, `amount` |

Money should be represented as a value object backed by minor units:

```text
Money(minorUnits: Long, currencyCode: String)
```

Do not use `Double` or `Float` for money. They introduce rounding errors.

The optimizer currently uses `Int` amounts. The optimizer adapter must validate that converted debt amounts fit in `Int` until the optimizer contract changes.

Expense split model:

| Option | Pros | Cons |
| --- | --- | --- |
| Equal split only | Simple MVP | Harder to extend |
| Share weights | Supports equal and weighted splits | Needs deterministic rounding |
| Explicit share amounts | Precise | More input complexity |

Recommendation: store `shareWeight`, defaulting to `1`. This supports equal splits now and weighted/custom splits later.

For partial participation, only selected participants should share the expense. Example:

```text
Trip participants:
- Alice
- Bob
- Charlie

Dinner:
- payer: Alice
- involved participants: Alice, Charlie

Bob does not participate in this expense.
```

## 3. Data Model

Use normalized relational storage.

Tables:

| Table | Purpose |
| --- | --- |
| `sessions` | Session metadata |
| `participants` | Participants belonging to sessions |
| `expenses` | Expense header data |
| `expense_participants` | Many-to-many expense participant inclusion and split weights |
| `settlements` | Generated settlement snapshots |
| `settlement_transfers` | Optimized settlement transfers |
| `settings` | Local app preferences |
| `outbox_events` | Future sync-ready local change log, optional in MVP |

Suggested schema:

```text
sessions(
  id TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  deleted_at INTEGER NULL
)

participants(
  id TEXT PRIMARY KEY,
  session_id TEXT NOT NULL,
  name TEXT NOT NULL,
  avatar_color TEXT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  deleted_at INTEGER NULL,
  FOREIGN KEY(session_id) REFERENCES sessions(id)
)

expenses(
  id TEXT PRIMARY KEY,
  session_id TEXT NOT NULL,
  title TEXT NOT NULL,
  amount_minor INTEGER NOT NULL,
  currency_code TEXT NOT NULL,
  payer_participant_id TEXT NOT NULL,
  date INTEGER NOT NULL,
  note TEXT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  deleted_at INTEGER NULL,
  FOREIGN KEY(session_id) REFERENCES sessions(id),
  FOREIGN KEY(payer_participant_id) REFERENCES participants(id)
)

expense_participants(
  expense_id TEXT NOT NULL,
  participant_id TEXT NOT NULL,
  share_weight INTEGER NOT NULL,
  PRIMARY KEY(expense_id, participant_id),
  FOREIGN KEY(expense_id) REFERENCES expenses(id),
  FOREIGN KEY(participant_id) REFERENCES participants(id)
)

settlements(
  id TEXT PRIMARY KEY,
  session_id TEXT NOT NULL,
  generated_at INTEGER NOT NULL,
  source_revision INTEGER NOT NULL,
  FOREIGN KEY(session_id) REFERENCES sessions(id)
)

settlement_transfers(
  id TEXT PRIMARY KEY,
  settlement_id TEXT NOT NULL,
  from_participant_id TEXT NOT NULL,
  to_participant_id TEXT NOT NULL,
  amount_minor INTEGER NOT NULL,
  currency_code TEXT NOT NULL,
  FOREIGN KEY(settlement_id) REFERENCES settlements(id)
)
```

Recommended indexes:

```text
participants(session_id)
expenses(session_id, date)
expense_participants(expense_id)
expense_participants(participant_id)
settlements(session_id, generated_at)
settlement_transfers(settlement_id)
```

Persistence rules:

- Store timestamps as epoch milliseconds.
- Store money as integer minor units.
- Use transactions for expense writes that also update participant shares.
- Recalculate balances from expenses.
- Persist generated settlement snapshots.
- Include `deleted_at` if future sync remains a priority. If permanent offline-only behavior becomes a product decision, hard delete is simpler.

## 4. UI Flow

Primary flow:

```text
Home / Session List
  -> Create Session
  -> Session Details
      -> Participants
      -> Expenses
          -> Create/Edit Expense
      -> Settlement
  -> Settings
```

Session lifecycle:

```text
Create session
Add participants
Add expenses
Review balances
Generate settlement
View optimized transfers
```

Settlement should be explicitly generated or regenerated. The app should not silently overwrite past settlements without showing that expenses changed after the latest settlement.

Important UI states:

- Empty session list.
- Empty participant list.
- Empty expense list.
- Invalid expense because payer, amount, or selected participants are missing.
- Settlement stale because expenses or participants changed.
- Settlement unavailable because there are fewer than two participants or no expenses.

## 5. Screen List

| Screen | Purpose | Interactions | State | ViewModel responsibilities |
| --- | --- | --- | --- | --- |
| Home / Session List | Show all sessions and recent activity | Create, open, delete/archive session | Sessions, loading, empty state | Observe sessions, delete/archive session, expose summaries |
| Create/Edit Session | Create or edit title/description | Save, cancel | Form fields, validation | Validate input, call create/update use case |
| Session Details | Dashboard for one session | Navigate to participants, expenses, settlement | Session, participants summary, expense total, settlement status | Observe aggregate session details |
| Participant Management | Add/edit/remove participants | Add participant, edit name/color, remove | Participant list, validation, deletion warnings | Manage participants and prevent unsafe deletion |
| Expense List | Show expenses for session | Create, edit, delete, filter/sort | Expense list, total, selected sort | Observe expenses, delete expense |
| Expense Create/Edit | Capture expense | Set title, amount, payer, participants, date, note | Form state, participants, validation | Validate payer/participants/amount, save expense |
| Settlement Screen | Display balances and optimized transfers | Generate/regenerate settlement | Balances, transfers, stale flag | Calculate balances, call optimizer adapter, persist settlement |
| Settings | Local preferences | Set default currency, theme | Settings state | Observe/update settings |

## 6. Navigation Proposal

Recommendation: use official Compose Multiplatform Navigation Compose.

Comparison:

| Option | Pros | Cons | Decision |
| --- | --- | --- | --- |
| Navigation Compose | Official, familiar, works with Compose Multiplatform, simple stack-based app | Requires route discipline and serialization setup | Recommended |
| Decompose | Strong lifecycle/navigation model, excellent for complex shared navigation | Heavier architecture than this app needs | Not needed initially |
| Voyager | Simple Compose-first navigation, Koin support | Third-party lifecycle/navigation abstraction | Valid but less preferable than official API |

Recommended routes:

```text
SessionListRoute
SessionDetailsRoute(sessionId)
ParticipantsRoute(sessionId)
ExpenseListRoute(sessionId)
ExpenseEditRoute(sessionId, expenseId?)
SettlementRoute(sessionId)
SettingsRoute
```

Navigation ownership should remain in UI. ViewModels should emit events such as `ExpenseSaved`; they should not call `NavController` directly.

## 7. Persistence Proposal

Recommendation: SQLDelight.

SQLDelight is the best fit because the app has relational data, needs Android/iOS support, benefits from explicit migrations, and should keep persistence behavior testable and transparent.

Comparison:

| Option | Pros | Cons |
| --- | --- | --- |
| SQLDelight | Mature KMP story, schema-first, type-safe SQL, clear migrations, good for relational joins | Requires writing SQL manually |
| Room KMP | Official AndroidX API, familiar Android model, KMP support | Requires KSP per target, more annotation-generated machinery, Android-shaped API |
| DataStore / Settings | Good for preferences | Not suitable for relational sessions/expenses |
| Realm | Object database, sync heritage | Adds heavier runtime/model constraints for a simple offline app |

Room KMP is valid, but SQLDelight is preferable here because this project has a compact relational schema, no existing Room investment, and generated SQL APIs make repository tests and migrations straightforward.

Repository interfaces should live in `domain`. SQLDelight classes, query adapters, and mappers should live in `data`.

## 8. Folder / Module Structure

Recommended initial structure:

```text
SplitIt/
  settings.gradle.kts
  build.gradle.kts
  gradle/libs.versions.toml

  composeApp/
    src/
      commonMain/
        kotlin/com/example/splitit/
          App.kt
          di/
            AppModules.kt
            DataModule.kt
            DomainModule.kt
            PresentationModule.kt
          navigation/
            AppNavHost.kt
            Routes.kt
          ui/
            theme/
            components/
            screens/
              sessions/
              sessiondetail/
              participants/
              expenses/
              settlement/
              settings/
          presentation/
            sessions/
            sessiondetail/
            participants/
            expenses/
            settlement/
            settings/
          domain/
            model/
            value/
            repository/
            usecase/
            service/
              BalanceCalculator.kt
              SettlementGenerator.kt
            optimizer/
              PaymentOptimizerAdapter.kt
          data/
            database/
              SplitItDatabase.sq
              DatabaseDriverFactory.kt
            mapper/
            repository/
            local/
          logic/
            optimizers/
              existing optimizer module, unchanged
        sqldelight/com/example/splitit/data/database/
          Session.sq
          Participant.sq
          Expense.sq
          Settlement.sq
          Settings.sq

      androidMain/
        kotlin/com/example/splitit/
          MainActivity.kt
          data/database/DatabaseDriverFactory.android.kt

      iosMain/
        kotlin/com/example/splitit/
          MainViewController.kt
          data/database/DatabaseDriverFactory.ios.kt

      commonTest/
        kotlin/com/example/splitit/
          domain/
          data/
          presentation/
          testutil/
```

If the project grows, split into Gradle modules later:

```text
:core:domain
:core:data
:core:optimizer
:shared:presentation
:composeApp
```

Do not start with heavy Gradle modularization before the data and use-case boundaries stabilize. Package-level Clean Architecture is enough for the first implementation phases.

## 9. Roadmap

### Phase 1: Architecture Foundation

Objective: establish clean package boundaries and dependency contracts.

Deliverables:

- Domain models and value objects.
- Repository interfaces.
- Use-case contracts.
- Optimizer adapter design.
- Koin module layout.

Dependencies:

- Existing optimizer API.

Estimated complexity: medium.

Acceptance criteria:

- No UI depends directly on data storage.
- No domain code depends on Compose, SQLDelight, Android, or iOS.
- Optimizer remains unchanged.

### Phase 2: Persistence Foundation

Objective: add durable offline storage.

Deliverables:

- SQLDelight dependency setup.
- Database schema and migrations.
- Android/iOS driver factories.
- Repository implementations.
- Mapper layer.

Dependencies:

- Phase 1.

Estimated complexity: medium-high.

Acceptance criteria:

- Sessions, participants, expenses, and settlements persist after app restart.
- Repository tests can run against a test database where supported.
- Multi-table writes are transactional.

### Phase 3: Session List and Session Creation

Objective: make sessions usable end to end.

Deliverables:

- Session list screen.
- Create/edit session screen.
- Session repository integration.
- Basic navigation.

Dependencies:

- Phase 2.

Estimated complexity: medium.

Acceptance criteria:

- User can create, edit, open, and delete/archive sessions.
- Empty and populated states work.
- State survives configuration changes.

### Phase 4: Participant Management

Objective: manage session participants.

Deliverables:

- Participant list/edit UI.
- Add/edit/remove participant use cases.
- Color/avatar assignment.

Dependencies:

- Phase 3.

Estimated complexity: medium.

Acceptance criteria:

- Participants are persisted per session.
- Participant removal is blocked or clearly handled when used by expenses.
- Duplicate names are either allowed with distinct IDs or rejected by explicit product rule.

Recommendation: allow duplicate display names because IDs are authoritative.

### Phase 5: Expense Management

Objective: create and manage expenses.

Deliverables:

- Expense list.
- Expense create/edit form.
- Payer selection.
- Participant inclusion selection.
- Equal split calculation.

Dependencies:

- Phase 4.

Estimated complexity: high.

Acceptance criteria:

- Expense can involve any subset of session participants.
- Payer must belong to the session.
- Amount must be positive.
- Expense participants must be non-empty.
- Expense persists with participant shares.

### Phase 6: Balance Calculation

Objective: convert expenses into raw debts.

Deliverables:

- Balance calculator service.
- Deterministic rounding strategy.
- Domain unit tests for split scenarios.

Dependencies:

- Phase 5.

Estimated complexity: high.

Acceptance criteria:

- Partial expense participation works correctly.
- Sum of balances equals zero.
- Rounding never creates or loses minor units.
- Tests cover payer included and payer not included edge cases.

Open product decision:

- Should the payer be automatically included in participants involved, or can they pay for others without sharing?

Recommendation:

- Allow both, but default the payer as selected in the UI.

### Phase 7: Settlement Generation

Objective: integrate the completed optimizer safely.

Deliverables:

- `PaymentOptimizerAdapter`.
- `GenerateSettlementUseCase`.
- Settlement persistence.
- Settlement screen.

Dependencies:

- Phase 6.

Estimated complexity: medium-high.

Acceptance criteria:

- Raw debts are passed to the existing optimizer.
- Optimized transfers are displayed and persisted.
- Settlement is marked stale when source expenses or participants change.
- Existing optimizer tests remain untouched and passing.

### Phase 8: Settings

Objective: add local app preferences.

Deliverables:

- Settings screen.
- Default currency.
- Theme preference.
- Optional settlement display preferences.

Dependencies:

- Phase 2.

Estimated complexity: low-medium.

Acceptance criteria:

- Settings persist locally.
- Currency is applied to new sessions/expenses.
- Existing expenses preserve their stored currency.

### Phase 9: Test Hardening

Objective: make core behavior regression-resistant.

Deliverables:

- Domain use-case tests.
- Repository tests.
- ViewModel tests.
- Test fixtures/builders.

Dependencies:

- Phases 1 through 8.

Estimated complexity: medium.

Acceptance criteria:

- Balance and settlement behavior has broad unit coverage.
- Repository tests cover CRUD and transaction boundaries.
- ViewModel tests cover state transitions and validation.

### Phase 10: UX and Performance Pass

Objective: prepare for hundreds of sessions and smooth usage.

Deliverables:

- Lazy lists.
- Stable UI state models.
- Search/filter for sessions and expenses if needed.
- Recomposition review.
- Loading/error polish.

Dependencies:

- Functional phases.

Estimated complexity: medium.

Acceptance criteria:

- Lists remain responsive with hundreds of sessions and thousands of expenses.
- No obvious unnecessary recomposition hotspots.
- UI handles empty, error, and loading states consistently.

## 10. Technical Risks

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Existing optimizer uses mutable `Payment` / `Participant` and `Int` amount | Domain pollution, overflow risk | Isolate behind adapter and validate ranges |
| Current `Participant.equals` uses referential string equality | Incorrect equality behavior | Do not reuse as app domain participant |
| Money rounding | Incorrect balances | Use minor units and deterministic remainder distribution |
| Participant deletion after expenses | Broken historical data | Block deletion, soft-delete, or allow inactive participants |
| Generated settlements becoming stale | User confusion | Store source revision and show stale status |
| SQL migrations | Data loss risk | Version schema from first persistence phase and test migrations |
| iOS database driver setup | Platform-specific bugs | Keep driver factory tiny and covered by smoke tests |
| Future cloud sync | Refactor risk | Stable IDs, timestamps, soft delete, repository abstraction |

## 11. Future Improvements

- Custom split amounts and percentages.
- Multiple currencies per session with exchange-rate snapshots.
- Export/share settlement summary.
- Local backup/restore.
- Search and filters.
- Recurring expenses.
- Attach receipt images using local file storage.
- Optional device-to-device sync.
- Cloud sync through repository composition: `LocalDataSource + RemoteDataSource + SyncCoordinator`.
- Audit/history screen showing session changes.
- Accessibility and large-screen layout refinements.

## Testing Strategy

Domain unit tests should cover:

- Money arithmetic and rounding.
- Expense split calculation.
- Balance calculation.
- Settlement generation before and after optimization.
- Edge cases with partial participants.
- Validation rules.

Repository tests should cover:

- Session CRUD.
- Participant CRUD.
- Expense CRUD with participant shares.
- Settlement persistence.
- Transaction rollback behavior.
- Query ordering and filtering.

ViewModel tests should cover:

- Initial state.
- Loading, empty, and error states.
- Form validation.
- Save/delete events.
- Settlement stale state.

Avoid over-testing:

- Compose layout internals.
- Exact visual styling in early phases.
- Third-party library behavior.
- The completed optimizer internals, beyond existing tests.

Focus UI tests later on critical user flows once the UX stabilizes.
