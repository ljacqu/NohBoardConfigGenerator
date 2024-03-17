package ch.jalu.nohboardconfiggen.definition.parser;

import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;
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

public class DefinitionParser {

    private final Map<String, String> attributeNamesToValue = new HashMap<>();
    final Map<String, Variable> variablesByName = new HashMap<>();
    @Getter
    final List<KeyRow> keyRows = new ArrayList<>();


    public void parse(List<String> lines) {
        int lineNumber = 1;
        boolean isHeaderSection = true;
        KeyRow currentRow = new KeyRow();

        for (String line : lines) {
            if (isHeaderSection) {
                isHeaderSection = !parseHeaderLine(line, lineNumber);
            } else {
                KeyboardLineParseResult key = parseKeyLine(line, lineNumber);
                if (key instanceof KeyboardRowEnd && !currentRow.isEmpty()) {
                    keyRows.add(currentRow);
                    currentRow = new KeyRow();
                } else if (key instanceof KeyLine keyLine) {
                    currentRow.add(keyLine);
                }
            }
            ++lineNumber;
        }
        if (!currentRow.isEmpty()) {
            keyRows.add(currentRow);
        }
    }

    public List<Attribute> getAttributes() {
        return attributeNamesToValue.entrySet().stream()
            .map(entry -> new Attribute(entry.getKey(), entry.getValue()))
            .toList();
    }

    @VisibleForTesting
    boolean parseHeaderLine(String line, int lineNumber) {
        Tokenizer tokenizer = new Tokenizer(line, lineNumber);
        tokenizer.skipWhitespace();

        if (tokenizer.hasNext()) {
            char chr = tokenizer.next();
            if (chr == '#') {
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

    @VisibleForTesting
    KeyboardLineParseResult parseKeyLine(String line, int lineNumber) {
        Tokenizer tokenizer = new Tokenizer(line, lineNumber);
        tokenizer.skipWhitespace();
        if (!tokenizer.hasNext()) {
            return new KeyboardRowEnd();
        } else if (tokenizer.peek() == '#') {
            return null;
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
                attributes.addAll(parseAttributeDeclaration(tokenizer));
            } else if (chr == '#') {
                break;
            } else if (chr == '$') {
                tokenizer.next();
                Variable v = variablesByName.get(parseVariableIdentifier(tokenizer));
                if (v instanceof AttributeVariable av) {
                    attributes.addAll(av.attributes());
                } else {
                    // TODO: Text variables cannot be used as key aliases
                    throw new IllegalStateException(
                        "Variable is not an attribute variable on " + tokenizer.getLineNrColText());
                }
            } else {
                keys.add(parseKeyBinding(tokenizer));
            }

            tokenizer.skipWhitespace();
        }

        return new KeyLine(keyName, keys, attributes);
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
                    throw new IllegalStateException("After ampersand, expect another key, but got "
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
                throw new IllegalStateException("Unexpected '" + nextChar + "' on " + tokenizer.getLineNrColText()
                    + ". Wrap complex names in double quotes");
            }
            char keyChar = tokenizer.next();
            if (tokenizer.hasNext()) {
                char followingChar = tokenizer.peek();
                if (!Character.isWhitespace(followingChar) && followingChar != '&') {
                    throw new IllegalStateException("Invalid key name on " + tokenizer.getLineNrColText()
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
            || (c == '_');
    }

    private boolean isSimpleValueChar(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || (c >= '0' && c <= '9')
            || (c == '_' || c == '.');
    }

    private void processAttributes(Tokenizer tokenizer) {
        List<Attribute> attributes = parseAttributeDeclaration(tokenizer);
        for (Attribute attribute : attributes) {
            String prev = attributeNamesToValue.put(attribute.name(), attribute.value());
            if (prev != null) {
                throw new IllegalStateException("Attribute '" + attribute.name() + "' is declared multiple times");
            }
        }
    }

    private List<Attribute> parseAttributeDeclaration(Tokenizer tokenizer) {
        List<Attribute> attributes = new ArrayList<>();

        while (true) {
            // Get attribute name
            String identifier = tokenizer.nextAllMatching(this::isValidIdentifierChar, true);
            if (identifier.isEmpty()) {
                String actual = tokenizer.hasNext() ? "'" + tokenizer.next() + "'" : "end of line";
                throw new IllegalStateException("Expected attribute identifier ([a-zA-Z0-9_]), but got "
                    + actual + " on " + tokenizer.getLineNrColText());
            }

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
                return attributes;
            } else if (next != ',') {
                throw new IllegalStateException("Unexpected character '" + next
                    + "' on " + tokenizer.getLineNrColText());
            }
        }
    }

    private void processVariable(Tokenizer tokenizer) {
        Variable variable = parseVariableDeclaration(tokenizer);
        Object prev = variablesByName.put(variable.name(), variable);
        if (prev != null) {
            throw new IllegalArgumentException("The variable $" + variable.name() + " was already defined");
        }
    }

    private Variable parseVariableDeclaration(Tokenizer tokenizer) {
        // '$' was already consumed, so identifier is without the starting '$'
        String identifier = tokenizer.nextAllMatching(this::isValidIdentifierChar, false);
        if (identifier.isEmpty()) {
            String actual = tokenizer.hasNext() ? "'" + tokenizer.next() + "'" : "end of line";
            throw new IllegalStateException("Expected attribute identifier ([a-zA-Z0-9_]), but got "
                + actual + " on " + tokenizer.getLineNrColText());
        }

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
        String identifier = parseVariableIdentifier(tokenizer);

        Variable variable = variablesByName.get(identifier);
        if (variable instanceof ValueVariable vv) {
            return vv.value();
        } else if (variable instanceof AttributeVariable) {
            throw new IllegalStateException("Invalid variable usage of $" + identifier
                + ": variable contains attribute(s), not a value!");
        } else {
            throw new IllegalStateException("Unknown variable: $" + identifier);
        }
    }

    private String parseVariableIdentifier(Tokenizer tokenizer) {
        // '$' already consumed
        String identifier = tokenizer.nextAllMatching(this::isValidIdentifierChar, false);
        if (identifier.isEmpty()) {
            String actual = tokenizer.hasNext() ? "'" + tokenizer.next() + "'" : "end of line";
            throw new IllegalStateException("Expected attribute identifier ([a-zA-Z0-9_]), but got "
                + actual + " on " + tokenizer.getLineNrColText());
        }
        return identifier;
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
                throw new IllegalStateException(
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
            throw new IllegalStateException("Invalid syntax on " + tokenizer.getLineNrText());
        }
    }

    private void expectEndOfContent(Tokenizer tokenizer) {
        tokenizer.skipWhitespace();
        if (!tokenizer.isEmptyOrHasCommentStart()) {
            char chr = tokenizer.next();
            throw new IllegalStateException("Expected end of line, but got '" + chr
                + "' on " + tokenizer.getLineNrColText());
        }
    }

    private String parseSimpleText(Tokenizer tokenizer) {
        if (tokenizer.peek() == '$') {
            tokenizer.next();
            return parseAndResolveVariableValue(tokenizer);
        }

        String value = tokenizer.nextAllMatching(this::isSimpleValueChar, false);
        if (value.isEmpty()) {
            throw new IllegalStateException("Unexpected character '" + tokenizer.peek() + "' on "
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
                throw new IllegalStateException(
                    "Unknown escape: \\" + nextChar + " on " + tokenizer.getLineNrColText());
        }
    }
}
