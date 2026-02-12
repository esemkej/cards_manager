# Changelog

## v0.14.2-alpha
- Fixes around creating image-only items (better handling/validation during creation)
- Added safety nets to prevent saving empty items
- Bug fix: UI no longer allows adding pictures to folder items

---

## v0.14.1-alpha
- Ability to add or remove codes for picture items (convert image-only items to card+image and back)
- Display "None" next to Code type when no code is present for better clarity
- Card name text is now always white for improved readability and consistency

---

## v0.14.0-alpha
- Support for pictures
- Ability to attach pictures to card items
- Ability to create standalone picture items

---

## v0.13.1-alpha
- Fixed duplicate ID issue after import by recalculating `lastId` from the highest existing ID, including nested folders
- Fixed RecyclerView height and display inconsistencies
- Added an explanation dialog for potentially valid EAN-13 codes with an incorrect checksum
- Fixed an issue where importing data from a file did not refresh the list immediately
- Fixed an APK installation issue caused by faulty signing
- Other quality of life updates

---

## v0.13.0-alpha
- Updated card display dialog
- Ability to edit all information of existing cards and folders
- Ability to enter codes manually and pick code types
- Fixed a bug that allowed creation of folders with no name
- Optimized certain parts of the code
- WARNING: After importing data and later adding items, some of the new items could have duplicate ids. This will be fixed in upcoming updates

---

## v0.12.3-alpha
- Fixed a crash when trying to create/change card colors

---

## v0.12.2-alpha
- Fixed cards saving as debug_code, debug_type
- Added the option to sort by item type
- Unfortunately needed to change keystores, so please make sure to reinstall the app, and backup your data with import export feature, and don't use any older versions than this

---

## v0.12.1-alpha
- Main screen now displays a notification when no items exist.
- Added the ability to edit names of created items.

---

## v0.12.0-alpha
- First official public release.
- Fully local-first data storage.
- Membership cards and folders with nested structure support.
- Favorites system for both cards and folders.
- Sorting by name, creation order (ID-based), and usage count.
- Filtering between all items and favorites.
- Gradient-based visual customization.
- Adding membership codes via camera scanning or image-based scanning.
- Camera permission requested only when initiating a scan.
- Manual import and export system using JSON (clipboard and files).
- Import safety mechanism preventing data loss on failed imports.
- UI layout refinements and barcode rendering fixes.
- Tutorial and onboarding flows included.
