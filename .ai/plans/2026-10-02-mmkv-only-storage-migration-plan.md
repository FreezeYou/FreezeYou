# MMKV key value storage consolidation plan

| Field | Value |
|---|---|
| Created | 2026-10-02 |
| Updated | 2026-10-03 |
| Application | FreezeYou Android app |
| Scope | Consolidate app-managed key-value storage and counters; remove Tray |
| Target storage | MMKV for key-value data and counters; Android SQLite for tasks and categories |
| Current MMKV dependency | `com.tencent:mmkv:2.4.1` |
| Compatibility requirement | Preserve existing installations and supported backups |
| Priority | Long-term correctness, maintainability, and measured performance over minimizing initial effort |
| Counter migration | Required implementation phase with correctness and performance release gates |
| Status | Planned implementation |

## Decision

Make MMKV the standard backend for app-managed key-value data and counters, and the app's only third-party storage SDK. Migrate Tray, SharedPreferences, and the three statistics databases through a staged implementation that preserves existing values, identifiers, settings behavior, and backup compatibility. Keep Android SQLite for scheduled tasks, trigger tasks, and custom categories.

Normal key-value reads and writes will use MMKV repositories, with one central `StatisticsStore` owning counter reads, increments, resets, snapshots, and imports. SQLite reads and writes remain valid for the three retained task and category databases. Isolated, read-only legacy readers for Tray, SharedPreferences, and the old statistics databases will support upgrades from older installations. Icons, APKs, logs, and exported backup files remain ordinary files. Existing transient authentication state stays in MMKV shared memory.

This scope removes overlapping preference stores and their synchronization logic. Retaining SQLite avoids rebuilding task and category queries, ID allocation, and database transaction behavior in the MMKV layer. Android provides SQLite as a platform facility, so retaining it does not add a bundled third-party storage SDK. See the [Android SQLite API documentation](https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase).

Accept the initial engineering effort needed to make the counter implementation reliable and maintainable. Counter migration is part of completion, not optional follow-up work. Expected performance benefits remain unmeasured for FreezeYou; correctness tests and representative device benchmarks must pass before release.

This document describes implementation work; it does not record completed code changes or successful runtime tests.

## Current storage inventory

| Storage | Existing responsibilities | Migration outcome |
|---|---|---|
| MMKV 2.4.1 | Settings, average operation timing, and transient authentication state | Retain existing identifiers and extend the storage layer |
| Tray 0.12.0 | One-key lists, URI and installer allowlists, notification state, folder registry, delayed-task tracking, and remaining preference reads | Move callers to MMKV and remove the bundled SDK |
| SharedPreferences | Settings, UI state, version information, package labels, task-editor state, and shortcut-folder preferences | Move persistent values and observation to MMKV |
| SQLite | Scheduled tasks, trigger tasks, and custom categories | Retain existing databases, records, IDs, and queries |
| SQLite statistics | Freeze/unfreeze/usage counters | Migrate through `StatisticsStore` to MMKV in Phase 5 |

Three app-owned SQLite databases remain active after this migration; three become legacy counter migration sources:

| Database | Decision |
|---|---|
| `scheduledTasks` | Keep SQLite for scheduled task records |
| `scheduledTriggerTasks` | Keep SQLite for trigger task records |
| `userDefinedCategories` | Keep SQLite for custom categories |
| `ApplicationsFreezeTimes` | Migrate freeze counters to MMKV; retain legacy data until verified cleanup |
| `ApplicationsUFreezeTimes` | Migrate unfreeze counters to MMKV; retain legacy data until verified cleanup |
| `ApplicationsUseTimes` | Migrate usage counters to MMKV; retain legacy data until verified cleanup |

Key entry points are `app/build.gradle`, `MainApplication.kt`, `Main.kt`, `storage/mmkv/`, `storage/key/`, `SettingsUtils.kt`, `TasksUtils.kt`, `OneKeyListUtils.kt`, `DataStatisticsUtils.kt`, and `BackupUtils.kt` under `app/src/main/java/cf/playhi/freezeyou/` where applicable. Counter callers also include `AccessibilityService.kt` and `ui/fragment/settings/SettingsManageSpaceFragment.kt`. Changes in mixed-storage helpers must preserve SQL behavior for task and category records.

## Phase 1 Complete the MMKV storage layer

