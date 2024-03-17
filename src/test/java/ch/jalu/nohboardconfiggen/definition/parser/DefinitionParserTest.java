package ch.jalu.nohboardconfiggen.definition.parser;

import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyLine;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyNameSet;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyRow;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyboardRowEnd;
import ch.jalu.nohboardconfiggen.definition.parser.element.Variable.AttributeVariable;
import ch.jalu.nohboardconfiggen.definition.parser.element.Variable.ValueVariable;
import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
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
            assertThat(parser.getAttributes(), empty());
        }

        @Test
        void shouldParseComment() {
            // given / when
            parser.parseHeaderLine("# Test", 20);
            parser.parseHeaderLine("  # Comment", 20);

            // then
            assertThat(parser.getAttributes(), empty());
        }

        @Test
        void shouldParseAttributes() {
            // given / when
            parser.parseHeaderLine("[width=40px]", 20);
            parser.parseHeaderLine("[height=36px, keyboard=fr]", 20);

            // then
            assertThat(parser.getAttributes(), containsInAnyOrder(
                new Attribute("width", "40px"),
                new Attribute("height", "36px"),
                new Attribute("keyboard", "fr")));
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
            assertThat(parser.getAttributes(), containsInAnyOrder(
                new Attribute("width", "30px"),
                new Attribute("height", "20"),
                new Attribute("keyboard", "de")));
        }

        @Test
        void shouldParseAttributesWithValuesInDoubleQuotes() {
            // given / when
            parser.parseHeaderLine("""
                [ keyboard="nl", width="20px", height="30" ]""", 2);

            // then
            assertThat(parser.getAttributes(), containsInAnyOrder(
                new Attribute("keyboard", "nl"),
                new Attribute("width", "20px"),
                new Attribute("height", "30")));
        }

        @Test
        void shouldHandleEscapesAndNotParseSpecialCharsWithinDoubleQuotes() {
            // given / when
            parser.parseHeaderLine("""
                [ title="T\\"A", subtitle="[\\\\o=D]", hint="a,b,\\$c" ]""", 8);

            // then
            assertThat(parser.getAttributes(), containsInAnyOrder(
                new Attribute("title", "T\"A"),
                new Attribute("subtitle", "[\\o=D]"),
                new Attribute("hint", "a,b,$c")));
        }

        @Test
        void shouldParseAttributeFollowedByComment() {
            // given / when
            parser.parseHeaderLine("[ width = 20 ] # note: check this", 6);

            // then
            assertThat(parser.getAttributes(), contains(new Attribute("width", "20")));
        }

        @Test
        void shouldThrowForInvalidCharactersOutsideOfDoubleQuotes() {
            // given / when
            IllegalStateException ex1 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[title = test~toast ]", 4));
            IllegalStateException ex2 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[ symbol = ? ]", 4));
            IllegalStateException ex3 = assertThrows(IllegalStateException.class,
                () -> parser.parseHeaderLine("[ comma = , ]", 4));

            // then
            assertThat(ex1.getMessage(), equalTo("Unexpected character '~' on line 4, column 14"));
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
            assertThat(parser.getAttributes(), contains(new Attribute("width", "40")));
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
        void shouldProcessEmptyLineOrLineWithComment() {
            // given / when / then
            assertThat(parser.parseKeyLine("", 1), instanceOf(KeyboardRowEnd.class));
            assertThat(parser.parseKeyLine(" \t  ", 2), instanceOf(KeyboardRowEnd.class));
            assertThat(parser.parseKeyLine("# test", 3), nullValue());
            assertThat(parser.parseKeyLine("    # test", 4), nullValue());
        }

        @Test
        void shouldParseSimpleKeyDefinitions() {
            // given / when
            KeyLine key1 = (KeyLine) parser.parseKeyLine("Jump Space", 11);
            KeyLine key2 = (KeyLine) parser.parseKeyLine("Crouch LeftCtrl RightCtrl ", 12);
            KeyLine key3 = (KeyLine) parser.parseKeyLine("Sprint  /  # comment", 13);

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
            KeyLine key1 = (KeyLine) parser.parseKeyLine("Jump Space [width = 20px]", 15);
            KeyLine key2 = (KeyLine) parser.parseKeyLine("Crouch Ctrl [height = 40, dark = true]", 16);
            KeyLine key3 = (KeyLine) parser.parseKeyLine("Action E [width=20][height=40px]", 16);

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
            KeyLine key1 = (KeyLine) parser.parseKeyLine("Econ LeftAlt & R RightAlt & R", 20);
            KeyLine key2 = (KeyLine) parser.parseKeyLine("Grid Alt & Shift & G [color=red]", 21);

            // then
            assertThat(key1.displayText(), equalTo("Econ"));
            assertThat(key1.keys(), contains(new KeyNameSet("LeftAlt", "R"), new KeyNameSet("RightAlt", "R")));
            assertThat(key1.attributes(), empty());

            assertThat(key2.displayText(), equalTo("Grid"));
            assertThat(key2.keys(), contains(new KeyNameSet("Alt", "Shift", "G")));
            assertThat(key2.attributes(), contains(new Attribute("color", "red")));
        }

        @Test
        void shouldParseDefinitionWithDoubleQuotes() {
            // given / when
            KeyLine key1 = (KeyLine) parser.parseKeyLine("\"A b\" H", 1);
            KeyLine key2 = (KeyLine) parser.parseKeyLine("Jump  \"LeftShift\" [width = \"30px\"]", 2);
            KeyLine key3 = (KeyLine) parser.parseKeyLine("Econ \"LeftAlt\" & \"R\"", 3);

            // then
            assertThat(key1.displayText(), equalTo("A b"));
            assertThat(key1.keys(), contains(new KeyNameSet("H")));
            assertThat(key1.attributes(), empty());

            assertThat(key2.displayText(), equalTo("Jump"));
            assertThat(key2.keys(), contains(new KeyNameSet("LeftShift")));
            assertThat(key2.attributes(), contains(new Attribute("width", "30px")));

            assertThat(key3.displayText(), equalTo("Econ"));
            assertThat(key3.keys(), contains(new KeyNameSet("LeftAlt", "R")));
            assertThat(key3.attributes(), empty());
        }

        @Test
        void shouldResolveVariables() {
            // given
            parser.parseHeaderLine("$action = Grab", 1);
            parser.parseHeaderLine("$small = 20", 2);
            parser.parseHeaderLine("$medium = 30px ", 3);

            // when
            KeyLine key1 = (KeyLine) parser.parseKeyLine("$action E [width=$small, height=$small]", 10);
            KeyLine key2 = (KeyLine) parser.parseKeyLine("Jump Space [width=$medium]", 11);

            // then
            assertThat(key1.displayText(), equalTo("Grab"));
            assertThat(key1.keys(), contains(new KeyNameSet("E")));
            assertThat(key1.attributes(), contains(new Attribute("width", "20"), new Attribute("height", "20")));

            assertThat(key2.displayText(), equalTo("Jump"));
            assertThat(key2.keys(), contains(new KeyNameSet("Space")));
            assertThat(key2.attributes(), contains(new Attribute("width", "30px")));
        }

        @Test
        void shouldResolveAttributeVariables() {
            // given
            parser.parseHeaderLine("$small = [width=20, height=20]", 1);

            // when
            KeyLine key1 = (KeyLine) parser.parseKeyLine("Action LeftCtrl $small", 1);
            KeyLine key2 = (KeyLine) parser.parseKeyLine("Walk LeftShift $small [style=bold]", 1);

            // then
            assertThat(key1.displayText(), equalTo("Action"));
            assertThat(key1.keys(), contains(new KeyNameSet("LeftCtrl")));
            assertThat(key1.attributes(), contains(new Attribute("width", "20"), new Attribute("height", "20")));

            assertThat(key2.displayText(), equalTo("Walk"));
            assertThat(key2.keys(), contains(new KeyNameSet("LeftShift")));
            assertThat(key2.attributes(), contains(new Attribute("width", "20"), new Attribute("height", "20"), new Attribute("style", "bold")));
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

    @Test
    void shouldParse() {
        // given / when
        parser.parse(List.of(
            "[width=35]",
            "[keyboard=de]",
            "",
            "Keys:",
            "# Faking WASD: arrow keys are mapped",
            "W ArrowUp [marginLeft=1k]",
            "",
            "A ArrowLeft",
            "S ArrowDown",
            "D ArrowRight",
            "",
            "Jump Space [width=3]"
        ));

        // then
        assertThat(parser.getAttributes(), containsInAnyOrder(
            new Attribute("width", "35"),
            new Attribute("keyboard", "de")));

        assertThat(parser.getKeyRows(), hasSize(3));
        KeyRow row1 = parser.getKeyRows().get(0);
        assertThat(row1, hasSize(1));
        assertThat(row1.get(0), isKey("W", "ArrowUp"));
        assertThat(row1.get(0).attributes(), contains(new Attribute("marginLeft", "1k")));

        KeyRow row2 = parser.getKeyRows().get(1);
        assertThat(row2, hasSize(3));
        assertThat(row2.get(0), isKey("A", "ArrowLeft"));
        assertThat(row2.get(1), isKey("S", "ArrowDown"));
        assertThat(row2.get(2), isKey("D", "ArrowRight"));
        row2.forEach(key -> assertThat(key.attributes(), empty()));

        KeyRow row3 = parser.getKeyRows().get(2);
        assertThat(row3, hasSize(1));
        assertThat(row3.get(0), isKey("Jump", "Space"));
        assertThat(row3.get(0).attributes(), contains(new Attribute("width", "3")));
    }

    @Test
    void shouldParse2() {
        // given / when
        parser.parse(List.of(
            "Keys:",
            "",
            "  Flashlight Q Num1",
            "  Grenade W Num2",
            "  Action E Num3",
            "",
            "  Jump A Num4",
            "  # Change text?",
            "  Toggle S Num5",
            "  \"U. D.\" D Num6"
        ));

        // then
        assertThat(parser.getAttributes(), empty());

        assertThat(parser.getKeyRows(), hasSize(2));
        KeyRow row1 = parser.getKeyRows().get(0);
        assertThat(row1, hasSize(3));
        assertThat(row1.get(0), isKey("Flashlight", "Q", "Num1"));
        assertThat(row1.get(1), isKey("Grenade", "W", "Num2"));
        assertThat(row1.get(2), isKey("Action", "E", "Num3"));

        KeyRow row2 = parser.getKeyRows().get(1);
        assertThat(row2, hasSize(3));
        assertThat(row2.get(0), isKey("Jump", "A", "Num4"));
        assertThat(row2.get(1), isKey("Toggle", "S", "Num5"));
        assertThat(row2.get(2), isKey("U. D.", "D", "Num6"));

        parser.getKeyRows().stream()
            .flatMap(row -> row.stream())
            .forEach(keyLine -> assertThat(keyLine.attributes(), empty()));
    }

    @Test
    void shouldParse3() {
        // given / when
        parser.parse(List.of(
            "# Set some attributes",
            "[keyboard=\"en-us\"]",
            "[width=40]",
            "",
            "# Variables",
            "$smallKey = [width=20, height=20]",
            "$prefix = \"\\$\"",
            "",
            "Keys:",
            "",
            "  \"$prefix 1\" Q    $smallKey",
            "  \"$prefix 2\" W    $smallKey [marginLeft=5px]",
            "  \"$prefix 2b\" W & E",
            ""
        ));

        // then
        assertThat(parser.getAttributes(), containsInAnyOrder(
            new Attribute("keyboard", "en-us"),
            new Attribute("width", "40")));

        assertThat(parser.getKeyRows(), hasSize(1));
        KeyRow row = parser.getKeyRows().get(0);
        assertThat(row, hasSize(3));

        assertThat(row.get(0), isKey("$ 1", "Q"));
        assertThat(row.get(0).attributes(), contains(new Attribute("width", "20"), new Attribute("height", "20")));

        assertThat(row.get(1), isKey("$ 2", "W"));
        assertThat(row.get(1).attributes(), contains(new Attribute("width", "20"), new Attribute("height", "20"), new Attribute("marginLeft", "5px")));

        assertThat(row.get(2).displayText(), equalTo("$ 2b"));
        assertThat(row.get(2).keys(), contains(new KeyNameSet("W", "E")));
        assertThat(row.get(2).attributes(), empty());
    }

    private static Matcher<KeyLine> isKey(String displayText, String... individualKeyBinds) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(KeyLine keyLine) {
                if (!equalTo(displayText).matches(keyLine.displayText())) {
                    return false;
                }

                List<String> keyBindings = keyLine.keys().stream()
                    .map(keyNameSet -> {
                        if (keyNameSet.keys().size() != 1) {
                            throw new IllegalArgumentException(
                                "The given key has key binding combinations; this matcher cannot be used for it");
                        }
                        return Iterables.getOnlyElement(keyNameSet.keys());
                    }).toList();
                return contains(individualKeyBinds).matches(keyBindings);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("KeyLine[displayText=" + displayText + ", keys="
                    + Arrays.toString(individualKeyBinds) + "]");
            }

            @Override
            protected void describeMismatchSafely(KeyLine item, Description mismatchDescription) {
                mismatchDescription.appendText("KeyLine[displayText=" + item.displayText() + ", keys="
                    + item.keys() + "]");
            }
        };
    }
}