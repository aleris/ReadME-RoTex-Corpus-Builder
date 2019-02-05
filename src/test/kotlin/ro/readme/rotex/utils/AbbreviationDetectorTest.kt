package ro.readme.rotex.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class AbbreviationDetectorTest {

    private val abbreviationDetector = AbbreviationDetector()

    @Test
    fun isWhiteListAbbreviationYes() {
        assertTrue(abbreviationDetector.isWhiteListAbbreviation("nr."))
    }

    @Test
    fun isWhiteListAbbreviationNot() {
        assertFalse(abbreviationDetector.isWhiteListAbbreviation("numărul"))
    }

    @Test
    fun isLikelyAbbreviationYes1() {
        assertTrue(abbreviationDetector.isLikelyAbbreviation("B.E.J.A."))
    }

    @Test
    fun isLikelyAbbreviationYes2() {
        assertTrue(abbreviationDetector.isLikelyAbbreviation("M. Ap. N."))
    }

    @Test
    fun isLikelyAbbreviationYes3() {
        assertTrue(abbreviationDetector.isLikelyAbbreviation("a. bDe.f."))
    }

    @Test
    fun isLikelyAbbreviationYes4() {
        assertTrue(abbreviationDetector.isLikelyAbbreviation("M."))
    }

    @Test
    fun isLikelyAbbreviationNo1() {
        assertFalse(abbreviationDetector.isLikelyAbbreviation("Ministerul"))
    }

    @Test
    fun isLikelyAbbreviationNo2() {
        assertFalse(abbreviationDetector.isLikelyAbbreviation("za. Pentru"))
    }

    @Test
    fun isLikelyAbbreviationNo3() {
        assertFalse(abbreviationDetector.isLikelyAbbreviation("a. b2. c."))
    }

    @Test
    fun isLikelyAbbreviationNo4() {
        assertFalse(abbreviationDetector.isLikelyAbbreviation("a. bcdef. g."))
    }

    @Test
    fun isLikelyAbbreviationNo5() {
        assertFalse(abbreviationDetector.isLikelyAbbreviation("este. R."))
    }

    @Test
    fun isLikelyAbbreviationNo6() {
        assertFalse(abbreviationDetector.isLikelyAbbreviation("za. R."))
    }

    @Test
    fun isLikelyAbbreviationNo7() {
        assertFalse(abbreviationDetector.isLikelyAbbreviation("noi."))
    }

    @Test
    fun isAbbreviation1() {
        assertTrue(abbreviationDetector.isAbbreviation("nr."))
    }

    @Test
    fun isAbbreviation2() {
        assertTrue(abbreviationDetector.isAbbreviation("B.E.J.A."))
    }

    @Test
    fun checkAbbreviation1() {
        val abbreviationItem = abbreviationDetector.checkAbbreviation("dr.")
        assertTrue(abbreviationItem.isAbbreviation)
        assertTrue(abbreviationItem.requireContinuation)
    }

    @Test
    fun checkAbbreviation2() {
        val abbreviationItem = abbreviationDetector.checkAbbreviation("etc.")
        assertTrue(abbreviationItem.isAbbreviation)
        assertFalse(abbreviationItem.requireContinuation)
    }

    @Test
    fun checkAbbreviation3() {
        val abbreviationItem = abbreviationDetector.checkAbbreviation("B.E.J.A.")
        assertTrue(abbreviationItem.isAbbreviation)
        assertTrue(abbreviationItem.requireContinuation)
    }
}
