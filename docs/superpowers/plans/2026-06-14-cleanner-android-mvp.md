# Cleanner Android MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a runnable Android MVP that scans QQ and WeChat storage, explains folder/file purpose, lets the user select files, deletes only confirmed selections, and records cleanup summaries.

**Architecture:** Use a single Android app module with focused Kotlin packages for domain models, folder rules, analysis, scanning, deletion, history, permissions, and Compose UI. The scanner produces facts, the analyzer produces user-facing explanations, and the deletion engine only acts on explicit selections.

**Tech Stack:** Kotlin, Android Gradle Plugin, Jetpack Compose, Kotlin coroutines, AndroidX lifecycle/navigation, JUnit.

---

### Task 1: Project Skeleton

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/sancheeese/cleanner/MainActivity.kt`

- [ ] Add Android Gradle project configuration.
- [ ] Add app manifest with all-files access permission.
- [ ] Add a minimal Compose `MainActivity`.
- [ ] Run Gradle help or assemble when Gradle is available.

### Task 2: Domain And Analyzer TDD

**Files:**
- Create: `app/src/main/java/com/sancheeese/cleanner/core/model/Models.kt`
- Create: `app/src/main/java/com/sancheeese/cleanner/core/rules/FolderRuleRegistry.kt`
- Create: `app/src/main/java/com/sancheeese/cleanner/core/analyze/FileAnalyzer.kt`
- Create: `app/src/test/java/com/sancheeese/cleanner/core/analyze/FileAnalyzerTest.kt`
- Create: `app/src/test/java/com/sancheeese/cleanner/core/rules/FolderRuleRegistryTest.kt`

- [ ] Write tests for WeChat image folder matching and APK classification.
- [ ] Run tests and confirm they fail before implementation.
- [ ] Implement models, folder rules, and analyzer.
- [ ] Run tests and confirm they pass.

### Task 3: Scanner And Metadata

**Files:**
- Create: `app/src/main/java/com/sancheeese/cleanner/core/scan/FileScanner.kt`
- Create: `app/src/main/java/com/sancheeese/cleanner/core/metadata/MetadataReader.kt`
- Create: `app/src/test/java/com/sancheeese/cleanner/core/scan/FileScannerTest.kt`

- [ ] Write tests for scan root filtering and cancellation-safe result creation.
- [ ] Implement scanner interfaces and local filesystem scanner.
- [ ] Implement metadata reader wrappers for image, video, APK, document, archive, and unknown metadata.

### Task 4: Deletion And History

**Files:**
- Create: `app/src/main/java/com/sancheeese/cleanner/core/delete/SafeDeletionEngine.kt`
- Create: `app/src/main/java/com/sancheeese/cleanner/core/history/CleanupHistoryStore.kt`
- Create: `app/src/test/java/com/sancheeese/cleanner/core/delete/SafeDeletionEngineTest.kt`

- [ ] Write tests proving only selected files are deleted.
- [ ] Implement safe deletion result reporting.
- [ ] Implement SharedPreferences-backed cleanup history.

### Task 5: App State And UI

**Files:**
- Create: `app/src/main/java/com/sancheeese/cleanner/app/CleannerApp.kt`
- Create: `app/src/main/java/com/sancheeese/cleanner/app/CleannerViewModel.kt`
- Create: `app/src/main/java/com/sancheeese/cleanner/app/PermissionGateway.kt`
- Create: `app/src/main/java/com/sancheeese/cleanner/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/sancheeese/cleanner/ui/screens/*.kt`

- [ ] Implement permission, home, scan, analysis, details, confirmation, and result screens.
- [ ] Wire ViewModel state to scanner, analyzer, deletion engine, and history store.
- [ ] Provide sample fallback data when no QQ/WeChat folders are found so the UI remains understandable.

### Task 6: Verification And Delivery

**Files:**
- Modify: `README.md`

- [ ] Add build/run instructions.
- [ ] Run unit tests if Gradle can resolve dependencies.
- [ ] Run assemble if Android SDK is available.
- [ ] Commit and push to `origin/main`.
