package ch.jalu.nohboardconfiggen.definition.parser;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link LineChars}.
 */
class LineCharsTest {

    @Test
    void shouldReturnNext() {
        // given
        LineChars lineChars = new LineChars("abcd", 12);

        // when / then
        assertThat(lineChars.next(), equalTo('a'));
        assertThat(lineChars.next(), equalTo('b'));
        assertThat(lineChars.next(), equalTo('c'));
    }

    @Test
    void shouldReturnAllNextMatching() {
        // given
        LineChars lineChars1 = new LineChars("1337 hacks", 3);
        LineChars lineChars2 = new LineChars("1337", 3);

        // when / then
        assertThat(lineChars1.nextAllMatching(Character::isDigit, false), equalTo("1337"));
        assertThat(lineChars1.next(), equalTo(' '));
        assertThat(lineChars2.nextAllMatching(Character::isDigit, false), equalTo("1337"));
        assertThat(lineChars2.hasNext(), equalTo(false));
    }

    @Test
    void shouldReturnNothingIfNoMatch() {
        // given
        LineChars lineChars = new LineChars("1337 hacks", 3);

        // when / then
        assertThat(lineChars.nextAllMatching(Character::isWhitespace, false), equalTo(""));
        assertThat(lineChars.next(), equalTo('1'));
    }

    @Test
    void shouldReturnAllMatchingIgnoringWhitespace() {
        // given
        LineChars lineChars1 = new LineChars("  1337 hacks", 1);
        LineChars lineChars2 = new LineChars("  abc", 1);
        LineChars lineChars3 = new LineChars("1337hacks", 1);
        LineChars lineChars4 = new LineChars("1337 hacks", 1);

        // when / then
        assertThat(lineChars1.nextAllMatching(Character::isDigit, true), equalTo("1337"));
        assertThat(lineChars1.next(), equalTo(' '));
        assertThat(lineChars2.nextAllMatching(Character::isDigit, true), equalTo(""));
        assertThat(lineChars2.next(), equalTo('a'));
        assertThat(lineChars3.nextAllMatching(Character::isDigit, true), equalTo("1337"));
        assertThat(lineChars3.next(), equalTo('h'));
        assertThat(lineChars4.nextAllMatching(Character::isDigit, true), equalTo("1337"));
        assertThat(lineChars4.next(), equalTo(' '));
    }

    @Test
    void shouldPeekAndNotAdvanceInternalPointer() {
        // given
        LineChars lineChars = new LineChars("The quick brown fox", 1);

        // when / then
        assertThat(lineChars.peek(), equalTo('T'));
        assertThat(lineChars.peek(), equalTo('T'));
        assertThat(lineChars.peek(), equalTo('T'));
    }

    @Test
    void shouldSkipWhitespaceOrDoNothing() {
        // given
        LineChars lineChars1 = new LineChars("   Test", 3);
        LineChars lineChars2 = new LineChars("Test", 4);

        // when
        lineChars1.skipWhitespace();
        lineChars2.skipWhitespace();

        // then
        assertThat(lineChars1.next(), equalTo('T'));
        assertThat(lineChars2.next(), equalTo('T'));
    }

    @Test
    void shouldExpectAfterOptionalWhitespace() {
        // given
        LineChars lineChars1 = new LineChars("  ; #", 3);
        LineChars lineChars2 = new LineChars("; [", 3);

        // when / then
        lineChars1.expectCharAfterOptionalWhitespace(';'); // no exception
        assertThat(lineChars1.nextNonWhitespace(), equalTo('#'));
        lineChars2.expectCharAfterOptionalWhitespace(';'); // no exception
        assertThat(lineChars2.nextNonWhitespace(), equalTo('['));
    }

    @Test
    void shouldThrowForCharacterThatIsNotTheExpectedOne() {
        // given
        LineChars lineChars = new LineChars("test: d", 12);
        lineChars.nextAllMatching(Character::isLetter, false);

        // when
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> lineChars.expectCharAfterOptionalWhitespace('='));

        // then
        assertThat(ex.getMessage(), equalTo("Expected '=' but got ':' on line 12, column 5"));
    }

    @Test
    void shouldCreateLineIdTexts() {
        // given
        LineChars lineChars1 = new LineChars("1337 hacks", 17);
        lineChars1.nextAllMatching(Character::isDigit, true);

        // when / then
        assertThat(lineChars1.getLineNrText(), equalTo("line 17"));
        assertThat(lineChars1.getLineNrColText(), equalTo("line 17, column 4"));
    }

    @Nested
    class EndOfLineExceptionTests {

        @Test
        void next() {
            // given
            LineChars lineChars = createLineCharsAtEndOfLine();

            // when
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                lineChars::next);

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void peek() {
            // given
            LineChars lineChars = createLineCharsAtEndOfLine();

            // when
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                lineChars::peek);

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void nextNonWhitespace() {
            // given
            LineChars lineChars = createLineCharsAtEndOfLine();

            // when
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                lineChars::nextNonWhitespace);

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void nextNonWhitespaceAfterWhitespace() {
            // given
            LineChars lineChars = new LineChars("a   ", 4);
            lineChars.next(); // pop 'a'

            // when
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                lineChars::nextNonWhitespace);

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void expectCharAfterOptionalWhitespace() {
            // given
            LineChars lineChars = createLineCharsAtEndOfLine();

            // when
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> lineChars.expectCharAfterOptionalWhitespace('}'));

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void nextAllMatching() {
            // given
            LineChars lineChars = createLineCharsAtEndOfLine();

            // when
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> lineChars.nextAllMatching(Character::isDigit, false));

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void expectCharAfterOptionalWhitespaceAfterWhitespace() {
            // given
            LineChars lineChars = new LineChars("a   ", 4);
            lineChars.next(); // pop 'a'

            // when
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> lineChars.expectCharAfterOptionalWhitespace(';'));

            // then
            assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
        }

        @Test
        void noExceptionForSkipWhitespace() {
            // given
            LineChars lineChars = createLineCharsAtEndOfLine();

            // when / then
            lineChars.skipWhitespace(); // no exception
        }
    }

    private static LineChars createLineCharsAtEndOfLine() {
        LineChars lineChars = new LineChars("a", 4);
        lineChars.next();
        return lineChars;
    }
}