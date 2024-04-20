package ch.jalu.nohboardconfiggen.definition.parser;

import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;
import ch.jalu.nohboardconfiggen.definition.parser.element.AttributeList;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyLine;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyNameSet;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyRow;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyboardLineParseResult;
import ch.jalu.nohboardconfiggen.definition.parser.element.KeyboardRowEnd;
import ch.jalu.nohboardconfiggen.definition.parser.element.Variable;
import ch.jalu.nohboardconfiggen.definition.parser.element.Variable.AttributeVariable;
import ch.jalu.nohboardconfiggen.definition.parser.element.Variable.ValueVariable;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parses a keyboard config file. This parser is stateful, i.e. a new instance should be
 * created for every parsing.
 */
public class DefinitionParser {

    private final Map<String, String> attributeNamesToValue = new HashMap<>();
    final Map<String, Variable> variablesByName = new HashMap<>();
    @Getter
    private List<KeyRow> keyRows;

    /**
     * Parses the given lines.
     *
     * @param lines the lines to parse
     */
    public void parse(List<String> lines) {
        int lineNumber = 1;
        boolean isHeaderSection = true;
        RowsContainer rowsContainer = new RowsContainer();

        for (String line : lines) {
            Tokenizer tokenizer = new Tokenizer(line, lineNumber);

            if (isHeaderSection) {
                isHeaderSection = !parseHeaderLine(tokenizer);
            } else {
                KeyboardLineParseResult parseResult = parseKeyLine(tokenizer);
                rowsContainer.processKeyLineResult(parseResult);
            }
            if (tokenizer.hasNext()) {
                throw new IllegalStateException(
                    "Internal error: tokenizer still has values on " + tokenizer.getLineNrText());
            }

            ++lineNumber;
        }
        keyRows = rowsContainer.build();
    }

    /**
     * Returns all general attributes that were parsed.
     *
     * @return keyboard-level attributes
     */
    public List<Attribute> buildAttributes() {
        return attributeNamesToValue.entrySet().stream()
            .map(entry -> new Attribute(entry.getKey(), entry.getValue()))
            .toList();
    }

    /**
     * Parses the given line in the header section of the definition file (before the "Keys" section).
     * This method updates this parser's state by adding new attribute or variable data, as parsed by the line.
     *
     * @param tokenizer tokenizer with the line to parse
     * @return true if the start of the keys section was detected, false otherwise
     */
    @VisibleForTesting
    boolean parseHeaderLine(Tokenizer tokenizer) {
        tokenizer.skipWhitespace();

        if (tokenizer.hasNext()) {
            char chr = tokenizer.next();
            if (chr == '#') {
                tokenizer.moveToEnd();
                return false; // Comment - ignore rest
            } else if (chr == '[') {
                processAttributes(tokenizer);
                expectEndOfContent(tokenizer);
            } else if (chr == '$') {
                processVariable(tokenizer);
                expectEndOfContent(tokenizer);
            } else {
                expectKeysSectionOrThrow(chr, tokenizer);
                return true;
            }
        }
        return false;
    }

    /**
     * Parses the given line in the keys section of the definition file.
     *
     * @param tokenizer tokenizer with the line to parse
     * @return result of the parse, null if there is nothing to do
     */
    @VisibleForTesting
    KeyboardLineParseResult parseKeyLine(Tokenizer tokenizer) {
        tokenizer.skipWhitespace();
        if (!tokenizer.hasNext()) {
            return new KeyboardRowEnd();
        } else if (tokenizer.peek() == '#') {
            tokenizer.moveToEnd();
            return null;
        } else if (tokenizer.peek() == '[') {
            tokenizer.next();
            return new AttributeList(parseAttributesUntilLineEnd(tokenizer));
        }

        String keyName;
        char next = tokenizer.peek();
        if (next == '"') {
            keyName = parseTextInDoubleQuotes(tokenizer);
        } else {
            keyName = parseUnquotedKeyName(tokenizer);
        }

        // After key name, expect keys or attributes
        List<Attribute> attributes = new ArrayList<>();
        List<KeyNameSet> keys = new ArrayList<>();

        tokenizer.skipWhitespace();
        while (tokenizer.hasNext()) {
            char chr = tokenizer.peek();
            if (chr == '[') {
                tokenizer.next();
                attributes.addAll(parseAttributesUntilLineEnd(tokenizer));
            } else if (chr == '#') {
                tokenizer.moveToEnd();
            } else if (chr == '$') {
                tokenizer.next();
                Variable v = variablesByName.get(extractVariableIdentifierOrThrow(tokenizer));
                if (v instanceof AttributeVariable av) {
                    attributes.addAll(av.attributes());
                } else {
                    // TODO: Text variables cannot be used as key aliases
                    throw new ParserException(
                        "Variable is not an attribute variable on " + tokenizer.getLineNrColText());
                }
            } else {
                keys.add(parseKeyBinding(tokenizer));
            }

            tokenizer.skipWhitespace();
        }

        return new KeyLine(keyName, keys, attributes);
    }

