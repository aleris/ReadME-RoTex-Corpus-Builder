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

    fun stitchLines(): TextCleaner {
        val result = StringBuilder()
        Scanner(cleaned).use { scanner ->
            var stitchedLine = StringBuilder()
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                if (stitchedLine.isNotBlank() && line.matches(Regex("^[A-ZĂÂÎȘȚ][^.]+"))) {
                    result.append(stitchedLine.toString().replace(Regex("  "), " ").trim()).append(" \n")
                    stitchedLine = StringBuilder(line)
                } else {
                    stitchedLine.append(" ").append(line)
                }
            }
            result.append(stitchedLine.toString().replace(Regex("  "), " ").trim())
        }
        cleaned = result.toString()
        return this
    }
}
