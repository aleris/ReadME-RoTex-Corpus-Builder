package ro.readme.rotex.utils

class DiacriticsGuesser {
    private val foundMap = HashMap<Char, Char?>()
    private val replacementsMap = HashMap<Char, Char>()

    fun guessDiacriticsReplacements(text: String): Map<Char, Char> {
        CharacterClass.ROMANIAN_DIACRITICS.characters!!.forEach { foundMap[it] = null }

        val tokens = StandardPunctuationTokenizer().tokenize(text)
        for (i in 0..1) {
            for (token in tokens) {
                val tokenText = token.getCoveredText(text)
                // No words with a single letter with diacritic
                if (tokenText.length < 2) {
                    continue
                }

                // skip numbers
                if (TextUtils.hasOnlyDigits(tokenText)) {
                    continue
                }

                // replace diacritics replacements found so far
                val bestWord = replaceDiacritcsFromMap(tokenText)

                if (TextUtils.hasOnlyLetters(bestWord)) {
                    if (DexDictionary.containsWord(bestWord)) {
                        saveReplacementFromAlreadyCompleteWord(bestWord)
                        if (isMapComplete(foundMap)) return replacementsMap
                    }
                    continue
                }

                val nli = findSingleNonAsciiPos(bestWord)
                if (nli < 0) {
                    continue
                }

                val o = bestWord[nli]
                val isFirstInSentence = nli == 0 && previousWasEndSentence(token.start - 1, text)
                val allOtherAreUppercase = allOtherAreUppercase(bestWord, nli)
                if (isFirstInSentence || allOtherAreUppercase) {
                    for (d in CharacterClass.ROMANIAN_DIACRITICS.characters.filter { it.isUpperCase() }) {
                        if (saveReplacementFromReplacedWord(bestWord, o, d)) break
                    }
                } else {
                    for (d in CharacterClass.ROMANIAN_DIACRITICS.characters.filter { it.isLowerCase() }) {
                        if (saveReplacementFromReplacedWord(bestWord, o, d)) break
                    }
                }
                if (isMapComplete(foundMap)) return replacementsMap
            }
            if (isMapComplete(foundMap)) return replacementsMap
        }
        return replacementsMap
    }

    private fun saveReplacementFromReplacedWord(bestWord: String, o: Char, d: Char): Boolean {
        val withReplacement = bestWord.replaceFirst(o, d)
        if (DexDictionary.containsWord(withReplacement)) {
            if (null == foundMap[d]) {
                foundMap[d] = o
            }
            replacementsMap[o] = d
            return true
        }
        return false
    }

    private fun replaceDiacritcsFromMap(word: String): String {
        var r = word
        for (c in word) {
            if (!Character.isLetter(c)) {
                val rc = replacementsMap[c]
                if (null != rc) {
                    r = r.replace(c, rc)
                }
            }
        }
        return r
    }

    private fun saveReplacementFromAlreadyCompleteWord(word: String) {
        for (c in word) {
            if (null == foundMap[c] && CharacterClass.ROMANIAN_DIACRITICS.contains(c)) {
                foundMap[c] = c
                replacementsMap[c] = c
            }
        }
    }

    private fun isMapComplete(map: HashMap<Char, Char?>): Boolean =
        map.filter { it.value == null }.isEmpty()

    private fun findSingleNonAsciiPos(word: String): Int {
        var ci = -1
        for ((i, c) in word.withIndex()) {
            if (!c.isLetter() || !TextUtils.isAsciiCharacter(c)) {
                ci = if (-1 < ci) {
                    -2
                } else {
                    i
                }
            }
        }
        return ci
    }

    private fun allOtherAreUppercase(word: String, excludeCharIndex: Int): Boolean {
        for ((i, c) in word.withIndex()) {
            if (i == excludeCharIndex) {
                continue
            }
            if (!c.isUpperCase()) {
                return false
            }
        }
        return true
    }

    private fun previousWasEndSentence(index: Int, text: String): Boolean {
        var i = index
        while (0 <= i--) {
            val c = text[i]
            if (TextUtils.isWhitespace(c)) continue
            if (CharacterClass.END_SENTENCE.contains(c)) return true
            return false
        }
        return true
    }
}
