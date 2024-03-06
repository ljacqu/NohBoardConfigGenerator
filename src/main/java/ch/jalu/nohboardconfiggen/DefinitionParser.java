package ch.jalu.nohboardconfiggen;

import ch.jalu.nohboardconfiggen.definition.KeyBinding;
import ch.jalu.nohboardconfiggen.definition.KeyDefinition;
import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.KeyboardRow;
import ch.jalu.nohboardconfiggen.definition.Unit;
import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;
import ch.jalu.nohboardconfiggen.keycode.KeyboardLayout;
import ch.jalu.nohboardconfiggen.keycode.KeyboardRegion;
import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefinitionParser {

    private static final Pattern PROPERTY_DEFINITION_PATTERN = Pattern.compile("(\\w+)=([a-zA-Z0-9.]+)");

    private static final Pattern NUMERIC_VALUE_WITH_OPTIONAL_UNIT = Pattern.compile("(\\d+(\\.\\d+)?)(\\w+)?");

    private final Map<String, String> variables = new LinkedHashMap<>();
    private KeyboardLayout keyboardLayout;

    public KeyboardConfig parseConfig(Path file) {
        List<String> lines = readAllLines(file);

        KeyboardConfig config = new KeyboardConfig();

        boolean foundKeysSection = false;
        KeyboardRow row = new KeyboardRow();

        for (String line : lines) {
            line = line.trim();

            if (!foundKeysSection) {
                if (handleGlobalProperty(line, config)) {
                    continue; // property saved
                } else if (line.startsWith("$")) {
                    processVariable(line);
                } else if (line.equals("Keys:")) {
                    foundKeysSection = true;
                    if (keyboardLayout == null) {
                        keyboardLayout = KeyboardLayout.create(null);
                    }
                } else if (!line.isEmpty()) {
                    throw new IllegalArgumentException("Unexpected line in config section: " + line);
                }
            } else {
                if (line.isEmpty()) {
                    config.getRows().add(row);
                    row = new KeyboardRow();
                } else {
                    if (handleRowProperty(line, row)) {
                        continue; // property saved
                    } else {
                        row.getKeys().add(parseKeyLine(line));
                    }
                }
            }
        }
        config.getRows().add(row);

        config.getRows().removeIf(configRow -> configRow.getKeys().isEmpty());
        if (config.getRows().isEmpty()) {
            throw new IllegalArgumentException("No rows defined for keyboard");
        }
        return config;
    }

    private boolean handleGlobalProperty(String line, KeyboardConfig config) {
        Matcher propertyDefinitionMatcher = PROPERTY_DEFINITION_PATTERN.matcher(line);
        if (!propertyDefinitionMatcher.matches()) {
            return false;
        }

        String propertyName = propertyDefinitionMatcher.group(1);
        String valueRaw = propertyDefinitionMatcher.group(2);

        switch (propertyName) {
            case "keyboard":
                keyboardLayout = KeyboardLayout.create(KeyboardRegion.findByCodeOrThrow(valueRaw));
                break;
            case "width":
                config.setWidth(extractPixelPropertyOrThrow(valueRaw, propertyName, line));
                break;
            case "height":
                config.setHeight(extractPixelPropertyOrThrow(valueRaw, propertyName, line));
                break;
            case "spacing":
                config.setSpacing(extractPixelPropertyOrThrow(valueRaw, propertyName, line));
                break;
            default:
                throw new IllegalArgumentException("Unknown property '" + propertyName + "' in line: " + line);
        }
        return true;
    }

    private static int extractPixelPropertyOrThrow(String valueRaw, String propertyName, String fullLine) {
        Matcher numericValueMatcher = NUMERIC_VALUE_WITH_OPTIONAL_UNIT.matcher(valueRaw);
        Integer value = numericValueMatcher.matches()
            ? Ints.tryParse(numericValueMatcher.group(1))
            : null;
        if (value == null) {
            throw new IllegalArgumentException("Invalid value for property '" + propertyName
                + "' found, expected integer. Full line: " + fullLine);
        }

        String unitInput = numericValueMatcher.group(3);
        if (unitInput != null) {
            Unit unit = Unit.fromSymbolOrDefaultIfNull(unitInput);
            if (unit != Unit.PIXEL) {
                throw new IllegalArgumentException("Found invalid unit for property '" + propertyName
                    + "'; expected pixels. Line: " + fullLine);
            }
        }
        return value;
    }

    private KeyDefinition parseKeyLine(String line) {
        int firstSpaceIndex = line.indexOf(' ');
        KeyDefinition key = new KeyDefinition();
        key.setText(firstSpaceIndex >= 0 ? line.substring(0, firstSpaceIndex) : line);

        String remainder = firstSpaceIndex >= 0 ? replaceVariables(line.substring(firstSpaceIndex)) : "";
        for (String linePart : remainder.split(" ")) {
            if (linePart.isEmpty() || processPropertyForKey(linePart, key, line)) {
                continue;
            }

            int keyCode = keyboardLayout.getKeyCodeOrThrow(linePart);
            key.getKeys().add(new KeyBinding(keyCode));
        }

        if (key.getKeys().isEmpty()) {
            throw new IllegalArgumentException("No keyboard key was defined for line: " + line);
        }
        return key;
    }

    private boolean processPropertyForKey(String linePart, KeyDefinition key, String fullLine) {
        Matcher propertyDefinitionMatcher = PROPERTY_DEFINITION_PATTERN.matcher(linePart);
        if (!propertyDefinitionMatcher.matches()) {
            return false;
        }

        String propertyName = propertyDefinitionMatcher.group(1);
        String valueRaw = propertyDefinitionMatcher.group(2);

        if ("id".equals(propertyName)) {
            Integer value = Ints.tryParse(valueRaw);
            if (value != null) {
                key.setId(value);
                return true;
            } else {
                throw new IllegalArgumentException("Invalid value '"  + valueRaw + "' for property 'id'");
            }
        }

        ValueWithUnit value = parseValueWithOptionalUnit(valueRaw, propertyName, fullLine);
        switch (propertyName) {
            case "width":
                key.setCustomWidth(value);
                break;
            case "height":
                key.setCustomHeight(value);
                break;
            case "marginTop":
                key.setMarginTop(value);
                break;
            case "marginLeft":
                key.setMarginLeft(value);
                break;
            case "stacked":
                key.setStacked(value.value().compareTo(BigDecimal.ZERO) != 0);
                break;
            default:
                throw new IllegalArgumentException("Unknown property '" + propertyName + "' in line: " + fullLine);
        }
        return true;
    }

    private boolean handleRowProperty(String line, KeyboardRow row) {
        Matcher propertyDefinitionMatcher = PROPERTY_DEFINITION_PATTERN.matcher(line);
        if (!propertyDefinitionMatcher.matches()) {
            return false;
        }

        String propertyName = propertyDefinitionMatcher.group(1);
        String valueRaw = propertyDefinitionMatcher.group(2);
        ValueWithUnit value = parseValueWithOptionalUnit(valueRaw, propertyName, line);

        switch (propertyName) {
            case "marginTop":
                row.setMarginTop(value);
                break;
            case "marginLeft":
                row.setMarginLeft(value);
                break;
            default:
                throw new IllegalArgumentException("Unknown row property '" + propertyName + "' in line: " + line);
        }
        return true;
    }

    private ValueWithUnit parseValueWithOptionalUnit(String propertyValue, String propertyName, String fullLine) {
        Matcher matcher = NUMERIC_VALUE_WITH_OPTIONAL_UNIT.matcher(propertyValue);
        String numericValueRaw = matcher.matches() ? matcher.group(1) : "undefined";
        BigDecimal value = NumberUtils.parseBigDecimalOrThrow(numericValueRaw,
            () -> "Invalid number for property '" + propertyName + "' in line: " + fullLine);

        String unitSpecifier = matcher.group(3);
        Unit unit = Unit.fromSymbolOrDefaultIfNull(unitSpecifier);
        return new ValueWithUnit(value, unit);
    }

    private void processVariable(String line) {
        int firstEqualsIndex = line.indexOf('=');
        if (firstEqualsIndex <= 0) {
            throw new IllegalArgumentException("Invalid variable assignment, line: " + line);
        }

        String varName = line.substring(0, firstEqualsIndex);
        if (varName.equals("$") || !varName.trim().equals(varName)) {
            throw new IllegalArgumentException("Invalid variable name in line: " + line);
        }
        String value = line.substring(firstEqualsIndex + 1);
        variables.put(varName, value);
    }

    private String replaceVariables(String text) {
        if (text.indexOf('$') >= 0) {
            String result = text;
            for (Map.Entry<String, String> variableByNameAndValue : variables.entrySet()) {
                result = result.replace(variableByNameAndValue.getKey(), variableByNameAndValue.getValue());
            }
            return result;
        }
        return text;
    }

    private static List<String> readAllLines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + file, e);
        }
    }
}
