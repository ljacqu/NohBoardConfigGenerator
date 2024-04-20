package ch.jalu.nohboardconfiggen.definition.parser;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link Tokenizer}.
 */
class TokenizerTest {

    @Test
    void shouldReturnNext() {
        // given
        Tokenizer tokenizer = new Tokenizer("abcd", 12);

        // when / then
        assertThat(tokenizer.next(), equalTo('a'));
        assertThat(tokenizer.next(), equalTo('b'));
        assertThat(tokenizer.next(), equalTo('c'));
    }

    @Test
    void shouldReturnAllNextMatching() {
        // given
        Tokenizer tokenizer1 = new Tokenizer("1337 hacks", 3);
        Tokenizer tokenizer2 = new Tokenizer("1337", 3);

        // when / then
        assertThat(tokenizer1.nextAllMatching(Character::isDigit, false), equalTo("1337"));
        assertThat(tokenizer1.next(), equalTo(' '));
        assertThat(tokenizer2.nextAllMatching(Character::isDigit, false), equalTo("1337"));
        assertThat(tokenizer2.hasNext(), equalTo(false));
    }

    @Test
    void shouldReturnNothingIfNoMatch() {
        // given
        Tokenizer tokenizer = new Tokenizer("1337 hacks", 3);

        // when / then
        assertThat(tokenizer.nextAllMatching(Character::isWhitespace, false), equalTo(""));
        assertThat(tokenizer.next(), equalTo('1'));
    }

    @Test
    void shouldReturnAllMatchingIgnoringWhitespace() {
        // given
        Tokenizer tokenizer1 = new Tokenizer("  1337 hacks", 1);
        Tokenizer tokenizer2 = new Tokenizer("  abc", 1);
        Tokenizer tokenizer3 = new Tokenizer("1337hacks", 1);
        Tokenizer tokenizer4 = new Tokenizer("1337 hacks", 1);

        // when / then
        assertThat(tokenizer1.nextAllMatching(Character::isDigit, true), equalTo("1337"));
        assertThat(tokenizer1.next(), equalTo(' '));
        assertThat(tokenizer2.nextAllMatching(Character::isDigit, true), equalTo(""));
        assertThat(tokenizer2.next(), equalTo('a'));
        assertThat(tokenizer3.nextAllMatching(Character::isDigit, true), equalTo("1337"));
        assertThat(tokenizer3.next(), equalTo('h'));
        assertThat(tokenizer4.nextAllMatching(Character::isDigit, true), equalTo("1337"));
        assertThat(tokenizer4.next(), equalTo(' '));
    }

    @Test
    void shouldPeekAndNotAdvanceInternalPointer() {
        // given
        Tokenizer tokenizer = new Tokenizer("The quick brown fox", 1);

        // when / then
        assertThat(tokenizer.peek(), equalTo('T'));
        assertThat(tokenizer.peek(), equalTo('T'));
        assertThat(tokenizer.peek(), equalTo('T'));
    }

    @Test
    void shouldSkipWhitespaceOrDoNothing() {
        // given
        Tokenizer tokenizer1 = new Tokenizer("   Test", 3);
        Tokenizer tokenizer2 = new Tokenizer("Test", 4);

        // when
        tokenizer1.skipWhitespace();
        tokenizer2.skipWhitespace();

        // then
        assertThat(tokenizer1.next(), equalTo('T'));
        assertThat(tokenizer2.next(), equalTo('T'));
    }

    @Test
    void shouldExpectAfterOptionalWhitespace() {
        // given
        Tokenizer tokenizer1 = new Tokenizer("  ; #", 3);
        Tokenizer tokenizer2 = new Tokenizer("; [", 3);

        // when / then
        tokenizer1.expectCharAfterOptionalWhitespace(';'); // no exception
        assertThat(tokenizer1.nextNonWhitespace(), equalTo('#'));
        tokenizer2.expectCharAfterOptionalWhitespace(';'); // no exception
        assertThat(tokenizer2.nextNonWhitespace(), equalTo('['));
    }

    @Test
    void shouldThrowForCharacterThatIsNotTheExpectedOne() {
        // given
        Tokenizer tokenizer = new Tokenizer("test: d", 12);
        tokenizer.nextAllMatching(Character::isLetter, false);

        // when
        ParserException ex = assertThrows(ParserException.class,
            () -> tokenizer.expectCharAfterOptionalWhitespace('='));

        // then
        assertThat(ex.getMessage(), equalTo("Expected '=' but got ':' on line 12, column 5"));
    }

    @Test
    void shouldCreateLineIdTexts() {
        // given
        Tokenizer tokenizer1 = new Tokenizer("1337 hacks", 17);
        tokenizer1.nextAllMatching(Character::isDigit, true);

        // when / then
        assertThat(tokenizer1.getLineNrText(), equalTo("line 17"));
        assertThat(tokenizer1.getLineNrColText(), equalTo("line 17, column 4"));
    }

    @Nested
    class EndOfLineExceptionTests {

        @Test
        void next() {
            // given
            Tokenizer tokenizer = createLineCharsAtEndOfLine();

            // when
            ParserException ex = assertThrows(ParserException.class,
                tokenizer::next);

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void peek() {
            // given
            Tokenizer tokenizer = createLineCharsAtEndOfLine();

            // when
            ParserException ex = assertThrows(ParserException.class,
                tokenizer::peek);

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void nextNonWhitespace() {
            // given
            Tokenizer tokenizer = createLineCharsAtEndOfLine();

            // when
            ParserException ex = assertThrows(ParserException.class,
                tokenizer::nextNonWhitespace);

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void nextNonWhitespaceAfterWhitespace() {
            // given
            Tokenizer tokenizer = new Tokenizer("a   ", 4);
            tokenizer.next(); // pop 'a'

            // when
            ParserException ex = assertThrows(ParserException.class,
                tokenizer::nextNonWhitespace);

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void expectCharAfterOptionalWhitespace() {
            // given
            Tokenizer tokenizer = createLineCharsAtEndOfLine();

            // when
            ParserException ex = assertThrows(ParserException.class,
                () -> tokenizer.expectCharAfterOptionalWhitespace('}'));

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void nextAllMatching() {
            // given
            Tokenizer tokenizer = createLineCharsAtEndOfLine();

            // when
            ParserException ex = assertThrows(ParserException.class,
                () -> tokenizer.nextAllMatching(Character::isDigit, false));

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void expectCharAfterOptionalWhitespaceAfterWhitespace() {
            // given
            Tokenizer tokenizer = new Tokenizer("a   ", 4);
            tokenizer.next(); // pop 'a'

            // when
            ParserException ex = assertThrows(ParserException.class,
                () -> tokenizer.expectCharAfterOptionalWhitespace(';'));

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void noExceptionForSkipWhitespace() {
            // given
            Tokenizer tokenizer = createLineCharsAtEndOfLine();

            // when / then
            tokenizer.skipWhitespace(); // no exception
        }
    }

    private static Tokenizer createLineCharsAtEndOfLine() {
        Tokenizer tokenizer = new Tokenizer("a", 4);
        tokenizer.next();
        return tokenizer;
    }
}