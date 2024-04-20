package ch.jalu.nohboardconfiggen.definition.parser;

import java.util.function.Predicate;

/**
 * Goes through a String character by character, allowing to fully process a text. The tokenizer
 * keeps its current position and can deliver one or more characters through its methods.
 */
class Tokenizer {

    private final int lineNumber;
    private final char[] chars;
    /** Current position of this tokenizer in {@link #chars}. */
    private int pos;

    /**
     * Constructor.
     *
     * @param text the string to wrap
     * @param lineNumber the line number to the text comes from (for error messages)
     */
    Tokenizer(String text, int lineNumber) {
        this.chars = text.toCharArray();
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the next character without advancing the internal pointer. Throws an exception
     * if this tokenizer is at the end of the line.
     *
     * @return next character
     */
    char peek() {
        if (pos < chars.length) {
            return chars[pos];
        }
        throw new ParserException("Unexpected end of line on " + getLineNrText());
    }

    /**
     * Returns the next character. Throws an exception if this tokenizer is at the end of the line.
     *
     * @return next character
     */
    char next() {
        if (pos < chars.length) {
            return chars[pos++];
        }
        throw new ParserException("Unexpected end of line on " + getLineNrText());
    }

    /**
     * Returns the next character that is not whitespace. Throws an exception if no non-whitespace character
     * can be found.
     *
     * @return next non-whitespace character
     */
    char nextNonWhitespace() {
        skipWhitespace();
        return next();
    }

    /**
     * Advances this tokenizer's pointer to the next character that is not whitespace.
     */
    void skipWhitespace() {
        while (hasNext()) {
            char chr = chars[pos];
            if (!Character.isWhitespace(chr)) {
                return;
            }
            ++pos;
        }
    }

    /**
     * Returns all characters from the current pointer onwards that consecutively match the given predicate.
     * An exception is thrown if this tokenizer is at the end of the line. Note that an empty String is returned
     * if the next character does not match the predicate.
     *
     * @param predicate the predicate the characters must match
     * @param ignoreInitialWhitespace true if any whitespace at the current pointer should be skipped
     * @return matching characters (may be empty)
     */
    String nextAllMatching(Predicate<Character> predicate, boolean ignoreInitialWhitespace) {
        if (ignoreInitialWhitespace) {
            skipWhitespace();
        }

        int start = pos;
        while (hasNext() && predicate.test(chars[pos])) {
            ++pos;
        }
        int endExclusive = pos;

        if (start == endExclusive && !hasNext()) {
            throw new ParserException("Unexpected end of line on " + getLineNrText());
        }

        char[] matchingChars = new char[endExclusive - start];
        System.arraycopy(chars, start, matchingChars, 0, endExclusive - start);
        return new String(matchingChars);
    }

    /**
     * Verifies that the next character, after possible whitespace, is the provided expected character and advances
     * the pointer of this tokenizer past the character. An exception is thrown if the character does not match,
     * or if this tokenizer is at the end of the line.
     *
     * @param expectedChar the character that is expected to be encountered
     */
    void expectCharAfterOptionalWhitespace(char expectedChar) {
        char next = nextNonWhitespace();
        if (next != expectedChar) {
            throw new ParserException("Expected '" + expectedChar + "' but got '"
                + next + "' on " + getLineNrColText());
        }
    }

    /**
     * @return true if this tokenizer is not at the end of the line
     */
    boolean hasNext() {
        return pos < chars.length;
    }

    /**
     * @return true if this tokenizer is at the end of the line, or if the next character indicates
     *         the start of a comment
     */
    boolean isEmptyOrHasCommentStart() {
        return !hasNext() || chars[pos] == '#';
    }

    /**
     * @return text with the line number, for errors
     */
    String getLineNrText() {
        return "line " + lineNumber;
    }

    /**
     * @return text with the line number and this tokenizer's current position, for errors
     */
    String getLineNrColText() {
        return "line " + lineNumber + ", column " + pos;
    }

    /**
     * Moves the internal pointer of this tokenizer to the end of the line.
     */
    void moveToEnd() {
        pos = chars.length;
    }
}
