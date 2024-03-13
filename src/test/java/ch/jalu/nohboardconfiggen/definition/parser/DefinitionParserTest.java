package ch.jalu.nohboardconfiggen.definition.parser;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link DefinitionParser}.
 */
class DefinitionParserTest {

    private final DefinitionParser parser = new DefinitionParser();

    @Nested
    class HeaderLineParse {

        @Test
        void shouldParseEmptyLine() {
            // given / when
            parser.parseHeaderLine("", 20);
            parser.parseHeaderLine(" ", 20);

            // then
            assertThat(parser.propertyNamesToValue, anEmptyMap());
        }

        @Test
        void shouldParseComment() {
            // given / when
            parser.parseHeaderLine("# Test", 20);
            parser.parseHeaderLine("  # Comment", 20);

            // then
            assertThat(parser.propertyNamesToValue, anEmptyMap());
        }

        @Test
        void shouldParseProperties() {
            // given / when
            parser.parseHeaderLine("[width=40px]", 20);
            parser.parseHeaderLine("[height=36px, keyboard=fr]", 20);

            // then
            assertThat(parser.propertyNamesToValue, aMapWithSize(3));
            assertThat(parser.propertyNamesToValue.get("width"), equalTo("40px"));
            assertThat(parser.propertyNamesToValue.get("height"), equalTo("36px"));
            assertThat(parser.propertyNamesToValue.get("keyboard"), equalTo("fr"));
        }

        /**
         * Tests some variations in whitespace in property declarations.
         */
        @Test
        void shouldParseProperties2() {
            // given / when
            parser.parseHeaderLine("[ width =  30px ]", 20);
            parser.parseHeaderLine("[ height= 20 , keyboard =de ]", 20);

            // then
            assertThat(parser.propertyNamesToValue, aMapWithSize(3));
            assertThat(parser.propertyNamesToValue.get("width"), equalTo("30px"));
            assertThat(parser.propertyNamesToValue.get("height"), equalTo("20"));
            assertThat(parser.propertyNamesToValue.get("keyboard"), equalTo("de"));
        }

        @Test
        void shouldParsePropertiesWithValuesInDoubleQuotes() {
            // given / when
            parser.parseHeaderLine("""
                [ keyboard="nl", width="20px", height="30" ]""", 2);

            // then
            assertThat(parser.propertyNamesToValue, aMapWithSize(3));
            assertThat(parser.propertyNamesToValue.get("keyboard"), equalTo("nl"));
            assertThat(parser.propertyNamesToValue.get("width"), equalTo("20px"));
            assertThat(parser.propertyNamesToValue.get("height"), equalTo("30"));
        }

        @Test
        void shouldHandleEscapesAndNotParseSpecialCharsWithinDoubleQuotes() {
            // given / when
            parser.parseHeaderLine("""
                [ title="T\\"A", subtitle="[\\\\o=D]", hint="a,b,\\$c" ]""", 8);

            // then
            assertThat(parser.propertyNamesToValue, aMapWithSize(3));
            assertThat(parser.propertyNamesToValue.get("title"), equalTo("T\"A"));
            assertThat(parser.propertyNamesToValue.get("subtitle"), equalTo("[\\o=D]"));
            assertThat(parser.propertyNamesToValue.get("hint"), equalTo("a,b,$c"));
        }

        @Test
        void shouldParsePropertyFollowedByComment() {
            // given / when
            parser.parseHeaderLine("[ width = 20 ] # note: check this", 6);

            // then
            assertThat(parser.propertyNamesToValue, aMapWithSize(1));
            assertThat(parser.propertyNamesToValue.get("width"), equalTo("20"));
        }

        @Test
        void shouldThrowForInvalidCharactersOutsideOfDoubleQuotes() {
            // given / when
            IllegalStateException ex1 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[title = test.toast ]", 4));
            IllegalStateException ex2 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[ symbol = ? ]", 4));
            IllegalStateException ex3 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[ comma = , ]", 4));

            // then
            assertThat(ex1.getMessage(), equalTo("Unexpected character '.' on line 4, column 14"));
            assertThat(ex2.getMessage(), equalTo("Unexpected character '?' on line 4, column 11. Use double quotes around complex values"));
            assertThat(ex3.getMessage(), equalTo("Unexpected character ',' on line 4, column 10. Use double quotes around complex values"));
        }