    private List<Attribute> parseAttributesUntilLineEnd(Tokenizer tokenizer) {
        List<Attribute> attributes = new ArrayList<>();
        parseAttributeDeclaration(tokenizer, attributes);

        do {
            tokenizer.skipWhitespace();
            if (tokenizer.hasNext()) {
                char chr = tokenizer.next();
                if (chr != '[') {
                    throw new ParserException("Expected only attributes to be declared, but found '" + chr + "' on "
                        + tokenizer.getLineNrColText());
                } else {
                    parseAttributeDeclaration(tokenizer, attributes);
                }
            } else {
                return attributes;
            }
        } while (true);
    }

    private String parseUnquotedKeyName(Tokenizer tokenizer) {
        // Key name
        StringBuilder keyName = new StringBuilder();
        while (tokenizer.hasNext() && !Character.isWhitespace(tokenizer.peek())) {
            char chr = tokenizer.next();
            if (chr == '$') {
                keyName.append(parseAndResolveVariableValue(tokenizer));
            } else if (chr == '\\') {
                keyName.append(handleBackslashEscape(tokenizer));
            } else {
                keyName.append(chr);
            }
        }
        return keyName.toString();
    }

    private KeyNameSet parseKeyBinding(Tokenizer tokenizer) {
        Set<String> keyNames = new HashSet<>();
        while (true) {
            String keyName = parseKeyBindingName(tokenizer);
            keyNames.add(keyName);

            tokenizer.skipWhitespace();
            if (!tokenizer.hasNext() || tokenizer.peek() != '&') {
                break;
            } else {
                tokenizer.next(); // '&'
                tokenizer.skipWhitespace();
                // TODO: This is hacky
                if (!tokenizer.hasNext() || tokenizer.peek() == '&' || tokenizer.peek() == '[') {
                    String next = tokenizer.hasNext() ? "'" + tokenizer.next() + "'" : "end of line";
                    throw new ParserException("After ampersand, expect another key, but got "
                        + next + " on " + tokenizer.getLineNrColText());
                }
            }
        }

        return new KeyNameSet(keyNames);
    }

    private String parseKeyBindingName(Tokenizer tokenizer) {
        char nextChar = tokenizer.peek();
        if (nextChar == '"') {
            return parseTextInDoubleQuotes(tokenizer);
        } else if (isSimpleValueChar(nextChar)) {
            return tokenizer.nextAllMatching(this::isSimpleValueChar, false);
        } else {
            if (nextChar == '&' || nextChar == '$' || nextChar == '#') {
                throw new ParserException("Unexpected '" + nextChar + "' on " + tokenizer.getLineNrColText()
                    + ". Wrap complex names in double quotes");
            }
            char keyChar = tokenizer.next();
            if (tokenizer.hasNext()) {
                char followingChar = tokenizer.peek();
                if (!Character.isWhitespace(followingChar) && followingChar != '&') {
                    throw new ParserException("Invalid key name on " + tokenizer.getLineNrColText()
                        + ". Wrap complex names in double quotes");
                }
            }
            return String.valueOf(keyChar);
        }
    }

