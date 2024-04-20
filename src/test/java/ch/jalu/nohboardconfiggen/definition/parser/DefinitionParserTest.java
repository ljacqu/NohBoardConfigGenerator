package ch.jalu.nohboardconfiggen.definition.parser;

import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;
import ch.jalu.nohboardconfiggen.definition.parser.element.AttributeList;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyLine;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyNameSet;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyRow;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyboardLineParseResult;
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
import static org.hamcrest.Matchers.anEmptyMap;
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
            parseHeaderLine("");
            parseHeaderLine(" ");

            // then
            assertThat(parser.buildAttributes(), empty());
            assertThat(parser.variablesByName, anEmptyMap());
        }

        @Test
        void shouldParseComment() {
            // given / when
            parseHeaderLine("# Test");
            parseHeaderLine("  # Comment");

            // then
            assertThat(parser.buildAttributes(), empty());
            assertThat(parser.variablesByName, anEmptyMap());
        }

        @Test
        void shouldParseAttributes() {
            // given / when
            parseHeaderLine("[width=40px]");
            parseHeaderLine("[height=36px, keyboard=fr]");

            // then
            assertThat(parser.buildAttributes(), containsInAnyOrder(
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
            parseHeaderLine("[ width =  30px ]");
            parseHeaderLine("[ height= 20 , keyboard =de ]");

            // then
            assertThat(parser.buildAttributes(), containsInAnyOrder(
                new Attribute("width", "30px"),
                new Attribute("height", "20"),
                new Attribute("keyboard", "de")));
        }

        @Test
        void shouldParseAttributesWithValuesInDoubleQuotes() {
            // given / when
            parseHeaderLine("""
                [ keyboard="nl", width="20px", height="30" ]""");

            // then
            assertThat(parser.buildAttributes(), containsInAnyOrder(
                new Attribute("keyboard", "nl"),
                new Attribute("width", "20px"),
                new Attribute("height", "30")));
        }

        @Test
        void shouldHandleEscapesAndNotParseSpecialCharsWithinDoubleQuotes() {
            // given / when
            parseHeaderLine("""
                [ title="T\\"A", subtitle="[\\\\o=D]", hint="a,b,\\$c" ]""");

            // then
            assertThat(parser.buildAttributes(), containsInAnyOrder(
                new Attribute("title", "T\"A"),
                new Attribute("subtitle", "[\\o=D]"),
                new Attribute("hint", "a,b,$c")));
        }

        @Test
        void shouldParseAttributeFollowedByComment() {
            // given / when
            parseHeaderLine("[ width = 20 ] # note: check this");

            // then
            assertThat(parser.buildAttributes(), contains(new Attribute("width", "20")));
        }

        @Test
        void shouldThrowForInvalidCharactersOutsideOfDoubleQuotes() {
            // given / when
            ParserException ex1 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[title = test~toast ]", 4));
            ParserException ex2 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[ symbol = ? ]", 4));
            ParserException ex3 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[ comma = , ]", 4));

            // then
            assertThat(ex1.getMessage(), equalTo("Unexpected character '~' on line 4, column 14"));
            assertThat(ex2.getMessage(), equalTo("Unexpected character '?' on line 4, column 11. Use double quotes around complex values"));
            assertThat(ex3.getMessage(), equalTo("Unexpected character ',' on line 4, column 10. Use double quotes around complex values"));
        }

        @Test
        void shouldThrowForInvalidAttributeNameChars() {
            // given / when
            ParserException ex1 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[ wîdth = unset ]", 4));
            ParserException ex2 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[ max~height = 40 ]", 4));
            ParserException ex3 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[ t(e)st = 30 ]", 4));

            // then
            assertThat(ex1.getMessage(), equalTo("Expected '=' but got 'î' on line 4, column 4"));
            assertThat(ex2.getMessage(), equalTo("Expected '=' but got '~' on line 4, column 6"));
            assertThat(ex3.getMessage(), equalTo("Expected '=' but got '(' on line 4, column 4"));
        }

        @Test
        void shouldThrowForUnexpectedEndOfLine() {
            // given / when
            ParserException ex1 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[title = 20", 4));
            ParserException ex2 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[title =", 4));
            ParserException ex3 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[title", 4));
            ParserException ex4 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[", 4));
            ParserException ex5 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[ width = 20, title = ", 4));
            ParserException ex6 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[ title = \"t\", ", 4));

            // then
            Stream.of(ex1, ex2, ex3, ex4, ex5, ex6).forEach(ex -> {
                assertThat(ex.getMessage(), equalTo("Unexpected end of line on line 4"));
            });
        }

        @Test
        void shouldThrowForUnexpectedComma() {
            // given / when
            ParserException ex = assertThrows(ParserException.class,
                () -> parseHeaderLine("[ , width = 20 ]", 4));

            // then
            assertThat(ex.getMessage(), equalTo("Expected attribute identifier ([a-zA-Z0-9_-]), but got ',' on line 4, column 3"));
        }

        @Test
        void shouldThrowForUnclosedDoubleQuotes() {
            // given
            String invalidString1 = """
                [title = "test]""";
            String invalidString2 = """
                [title = "test\\"]""";

            // when
            ParserException ex1 = assertThrows(ParserException.class,
                () -> parseHeaderLine(invalidString1, 4));
            ParserException ex2 = assertThrows(ParserException.class,
                () -> parseHeaderLine(invalidString2, 4));

            // then
            assertThat(ex1.getMessage(), equalTo("Unexpected end of line; \" not closed on line 4"));
            assertThat(ex2.getMessage(), equalTo("Unexpected end of line; \" not closed on line 4"));
        }

        @Test
        void shouldThrowForUnknownEscape() {
            // given / when
            ParserException ex1 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[title = \"te\\a\"]", 4));
            ParserException ex2 = assertThrows(ParserException.class,
                () -> parseHeaderLine("[title = \"po \\:\"", 4));

            // then
            assertThat(ex1.getMessage(), equalTo("Unknown escape: \\a on line 4, column 14"));
            assertThat(ex2.getMessage(), equalTo("Unknown escape: \\: on line 4, column 15"));
        }

        @Test
        void shouldParseValueVariable() {
            // given / when
            parseHeaderLine("$keyWidth=40", 20);
            parseHeaderLine("$keyHeight = 20px", 21);

            // then
            assertThat(parser.variablesByName, aMapWithSize(2));
            assertThat(parser.variablesByName.get("keyWidth"), equalTo(new ValueVariable("keyWidth", "40")));
            assertThat(parser.variablesByName.get("keyHeight"), equalTo(new ValueVariable("keyHeight", "20px")));
        }

        @Test
        void shouldParseAttributeVariable() {
            // given / when
            parseHeaderLine("$bigKey = [width = 60, height =  54]", 20);
            parseHeaderLine("$smallKey=[width=30px,height=28px]", 21);

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
            parseHeaderLine("$width = 40");

            // when
            parseHeaderLine("[width = $width]");

            // then
            assertThat(parser.buildAttributes(), contains(new Attribute("width", "40")));
        }

        @Test
        void shouldParseVariableDeclarationWithVariable() {
            // given
            parseHeaderLine("$suffix = \"You know?\"");

            // when
            parseHeaderLine("$statement = \"Crazy. $suffix\"");

            // then
            assertThat(parser.variablesByName, aMapWithSize(2));
            assertThat(parser.variablesByName.get("suffix"), equalTo(new ValueVariable("suffix", "You know?")));
            assertThat(parser.variablesByName.get("statement"), equalTo(new ValueVariable("statement", "Crazy. You know?")));
        }

        @Test
        void shouldThrowForEmptyVariableName() {
            // given / when
            ParserException ex = assertThrows(ParserException.class,
                () -> parseHeaderLine("$ = 5", 2));

            // then
            assertThat(ex.getMessage(), equalTo("Expected variable identifier ([a-zA-Z0-9_-]), but got ' ' on line 2, column 2"));
        }

        @Test
        void shouldThrowForInvalidVariableAssignmentSyntax() {
            // given / when
            ParserException ex1 = assertThrows(ParserException.class,
                () -> parseHeaderLine("$test =", 2));
            ParserException ex2 = assertThrows(ParserException.class,
                () -> parseHeaderLine("$test = abc def", 3));
            ParserException ex3 = assertThrows(ParserException.class,
                () -> parseHeaderLine("$test : 50", 4));
            ParserException ex4 = assertThrows(ParserException.class,
                () -> parseHeaderLine("$test", 5));

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
            assertThat(parseKeyLine("", 1), instanceOf(KeyboardRowEnd.class));
            assertThat(parseKeyLine(" \t  ", 2), instanceOf(KeyboardRowEnd.class));
            assertThat(parseKeyLine("# test", 3), nullValue());
            assertThat(parseKeyLine("    # test", 4), nullValue());
        }

        @Test
        void shouldParseSimpleKeyDefinitions() {
            // given / when
            KeyLine key1 = (KeyLine) parseKeyLine("Jump Space");
            KeyLine key2 = (KeyLine) parseKeyLine("Crouch LeftCtrl RightCtrl ");
            KeyLine key3 = (KeyLine) parseKeyLine("Sprint  /  # comment");

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
            KeyLine key1 = (KeyLine) parseKeyLine("Jump Space [width = 20px]");
            KeyLine key2 = (KeyLine) parseKeyLine("Crouch Ctrl [height = 40, dark = true]");
            KeyLine key3 = (KeyLine) parseKeyLine("Action E [width=20][height=40px]");

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
            KeyLine key1 = (KeyLine) parseKeyLine("Econ LeftAlt & R RightAlt & R");
            KeyLine key2 = (KeyLine) parseKeyLine("Grid Alt & Shift & G [color=red]");

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
            KeyLine key1 = (KeyLine) parseKeyLine("\"A b\" H");
            KeyLine key2 = (KeyLine) parseKeyLine("Jump  \"LeftShift\" [width = \"30px\"]");
            KeyLine key3 = (KeyLine) parseKeyLine("Econ \"LeftAlt\" & \"R\"");

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
        void shouldParseAttribute() {
            // given / when
            AttributeList attributes1 = (AttributeList) parseKeyLine("[width=20]");
            AttributeList attributes2 = (AttributeList) parseKeyLine("[marginLeft=5px, marginTop=10]");
            AttributeList attributes3 = (AttributeList) parseKeyLine("[marginLeft=5px] [ marginTop = 10]");

            // then
            assertThat(attributes1.attributes(), contains(new Attribute("width", "20")));
            assertThat(attributes2.attributes(), contains(new Attribute("marginLeft", "5px"), new Attribute("marginTop", "10")));
            assertThat(attributes3.attributes(), contains(new Attribute("marginLeft", "5px"), new Attribute("marginTop", "10")));
        }

        @Test
        void shouldThrowForUnexpectedContentAfterAttributes() {
            // given / when
            ParserException ex1 = assertThrows(ParserException.class, () -> parseKeyLine("[width=20] A", 1));
            ParserException ex2 = assertThrows(ParserException.class, () -> parseKeyLine("[width=20] [keyboard=fr] &", 2));
            ParserException ex3 = assertThrows(ParserException.class, () -> parseKeyLine("[height=40] $var", 3));

            // then
            assertThat(ex1.getMessage(), equalTo("Expected only attributes to be declared, but found 'A' on line 1, column 12"));
            assertThat(ex2.getMessage(), equalTo("Expected only attributes to be declared, but found '&' on line 2, column 26"));
            assertThat(ex3.getMessage(), equalTo("Expected only attributes to be declared, but found '$' on line 3, column 13"));
        }

        @Test
        void shouldResolveVariables() {
            // given
            parseHeaderLine("$action = Grab");
            parseHeaderLine("$small = 20");
            parseHeaderLine("$medium = 30px ");

            // when
            KeyLine key1 = (KeyLine) parseKeyLine("$action E [width=$small, height=$small]", 10);
            KeyLine key2 = (KeyLine) parseKeyLine("Jump Space [width=$medium]", 11);

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
            parseHeaderLine("$small = [width=20, height=20]");

            // when
            KeyLine key1 = (KeyLine) parseKeyLine("Action LeftCtrl $small");
            KeyLine key2 = (KeyLine) parseKeyLine("Walk LeftShift $small [style=bold]");

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
            ParserException ex1 = assertThrows(ParserException.class,
                () -> parseKeyLine("Jmp &", 1));
            ParserException ex2 = assertThrows(ParserException.class,
                () -> parseKeyLine("Jmp & A", 1));
            ParserException ex3 = assertThrows(ParserException.class,
                () -> parseKeyLine("Jmp A &", 1));
            ParserException ex4 = assertThrows(ParserException.class,
                () -> parseKeyLine("Jmp A & [width=20px]", 1));
            ParserException ex5 = assertThrows(ParserException.class,
                () -> parseKeyLine("Jmp A & # some comment", 1));

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
        assertThat(parser.buildAttributes(), containsInAnyOrder(
            new Attribute("width", "35"),
            new Attribute("keyboard", "de")));

        assertThat(parser.getKeyRows(), hasSize(3));
        KeyRow row1 = parser.getKeyRows().get(0);
        assertThat(row1.getKeys(), hasSize(1));
        assertThat(row1.getKey(0), isKey("W", "ArrowUp"));
        assertThat(row1.getKey(0).attributes(), contains(new Attribute("marginLeft", "1k")));

        KeyRow row2 = parser.getKeyRows().get(1);
        assertThat(row2.getKeys(), hasSize(3));
        assertThat(row2.getKey(0), isKey("A", "ArrowLeft"));
        assertThat(row2.getKey(1), isKey("S", "ArrowDown"));
        assertThat(row2.getKey(2), isKey("D", "ArrowRight"));
        row2.getKeys().forEach(key -> assertThat(key.attributes(), empty()));

        KeyRow row3 = parser.getKeyRows().get(2);
        assertThat(row3.getKeys(), hasSize(1));
        assertThat(row3.getKey(0), isKey("Jump", "Space"));
        assertThat(row3.getKey(0).attributes(), contains(new Attribute("width", "3")));
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
        assertThat(parser.buildAttributes(), empty());

        assertThat(parser.getKeyRows(), hasSize(2));
        KeyRow row1 = parser.getKeyRows().get(0);
        assertThat(row1.getKeys(), hasSize(3));
        assertThat(row1.getKey(0), isKey("Flashlight", "Q", "Num1"));
        assertThat(row1.getKey(1), isKey("Grenade", "W", "Num2"));
        assertThat(row1.getKey(2), isKey("Action", "E", "Num3"));

        KeyRow row2 = parser.getKeyRows().get(1);
        assertThat(row2.getKeys(), hasSize(3));
        assertThat(row2.getKey(0), isKey("Jump", "A", "Num4"));
        assertThat(row2.getKey(1), isKey("Toggle", "S", "Num5"));
        assertThat(row2.getKey(2), isKey("U. D.", "D", "Num6"));

        parser.getKeyRows().stream()
            .flatMap(row -> row.getKeys().stream())
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
        assertThat(parser.buildAttributes(), containsInAnyOrder(
            new Attribute("keyboard", "en-us"),
            new Attribute("width", "40")));

        assertThat(parser.getKeyRows(), hasSize(1));
        KeyRow row = parser.getKeyRows().get(0);
        assertThat(row.getKeys(), hasSize(3));

        assertThat(row.getKey(0), isKey("$ 1", "Q"));
        assertThat(row.getKey(0).attributes(), contains(new Attribute("width", "20"), new Attribute("height", "20")));

        assertThat(row.getKey(1), isKey("$ 2", "W"));
        assertThat(row.getKey(1).attributes(), contains(new Attribute("width", "20"), new Attribute("height", "20"), new Attribute("marginLeft", "5px")));

        assertThat(row.getKey(2).displayText(), equalTo("$ 2b"));
        assertThat(row.getKey(2).keys(), contains(new KeyNameSet("W", "E")));
        assertThat(row.getKey(2).attributes(), empty());
    }

    private void parseHeaderLine(String text) {
        parseHeaderLine(text, 4);
    }

    private void parseHeaderLine(String text, int lineNumber) {
        Tokenizer tokenizer = new Tokenizer(text, lineNumber);
        parser.parseHeaderLine(tokenizer);
        assertThat("Tokenizer should be fully consumed", tokenizer.hasNext(), equalTo(false));
    }

    private KeyboardLineParseResult parseKeyLine(String text) {
        return parseKeyLine(text, 4);
    }

    private KeyboardLineParseResult parseKeyLine(String text, int lineNumber) {
        Tokenizer tokenizer = new Tokenizer(text, lineNumber);
        KeyboardLineParseResult result = parser.parseKeyLine(tokenizer);
        assertThat("Tokenizer should be fully consumed", tokenizer.hasNext(), equalTo(false));
        return result;
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