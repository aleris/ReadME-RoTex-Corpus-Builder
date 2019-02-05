package ro.readme.rotex.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StandardPunctuationTokenizerTest {

    @Test
    fun testTokenize1() {
        test("Este unul.",
                arrayListOf("Este", "unul", "."))
    }

    @Test
    fun testTokenize2() {
        test("Este unul,doi.",
            arrayListOf("Este", "unul", ",", "doi", "."))
    }

    @Test
    fun testTokenize3() {
        test("Este unul;doi.",
            arrayListOf("Este", "unul", ";", "doi", "."))
    }

    @Test
    fun testTokenize4() {
        test("Este unul! Este doi? Este: 3. Este 3;4.",
            arrayListOf("Este", "unul", "!", "Este", "doi", "?", "Este", ":", "3", ".", "Este", "3", ";", "4", "."))
    }

    @Test
    fun testTokenize5() {
        test("Este greșit1",
            arrayListOf("Este", "greșit1"))
    }

    @Test
    fun testTokenize6() {
        test("Este greș|t",
            arrayListOf("Este", "greș|t"))
    }


    private fun test(s: String, result: List<String>) {
        val tokenSpans = StandardPunctuationTokenizer().tokenize(s)
        val tokens = Span.spansToStrings(tokenSpans, s)
        println("IN: $s")
        println("EXPECT: \"${result.joinToString("\", \"")}\"")
        println("RESULT: \"${tokens.joinToString("\", \"")}\"")
        assertEquals(true, tokens == result)
    }
}
