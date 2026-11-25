🌐 **Sprache:** [English](README.md) | [Deutsch](README.de.md) | [Español](README.es.md) | [Français](README.fr.md)

---

# ✅ pobvol My Checks

![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)
![Platform: Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Privacy: 100% Offline](https://img.shields.io/badge/Privacy-100%25%20Local%20%26%20Offline-success)


<img src="images/pobvol_my_checks.png" width="100" alt="Logo"> **My Checks** ist eine mobile Anwendung zum Erstellen und Verwalten digitaler Checklisten, mit denen Sie die Ergebnisse von Inspektionen und Prüfungen (wie z. B. UVV-Prüfungen, Blutdruck, etc.) erfassen und sich diese als PDF-Bericht ausgeben lassen. Die App ist Freie Software und wird als Open Source über GitHub veröffentlicht und bereitgestellt. Die App wurde mit Google Android Studio sowie Gemini 3.5 Flash Lite und Gemini 3.6 Flash entwickelt.


## 📸 Screenshots


<img src="images/de/start.jpg" width="40%" alt="Launcher"> 
<img src="images/de/dashboard.jpg" width="40%" alt="Dashboard"> 
<img src="images/de/my-checklists.jpg" width="40%" alt="Checklisten">
<img src="images/de/fill-out-checklist.jpg" width="40%" alt="Fill-out"> 
<img src="images/de/my-logs.jpg" width="40%" alt="Logs"> 
<img src="images/de/pdf.jpg" width="40%" alt="PDF">
<img src="images/de/charts.jpg" width="40%" alt="Charts"> 

---

## ✨ Hauptfunktionen

* 📋 **Checklisten-Verwaltung (*My Checklists*):**
  * Eigene Prüfvorlagen erstellen, duplizieren und nach Kategorien (z. B. UVV, Health, Personal) filtern.
  * Flexible Eingabetypen pro Prüfpunkt: Freitext, Zahlenwerte, Checkboxen, Auswahllisten (Combobox), Mehrfachauswahl sowie das Scannen von NFC-Tags und QR-Codes.
  * Pflicht- oder optionale Felder konfigurieren und Prüfpunkte einfach neu anordnen.
* 🔍 **Prüfungsdurchführung (*My Logs*):**
  * Prüfprotokolle nach Status (*Alle*, *Abgeschlossen*, *In Bearbeitung*), Vorlage oder Kunde filtern und durchsuchen.
  * Prüferdaten, Notizen zu Kunde und Gerät, NFC-Scans sowie Fotodokumentationen direkt vor Ort erfassen.
  * Versandfertige **PDF-Prüfberichte** inklusive Prüfergebnissen, digitalen Prüfplaketten und Unterschriften generieren.
* 📝 **Textbausteine (*Text Blocks*):**
  * Wiederverwendbare Standardtexte für wiederkehrende Mängel oder Bestätigungen mängelfreier Prüfungen erstellen, organisieren und einfügen.
* ⚙️ **Einstellungen & Lokalisierung:**
  * Mehrsprachige Benutzeroberfläche (Englisch, Deutsch, Spanisch, Französisch) und Design-Unterstützung (System, Hell, Dunkel).
  * Konfigurierbarer Standard-Prüfername, Sprachausgabe (TTS) und eigene Kategorien.
* 💾 **Datenbank & Backup:**
  * Vollständige Offline-Funktionalität mit lokalem Export/Import von Sicherungskopien, Zurücksetzen der Datenbank und integrierten Beispieldaten.

---

## 💾 Datenbankdiagramm

<img src="images/sqlite-db-schema-pobvol-my-checks.jpg" alt="DB Schema">

Das Bild visualisiert genau die Beziehungen und Struktur der SQLite-Datenbank – von Checklisten über Checkpunkten / Fragen, Protokollen, Antworten, Fotos bis hin zu Textbausteinen und Nachschlagetabellen.

---

## 🔒 Datenschutz

* **100 % lokale Speicherung:** Alle Checklisten, Prüfprotokolle, Fotos, Textbausteine und Einstellungen werden ausschließlich lokal auf Ihrem Gerät gespeichert.

* **Keine Datenweitergabe:** Es werden keinerlei Daten an externe Server, Cloud-Dienste oder Dritte übertragen. Sie behalten jederzeit die volle Kontrolle über Ihre Prüfdaten.

* [Privacy Policy](PRIVACY_POLICY.md)

---

## ⚖️ Lizenz

Sie können die App unter den Bedingungen der GNU General Public License v3 herunterladen, installieren, betreiben, modifizieren und weiterverbreiten.  
Siehe [https://www.gnu.org/licenses/gpl-3.0](https://www.gnu.org/licenses/gpl-3.0)

---

## ⚠️ Haftungsausschluss

Die App wird in der Hoffnung bereitgestellt, dass sie nützlich sein wird, jedoch OHNE JEGLICHE GEWÄHRLEISTUNG; auch ohne die implizite Gewährleistung der MARKTGÄNGIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK. Der Entwickler haftet nicht für Datenverluste oder fehlerhafte Protokolle.

---

## ©️ Urheberrecht

Copyright © 2026 Volker Pobloth, pobvol Software Services

---

## 📲 Installation (Android)

Da **pobvol My Checks** als freie Open-Source-Software ohne Google-Play-Zwang entwickelt wird, kannst du die App direkt über GitHub auf deinem Android-Smartphone oder -Tablet installieren.

### Option 1: Direkte Installation per APK (Schnell & Einfach)

1. Öffne auf deinem Android-Gerät die **[GitHub Releases-Seite](https://github.com/pobvolcom/pobvol_My_Checks_Android/releases/latest)**.
2. Scrolle ganz nach unten zum Bereich **Assets** und tippe auf **`app-release.apk`**, um die Installationsdatei herunterzuladen.
3. Öffne die heruntergeladene Datei über die Benachrichtigungsleiste oder deine **Dateien / Downloads**-App.
4. **Berechtigung erteilen (falls gefragt):** Android blockiert aus Sicherheitsgründen standardmäßig Apps außerhalb des Play Stores. Tippe im Hinweis-Fenster auf **Einstellungen** und aktiviere **„Aus dieser Quelle zulassen“** für deinen Browser bzw. Dateimanager.
5. Tippe anschließend auf **Installieren**.

> **Hinweis zu Google Play Protect:** Falls beim Installieren eine Warnung von Play Protect erscheint (*„Unbekannte App“*), tippe auf **Weitere Details** und wähle **Trotzdem installieren**. Die App ist vollständig Open Source (GPL-3.0) und speichert alle Daten ausschließlich lokal auf deinem Gerät.

---

### Option 2: Automatische Updates mit Obtainium (Empfohlen)

Wenn du zukünftige Updates bequem und automatisch direkt von GitHub erhalten möchtest, empfiehlt sich die kostenlose Open-Source-App **[Obtainium](https://github.com/ImranR98/Obtainium)**:

1. Installiere **Obtainium** auf deinem Gerät.
2. Öffne Obtainium, tippe auf **App hinzufügen** und füge folgende URL ein:
   ```text
   [https://github.com/pobvolcom/pobvol_My_Checks_Android](https://github.com/pobvolcom/pobvol_My_Checks_Android)
3. ​Tippe auf Hinzufügen und anschließend auf Installieren. Obtainium informiert dich ab sofort automatisch über neue Versionen. 



