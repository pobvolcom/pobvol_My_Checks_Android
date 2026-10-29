# pobvol My Checks
Create checklists quickly and easily | Log results | Free Software | Open Source | Android Smartphones and Tablets

With the Android app pobvol My Checks, you can easily and quickly create custom checklists with checkpoints and questions, and then record, analyze, and output the results of inspections and other activities in PDF reports. All captured data is automatically backed up in SQL files. Data structures do not need to be programmed in databases with great effort. German, English, Spanish, and French are supported.

# Dashboard & Navigation

The dashboard is your starting point. Use the cards to navigate to your checklists and logs, and access settings and help via the top bar.

# Settings & Customization

Tap the gear icon (⚙) to open settings. The "General Settings" dialog will appear.

First, customize appearance, preferred language, and categories to your needs. Enter your name in the Inspector field. Your name will be included and output in inspection reports.

Tap the "Customize PDF Report Layout" button to open the "PDF Report Designer" dialog. Customize the PDF report layout (headers, footers, page breaks, date, and status display).

# My Checklists

Tap the "My Checklists" card on the dashboard to open this screen.

Add Checklist: Create new checklists quickly. Tap the **`+`** button in the bottom right corner to open the "Add Checklist" dialog. Enter a title, assign a category, and optionally provide a short description. Tap "Add" to save your new checklist, creating it and displaying it in the overview.

Search Checklists: Tap the search icon (🔍) in the top bar to find checklists by title, description, or category.

Filter by Category: Filter the list by tapping a category (Health, UVV, etc.). Tap All to clear the filter.

Edit Checklist: Tap the edit icon (✏️) on a checklist card to open "Edit Checklist". Here you can adjust title, category, and description.

Checkpoints / Questions: Tap the question icon (💬) on a checklist card to open the "Checkpoints/Questions" screen. Here you manage checkpoints and questions for the selected checklist.

Delete Checklist: Tap the delete icon (🗑️) to open a confirmation window. Deleting a checklist automatically deletes its questions. Please note: Checklists with recorded logs cannot be deleted.

Add Inspection: Tap a checklist card to start recording a new inspection. The "Fill Checklist" form opens.

View Inspection Logs: Tap the log icon (📋) on a checklist card to open "My Logs" for that checklist.

View Chart: Tap the chart icon (📋) on a checklist card to open analytics charts showing daily submissions and numeric trends.

# Checkpoints / Questions

Manage checkpoints and questions for a checklist here. Supported answer types: Text, Number, Checkbox, Combobox, Multi-select, NFC, and QR Code.

- Text: Flexible text input

- Number: Numeric value input. You can optionally set minimum and maximum limits.

- Checkbox: Yes/No toggle

- Combobox: Single choice from comma-separated options. You can assign background colors (green, yellow, or red) using color tags in brackets, such as `no defects [green]`, `repair required [red]`, or `defects corrected [yellow]`. In the PDF export, the selected option is printed with a highlighted background box.

- Multi-select: Multiple choices from comma-separated options with color tags in brackets (e.g., `Option 1 [green], Option 2 [red], Option 3 [yellow]`). In PDF export, selected options are printed with highlighted background boxes.

- NFC: Read physical NFC tags and import data

- QR Code: Scan QR codes with the camera and populate the answer field.

Add Checkpoint / Question: Tap the **`+`** button in the bottom right corner, enter a title, select an answer type, and save.

Adjust Order: Use the up (↑) and down (↓) arrows to adjust the sort order.

Edit / Delete: Tap the edit or delete icons on any question card.

# Performing Inspections & Recording Results

Tap a checklist card on the dashboard to open the "Fill Checklist" form.

- Inspector: Automatically filled from settings; editable.

- Date: Pre-filled with current date; editable.

- Status: Pre-filled as "In progress".

- Notes: Add optional inspection notes to help identify logs easily in the overview.

- Checkpoints / Questions: Answer required questions. Status automatically updates to "Completed" once all required questions are answered.

- NFC Tags: Tap the NFC button and hold an NFC tag to your device.

- QR Codes: Tap the scan QR code button to use the camera scanner.

# My Logs & PDF and SQL Export

View recorded logs, filter lists, edit answers, and generate A4 PDF reports.
Search & Filter: Search logs by date, title, or notes, and filter by status.
Edit / Delete Logs: Modify answers or delete logs as needed.

View PDF: Tap the PDF icon on a log card to open, print, or share the generated PDF report.
PDF reports are automatically saved to the device folder Downloads/MyChecks with filename Submission_id_Report.pdf.

# Data Security & SQL Export

All inspection data is securely stored in your local Android SQLite database with foreign key cascades and schema migrations. Additionally, completed reports are exported as SQL.TXT files in Downloads/MyChecks.

# Technology / Architecture

- Language: Kotlin
- UI Framework: Jetpack Compose & Material 3 (with Edge-to-Edge support)
- Database: Room Persistence Library (SQLite)
- Document Generation: Android `PdfDocument` API (A4 vector rendering)
- Image Processing: Coil Compose
- Permissions & Hardware: Camera contracts, NFC foreground dispatch, and Scoped Storage MediaStore APIs
- Supported Devices: Android Smartphones and Tablets
- Supported Languages: German, English, Spanish, and French

# Copyright

The app is Free Software and delivered as open source. You can download, install, operate, modify, and redistribute this software under the terms of the GNU General Public License v3. Copyright © 2026 Volker Pobloth, pobvol Software Services. Developed with Google Android Studio and Gemini 3.5 Flash Lite and Gemini 3.6 Flash

The App is provided in the hope that it will be useful, but WITHOUT WARRANTY OF ANY KIND; even without the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 

# Contact Me

If you have any questions or suggestions about this project, do not hesitate to contact me at:

- Developer: Volker Pobloth, pobvol Software Services, poppi66292@gmail.com
- GitHub: pobvolcom/pobvol_My_Checks_Android

