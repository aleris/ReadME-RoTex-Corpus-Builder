package ro.readme.rotex.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class SimpleTokenizerTest {

    @Test
    fun testTokenize1() {
        test("Nici \"răspunsul\" nu-l așteptă decât M100 acum?! ",
                arrayListOf("Nici", "\"", "răspunsul", "\"", "nu", "-", "l", "așteptă", "decât", "M", "100", "acum", "?", "!"))
    }

    @Test
    fun testTokenize2() {
        test("Apoi lua 100.000 $ și îl rugă să-i dea «ceva ceva» ce și-ar lua la pct.54. ",
                arrayListOf("Apoi", "lua", "100", ".", "000", "$", "și", "îl", "rugă", "să", "-", "i", "dea", "«", "ceva", "ceva", "»", "ce", "și", "-", "ar", "lua", "la", "pct", ".", "54", "."))
    }

    @Test
    fun testTokenize3() {
        test("Exprimaţi-vă în acest sens într-o doară.",
                arrayListOf("Exprimaţi", "-", "vă", "în", "acest", "sens", "într", "-", "o", "doară", "."))
    }

    @Test
    fun testTokenize4() {
        test("Ia-o pe drum în jos...",
                arrayListOf("Ia", "-", "o", "pe", "drum", "în", "jos", "..."))
    }

    @Test
    fun testTokenize5() {
        test("Du-te s-o asculți.",
                arrayListOf("Du", "-", "te", "s", "-", "o", "asculți", "."))
    }

    @Test
    fun testTokenize6() {
        test("Ce-mi „dați\"?",
                arrayListOf("Ce", "-", "mi", "„", "dați", "\"", "?"))
    }

    @Test
    fun testTokenize7() {
        test("Ce i-a dat!",
                arrayListOf("Ce", "i", "-", "a", "dat", "!"))
    }

    @Test
    fun testTokenize8() {
        test("Și-apoi de ce nu? ",
                arrayListOf("Și", "-", "apoi", "de", "ce", "nu", "?"))
    }

    @Test
    fun testTokenize9() {
        test("În 2000 Daniel-Vasile să-ți aducă. L-au ajutat ei.",
                arrayListOf("În", "2000", "Daniel", "-", "Vasile", "să", "-", "ți", "aducă", ".", "L", "-", "au", "ajutat", "ei", "."))
    }

    @Test
    fun testTokenize10() {
        test("Făcea să-și înfunde nasul.",
                arrayListOf("Făcea", "să", "-", "și", "înfunde", "nasul", "."))
    }

    @Test
    fun testTokenize11() {
        test("Modificarea art.140 alin. (1) din Legea nr. 571/2003 privind Codul fiscal, în sensul revenirii la cota de TVA de 19% la 16,2% din PIB, însumând 22 de miliarde.",
                arrayListOf("Modificarea", "art", ".", "140", "alin", ".", "(", "1", ")", "din", "Legea", "nr", ".", "571", "/", "2003", "privind", "Codul", "fiscal", ",", "în", "sensul", "revenirii", "la", "cota", "de", "TVA", "de", "19", "%", "la", "16", ",", "2", "%", "din", "PIB", ",", "însumând", "22", "de", "miliarde", "."))
    }

    @Test
    fun testTokenize12() {
        test("Din I.N.M.H. sau U.E..",
                arrayListOf("Din", "I", ".", "N", ".", "M", ".", "H", ".", "sau", "U", ".", "E", ".."))
    }

    @Test
    fun testTokenize13() {
        test("Așa au zis: nu e super...",
                arrayListOf("Așa", "au", "zis", ":", "nu", "e", "super", "..."))
    }

    @Test
    fun testTokenize14() {
        test("Așa au zis la 12.04.2011.",
                arrayListOf("Așa", "au", "zis", "la", "12", ".", "04", ".", "2011", "."))
    }

    private fun test(s: String, result: List<String>) {
        val tokenSpans = SimpleTokenizer().tokenize(s)
        val tokens = Span.spansToStrings(tokenSpans, s)
        println("IN: $s")
        println("EXPECT: \"${result.joinToString("\", \"")}\"")
        println("RESULT: \"${tokens.joinToString("\", \"")}\"")
        assertEquals(true, tokens == result)
    }
}
