🌐 **Language:** [English](README.md) | [Deutsch](README.de.md) | [Español](README.es.md) | [Français](README.fr.md)

---

# ✅ pobvol My Checks

![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)
![Platform: Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Privacy: 100% Offline](https://img.shields.io/badge/Privacy-100%25%20Local%20%26%20Offline-success)


<img src="images/pobvol_my_checks.png" width="100" alt="Logo"> **My Checks** is a mobile application for creating, managing, and executing digital checklists, safety inspections, and equipment checks (such as UVV inspections), complete with automated PDF report generation. The app is Free Software, published and delivered as open source via GitHub. The app was developed with Google Android Studio and Gemini 3.5 Flash Lite and Gemini 3.6 Flash.

---

## 📸 Screenshots

| Dashboard | Checklists | Inspection | PDF |
| :---: | :---: | :---: | :---: |
| <img src="images/en/dashboard.png" width="100" alt="Dashboard"> | <img src="images/en/my-checklists.png" width="100" alt="Checklists"> | <img src="images/en/fill-out-checklist.png" width="100" alt="Inspection"> | <img src="images/en/pdf.png" width="100" alt="PDF"> |

---

## ✨ Key Features

* 📋 **Checklist Management (*My Checklists*):**
  * Create, duplicate, and filter custom inspection templates by category (e.g., UVV, Health, Personal).
  * Flexible input types per checkpoint: free text, numeric values, checkboxes, comboboxes, multi-select, as well as NFC tag and QR code scanning.
  * Configure required or optional fields and reorder checkpoints easily.
* 🔍 **Inspection Execution (*My Logs*):**
  * Filter and search inspection logs by status (*All*, *Completed*, *In progress*), template, or customer.
  * Record inspector details, customer/device notes, NFC scans, and photo documentation on-site.
  * Generate ready-to-share **PDF inspection reports** including test results, digital inspection badges, and signatures.
* 📝 **Text Blocks (*Text Blocks*):**
  * Create, organize, and insert reusable standard texts for recurring defects or pass confirmations.
* ⚙️ **Settings & Localization:**
  * Multi-language interface (English, German, Spanish, French) and theme support (System, Light, Dark).
  * Configurable default inspector name, Text-to-Speech (TTS) voice feedback, and custom categories.
* 💾 **Database & Backup:**
  * Full offline capability with local backup export/import, database reset, and built-in demo data.

---

## 💾 Database schema

<img src="images/sqlite-db-schema-pobvol-my-checks.jpg" alt="DB Schema">

The image precisely visualizes the relationships and structure of the SQLite database from the app – from checklist templates to checkpoints, logs, photo responses, to text modules and general app settings.

---

## 🔒 Privacy

* **100% Local Storage:** All checklists, inspection logs, photos, text blocks, and settings are stored exclusively locally on your device.

* **No Data Sharing:** No data is transmitted to external servers, cloud services, or third parties. You retain full control over your inspection data at all times.

* [Privacy Policy](PRIVACY_POLICY.md)

---

## ⚖️ License

You can download, install, operate, modify, and redistribute the app under the terms of the GNU General Public License v3.  
See [https://www.gnu.org/licenses/gpl-3.0](https://www.gnu.org/licenses/gpl-3.0)

---

## ⚠️ Disclaimer

The App is provided in the hope that it will be useful, but WITHOUT WARRANTY OF ANY KIND; even without the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. The Developer is not liable for any loss of data or erroneous logs.

---

## ©️ Copyright

Copyright © 2026 Volker Pobloth, pobvol Software Services

---

## 📲 Installation (Android)

Since **pobvol My Checks** is developed as free, open-source software without Google Play dependencies, you can install it directly from GitHub on your Android smartphone or tablet.

### Option 1: Direct APK Installation (Quick & Easy)

1. On your Android device, go to the **[GitHub Releases page](https://github.com/pobvolcom/pobvol_My_Checks_Android/releases/latest)**.
2. Scroll down to the **Assets** section and tap **`app-release.apk`** to download the installer.
3. Open the downloaded file from your notification shade or your **Files / Downloads** app.
4. **Allow installation (if prompted):** By default, Android blocks installations from outside the Play Store. Tap **Settings** in the prompt and enable **"Allow from this source"** for your browser or file manager.
5. Tap **Install**.

> **Note on Google Play Protect:** If Play Protect displays a warning (*"Unrecognized app"*), tap **More details** and select **Install anyway**. The app is 100% open source (GPL-3.0) and stores all data strictly locally on your device.

---

### Option 2: Automatic Updates via Obtainium (Recommended)

To receive future updates automatically directly from GitHub, you can use the free open-source app **[Obtainium](https://github.com/ImranR98/Obtainium)**:

1. Install **Obtainium** on your Android device.
2. Open Obtainium, tap **Add App**, and paste the following repository URL:
   ```text
   [https://github.com/pobvolcom/pobvol_My_Checks_Android](https://github.com/pobvolcom/pobvol_My_Checks_Android)
3. ​Tap Add and then Install. Obtainium will automatically notify you whenever a new release is published.


