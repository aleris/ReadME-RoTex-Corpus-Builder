package ro.readme.rotex.utils

import java.nio.charset.Charset
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object TextUtils {
    fun hasOnlyLetters(s: String): Boolean = s.all { Character.isLetter(it) }

    fun hasOnlyDigits(s: String): Boolean = s.all { Character.isDigit(it) }

    fun isWhitespace(char: Char): Boolean {
        if (Character.isWhitespace(char)) {
            return true
        }

        val type = Character.getType(char)

        return type == Character.SPACE_SEPARATOR.toInt() || type == Character.LINE_SEPARATOR.toInt()
    }

    private val iso88591Encoder = Charset.forName(Charsets.ISO_8859_1.name()).newEncoder()

    fun isRomanianCharacter(c: Char): Boolean =
        iso88591Encoder.canEncode(c)

    private val asciiEncoder = Charset.forName(Charsets.US_ASCII.name()).newEncoder()

    fun isAsciiCharacter(c: Char): Boolean =
        asciiEncoder.canEncode(c)

    fun formatDecimal(number: Int, fractionCount: Int = -1): String {
        return formatDecimal(number.toDouble(), fractionCount)
    }

    fun formatDecimal(number: Long, fractionCount: Int = -1): String {
        return formatDecimal(number.toDouble(), fractionCount)
    }

    fun formatDecimal(number: Double, fractionCount: Int = -1): String {
        val df = DecimalFormat()
        val symbols = DecimalFormatSymbols()
        symbols.groupingSeparator = '.'
        df.decimalFormatSymbols = symbols
        df.groupingSize = 3
        if (fractionCount == -1) {
            if (number % 1 == .0) {
                df.maximumFractionDigits = 0
            } else {
                df.maximumFractionDigits = 2
            }
        } else {
            df.maximumFractionDigits = fractionCount
        }
        return df.format(number)
    }

    fun formatPercent(number: Double): String {
        val df = DecimalFormat()
        val symbols = DecimalFormatSymbols()
        symbols.groupingSeparator = '.'
        df.decimalFormatSymbols = symbols
        df.groupingSize = 3
        df.maximumFractionDigits = 2
        return "${df.format(number)}%"
    }

    fun isSameCase(c1: Char, c2: Char): Boolean {
        return (c1.isLowerCase() && c2.isLowerCase()) ||
                (c1.isUpperCase() && c2.isUpperCase())
    }

    fun getMonthAsTowDigitNumberString(monthName: String): String = when(monthName.toLowerCase()) {
        "ianuarie" -> "01"
        "februarie" -> "02"
        "martie" -> "03"
        "aprilie" -> "04"
        "mai" -> "05"
        "iunie" -> "06"
        "iulie" -> "07"
        "august" -> "08"
        "septembrie" -> "09"
        "octombrie" -> "10"
        "noiembrie" -> "11"
        "decembrie" -> "12"
        else -> "00"
    }
}
