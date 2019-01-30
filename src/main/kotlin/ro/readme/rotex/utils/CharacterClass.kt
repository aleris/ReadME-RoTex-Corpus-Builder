package ro.readme.rotex.utils

enum class CharacterClass(val characters: Array<Char>? = null) {
    WHITESPACE(),
    ALPHABETIC(),
    NUMERIC(),
    ALPHANUMERIC(),
    DOT(arrayOf('.')),
    COMMA(arrayOf(',')),
    PERIOD(arrayOf(';')),
    END_SENTENCE(arrayOf('.', '!', '?', ':', ';')),
    BEGIN_CITATION(arrayOf('"', '«', '„', '“')),
    END_CITATION(arrayOf('"', '»', '”', '“')),
    DASH(arrayOf('\u002D', '\u00AD', '\u2013', '\u2014', '\u2015', '\u2011', '\u2012')),
    DATE_SEPARATORS(arrayOf('-', '.', '/', '\\')),
    TIME_SEPARATORS(arrayOf(':', '.')),
    NUMBER_SEPARATORS(arrayOf('.', ',')),
    STANDARD_PUNCTUATION(
        DOT.characters!! +
                COMMA.characters!! +
                PERIOD.characters!! +
                END_SENTENCE.characters!!
    ),
    ROMANIAN_DIACRITICS(arrayOf('ă', 'â', 'î', 'ș', 'ț', 'Ă', 'Â', 'Î', 'Ș', 'Ț')),
    OTHER();

    fun contains(char: Char): Boolean  = characters?.contains(char)
            ?: throw Exception("Cannot check from char list, use specialized functions.")
}
