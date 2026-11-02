package com.pobvol.mychecks.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {
    fun formatDateByLanguage(dateStr: String, preferredLang: String): String {
        if (dateStr.isBlank()) return dateStr
        try {
            val parts = when {
                dateStr.contains("-") -> dateStr.split(" ")[0].split("-")
                dateStr.contains(".") -> dateStr.split(" ")[0].split(".")
                dateStr.contains("/") -> dateStr.split(" ")[0].split("/")
                else -> emptyList()
            }
            if (parts.size >= 3) {
                val (year, month, day) = when {
                    dateStr.contains("-") -> Triple(parts[0], parts[1], parts[2])
                    preferredLang.startsWith("en", ignoreCase = true) -> Triple(
                        parts[2],
                        parts[0],
                        parts[1]
                    )

                    else -> Triple(parts[2], parts[1], parts[0])
                }
                val fullYear = if (year.length == 2) "20$year" else year
                return when {
                    preferredLang.startsWith("de", ignoreCase = true) -> "$day.$month.$fullYear"
                    preferredLang.startsWith(
                        "es",
                        ignoreCase = true
                    ) || preferredLang.startsWith(
                        "fr",
                        ignoreCase = true
                    ) -> "$day/$month/$fullYear"

                    else -> "$month/$day/$fullYear"
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
