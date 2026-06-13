# Cleanner

Cleanner is an Android app project for finding, classifying, and safely cleaning files created by QQ and WeChat.

The goal is to help users understand what is taking space on their phone before deleting anything. The app should separate files into clear categories, explain why a file may be safe to clean, and always let the user choose what to remove.

## Project Vision

QQ and WeChat can accumulate a large amount of local data over time: downloaded files, chat media, temporary cache, duplicated media, old packages, thumbnails, logs, and other generated content. Some of these files may be useful, while others are usually safe to delete.

Cleanner will provide a focused cleaning experience for these apps:

- Scan QQ and WeChat storage locations on Android devices.
- Group discovered files by app, type, source, size, and cleanup risk.
- Mark likely junk files, cache files, large files, duplicate-looking files, and user-owned files separately.
- Show enough detail before cleanup so the user can make an informed choice.
- Delete only files selected and confirmed by the user.

## MVP Scope

The first version should stay intentionally small and practical:

- Android app built from scratch.
- Local-only scanning and cleanup.
- QQ and WeChat file discovery.
- File categories for cache, media, documents, downloads, APK packages, logs, and unknown files.
- Cleanup recommendations with conservative default selections.
- Manual review before deletion.
- Cleanup result summary showing freed space and failed deletions.

## Privacy And Safety Principles

Cleanner should be designed around user trust:

- No cloud upload is required for scanning or classification.
- File contents should not be read unless needed for safe classification.
- Private chat data should never be deleted automatically.
- User-created or user-downloaded files should require explicit selection.
- Destructive actions should have confirmation and clear feedback.
- Android storage permissions should be requested only when needed and explained clearly.

## Planned Classification Levels

Files can be grouped by cleanup confidence:

| Level | Meaning | Default Action |
| --- | --- | --- |
| Safe cache | Temporary files, thumbnails, generated cache | Selected by default after review |
| Likely junk | Old logs, expired temp files, leftover packages | Recommended, but review first |
| Review needed | Large media, documents, received files | Not selected by default |
| Keep by default | Unknown or sensitive-looking files | Never selected automatically |

## Possible Tech Direction

The initial technical direction is:

- Kotlin for Android development.
- Jetpack Compose for UI.
- Android Storage Access Framework and scoped storage compatible scanning.
- Local rule-based classifier first, with room for smarter classification later.
- A scan index stored locally so repeated scans are faster.

This may change as Android permission constraints and target SDK requirements are explored.

## Roadmap

1. Define Android storage access strategy for QQ and WeChat folders.
2. Build the scanner and file metadata model.
3. Add rule-based classification.
4. Create a review UI for categories and individual files.
5. Implement safe deletion with confirmation and result reporting.
6. Add tests for classifier rules and deletion safeguards.
7. Polish UX, permission explanations, and error handling.

## Repository Status

This repository is currently at the planning stage. The first milestone is to turn this README into a concrete Android project skeleton and implementation plan.

## Author

Created by [Sancheeese](https://github.com/Sancheeese).
