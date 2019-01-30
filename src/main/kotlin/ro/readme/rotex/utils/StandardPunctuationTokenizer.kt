package ro.readme.rotex.utils

import java.util.ArrayList

class StandardPunctuationTokenizer {
    fun tokenize(sentence: String): ArrayList<Span> {
        var charType = CharacterClass.WHITESPACE
        var state = charType
        val tokens = ArrayList<Span>()
        val sl = sentence.length
        var start = -1
        var pc: Char = 0.toChar()

        for (ci in 0 until sl) {
            val c = sentence[ci]
            charType = when {
                TextUtils.isWhitespace(c) -> CharacterClass.WHITESPACE
                CharacterClass.STANDARD_PUNCTUATION.contains(c) -> CharacterClass.STANDARD_PUNCTUATION
                else -> CharacterClass.OTHER
            }

            if (state === CharacterClass.WHITESPACE) {
                if (charType !== CharacterClass.WHITESPACE) {
                    start = ci
                }
            } else if (charType !== state && c != pc) {
                tokens.add(Span(start, ci))
                start = ci
            }

            state = charType
            pc = c
        }

        if (charType !== CharacterClass.WHITESPACE) {
            tokens.add(Span(start, sl))
        }

        return tokens
    }
}