1. Extend the existing wrappers with the types and operations required by the key-value inventory: booleans, strings, integers, longs, collections or encoded values, key removal, key enumeration, and existence checks.
2. Check MMKV write results and expose failures to callers instead of treating every write as successful.
3. Define repositories and store boundaries for settings, lists and allowlists, folders, notification state, delayed-task tracking, statistics, caches, and migration metadata. Task definitions and categories remain in SQLite. Preserve the existing `DefaultMultiProcessKV` and `AverageTimeCostsKV` identifiers unless a separately tested migration requires a change.
4. Use consistent multiprocess configuration across the main, `:backgroundService`, and `:installAndUninstall` processes.
5. Protect read-modify-write operations, including counter increments, list edits, folder membership changes, and delayed-task tracking updates, against concurrent threads and processes. Define a consistent update and recovery strategy where related key-value changes must be committed together.
6. Preserve existing value encodings where practical. Version any necessary changes to structured key-value payloads and use the existing platform JSON facilities where appropriate.
7. Provide explicit change observation for callers and a refresh strategy for changes made by another process.

MMKV provides multiprocess stores and supported value types in its [Android tutorial](https://github.com/Tencent/MMKV/wiki/android_tutorial). Its [2.4.1 implementation](https://github.com/Tencent/MMKV/blob/v2.4.1/Android/MMKV/mmkv/src/main/java/com/tencent/mmkv/MMKV.java) exposes explicit interprocess locking, but does not implement SharedPreferences change listeners. Cross-process notifications also require an access or explicit change check; they must not be treated as automatic push updates.

Exit gate: the repositories can represent all key-value datasets in scope and preserve required concurrency and observation behavior.

## Phase 2 Build a migration that can resume

1. Replace the migration chain in `MainApplication` with a versioned coordinator. Initialize MMKV in each process and gate feature access until the required datasets are ready. Keep bulk migration work off the UI thread.
2. Inventory legacy keys, defaults, Tray and statistics database schemas, encodings, and historical migration markers. Define source precedence per key when MMKV, Tray, and SharedPreferences contain overlapping values. Do not blindly overwrite existing MMKV values or mistake a stored default for a missing key. Use a separate completion record for counter migration so preference migration alone cannot mark statistics ready.
3. Read legacy sources without modifying them. Include named preferences, historical one-key list formats, dynamically named folder preferences, the bundled Tray database, and the three statistics databases. Exclude `scheduledTasks`, `scheduledTriggerTasks`, and `userDefinedCategories` from migration and cleanup.
4. Coordinate migration ownership across threads and processes with an actual lock. A marker file's existence alone is not a concurrency mechanism.
5. Stage each key-value dataset, check writes, synchronize it, and verify values, entry counts, folder identifiers, and references before recording completion. For statistics, compare every package and counter type against its source value. Make every step safe to retry after process death.
6. Keep a failed or partially migrated dataset unavailable to normal writers until recovery completes. Report migration failure instead of continuing with empty defaults.
7. Retain original Tray, SharedPreferences, and statistics data until migration has been verified. Make cleanup a separate step with an explicit allowlist of migrated stores; it must preserve the three active task and category databases and prevent completed data from being imported again.
8. Support direct upgrades from older releases with isolated legacy readers, including read-only readers for Tray and statistics databases. Validate the Tray reader against the bundled `tray-0.12.0.aar` schema and representative installation fixtures before removing the SDK.

Users must not need to install an intermediate release to preserve their data. Retaining legacy data supports recovery; it does not automatically make downgrades safe after new MMKV writes.

Exit gate: fresh installs, direct upgrades, overlapping legacy values, and interrupted migrations produce the expected MMKV state without data loss or duplicate imports, while retained SQLite data remains intact.

## Phase 3 Migrate settings and SharedPreferences

1. Convert the SharedPreferences key classes to MMKV while preserving persisted key names, types, defaults, and XML key mappings.
2. Migrate default and named preferences, including `Ver`, `NameOfPackages`, task-editor state, and each shortcut folder's preferences. Task-editor preferences move to MMKV while saved task records stay in SQLite. Preserve folder UUIDs used by existing launcher shortcuts.
3. Attach an MMKV-backed `androidx.preference.PreferenceDataStore` before preference XML is loaded in every relevant settings, first-time setup, and task-editor screen. This is the existing Preference library's custom-backend interface, not a new Jetpack DataStore storage dependency. See [Android's custom preference storage documentation](https://developer.android.com/develop/ui/views/components/settings/use-saved-values).
4. Replace SharedPreferences listeners with repository observation and appropriate UI callbacks. Refresh folder and settings screens after external changes or lifecycle transitions.
5. Remove the SharedPreferences-to-MMKV copying logic in `SettingsUtils.kt`. All settings writes, including imports and programmatic changes, must use the same repository path.
6. Preserve validation, permission checks, authentication handling, language and theme refreshes, icon component changes, and service start/stop behavior. Apply side effects at the correct point relative to a successful write.
7. Update startup and receiver reads to use the same authoritative settings as the UI, including the screen-lock freeze service setting.

Exit gate: preference screens, background components, and backup imports agree on settings without duplicate persistence or lost notifications.

## Phase 4 Migrate Tray key value data

1. Move one-key lists, allowlists, notification state, the folder registry, and delayed-task tracking to the MMKV repositories. Preserve ordering, encoded allowlist payloads, and custom-list references.
2. Replace Tray access in `TasksUtils.kt`, services, receivers, the main screen, list management, URI handling, installer handling, and notification helpers. Keep SQL access to task definitions and categories in those same components.
3. Verify that migrated lists and preferences still resolve custom categories stored in `userDefinedCategories`. Preserve the existing encoded references used by list expansion and task execution.
4. Preserve folder UUIDs and the relationship between the migrated folder registry and folder preferences so existing launcher shortcuts continue to work.
5. Preserve delayed-task request codes and cancellation tracking. Verify existing scheduled and trigger tasks remain executable, editable, and cancellable without changing their SQLite IDs, payloads, or scheduling behavior.
6. Preserve the current statistics behavior as a baseline for Phase 5, including initialization, sorting, display, and resets. Keep counter conversion in its dedicated phase so its effects can be measured independently.

Exit gate: migrated lists, folders, allowlists, notifications, and delayed-task tracking behave correctly across process restarts and reboot, and their integration with SQLite tasks and categories remains intact.

## Phase 5 Migrate counters through StatisticsStore

### Centralize the counter API

1. Introduce one `StatisticsStore` API for reads, increments, snapshots for sorting, resets by counter type, and imports. All callers must use this API; direct access to MMKV counter keys is internal to the store.
2. Store each package and counter type as an individual numeric value in a dedicated MMKV statistics namespace configured with `MULTI_PROCESS_MODE`. Use explicit, stable key encoding and schema metadata. Avoid rewriting a serialized map for every increment.
3. Define the numeric type, missing-key behavior, overflow handling, and write-failure behavior. Preserve migrated values and existing first-event counting semantics; any change to counting behavior requires its own documented decision and tests.
4. Replace statistics database access in `DataStatisticsUtils.kt`, the main screen's count-map loaders, and reset controls. Route usage, freeze, and unfreeze events through the store while preserving caller behavior and sort ordering.
5. Reuse store instances within each process and keep potentially blocking storage work off the main thread. Preserve the defined ordering of increments, resets, and imports when dispatching work.

### Coordinate threads and processes

1. Give the store a shared in-process lock and use MMKV's interprocess lock around the entire read-increment-write operation. Acquire them in a fixed order and release them in `finally` blocks. `MULTI_PROCESS_MODE` alone does not make a sequence of reads and writes atomic.
2. Make every writer, including resets, imports, and migration, follow the same coordination rules. Define snapshot consistency and use the appropriate locking for readers that need a coherent view of several counters.
3. Keep critical sections short. Do not suspend, invoke UI callbacks, or execute unrelated work while holding the locks. Keep statistics contention separate from settings storage.
4. Define interruption-safe reset and import behavior. A lock prevents interleaving but does not provide a transaction across multiple keys after a crash. Use a staged snapshot or generation commit where needed, and verify that readers see a complete previous or new state.
5. Document the durability policy and what a successful increment guarantees. Match it in tests and performance comparisons; do not claim a speedup obtained only by weakening persistence guarantees.

MMKV's [2.4.1 native implementation](https://github.com/Tencent/MMKV/blob/v2.4.1/Core/MMKV.cpp) exposes an interprocess lock whose thread lock is scoped to the lock call. The repository must also coordinate same-process threads across the whole application-level operation.

### Preserve counters during migration

1. Add read-only migration for `ApplicationsFreezeTimes`, `ApplicationsUFreezeTimes`, and `ApplicationsUseTimes`, including their package-name encoding and `TimesList` values. Handle malformed or duplicate source records explicitly instead of silently dropping or double-counting them.
2. Gate counter access across every process while taking and importing the source snapshot. Prevent legacy counter writes once migration starts; do not dual-write SQLite and MMKV.
3. Stage imported counters, check write results, and compare every value and package/type mapping before publishing the MMKV state and recording completion. Test interruptions before and after each commit boundary.
4. After MMKV becomes authoritative, retries must preserve subsequent increments and resets. Do not fall back to stale SQLite counts or import them again on the next launch.
5. Retain source databases until validation and backup checks pass. Restrict cleanup to these verified legacy sources and preserve the task and category databases.

### Measure and verify before release

Capture a reproducible baseline before replacing the counter paths. Compare the current SQLite implementation, a small optimized SQLite benchmark reference, and the MMKV candidate on the same devices and datasets with equivalent durability and correctness requirements. The reference can reuse connections, index package lookups, and perform atomic SQL increments; it is a benchmark reference, not an additional production backend.

Measure cold initialization, warm increments, bursts from multiple threads and processes, loading all counts for sorting, and resets. Record typical and high-percentile latency, contention, and relevant memory or storage costs. Include a representative supported device and a lower-performance configuration. Separate one-time migration cost from steady-state operation.

Set the performance acceptance criteria before evaluating results. Require no lost completed increments in concurrency tests, correct reset/import ordering, safe recovery after process termination, and no material regression in representative counter or sorting workloads. Verify restart behavior against the documented durability contract and distinguish process termination from abrupt device power loss. Investigate regressions before release; a shorter implementation or an isolated microbenchmark win does not satisfy the quality goal.

Exit gate: `StatisticsStore` owns all counter access, migrated counts and existing behavior are preserved, concurrency and recovery tests pass, and measured results meet the agreed performance criteria. This gate is required for plan completion.

## Phase 6 Preserve backup and restore compatibility

1. Update Tray and SharedPreferences paths in `BackupUtils.kt` and the backup import chooser to use MMKV repositories. Retain the existing SQLite paths for task and category backup data.
2. Preserve the existing public JSON backup format and supported legacy imports. Keep current inclusion, exclusion, and validation behavior, including authentication-related handling.
3. Verify export/import round trips for settings, one-key lists, categories, scheduled tasks, and allowlists. Reconcile alarms and required settings side effects after import.
4. Review Android backup rules for persistent MMKV data, including statistics and companion metadata, migration state, and the retained SQLite task and category databases. Preserve backup coverage for migrated counters and retained records. Ensure a restored completion marker cannot cause missing MMKV data to be treated as successfully migrated.
5. Test a legacy Android backup containing the old statistics databases and a new backup containing MMKV counters. Restore counter data through `StatisticsStore` and the migration coordinator so restoring or retrying cannot revive stale counts over newer state. Keep the current public JSON backup coverage; adding previously omitted statistics to that format is a separate format change.
6. Keep transient authentication state and disposable caches out of durable backup coverage as appropriate. Verify that restored settings, counters, lists, tasks, and categories remain consistent across the supported backup mechanisms.

Exit gate: supported old backups import correctly and new backups restore equivalent application behavior.

## Phase 7 Remove legacy dependencies and verify

1. Remove `app/libs/tray-0.12.0.aar`, its Gradle dependency, `ImportTrayPreferences`, and obsolete migration paths once the replacement readers pass upgrade tests.
2. Verify that the merged manifest and packaged app contain no Tray provider or Tray classes. Update dependency or license listings that refer to the removed SDK.
3. Confine app-managed SharedPreferences access and knowledge of Tray and legacy statistics database formats to the migration package. Eliminate normal application reads and writes through Tray, SharedPreferences, and the old statistics databases. Retain normal platform SQLite access for `scheduledTasks`, `scheduledTriggerTasks`, and `userDefinedCategories`.
4. Retain AndroidX Preference as the settings UI library with MMKV underneath.
5. Coordinate native loading with the existing [Target SDK 37 migration plan](2026-08-09-target-sdk-37-migration-plan.md). That plan schedules ReLinker removal as compatibility work. ReLinker is a native loader, not a storage SDK; this migration must not reintroduce it if the SDK migration has already removed it.
6. Complete focused tests for key-value and counter migration, concurrency, interrupted updates, and backup compatibility. Verify MMKV's native behavior on Android devices or emulators, including separate processes, and cover integration with retained SQLite tasks and categories. Include the Phase 5 benchmark results in release evidence.
7. Build and smoke-test both debug and minified release variants using the repository's configured ABIs and SDK levels.

Exit gate: MMKV is the standard key-value backend and only third-party storage SDK, platform SQLite continues to serve its retained datasets, the packaged app is free of Tray, and all migration and regression checks pass.

## Verification matrix

| Scenario | Required result |
|---|---|
| Fresh install | Key-value and counter defaults are correct; normal app code creates no Tray, SharedPreferences, or legacy statistics stores; SQLite task and category databases remain available |
| Upgrade with mixed storage | Settings, lists, folders, allowlists, and all three counter types migrate; SQLite tasks and categories are preserved |
| Direct upgrade from an older supported release | Legacy readers work without an intermediate installation or Tray dependency |
| Process death during migration | Retry completes without missing data, duplicates, or false completion |
| Concurrent processes | No lost list edits, folder updates, notification changes, delayed-task tracking updates, or completed counter increments |
| Counter reset and import races | A defined ordering produces consistent values, with no partial reset/import visible after recovery |
| Counter process termination | Locks can be reacquired, committed migration is not replayed, and completed writes meet the documented durability contract |
| Counter performance | Cold and warm access, concurrent increments, snapshots for sorting, and resets meet predefined criteria against equivalent SQLite baselines |
| Migration cleanup | Only verified legacy stores are removed; the three active SQLite task and category databases remain intact |
| Settings changes and recreation | UI refreshes and permission, authentication, and service side effects remain correct |
| Reboot and task execution | SQLite time and trigger tasks still use migrated preferences and lists correctly; cancellation and enabled states remain correct |
| Category and statistics regression | SQLite custom-list references and MMKV counter values, initialization, sorting, and resets retain their defined behavior |
| Existing launcher shortcuts | Folder UUIDs and referenced data still resolve |
| Backup import and Android restore | Supported legacy and new backups restore equivalent behavior, valid references, and counters wherever covered by the existing backup mechanism |
| Write failure or malformed source | Failure is recoverable and does not silently replace data with defaults |
| Minified release | Native initialization, migration readers, MMKV key-value flows, and retained SQLite integrations work |

After adding the relevant tests, run the applicable Gradle tasks from the repository root:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease
.\gradlew.bat :app:connectedDebugAndroidTest
```

Review the dependency graph, merged manifest, packaged classes, and remaining storage API call sites. Treat SharedPreferences, Tray, or legacy statistics database access outside migration as a finding; allow expected access to the retained SQLite task and category databases. Verify that counter callers use `StatisticsStore` rather than direct MMKV operations. Run `git diff --check`. Compare against the current build baseline so unrelated existing issues are distinguished from migration regressions.

## Completion criteria

- All normal app-managed key-value reads and writes use MMKV repositories, with counters owned by `StatisticsStore`.
- SharedPreferences access and Tray and legacy statistics database readers are restricted to upgrades.
- Scheduled tasks, trigger tasks, and custom categories continue to use their existing SQLite databases.
- Freeze, unfreeze, and usage counters use MMKV, preserving migrated values and defined counting, sorting, and reset behavior.
- Tray is absent from dependencies, the merged manifest, and packaged application classes.
- MMKV is the only third-party storage SDK; platform SQLite remains supported.
- Existing MMKV values, folder UUIDs, references, and user settings survive migration; SQLite task IDs and records remain intact.
- Migration is coordinated across processes, verifiable, and safe to retry after interruption.
- Supported backup formats remain compatible.
- Counter concurrency, reset/import recovery, durability, and performance gates pass with recorded evidence.
- Required device, migration, concurrency, and release-build checks pass.

Implement the phases as reviewable changes and release only after the complete migration passes verification. Preserve unrelated local changes. Every implementation commit must include the requested `Co-Authored-By` trailer with the assistant's name and model name.
