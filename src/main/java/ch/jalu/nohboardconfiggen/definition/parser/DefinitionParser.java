package ch.jalu.nohboardconfiggen.definition.parser;

import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefinitionParser {

    final Map<String, String> propertyNamesToValue = new HashMap<>();

    public void parse(List<String> lines) {

        int lineNumber = 1;
        for (String line : lines) {
            parseHeaderLine(line, lineNumber);
            ++lineNumber;
        }
    }

    @VisibleForTesting
    void parseHeaderLine(String line, int lineNumber) {
        LineChars lineChars = new LineChars(line, lineNumber);
        while (lineChars.hasNext()) {
            char chr = lineChars.next();
            if (Character.isWhitespace(chr)) {
                continue;
            } else if (chr == '#') {
                return; // Comment - ignore
            } else if (chr == '[') {
                processProperties(parsePropertyDeclaration(lineChars));
            } else {
                throw new IllegalStateException("Unexpected character '" + chr + "' on " + lineChars.getLineNrText());
            }
        }

    }

    private boolean isValidIdentifierChar(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || (c >= '0' && c <= '9')
            || (c == '_');
    }

    private void processProperties(List<Property> properties) {
        for (Property property : properties) {
            String prev = propertyNamesToValue.put(property.name, property.value);
            if (prev != null) {
                throw new IllegalStateException("Property '" + property.name + "' is declared multiple times");
            }
        }
    }

    private List<Property> parsePropertyDeclaration(LineChars lineChars) {
        List<Property> properties = new ArrayList<>();

        while (true) {
            // Get property name
            String identifier = lineChars.nextAllMatching(this::isValidIdentifierChar, true);
            if (identifier.isEmpty()) {
                String actual = lineChars.hasNext() ? "'" + lineChars.next() + "'" : "end of line";
                throw new IllegalStateException("Expected property identifier ([a-zA-Z0-9_]), but got "
                    + actual + " on " + lineChars.getLineNrColText());
            }

            // Expect '='
            lineChars.expectCharAfterOptionalWhitespace('=');

            // Collect value
            lineChars.skipWhitespace();
            char next = lineChars.peek();
            String value = (next == '"')
                ? parseTextInDoubleQuotes(lineChars)
                : parseSimpleText(lineChars);

            properties.add(new Property(identifier, value));

            next = lineChars.nextNonWhitespace();
            if (next == ']') {
                return properties;
            } else if (next != ',') {
                throw new IllegalStateException("Unexpected character '" + next
                    + "' on " + lineChars.getLineNrColText());
            }
        }
    }

    private String parseTextInDoubleQuotes(LineChars lineChars) {
        StringBuilder value = new StringBuilder();
        char chr = lineChars.next();
        if (chr != '"') {
            throw new IllegalStateException("Expected double quote"); // should never happen
        }

        chr = lineChars.next();
        while (true) {
            if (chr == '"') {
                break;
            } else if (chr == '\\') {
                chr = handleBackslashEscape(lineChars);
            }
            value.append(chr);

            if (lineChars.hasNext()) {
                chr = lineChars.next();
            } else {
                throw new IllegalStateException(
                    "Unexpected end of line; \" not closed on " + lineChars.getLineNrText());
            }
        }
        return value.toString();
    }

    private String parseSimpleText(LineChars lineChars) {
        String value = lineChars.nextAllMatching(chr -> isValidIdentifierChar(chr), false);
        if (value.isEmpty()) {
            throw new IllegalStateException("Unexpected character '" + lineChars.peek() + "' on "
                + lineChars.getLineNrColText() + ". Use double quotes around complex values");
        }
        return value;
    }

    private static char handleBackslashEscape(LineChars lineChars) {
        char nextChar = lineChars.next();
        switch (nextChar) {
            case '\\':
            case '"':
                return nextChar;
            default:
                throw new IllegalStateException(
                    "Unknown escape: \\" + nextChar + " on " + lineChars.getLineNrColText());
        }
    }

    @Getter
    @AllArgsConstructor
    static final class Property {

        private final String name;
        private final String value;

    }
}
