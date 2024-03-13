package ch.jalu.nohboardconfiggen.definition.parser;

import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;
import ch.jalu.nohboardconfiggen.definition.parser.element.Variable;
import ch.jalu.nohboardconfiggen.definition.parser.element.Variable.AttributeVariable;
import ch.jalu.nohboardconfiggen.definition.parser.element.Variable.ValueVariable;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefinitionParser {

    final Map<String, String> attributeNamesToValue = new HashMap<>();
    final Map<String, Variable> variablesByName = new HashMap<>();


    public void parse(List<String> lines) {
        int lineNumber = 1;
        boolean isHeaderSection = true;
        for (String line : lines) {
            if (isHeaderSection) {
                isHeaderSection = !parseHeaderLine(line, lineNumber);
            } else {
                // TODO keys declaration
            }
            ++lineNumber;
        }
    }

    @VisibleForTesting
    boolean parseHeaderLine(String line, int lineNumber) {
        LineChars lineChars = new LineChars(line, lineNumber);
        lineChars.skipWhitespace();

        if (lineChars.hasNext()) {
            char chr = lineChars.next();
            if (chr == '#') {
                return false; // Comment - ignore rest
            } else if (chr == '[') {
                processAttributes(lineChars);
                expectEndOfContent(lineChars);
            } else if (chr == '$') {
                processVariable(lineChars);
                expectEndOfContent(lineChars);
            } else {
                expectKeysSectionOrThrow(chr, lineChars);
                return true;
            }
        }

        return false;
    }

    private boolean isValidIdentifierChar(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || (c >= '0' && c <= '9')
            || (c == '_');
    }

    private void processAttributes(LineChars lineChars) {
        List<Attribute> attributes = parseAttributeDeclaration(lineChars);
        for (Attribute attribute : attributes) {
            String prev = attributeNamesToValue.put(attribute.name(), attribute.value());
            if (prev != null) {
                throw new IllegalStateException("Attribute '" + attribute.name() + "' is declared multiple times");
            }
        }
    }

    private List<Attribute> parseAttributeDeclaration(LineChars lineChars) {
        List<Attribute> attributes = new ArrayList<>();

        while (true) {
            // Get attribute name
            String identifier = lineChars.nextAllMatching(this::isValidIdentifierChar, true);
            if (identifier.isEmpty()) {
                String actual = lineChars.hasNext() ? "'" + lineChars.next() + "'" : "end of line";
                throw new IllegalStateException("Expected attribute identifier ([a-zA-Z0-9_]), but got "
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

            attributes.add(new Attribute(identifier, value));

            next = lineChars.nextNonWhitespace();
            if (next == ']') {
                return attributes;
            } else if (next != ',') {
                throw new IllegalStateException("Unexpected character '" + next
                    + "' on " + lineChars.getLineNrColText());
            }
        }
    }

    private void processVariable(LineChars lineChars) {
        Variable variable = parseVariableDeclaration(lineChars);
        Object prev = variablesByName.put(variable.name(), variable);
        if (prev != null) {
            throw new IllegalArgumentException("The variable $" + variable.name() + " was already defined");
        }
    }

    private Variable parseVariableDeclaration(LineChars lineChars) {
        // '$' was already consumed, so identifier is without the starting '$'
        String identifier = lineChars.nextAllMatching(this::isValidIdentifierChar, false);

        // Expect '='
        lineChars.expectCharAfterOptionalWhitespace('=');

        // Next char determines what happens
        lineChars.skipWhitespace();
        char next = lineChars.peek();
        if (next == '[') {
            lineChars.next();
            List<Attribute> attributes = parseAttributeDeclaration(lineChars);
            return new AttributeVariable(identifier, attributes);
        } else if (next == '"') {
            String value = parseTextInDoubleQuotes(lineChars);
            return new ValueVariable(identifier, value);
        } else if (next == '$') {
            // todo var support
            return null;
        } else {
            String value = parseSimpleText(lineChars);
            return new ValueVariable(identifier, value);
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

    private void expectKeysSectionOrThrow(char firstCharacter, LineChars lineChars) {
        if (Character.toLowerCase(firstCharacter) == 'k'
                && Character.toLowerCase(lineChars.next()) == 'e'
                && Character.toLowerCase(lineChars.next()) == 'y'
                && Character.toLowerCase(lineChars.next()) == 's') {
            lineChars.expectCharAfterOptionalWhitespace(':');
            expectEndOfContent(lineChars);
        }
        throw new IllegalStateException("Invalid syntax on " + lineChars.getLineNrText());
    }

    private void expectEndOfContent(LineChars lineChars) {
        while (lineChars.hasNext()) {
            char chr = lineChars.next();
            if (Character.isWhitespace(chr)) {
                // continue
            } else if (chr == '#') {
                return; // Comment -> ignore rest
            } else {
                throw new IllegalStateException("Expected end of line, but got '" + chr
                    + "' on " + lineChars.getLineNrColText());
            }
        }
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
            case '$':
                return nextChar;
            default:
                throw new IllegalStateException(
                    "Unknown escape: \\" + nextChar + " on " + lineChars.getLineNrColText());
        }
    }
}
