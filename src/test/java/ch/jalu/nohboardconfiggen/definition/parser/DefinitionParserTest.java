package ch.jalu.nohboardconfiggen.definition.parser;

import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyLine;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyNameSet;
import ch.jalu.nohboardconfiggen.definition.parser.element.Variable.AttributeVariable;
import ch.jalu.nohboardconfiggen.definition.parser.element.Variable.ValueVariable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
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
            assertThat(parser.attributeNamesToValue, anEmptyMap());
        }

        @Test
        void shouldParseComment() {
            // given / when
            parser.parseHeaderLine("# Test", 20);
            parser.parseHeaderLine("  # Comment", 20);

            // then
            assertThat(parser.attributeNamesToValue, anEmptyMap());
        }

        @Test
        void shouldParseAttributes() {
            // given / when
            parser.parseHeaderLine("[width=40px]", 20);
            parser.parseHeaderLine("[height=36px, keyboard=fr]", 20);

            // then
            assertThat(parser.attributeNamesToValue, aMapWithSize(3));
            assertThat(parser.attributeNamesToValue.get("width"), equalTo("40px"));
            assertThat(parser.attributeNamesToValue.get("height"), equalTo("36px"));
            assertThat(parser.attributeNamesToValue.get("keyboard"), equalTo("fr"));
        }

        /**
         * Tests some variations in whitespace in attribute declarations.
         */
        @Test
        void shouldParseAttributes2() {
            // given / when
            parser.parseHeaderLine("[ width =  30px ]", 20);
            parser.parseHeaderLine("[ height= 20 , keyboard =de ]", 20);

            // then
            assertThat(parser.attributeNamesToValue, aMapWithSize(3));
            assertThat(parser.attributeNamesToValue.get("width"), equalTo("30px"));
            assertThat(parser.attributeNamesToValue.get("height"), equalTo("20"));
            assertThat(parser.attributeNamesToValue.get("keyboard"), equalTo("de"));
        }

        @Test
        void shouldParseAttributesWithValuesInDoubleQuotes() {
            // given / when
            parser.parseHeaderLine("""
                [ keyboard="nl", width="20px", height="30" ]""", 2);

            // then
            assertThat(parser.attributeNamesToValue, aMapWithSize(3));
            assertThat(parser.attributeNamesToValue.get("keyboard"), equalTo("nl"));
            assertThat(parser.attributeNamesToValue.get("width"), equalTo("20px"));
            assertThat(parser.attributeNamesToValue.get("height"), equalTo("30"));
        }

        @Test
        void shouldHandleEscapesAndNotParseSpecialCharsWithinDoubleQuotes() {
            // given / when
            parser.parseHeaderLine("""
                [ title="T\\"A", subtitle="[\\\\o=D]", hint="a,b,\\$c" ]""", 8);

            // then
            assertThat(parser.attributeNamesToValue, aMapWithSize(3));
            assertThat(parser.attributeNamesToValue.get("title"), equalTo("T\"A"));
            assertThat(parser.attributeNamesToValue.get("subtitle"), equalTo("[\\o=D]"));
            assertThat(parser.attributeNamesToValue.get("hint"), equalTo("a,b,$c"));
        }

        @Test
        void shouldParseAttributeFollowedByComment() {
            // given / when
            parser.parseHeaderLine("[ width = 20 ] # note: check this", 6);

            // then
            assertThat(parser.attributeNamesToValue, aMapWithSize(1));
            assertThat(parser.attributeNamesToValue.get("width"), equalTo("20"));
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
        void shouldThrowForInvalidAttributeNameChars() {
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
            assertThat(ex.getMessage(), equalTo("Expected attribute identifier ([a-zA-Z0-9_]), but got ',' on line 4, column 3"));
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
            assertThat(parser.variablesByName.get("keyWidth"), equalTo(new ValueVariable("keyWidth", "40")));
            assertThat(parser.variablesByName.get("keyHeight"), equalTo(new ValueVariable("keyHeight", "20px")));
        }

        @Test
        void shouldParseAttributeVariable() {
            // given / when
            parser.parseHeaderLine("$bigKey = [width = 60, height =  54]", 20);
            parser.parseHeaderLine("$smallKey=[width=30px,height=28px]", 21);

            // then
            assertThat(parser.variablesByName, aMapWithSize(2));

            List<Attribute> expectedBigKeyAttributes = List.of(
                new Attribute("width", "60"),
                new Attribute("height", "54"));
            assertThat(parser.variablesByName.get("bigKey"),
                equalTo(new AttributeVariable("bigKey", expectedBigKeyAttributes)));
            List<Attribute> expectedSmallKeyAttributes = List.of(
                new Attribute("width", "30px"),
                new Attribute("height", "28px"));
            assertThat(parser.variablesByName.get("smallKey"),
                equalTo(new AttributeVariable("smallKey", expectedSmallKeyAttributes)));
        }

        @Test
        void shouldParseAttributeWithVariableValue() {
            // given
            parser.parseHeaderLine("$width = 40", 1);

            // when
            parser.parseHeaderLine("[width = $width]", 2);

            // then
            assertThat(parser.attributeNamesToValue, aMapWithSize(1));
            assertThat(parser.attributeNamesToValue.get("width"), equalTo("40"));
        }

        @Test
        void shouldParseVariableDeclarationWithVariable() {
            // given
            parser.parseHeaderLine("$suffix = \"You know?\"", 1);

            // when
            parser.parseHeaderLine("$statement = \"Crazy. $suffix\"", 2);

            // then
            assertThat(parser.variablesByName, aMapWithSize(2));
            assertThat(parser.variablesByName.get("suffix"), equalTo(new ValueVariable("suffix", "You know?")));
            assertThat(parser.variablesByName.get("statement"), equalTo(new ValueVariable("statement", "Crazy. You know?")));
        }

        @Test
        void shouldThrowForEmptyVariableName() {
            // given / when
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("$ = 5", 2));

            // then
            assertThat(ex.getMessage(), equalTo("Expected attribute identifier ([a-zA-Z0-9_]), but got ' ' on line 2, column 2"));
        }

        @Test
        void shouldThrowForInvalidVariableAssignmentSyntax() {
            // given / when
            IllegalStateException ex1 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("$test =", 2));
            IllegalStateException ex2 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("$test = abc def", 3));
            IllegalStateException ex3 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("$test : 50", 4));
            IllegalStateException ex4 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("$test", 5));

            // then
            assertThat(ex1.getMessage(), equalTo("Unexpected end of line on line 2"));
            assertThat(ex2.getMessage(), equalTo("Expected end of line, but got 'd' on line 3, column 13"));
            assertThat(ex3.getMessage(), equalTo("Expected '=' but got ':' on line 4, column 7"));
            assertThat(ex4.getMessage(), equalTo("Unexpected end of line on line 5"));
        }
    }

    @Nested
    class KeyLineParse {

        @Test
        void shouldIgnoreEmptyLineOrLineWithComment() {
            // given / when / then
            assertThat(parser.parseKeyLine("", 1), nullValue());
            assertThat(parser.parseKeyLine(" \t  ", 2), nullValue());
            assertThat(parser.parseKeyLine("# test", 3), nullValue());
            assertThat(parser.parseKeyLine("    # test", 4), nullValue());
        }

        @Test
        void shouldParseSimpleKeyDefinitions() {
            // given / when
            KeyLine key1 = parser.parseKeyLine("Jump Space", 11);
            KeyLine key2 = parser.parseKeyLine("Crouch LeftCtrl RightCtrl ", 12);
            KeyLine key3 = parser.parseKeyLine("Sprint  /  # comment", 13);

            // then
            assertThat(key1.displayText(), equalTo("Jump"));
            assertThat(key1.keys(), contains(new KeyNameSet("Space")));
            assertThat(key1.attributes(), empty());

            assertThat(key2.displayText(), equalTo("Crouch"));
            assertThat(key2.keys(), containsInAnyOrder(new KeyNameSet("LeftCtrl"), new KeyNameSet("RightCtrl")));
            assertThat(key2.attributes(), empty());

            assertThat(key3.displayText(), equalTo("Sprint"));
            assertThat(key3.keys(), contains(new KeyNameSet("/")));
            assertThat(key3.attributes(), empty());
        }

        @Test
        void shouldParseKeyDefinitionWithAttribute() {
            // given / when
            KeyLine key1 = parser.parseKeyLine("Jump Space [width = 20px]", 15);
            KeyLine key2 = parser.parseKeyLine("Crouch Ctrl [height = 40, dark = true]", 16);
            KeyLine key3 = parser.parseKeyLine("Action E [width=20][height=40px]", 16);

            // then
            assertThat(key1.displayText(), equalTo("Jump"));
            assertThat(key1.keys(), contains(new KeyNameSet("Space")));
            assertThat(key1.attributes(), contains(new Attribute("width", "20px")));

            assertThat(key2.displayText(), equalTo("Crouch"));
            assertThat(key2.keys(), contains(new KeyNameSet("Ctrl")));
            assertThat(key2.attributes(), contains(new Attribute("height", "40"), new Attribute("dark", "true")));

            assertThat(key3.displayText(), equalTo("Action"));
            assertThat(key3.keys(), contains(new KeyNameSet("E")));
            assertThat(key3.attributes(), contains(new Attribute("width", "20"), new Attribute("height", "40px")));
        }

        @Test
        void shouldParseDefinitionWithMultipleKeys() {
            // given / when
            KeyLine key1 = parser.parseKeyLine("Econ LeftAlt & R RightAlt & R", 20);
            KeyLine key2 = parser.parseKeyLine("Grid Alt & Shift & G [color=red]", 21);

            // then
            assertThat(key1.displayText(), equalTo("Econ"));
            assertThat(key1.keys(), contains(new KeyNameSet(Set.of("LeftAlt", "R")), new KeyNameSet(Set.of("RightAlt", "R"))));
            assertThat(key1.attributes(), empty());

            assertThat(key2.displayText(), equalTo("Grid"));
            assertThat(key2.keys(), contains(new KeyNameSet(Set.of("Alt", "Shift", "G"))));
            assertThat(key2.attributes(), contains(new Attribute("color", "red")));
        }

        @Test
        void shouldParseDefinitionWithDoubleQuotes() {
            // given / when
            KeyLine key1 = parser.parseKeyLine("\"A b\" H", 1);
            KeyLine key2 = parser.parseKeyLine("Jump  \"LeftShift\" [width = \"30px\"]", 2);
            KeyLine key3 = parser.parseKeyLine("Econ \"LeftAlt\" & \"R\"", 3);

            // then
            assertThat(key1.displayText(), equalTo("A b"));
            assertThat(key1.keys(), contains(new KeyNameSet("H")));
            assertThat(key1.attributes(), empty());

            assertThat(key2.displayText(), equalTo("Jump"));
            assertThat(key2.keys(), contains(new KeyNameSet("LeftShift")));
            assertThat(key2.attributes(), contains(new Attribute("width", "30px")));

            assertThat(key3.displayText(), equalTo("Econ"));
            assertThat(key3.keys(), contains(new KeyNameSet(Set.of("LeftAlt", "R"))));
            assertThat(key3.attributes(), empty());
        }

        @Test
        void shouldResolveVariables() {
            // given
            parser.parseHeaderLine("$action = Grab", 1);
            parser.parseHeaderLine("$small = 20", 2);
            parser.parseHeaderLine("$medium = 30px ", 3);

            // when
            KeyLine key1 = parser.parseKeyLine("$action E [width=$small, height=$small]", 10);
            KeyLine key2 = parser.parseKeyLine("Jump Space [width=$medium]", 11);

            // then
            assertThat(key1.displayText(), equalTo("Grab"));
            assertThat(key1.keys(), contains(new KeyNameSet("E")));
            assertThat(key1.attributes(), contains(new Attribute("width", "20"), new Attribute("height", "20")));

            assertThat(key2.displayText(), equalTo("Jump"));
            assertThat(key2.keys(), contains(new KeyNameSet("Space")));
            assertThat(key2.attributes(), contains(new Attribute("width", "30px")));
        }

        @Test
        void shouldThrowForUnexpectedAmpersand() {
            // given / when
            IllegalStateException ex1 = assertThrows(IllegalStateException.class,
                () -> parser.parseKeyLine("Jmp &", 1));
            IllegalStateException ex2 = assertThrows(IllegalStateException.class,
                () -> parser.parseKeyLine("Jmp & A", 1));
            IllegalStateException ex3 = assertThrows(IllegalStateException.class,
                () -> parser.parseKeyLine("Jmp A &", 1));
            IllegalStateException ex4 = assertThrows(IllegalStateException.class,
                () -> parser.parseKeyLine("Jmp A & [width=20px]", 1));
            IllegalStateException ex5 = assertThrows(IllegalStateException.class,
                () -> parser.parseKeyLine("Jmp A & # some comment", 1));

            // then
            assertThat(ex1.getMessage(), equalTo("Unexpected '&' on line 1, column 4. Wrap complex names in double quotes"));
            assertThat(ex2.getMessage(), equalTo("Unexpected '&' on line 1, column 4. Wrap complex names in double quotes"));
            assertThat(ex3.getMessage(), equalTo("After ampersand, expect another key, but got end of line on line 1, column 7"));
            assertThat(ex4.getMessage(), equalTo("After ampersand, expect another key, but got '[' on line 1, column 9"));
            assertThat(ex5.getMessage(), equalTo("Unexpected '#' on line 1, column 8. Wrap complex names in double quotes"));
        }
    }
}