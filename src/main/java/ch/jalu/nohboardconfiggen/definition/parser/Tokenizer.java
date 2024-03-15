package ch.jalu.nohboardconfiggen.definition.parser;

import java.util.function.Predicate;

class Tokenizer {

    private final int lineNumber;
    private final char[] chars;
    private int pos;

    Tokenizer(String line, int lineNumber) {
        this.chars = line.toCharArray();
        this.lineNumber = lineNumber;
    }

    char peek() {
        if (pos < chars.length) {
            return chars[pos];
        }
        throw new IllegalStateException("Unexpected end of line on " + getLineNrText());
    }

    char next() {
        if (pos < chars.length) {
            return chars[pos++];
        }
        throw new IllegalStateException("Unexpected end of line on " + getLineNrText());
    }

    char nextNonWhitespace() {
        skipWhitespace();
        return next();
    }

    void skipWhitespace() {
        while (hasNext()) {
            char chr = chars[pos];
            if (!Character.isWhitespace(chr)) {
                return;
            }
            ++pos;
        }
    }

    String nextAllMatching(Predicate<Character> predicate, boolean ignoreInitialWhitespace) {
        if (ignoreInitialWhitespace) {
            skipWhitespace();
        }
        int start = pos;

        while (hasNext()) {
            char chr = next();
            if (!predicate.test(chr)) {
                --pos;
                break;
            }
        }
        int endExclusive = pos;
        if (start == endExclusive && !hasNext()) {
            throw new IllegalStateException("Unexpected end of line on " + getLineNrText());
        }

        char[] matchingChars = new char[endExclusive - start];
        System.arraycopy(chars, start, matchingChars, 0, endExclusive - start);
        return new String(matchingChars);
    }

    void expectCharAfterOptionalWhitespace(char expectedChar) {
        char next = nextNonWhitespace();
        if (next != expectedChar) {
            throw new IllegalStateException("Expected '" + expectedChar + "' but got '"
                + next + "' on " + getLineNrColText());
        }
    }

    boolean hasNext() {
        return pos < chars.length;
    }

    boolean isEmptyOrHasCommentStart() {
        return !hasNext() || chars[pos] == '#';
    }

    String getLineNrText() {
        return "line " + lineNumber;
    }

    String getLineNrColText() {
        return "line " + lineNumber + ", column " + pos;
    }
}
