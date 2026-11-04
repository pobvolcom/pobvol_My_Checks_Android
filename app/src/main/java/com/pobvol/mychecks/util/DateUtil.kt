package com.pobvol.mychecks.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {
    fun formatDateByLanguage(dateStr: String, preferredLang: String): String {
        return formatDateForDisplay(dateStr, preferredLang)
    }

    fun toIsoDate(dateStr: String, preferredLang: String = "de"): String {
        if (dateStr.isBlank()) return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*"))) return dateStr.split(" ")[0]
        try {
            val clean = dateStr.split(" ")[0].trim()
            val sep = when {
                clean.contains(".") -> "."
                clean.contains("-") -> "-"
                clean.contains("/") -> "/"
                else -> ""
            }
            if (sep.isBlank()) return dateStr
            val parts = clean.split(sep)
            if (parts.size < 3) return dateStr

            val n0 = parts[0].toIntOrNull() ?: return dateStr
            val n1 = parts[1].toIntOrNull() ?: return dateStr
            val n2 = parts[2].toIntOrNull() ?: return dateStr

            val year: Int
            val month: Int
            val day: Int

            if (parts[0].length == 4 || n0 > 31) {
                year = if (n0 < 100) 2000 + n0 else n0
                month = n1
                day = n2
            } else {
                year = if (n2 < 100) 2000 + n2 else n2
                if (n0 > 12) {
                    day = n0
                    month = n1
                } else if (n1 > 12) {
                    month = n0
                    day = n1
                } else {
                    val isUsFormat = preferredLang.startsWith("en", ignoreCase = true)
                    if (sep == "/" && isUsFormat) {
                        month = n0
                        day = n1
                    } else {
                        day = n0
                        month = n1
                    }
                }
            }
            return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
        } catch (_: Exception) {
        }
        return dateStr
    }

    fun formatDateForDisplay(dateStr: String, preferredLang: String): String {
        val iso = toIsoDate(dateStr, preferredLang)
        if (iso.length < 10) return dateStr
        try {
            val parts = iso.split("-")
            if (parts.size >= 3) {
                val year = parts[0]
                val month = parts[1]
                val day = parts[2]
                return when {
                    preferredLang.startsWith("de", ignoreCase = true) -> "$day.$month.$year"
                    preferredLang.startsWith(
                        "es",
                        ignoreCase = true
                    ) || preferredLang.startsWith("fr", ignoreCase = true) -> "$day/$month/$year"

                    else -> "$month/$day/$year"
                }
            }
        } catch (_: Exception) {
        }
        return dateStr
    }

    fun getCurrentDateForLanguage(preferredLang: String): String {
        val pattern = when {
            preferredLang.startsWith("de", ignoreCase = true) -> "dd.MM.yyyy"
            preferredLang.startsWith("es", ignoreCase = true) || preferredLang.startsWith(
                "fr",
                ignoreCase = true
            ) -> "dd/MM/yyyy"

            else -> "MM/dd/yyyy"
        }
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
    }

    fun formatNumberForPdf(valueStr: String, preferredLang: String): String {
        if (valueStr.isBlank()) return valueStr
        try {
            val cleanStr = valueStr.trim().replace(',', '.')
            val number = cleanStr.toDoubleOrNull() ?: return valueStr

            val decimalPlaces = if (cleanStr.contains(".")) {
                cleanStr.substringAfter('.').length
            } else {
                0
            }

            val symbols = java.text.DecimalFormatSymbols.getInstance().apply {
                when {
                    preferredLang.startsWith("fr", ignoreCase = true) -> {
                        groupingSeparator = ' '
                        decimalSeparator = ','
                    }

                    preferredLang.startsWith(
                        "es",
                        ignoreCase = true
                    ) || preferredLang.startsWith("de", ignoreCase = true) -> {
                        groupingSeparator = '.'
                        decimalSeparator = ','
                    }

                    else -> {
                        groupingSeparator = ','
                        decimalSeparator = '.'
                    }
                }
            }

            val pattern = if (decimalPlaces > 0) {
                "#,##0." + "0".repeat(decimalPlaces)
            } else {
                "#,##0"
            }

            val formatter = java.text.DecimalFormat(pattern, symbols)
            return formatter.format(number)
        } catch (_: Exception) {
        }
        return valueStr
    }
}
