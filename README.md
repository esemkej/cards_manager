# Cards

Cards is a local-first Android app for storing and organizing membership cards (e.g., Lidl, Costco, loyalty cards, gym passes) using QR codes and barcodes.

The app focuses on simplicity, speed, and user control. Data is currently stored entirely on the device, without accounts or background services. Cloud-based sync is not part of the current release, but may be introduced in the future if it can meet strict end-to-end encryption and privacy requirements.

Cards is designed to stay predictable, responsive, ad-free, and comfortable to use even with larger collections, while offering just enough flexibility to adapt to different wallets and memberships.

---

## Features

- **Membership Cards & Folders:** Store membership cards and organize them into folders, including nested folder structures.
- **Favorites:** Mark both membership cards and folders as favorites for quick access.
- **Sorting Options:** Sort items by name, creation order (based on internal IDs), usage count or item type, with ascending and descending modes.
- **Filtering:** Switch between viewing all items or favorites only.
- **Visual Customization:** Customize cards using gradient styles for better visual distinction.
- **QR & Barcode Scanning:** Add membership codes by scanning with the camera or by scanning existing images from device storage.
- **Permission-Aware Scanning:** Camera permission is requested only when the user explicitly starts a camera scan; image-based scanning works without it.
- **Local Import & Export:** Export data to the clipboard or to a JSON file. Import data by pasting text or selecting a JSON file.
- **Safe Imports:** Imports overwrite existing data only if they succeed; invalid input leaves existing data untouched.
- **Offline by Design:** No required internet connection, accounts, or background services.
- **Ad-Free by Design:** No ads, trackers, or monetization frameworks.

---

## What's New (v0.12.2-alpha)
- Fixed cards saving as debug_code, debug_type
- Added the option to sort by item type
- Unfortunately needed to change keystores, so please make sure to reinstall the app, and backup your data with import export feature, and don't use any older versions than this
