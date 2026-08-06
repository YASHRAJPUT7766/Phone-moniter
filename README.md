# App Time Machine — Project Scaffold

Kotlin + Jetpack Compose (Material 3) scaffold, MVVM / Room / Hilt /
WorkManager. Rebuilt from the spec's updated rule: **never simulate or
estimate history Android doesn't expose.** That rule shapes the schema
more than any other requirement, so it's documented first.

## How "no fake history" is enforced at the data layer

Two fields carry the whole rule:

- **`InstalledAppEntity.monitoringStartedAt`** — set exactly once, the
  first time `AppScanWorker` observes a package on this device, to
  `System.currentTimeMillis()`. Never backdated, never recomputed. This
  is the hard floor: nothing before it is real.
- **`TimelineEventEntity.sourceApi`** — every event names the Android API
  that produced it (`PackageManager`, `StorageStatsManager`,
  `UsageStatsManager`, `NetworkStatsManager`). If a value isn't available
  from a named API, no event is written — there's no "estimated" or
  "unknown" fallback path anywhere in the write path.

`InstalledApp.existedBeforeMonitoring` (`firstInstalledAt < monitoringStartedAt`)
is the derived flag the UI uses to show a disclosure on App Details: *"this
app predates monitoring — timeline starts from first scan."* That's the
one place the rule surfaces to the user directly rather than just being
silently enforced in the data layer.

`TimelineEventType.MONITORING_STARTED` is the one synthetic event every
app gets, written once, marking the actual boundary in its own timeline
between "Android's static metadata" and "what we've observed."

## What's fully wired

- **Gradle**: root + app `build.gradle.kts` (Compose BOM, Hilt, Room+KSP,
  WorkManager, DataStore, `vico` for charts).
- **Room** (`data/local/`): 9 entities — `InstalledAppEntity` (current
  state + monitoring floor) plus history tables for timeline events,
  storage/permission/version/usage/battery/network/notification
  snapshots — each with a `Flow`-based DAO.
- **DI** (`di/`): Hilt modules for the database/DAOs and repository
  bindings.
- **Domain** (`domain/`): UI-facing models decoupled from Room, three
  example use cases.
- **Repositories** (`data/repository/`): `AppRepository`,
  `TimelineRepository` — interface + impl.
- **WorkManager** (`data/worker/`): `AppScanWorker` (periodic diff scan),
  `StorageTrackingWorker` (daily snapshot), `WorkScheduler` (gated on
  `UserPreferencesRepository.isMonitoringActive` — workers never start
  before Permission Setup completes), `PackageChangeReceiver` (immediate
  scan on install/update/uninstall).
- **DataStore** (`data/preferences/`): `UserPreferencesRepository`,
  including `isMonitoringActive` / `startMonitoring()` — the single
  source of truth for whether background tracking should be running.
- **Compose UI**: Material 3 theme with Android 12+ dynamic color,
  Navigation Compose graph, four full screens (Dashboard, Installed
  Apps, Timeline, App Details) each with a Hilt ViewModel exposing
  `StateFlow<UiState>`. Dashboard includes the new Monitoring Status
  badge, Total Timeline Events, and Apps Monitored Today cards.

## What's now implemented (previously stubbed)

- `AppRepositoryImpl.refreshFromSystem()` — real `PackageManager` scan.
  For a package never seen before, inserts it with
  `monitoringStartedAt = System.currentTimeMillis()` (never backdated)
  and writes one `MONITORING_STARTED` event. For a known package, diffs
  version name/code, `lastUpdateTime`, and APK size against the stored
  row and writes `VERSION_UPDATED` / `APK_UPDATED` / `STORAGE_INCREASED`
  / `STORAGE_DECREASED` events only where a real change is detected.
  Packages that disappear from `PackageManager`'s list get
  `markUninstalled()` + one `UNINSTALLED` event; the row is kept (not
  deleted) so its history stays visible.
- `StorageTrackingWorker` — real `StorageStatsManager.queryStatsForPackage()`
  call (API 26+, gated on the `PACKAGE_USAGE_STATS` special permission).
  If the query fails (permission not granted yet) and there's no prior
  snapshot, that app is skipped for the day rather than writing an
  invented number — carrying forward `previous` only happens when a
  fresh read isn't available but a real historical value exists.

## What's intentionally still stubbed (`// TODO` in code)

Left as clearly marked stubs rather than guessed at:

- Permission diffing — needs `PackageManager.getPackageInfo(... GET_PERMISSIONS)`.
- Battery/network/usage stats — need `UsageStatsManager` /
  `NetworkStatsManager`, gated behind the user-granted "Usage Access"
  special permission (no runtime dialog; user must go to Settings).
- Notification stats — need a `NotificationListenerService` grant.
  "Blocked notifications" from earlier spec drafts was dropped: Android
  has no reliable per-app blocked-count API, so it can't be tracked
  without violating the no-fake-data rule.
- Room encryption — swap in SQLCipher's `SupportFactory` in
  `DatabaseModule`; noted inline.

## Screens not yet built

Routes reserved in `ui/navigation/Screen.kt`: Splash, Onboarding,
Permission Setup, Statistics, Reports, Compare Apps, Search, Filters,
Favorites, Export, Backup & Restore, Settings, About. Permission Setup
matters most functionally — it's what should call
`UserPreferencesRepository.startMonitoring()` and
`WorkScheduler.scheduleAllIfMonitoringActive()`; until it exists, no
background tracking will actually start.

## Next steps, roughly in order

1. Build Permission Setup — without it, `isMonitoringActive` stays false
   forever and every worker stays dormant.
2. Implement permission diffing against `PackageManager`'s
   `GET_PERMISSIONS` flag.
3. Build Statistics/Reports using the `vico` dependency already declared.

## Building the APK

### Locally

This scaffold doesn't ship a `gradle-wrapper.jar` binary (it can't be
verified inside a sandboxed environment without network access). To
build locally, either:

```bash
# One-time, if you have Gradle installed some other way (e.g. via sdkman):
gradle wrapper --gradle-version 8.9
./gradlew assembleDebug
```

or just use a locally installed Gradle 8.9+ directly:

```bash
gradle assembleDebug
```

The debug APK lands in `app/build/outputs/apk/debug/`.

### CI (GitHub Actions)

`.github/workflows/build.yml` builds a debug APK on every push/PR to
`main`/`master`, and can build a signed release APK via manual dispatch
(`workflow_dispatch`, choosing the `release` build type). It uses
`gradle/actions/setup-gradle`, which provisions the exact Gradle version
declared in `gradle/wrapper/gradle-wrapper.properties` without needing a
committed wrapper jar.

The finished APK is uploaded as a workflow artifact named
`AppTimeMachine-apk` — download it from the run's **Summary** page.

**For signed release builds**, add these repo secrets:

| Secret | Purpose |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Your `.jks`/`.keystore` file, base64-encoded |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Key alias inside the keystore |
| `RELEASE_KEY_PASSWORD` | Key password |

Without these secrets, `assembleRelease` still runs but produces an
**unsigned** APK (no `signingConfig` is attached — see
`app/build.gradle.kts`). Debug builds never need signing secrets; Android
signs debug builds automatically with a local debug key.
