# My Checks

**My Checks** is a powerful, free, and open-source Android application designed for creating custom
checklists, logging inspections, analyzing data trends, and generating professional PDF reports and
SQL backups.

---

## 🚀 Key Features

### 1. Checklist & Checkpoint Management

- Create and organize custom checklists by category (e.g., *UVV*, *Health*, *General*).
- Support for multiple response/checkpoint types:
    - **Text**: Flexible multi-line text input.
    - **Number**: Numeric input with optional minimum/maximum range validation.
    - **Checkbox**: Yes/No toggles.
    - **Combobox**: Single-choice options with user-defined background highlight colors (**Green**,
      **Yellow**, **Red**).
    - **NFC**: Read physical NFC tags or UIDs to instantly populate answer fields.
    - **QR Code**: Scan QR codes using the device camera.
- Add optional notes and **pictures** (captured via camera or uploaded from device) to any answer.

### 2. Inspection Logs & Submissions

- Fill out checklists, record inspector details, dates, statuses (*Completed*, *In Progress*,
  *Pending*), and inspection notes.
- Review past submissions, edit answers, or delete records.

### 3. Professional PDF Report Designer

- Fully customizable **PDF Report Layout** in Settings:
    - **Header Design**: Custom header title, checklist title, inspection date, inspector name,
      status, and submission notes.
    - **Answer Design**: Question titles, answer values, answer notes, answer pictures, and compact
      layout.
    - **Footer Design**: Timestamps, dynamic page numbers, and custom footer texts/disclaimers.
- Multi-page A4 PDF pagination supporting arbitrary numbers of questions, notes, and photos.
- Saved directly to the device's **`Downloads/MyChecks`** folder.

### 4. Data Export & Backup

- **SQL Export**: Every completed inspection automatically exports a complete database SQL dump to
  the `Downloads/MyChecks` folder alongside the PDF report.
- **Picture Naming**: Attached answer pictures are saved with explicit submission and answer IDs in
  their filename.

### 5. Analytics & Chart Visualizations

- View interactive analytics charts per checklist:
    - **Submissions per Day**: Track inspection frequency over time.
    - **Dual-Series Numeric Trends**: Plot the first and second numeric answer values per submission
      as smooth trend lines.

### 6. Localization

- Full multi-language support for **English**, **German** (*Deutsch*), **Spanish** (*Español*), and
  **French** (*Français*).

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose & Material 3 (with Edge-to-Edge support)
- **Database**: Room Persistence Library (SQLite with automatic schema migrations & cascade deletes)
- **Document Generation**: Android `PdfDocument` API (A4 vector rendering)
- **Image Loading**: Coil Compose
- **Permissions & Hardware**: Camera contracts, NFC foreground dispatch, and Scoped Storage
  MediaStore APIs

---

## 📄 License

This project is open source and distributed under the terms of
the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0).