        @Test
        void shouldThrowForInvalidPropertyNameChars() {
            // given / when
            IllegalStateException ex1 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[ wîdth = unset ]", 4));
            IllegalStateException ex2 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[ max-height = 40 ]", 4));
            IllegalStateException ex3 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[ t(e)st = 30 ]", 4));

            // then
            assertThat(ex1.getMessage(), equalTo("Expected '=' but got 'î' on line 4, column 4"));
            assertThat(ex2.getMessage(), equalTo("Expected '=' but got '-' on line 4, column 6"));
            assertThat(ex3.getMessage(), equalTo("Expected '=' but got '(' on line 4, column 4"));
        }

        @Test
        void shouldThrowForUnexpectedEndOfLine() {
            // given / when
            IllegalStateException ex1 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[title = 20", 4));
            IllegalStateException ex2 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[title =", 4));
            IllegalStateException ex3 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[title", 4));
            IllegalStateException ex4 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[", 4));
            IllegalStateException ex5 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[ width = 20, title = ", 4));
            IllegalStateException ex6 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[ title = \"t\", ", 4));

            // then
            Stream.of(ex1, ex2, ex3, ex4, ex5, ex6).forEach(ex -> {
                assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
            });
        }

        @Test
        void shouldThrowForUnexpectedComma() {
            // given / when
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[ , width = 20 ]", 4));

            // then
            assertThat(ex.getMessage(), equalTo("Expected property identifier ([a-zA-Z0-9_]), but got ',' on line 4, column 3"));
        }

        @Test
        void shouldThrowForUnclosedDoubleQuotes() {
            // given
            String invalidString1 = """
                [title = "test]""";
            String invalidString2 = """
                [title = "test\\"]""";

            // when
            IllegalStateException ex1 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine(invalidString1, 4));
            IllegalStateException ex2 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine(invalidString2, 4));

            // then
            assertThat(ex1.getMessage(), equalTo("Unexpected end of line; \" not closed on line 4"));
            assertThat(ex2.getMessage(), equalTo("Unexpected end of line; \" not closed on line 4"));
        }

        @Test
        void shouldThrowForUnknownEscape() {
            // given / when
            IllegalStateException ex1 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[title = \"te\\a\"]", 4));
            IllegalStateException ex2 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[title = \"po \\:\"", 4));

            // then
            assertThat(ex1.getMessage(), equalTo("Unknown escape: \\a on line 4, column 14"));
            assertThat(ex2.getMessage(), equalTo("Unknown escape: \\: on line 4, column 15"));
        }

        @Test
        void shouldParseValueVariable() {
            // given / when
            parser.parseHeaderLine("$keyWidth=40", 20);
            parser.parseHeaderLine("$keyHeight = 20px", 21);

            // then
            assertThat(parser.variablesByName, aMapWithSize(2));
            assertThat(parser.variablesByName.get("keyWidth"), equalTo(new DefinitionParser.ValueVariable("keyWidth", "40")));
            assertThat(parser.variablesByName.get("keyHeight"), equalTo(new DefinitionParser.ValueVariable("keyHeight", "20px")));
        }

        @Test
        void shouldParsePropertyVariable() {
            // given / when
            parser.parseHeaderLine("$bigKey = [width = 60, height =  54]", 20);
            parser.parseHeaderLine("$smallKey=[width=30px,height=28px]", 21);

            // then
            assertThat(parser.variablesByName, aMapWithSize(2));

            List<DefinitionParser.Property> expectedBigKeyProperties = List.of(
                new DefinitionParser.Property("width", "60"),
                new DefinitionParser.Property("height", "54"));
            assertThat(parser.variablesByName.get("bigKey"),
                equalTo(new DefinitionParser.PropertyVariable("bigKey", expectedBigKeyProperties)));
            List<DefinitionParser.Property> expectedSmallKeyProperties = List.of(
                new DefinitionParser.Property("width", "30px"),
                new DefinitionParser.Property("height", "28px"));
            assertThat(parser.variablesByName.get("smallKey"),
                equalTo(new DefinitionParser.PropertyVariable("smallKey", expectedSmallKeyProperties)));
        }
    }
}