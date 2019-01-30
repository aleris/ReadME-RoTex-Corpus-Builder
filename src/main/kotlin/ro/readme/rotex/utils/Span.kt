package ro.readme.rotex.utils

import java.io.Serializable
import java.util.*

/**
 * Class for storing start and end integer offsets.
 *
 */
class Span
/**
 * Initializes a new Span Object.
 *
 * @param start start of span.
 * @param end end of span, which is +1 more than the last element in the span.
 * @param prob probability of span.
 * @param tag the tag of the span
 */
constructor(
        /**
         * Return the start of a span.
         *
         * @return the start of a span.
         */
        val start: Int,

        /**
         * Return the end of a span.
         *
         * Note: that the returned index is one past the actual end of the span in the
         * text, or the first element past the end of the span.
         *
         * @return the end of a span.
         */
        val end: Int,

        val prob: Double = 0.0,

        var tag: Long = DefaultTags.NO_TAG

) : Comparable<Span>, Serializable {

    init {
        if (start < 0) {
            throw IllegalArgumentException("start index must be zero or greater: $start")
        }
        if (end < 0) {
            throw IllegalArgumentException("end index must be zero or greater: $end")
        }
        if (start > end) {
            throw IllegalArgumentException("start index must not be larger than end index: "
                    + "start=" + start + ", end=" + end)
        }
    }

    /**
     * Returns the length of this span.
     *
     * @return the length of the span.
     */
    fun length(): Int {
        return end - start
    }

    /**
     * Returns true if the specified span is contained by this span. Identical
     * spans are considered to contain each other.
     *
     * @param s The span to compare with this span.
     *
     * @return true is the specified span is contained by this span; false otherwise.
     */
    operator fun contains(s: Span): Boolean {
        return start <= s.start && s.end <= end
    }

    /**
     * Returns true if the specified index is contained inside this span. An index
     * with the value of end is considered outside the span.
     *
     * @param index the index to test with this span.
     *
     * @return true if the span contains this specified index; false otherwise.
     */
    operator fun contains(index: Int): Boolean {
        return start <= index && index < end
    }

    /**
     * Returns true if the specified span is the begin of this span and the
     * specified span is contained in this span.
     *
     * @param s The span to compare with this span.
     *
     * @return true if the specified span starts with this span and is contained
     * in this span; false otherwise
     */
    fun startsWith(s: Span): Boolean {
        return start == s.start && contains(s)
    }

    /**
     * Returns true if the specified span intersects with this span.
     *
     * @param other The span to compare with this span.
     *
     * @return true is the spans overlap; false otherwise.
     */
    fun intersects(other: Span): Boolean {
        //either s's start is in this or this' start is in s
        return contains(other) || other.contains(this)
                || other.start in start..(end - 1)
                || start in other.start..(other.end - 1)
    }

    /**
     * Returns true is the specified span crosses this span.
     *
     * @param other The span to compare with this span.
     *
     * @return true is the specified span overlaps this span and contains a
     * non-overlapping section; false otherwise.
     */
    fun crosses(other: Span): Boolean {
        //either s's start is in this or this' start is in s
        return (!this.contains(other) && !other.contains(this)
                && (other.start in start..(end - 1) || start in other.start..(other.end - 1)))
    }

    /**
     * Retrieves the string covered by the current span of the specified text.
     *
     * @param text
     *
     * @return the substring covered by the current span
     */
    fun getCoveredText(text: String): String {
        if (end > text.length) {
            throw IllegalArgumentException("The span " + toString()
                    + " is outside the given text which has length " + text.length + "!")
        }

        return text.substring(start, end)
    }

    /**
     * Return a copy of this span with leading and trailing white spaces removed.
     *
     * @param text
     *
     * @return the trimmed span or the same object if already trimmed
     */
    fun trim(text: String): Span {

        var newStartOffset = start

        run {
            var i = start
            while (i < end && TextUtils.isWhitespace(text[i])) {
                newStartOffset++
                i++
            }
        }

        var newEndOffset = end
        var i = end
        while (i > start && TextUtils.isWhitespace(text[i - 1])) {
            newEndOffset--
            i--
        }

        return if (newStartOffset == start && newEndOffset == end) {
            this
        } else if (newStartOffset > newEndOffset) {
            Span(start, start)
        } else {
            Span(newStartOffset, newEndOffset)
        }
    }

    /**
     * Return a copy of this span with leading and trailing given chars removed.
     *
     * @param text
     *
     * @return the trimmed span or the same object if already trimmed
     */
    fun trim(text: String, chars: Array<Char>): Span {

        var newStartOffset = start

        run {
            var i = start
            while (i < end && chars.contains(text[i])) {
                newStartOffset++
                i++
            }
        }

        var newEndOffset = end
        var i = end
        while (i > start && chars.contains(text[i - 1])) {
            newEndOffset--
            i--
        }

        return if (newStartOffset == start && newEndOffset == end) {
            this
        } else if (newStartOffset > newEndOffset) {
            Span(start, start)
        } else {
            Span(newStartOffset, newEndOffset)
        }
    }

    fun hasTag(tag: Long): Boolean {
        return this.tag and tag == tag
    }

    fun addTag(tag: Long) {
        this.tag = this.tag or tag
    }

    /**
     * Compares the specified span to the current span.
     */
    override fun compareTo(other: Span): Int {
        if (start < other.start) {
            return -1
        } else if (start == other.start) {
            if (end > other.end) {
                return -1
            } else if (end < other.end) {
                return 1
            } else {
                return 0
            }
        } else {
            return 1
        }
    }

    /**
     * Generates a hash code of the current span.
     */
    override fun hashCode(): Int {
        return Objects.hash(start, end)
    }

    /**
     * Checks if the specified span is equal to the current span.
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }

        if (other is Span) {
            val s = other as Span?

            return (start == s!!.start && end == s.end)
        }

        return false
    }

    /**
     * Generates a human readable string.
     */
    override fun toString(): String {
        val toStringBuffer = StringBuilder(10)
        toStringBuffer.append("([")
        toStringBuffer.append(start)
        toStringBuffer.append("..")
        toStringBuffer.append(end)
        toStringBuffer.append("]#")
        toStringBuffer.append(tag)
        toStringBuffer.append(")")
        return toStringBuffer.toString()
    }

    companion object {

        /**
         * Converts an array of [Span]s to an array of [String]s.
         *
         * @param spans
         * @param s
         * @return the strings
         */
        fun spansToStrings(spans: List<Span>, s: String): List<String> {
            return spans.map {
                it.getCoveredText(s)
            }
        }

        /**
         * Converts an sequence of [Span]s to an sequence of [String]s.
         *
         * @param spans
         * @param s
         * @return the strings
         */
        fun spansToStrings(spans: Sequence<Span>, s: String): Sequence<String> {
            return spans.map {
                it.getCoveredText(s)
            }
        }
    }

}