    private boolean isValidIdentifierChar(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || (c >= '0' && c <= '9')
            || (c == '_' || c == '-');
    }

    private boolean isSimpleValueChar(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || (c >= '0' && c <= '9')
            || (c == '_' || c == '.' || c == '-');
    }

    private void processAttributes(Tokenizer tokenizer) {
        List<Attribute> attributes = parseAttributeDeclaration(tokenizer);
        for (Attribute attribute : attributes) {
            String prev = attributeNamesToValue.put(attribute.name(), attribute.value());
            if (prev != null) {
                throw new ParserException("Attribute '" + attribute.name() + "' is declared multiple times");
            }
        }
    }

    private List<Attribute> parseAttributeDeclaration(Tokenizer tokenizer) {
        List<Attribute> attributes = new ArrayList<>();
        parseAttributeDeclaration(tokenizer, attributes);
        return attributes;
    }

    private void parseAttributeDeclaration(Tokenizer tokenizer, List<Attribute> attributes) {
        while (true) {
            // Get attribute name
            String identifier = extractAttributeIdentifierOrThrow(tokenizer);

            // Expect '='
            tokenizer.expectCharAfterOptionalWhitespace('=');

            // Collect value
            tokenizer.skipWhitespace();
            char next = tokenizer.peek();
            String value = (next == '"')
                ? parseTextInDoubleQuotes(tokenizer)
                : parseSimpleText(tokenizer);

            attributes.add(new Attribute(identifier, value));

            next = tokenizer.nextNonWhitespace();
            if (next == ']') {
                return;
            } else if (next != ',') {
                throw new ParserException("Unexpected character '" + next
                    + "' on " + tokenizer.getLineNrColText());
            }
        }
    }

    private void processVariable(Tokenizer tokenizer) {
        Variable variable = parseVariableDeclaration(tokenizer);
        Object prev = variablesByName.put(variable.name(), variable);
        if (prev != null) {
            throw new ParserException("The variable $" + variable.name() + " was already defined");
        }
    }

    private Variable parseVariableDeclaration(Tokenizer tokenizer) {
        // '$' was already consumed, so identifier is without the starting '$'
        String identifier = extractVariableIdentifierOrThrow(tokenizer);

        // Expect '='
        tokenizer.expectCharAfterOptionalWhitespace('=');

        // Next char determines what happens
        tokenizer.skipWhitespace();
        char next = tokenizer.peek();
        if (next == '[') {
            tokenizer.next();
            List<Attribute> attributes = parseAttributeDeclaration(tokenizer);
            return new AttributeVariable(identifier, attributes);
        } else if (next == '"') {
            String value = parseTextInDoubleQuotes(tokenizer);
            return new ValueVariable(identifier, value);
        } else {
            String value = parseSimpleText(tokenizer);
            return new ValueVariable(identifier, value);
        }
    }

    private String parseAndResolveVariableValue(Tokenizer tokenizer) {
        // '$' was already consumed
        String identifier = extractVariableIdentifierOrThrow(tokenizer);

        Variable variable = variablesByName.get(identifier);
        if (variable instanceof ValueVariable vv) {
            return vv.value();
        } else if (variable instanceof AttributeVariable) {
            throw new ParserException("Invalid variable usage of $" + identifier
                + ": variable contains attribute(s), not a value!");
        } else {
            throw new ParserException("Unknown variable: $" + identifier);
        }
    }

    private String parseTextInDoubleQuotes(Tokenizer tokenizer) {
        StringBuilder value = new StringBuilder();
        char chr = tokenizer.next();
        if (chr != '"') {
            throw new IllegalStateException("Expected double quote"); // should never happen
        }

        chr = tokenizer.next();
        while (true) {
            if (chr == '$') {
                value.append(parseAndResolveVariableValue(tokenizer));
            } else if (chr == '"') {
                break;
            } else if (chr == '\\') {
                value.append(handleBackslashEscape(tokenizer));
            } else {
                value.append(chr);
            }

            if (tokenizer.hasNext()) {
                chr = tokenizer.next();
            } else {
                throw new ParserException(
                    "Unexpected end of line; \" not closed on " + tokenizer.getLineNrText());
            }
        }
        return value.toString();
    }

