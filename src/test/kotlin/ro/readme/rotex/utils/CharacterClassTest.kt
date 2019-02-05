package ro.readme.rotex.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CharacterClassTest {

    @Test
    fun contains() {
        assertEquals(true, CharacterClass.DOT.contains('.'))
    }

    @Test
    fun containsNot() {
        assertEquals(false, CharacterClass.DOT.contains('!'))
    }

    @Test
    fun containsNotValid() {
        assertThrows(Exception::class.java) {
            CharacterClass.WHITESPACE.contains('a')
        }
    }
}
