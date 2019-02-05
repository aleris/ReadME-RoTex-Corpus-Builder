package ro.readme.rotex.utils

import java.io.BufferedReader

class BufferedSentenceReader {
    private val abbreviationDetector = AbbreviationDetector()

    fun read(bufferedReader: BufferedReader) = sequence {
        bufferedReader.use { reader ->
            // using an internal buffer and two pointers

            // the buffer contain the last two candidate sentences
            val currentBuffer = StringBuilder()
            var currentBufferIndex = 0

            // the first pointer is to the possible end of the first sentence in buffer

            var endIndex: Int
            // the second pointer is to the possible end of the second (last) sentence in buffer
            var nextEndIndex = 0

            // the idea is to check abbreviations for the first sentence end and the full next candidate sentence
            // text is needed to ensure the check is done for all plausible text

            // lines are added to the buffer char by char as they are read and processed
            var lineString = reader.readLine()
            var nonWhiteSpacePreviousChar = ' '
            while (null != lineString) {
                var i = 0
                while (i < lineString.length) {
                    val c = lineString[i]
                    currentBuffer.append(c)

                    // Usually sentences ends with special characters, @see END_SENTENCE_SIGNS
                    // Examples: ->Sunt aici.<-, ->Da!!!<-, ->Nu!?!<-, ->Da...<-
                    val isEndSentenceSign = CharacterClass.END_SENTENCE.contains(c)

                    // For citations, the end sentence signs can be enclosed in citation
                    // Examples: ->"Sunt aici..."<-
                    val isEndCitation = CharacterClass.END_CITATION.contains(c) && CharacterClass.END_SENTENCE.contains(
                        nonWhiteSpacePreviousChar
                    )

                    // Special case when the source stream contains titles without any end separator,
                    // consider end of lineString as possible end of sentence.
                    // Examples: ->"Titlu\nÎnceput frază"<-
                    val isEndOfLineNonWhitespace = i == lineString.lastIndex && !TextUtils.isWhitespace(c)

                    if (isEndSentenceSign || isEndCitation || isEndOfLineNonWhitespace) {
                        endIndex = nextEndIndex + 1
                        nextEndIndex = currentBufferIndex

                        if (endIndex in 3..(nextEndIndex - 1)) {
                            if (isNextAfterWhitespaceStartOfSentence(currentBuffer, endIndex)) {
                                var toProcess = true
                                // check for possible abbreviations, if the previous detected end sentence is actually
                                // an abbreviation, advance without yielding a sentence
                                if (CharacterClass.DOT.contains(currentBuffer[endIndex - 1])) {
                                    val abbreviationItem = checkAbbreviation(currentBuffer, endIndex)
                                    if (abbreviationItem.isAbbreviation) {
                                        toProcess = !abbreviationItem.requireContinuation
                                    }
                                }
                                if (toProcess) {
                                    // the first sentence is really a sentence, so send it
                                    val sentence = currentBuffer.substring(0, endIndex).toString().trim()
                                    yield(sentence)

                                    // reset current buffer so that the first sentence is removed
                                    val remaining = currentBuffer.substring(endIndex)
                                    currentBuffer.setLength(0)
                                    currentBuffer.append(remaining)
                                    currentBufferIndex -= endIndex
                                    nextEndIndex -= endIndex
                                }
                            }
                        }
                    }
                    if (!TextUtils.isWhitespace(c)) {
                        nonWhiteSpacePreviousChar = c
                    }
                    currentBufferIndex++
                    i++
                }

                lineString = reader.readLine()
            }
            // also process the last block of text remaining in stream
            if (currentBuffer.isNotEmpty()) {
                val sentence = currentBuffer.toString().trim()
                yield(sentence)
            }
        }
    }

    private fun checkAbbreviation(buffer: StringBuilder, start: Int): AbbreviationDetector.AbbreviationItem {
        // check to the left
        var i = start - 1
        var left = start
        while (0 <= i) {
            val c = buffer[i]
            if (Character.isLetter(c) || CharacterClass.DOT.contains(c)) {
                left = i
            } else {
                break
            }
            i--
        }

        // extend to right and check the entire area
        var right = start
        i = start + 1
        while (i < buffer.length) {
            val c = buffer[i]
            if (Character.isLetter(c) || CharacterClass.DOT.contains(c)) {
                right = i + 1
            } else {
                break
            }
            i++
        }

        val entireText = buffer.substring(left, right)
        if (left < right) {
            val abbreviationItem = abbreviationDetector.checkAbbreviation(entireText)
            if (abbreviationItem.isAbbreviation) {
                return abbreviationItem
            }
        }

        val leftText = buffer.substring(left, start)
        if (left < start) {
            val abbreviationItem = abbreviationDetector.checkAbbreviation(leftText)
            if (abbreviationItem.isAbbreviation) {
                return abbreviationItem
            }
        }

        return AbbreviationDetector.AbbreviationItem(leftText, isAbbreviation = false, requireContinuation = false)
    }

    private fun isNextAfterWhitespaceStartOfSentence(buffer: StringBuilder, start: Int): Boolean {
        var i = start
        while (i < buffer.length) {
            val c = buffer[i]
            if (!TextUtils.isWhitespace(c)) {
                return when {
                    // Starts with upper case
                    Character.isUpperCase(c) -> true

                    // Or starts with a dialog sign
                    CharacterClass.DASH.contains(c) -> true

                    // Or starts with a citation with an uppercase inside
                    i < buffer.lastIndex && CharacterClass.BEGIN_CITATION.contains(c) && Character.isUpperCase(buffer[i + 1]) -> true

                    else -> false
                }
            }
            i++
        }
        return false
    }
}