    private void expectKeysSectionOrThrow(char firstCharacter, Tokenizer tokenizer) {
        if (Character.toLowerCase(firstCharacter) == 'k'
                && Character.toLowerCase(tokenizer.next()) == 'e'
                && Character.toLowerCase(tokenizer.next()) == 'y'
                && Character.toLowerCase(tokenizer.next()) == 's') {
            tokenizer.expectCharAfterOptionalWhitespace(':');
            expectEndOfContent(tokenizer);
        } else {
            throw new ParserException("Invalid syntax on " + tokenizer.getLineNrText());
        }
    }

    private void expectEndOfContent(Tokenizer tokenizer) {
        tokenizer.skipWhitespace();
        if (!tokenizer.isEmptyOrHasCommentStart()) {
            char chr = tokenizer.next();
            throw new ParserException("Expected end of line, but got '" + chr
                + "' on " + tokenizer.getLineNrColText());
        }
        tokenizer.moveToEnd();
    }

    private String parseSimpleText(Tokenizer tokenizer) {
        if (tokenizer.peek() == '$') {
            tokenizer.next();
            return parseAndResolveVariableValue(tokenizer);
        }

        String value = tokenizer.nextAllMatching(this::isSimpleValueChar, false);
        if (value.isEmpty()) {
            throw new ParserException("Unexpected character '" + tokenizer.peek() + "' on "
                + tokenizer.getLineNrColText() + ". Use double quotes around complex values");
        }
        return value;
    }

    private static char handleBackslashEscape(Tokenizer tokenizer) {
        char nextChar = tokenizer.next();
        switch (nextChar) {
            case '\\':
            case '"':
            case '$':
                return nextChar;
            default:
                throw new ParserException(
                    "Unknown escape: \\" + nextChar + " on " + tokenizer.getLineNrColText());
        }
    }

    private String extractAttributeIdentifierOrThrow(Tokenizer tokenizer) {
        String identifier = tokenizer.nextAllMatching(this::isValidIdentifierChar, true);
        if (identifier.isEmpty()) {
            String actual = tokenizer.hasNext() ? "'" + tokenizer.next() + "'" : "end of line";
            throw new ParserException("Expected attribute identifier ([a-zA-Z0-9_-]), but got "
                + actual + " on " + tokenizer.getLineNrColText());
        }
        return identifier;
    }

    private String extractVariableIdentifierOrThrow(Tokenizer tokenizer) {
        // '$' was already consumed, so do not ignore initial whitespace -> $ size = 3 is not correct syntax
        String identifier = tokenizer.nextAllMatching(this::isValidIdentifierChar, false);
        if (identifier.isEmpty()) {
            String actual = tokenizer.hasNext() ? "'" + tokenizer.next() + "'" : "end of line";
            throw new ParserException("Expected variable identifier ([a-zA-Z0-9_-]), but got "
                + actual + " on " + tokenizer.getLineNrColText());
        }
        return identifier;
    }

    private static final class RowsContainer {

        private final List<KeyRow> rows = new ArrayList<>();
        private KeyRow currentRow = new KeyRow();

        void processKeyLineResult(KeyboardLineParseResult keyLineResult) {
            if (keyLineResult instanceof KeyLine keyLine) {
                currentRow.addKey(keyLine);
            } else if (keyLineResult instanceof AttributeList attributeList) {
                currentRow.getAttributes().addAll(attributeList.attributes());
            } else if (keyLineResult instanceof KeyboardRowEnd) {
                processKeyboardRowEnd();
            } else if (keyLineResult != null) {
                throw new IllegalStateException("Unexpected parse result of type '" + keyLineResult.getClass() + "'");
            }
        }

        List<KeyRow> build() {
            // build() is not a great name but at least drives the point home that it does something more than just
            // return rows, since it also potentially ends the currentRow
            processKeyboardRowEnd();
            return rows;
        }

        private void processKeyboardRowEnd() {
            if (currentRow.hasKeys()) {
                rows.add(currentRow);
                currentRow = new KeyRow();
            }
        }
    }
}
