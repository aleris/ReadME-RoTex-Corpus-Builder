package ro.readme.rotex.utils

import java.util.*

class TextCleaner(var cleaned: String) {

    fun stripDiacritics(): TextCleaner {
        cleaned = cleaned
            .replace('ă', 'a')
            .replace('â', 'a')
            .replace('î', 'i')
            .replace('ș', 's')
            .replace('ț', 't')
            .replace('Ă', 'A')
            .replace('Â', 'A')
            .replace('Î', 'I')
            .replace('Ș', 'S')
            .replace('Ț', 'T')
        return this
    }

    fun correctCedilaDiacritics(): TextCleaner {
        cleaned = cleaned
            .replace('ş', 'ș')
            .replace('Ş', 'Ș')
            .replace('ţ', 'ț')
            .replace('Ţ', 'Ț')
        return this
    }

    fun stripForSafeFileName(): TextCleaner {
        cleaned = cleaned
            .replace("/", "")
            .replace("\\", "")
            .replace("?", "")
            .replace("%", "")
            .replace("*", "")
            .replace(":", "")
            .replace("|", "")
            .replace("\"", "")
            .replace("<", "")
            .replace(">", "")
            //.replace(".", "")
        return this
    }

    fun correctHtmlEntitiesDiacritics(): TextCleaner {
        cleaned = cleaned
            .replace("&#539;", "ț")
            .replace("&#538;", "Ț")
            .replace("&#537;", "ș")
            .replace("&#536;", "Ș")
        return this
    }

    fun replaceEPubMultipleSpacesWithNewLine(): TextCleaner {
        cleaned = cleaned.replace("     ", "\n")
        return this
    }

    fun replaceWithGuessedDiacritics(): TextCleaner {
        val diacriticsGuesser = DiacriticsGuesser()
        val map = diacriticsGuesser.guessDiacriticsReplacements(cleaned)
        map.forEach { c, r ->
            cleaned = cleaned.replace(c, r)
        }
        return this
    }

    fun replaceMultipleSpacesWithNewLine(): TextCleaner {
        cleaned = cleaned.replace(Regex("  +"), "\n")
        return this
    }

    fun stitchLinesSimple(): TextCleaner {
        val result = StringBuilder()
        var previousWasEndSentence = true
        cleaned.lines().forEach { line ->
            if (previousWasEndSentence ||
                line.matches(Regex("^[A-ZĂÂÎȘȚ].+"))) {
                result.appendln()
            } else {
                result.append(" ")
            }
            val trimmedLine = line.trim()
            result.append(trimmedLine)
            previousWasEndSentence = trimmedLine.endsWith(CharacterClass.END_SENTENCE.toString())
        }
        result.appendln()
        cleaned = result.toString()
        return this
    }

    fun stripLineBrakeReplacement(): TextCleaner {
        cleaned = cleaned.replace("¬", "")
        return this
    }

    fun removeDuplicateLinesIgnoringNumbers(): TextCleaner {
        fun stripMappedLine(line: String) = line
            .replace(Regex("\\s+"), "")
            .replace(Regex("\\d+", RegexOption.MULTILINE), "")
            .replace(Regex("[IVXLCDM]+", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)), "")

        fun buildRepeatedLinesMap(text: String): Map<String, Int> {
            val repeatedHap = HashMap<String, Int>()

            val lines = text.lines()
            lines.forEach { line ->
                val mappedLine = stripMappedLine(line)
                if (mappedLine.isNotBlank()) {
                    repeatedHap.compute(mappedLine) { _, v -> if (null == v) 1 else v + 1 }
                }
            }

            val threshold = Math.max(5, (lines.size / 30) / 5)
            return repeatedHap.filter { me -> threshold < me.value }
        }

        val repeated = buildRepeatedLinesMap(cleaned)

        val sb = StringBuilder()
        cleaned.lines().forEach {
            val stripped = stripMappedLine(it)
            if (!repeated.containsKey(stripped)) {
                sb.appendln(it)
            }
        }

        cleaned = sb.toString()
        return this
    }

    fun removeLikelyPageNumber(): TextCleaner {
        cleaned = cleaned.replace(
            Regex(
                "^(?:(?:pag|pg|pagina|pp)\\.?)?\\s*(?:nr\\.?\\s*)?(?:(?:[IVXLCDM]|\\d)+.{1,4})?(?:\\d|[IVXLCDM])+\\.?\$",
                setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE
                )
            ),
            "")
        return this
    }

    fun removeDuplicateBlankLines(): TextCleaner {
        val sb = StringBuilder()
        var previousWasBlank = true
        cleaned.lines().forEach {
            if (!(it.isBlank() && previousWasBlank)) {
                sb.appendln(it)
            }
            previousWasBlank = it.isBlank()
        }
        cleaned = sb.toString()
        return this
    }
}
