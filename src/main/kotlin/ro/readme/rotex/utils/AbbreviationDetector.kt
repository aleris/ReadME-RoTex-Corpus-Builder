package ro.readme.rotex.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


class AbbreviationDetector {
    private val abbreviationMap by lazy {
        val map = HashMap<String, Boolean>()

        File(javaClass.classLoader.getResource("abbreviations_ro.csv").toURI())
            .readLines()
            .forEach { line: String ->
                if (!line.isEmpty()) {
                    val parts = line.split(";")
                    val abbreviation = parts[0]
                    val requireContinuation = parts[2].toBoolean()
                    map[abbreviation] = requireContinuation
                }
            }

        map
    }

    fun checkAbbreviation(text: String): AbbreviationItem {
        val abbreviationItem = checkWhiteListAbbreviation(text)
        if (abbreviationItem.isAbbreviation) return abbreviationItem

        if (isLikelyAbbreviation(text)) {
            return AbbreviationItem(text, isAbbreviation = true, requireContinuation = true)
        }

        return AbbreviationItem(text, isAbbreviation = false, requireContinuation = false)
    }

    fun isAbbreviation(text: String): Boolean {
        if (isWhiteListAbbreviation(text)) return true
        return isLikelyAbbreviation(text)
    }

    private fun checkWhiteListAbbreviation(text: String): AbbreviationItem {
        val r = abbreviationMap[text.toLowerCase()]
        return if (r != null) {
            AbbreviationItem(text, true, r)
        } else {
            AbbreviationItem(text, isAbbreviation = false, requireContinuation = false)
        }
    }

    fun isWhiteListAbbreviation(text: String): Boolean {
        return abbreviationMap.containsKey(text.toLowerCase())
    }

    fun isLikelyAbbreviation(text: String): Boolean {
        var state = CharacterClass.WHITESPACE
        var countGroups = 0
        var countMaxAlpha = 0
        val groupLengths = intArrayOf(0, 0, 0, 0, 0)
        val groupCases = intArrayOf(0, 0, 0, 0, 0)
        for (ci in 0..text.lastIndex) {
            val c = text[ci]
            val charType = when {
                TextUtils.isWhitespace(c) -> CharacterClass.WHITESPACE
                Character.isLetter(c) -> CharacterClass.ALPHABETIC
                CharacterClass.DOT.contains(c) -> CharacterClass.DOT
                else -> CharacterClass.OTHER
            }

            if (charType == CharacterClass.OTHER) {
                return false
            }

            if (charType == CharacterClass.ALPHABETIC && countGroups < groupLengths.size) {
                groupLengths[countGroups]++
                groupCases[countGroups] = if (Character.isUpperCase(c)) 2 else 1
            }

            if (state == CharacterClass.ALPHABETIC) {
                if (charType == CharacterClass.DOT) {
                    countGroups++
                } else if (charType == CharacterClass.ALPHABETIC) {
                    countMaxAlpha++
                    if (countMaxAlpha > 2 && Character.isLowerCase(c)) {
                        return false
                    }
                    if (countMaxAlpha > 3) {
                        return false
                    }
                } else {
                    countMaxAlpha = 0
                }
            }

            state = charType
        }
        if (countGroups == 1 && groupLengths[0] == 1) return true
        if (countGroups == 2 && groupLengths[1] == 1) return false
        if (countGroups == 1 && groupCases[0] == 1) return false
        return 1 < countGroups
    }


    data class AbbreviationItem(val value: String, val isAbbreviation: Boolean, val requireContinuation: Boolean)
}


