package ch.jalu.nohboardconfiggen;

import ch.jalu.nohboardconfiggen.definition.KeyCode;
import ch.jalu.nohboardconfiggen.definition.KeyDefinition;
import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.KeyboardRow;
import ch.jalu.nohboardconfiggen.definition.Unit;
import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;
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

    private static final Pattern PROPERTY_DEFINITION_PATTERN = Pattern.compile("(\\w+)=(-?\\d+(\\.\\d+)?)(\\w+)?");

    private final Map<String, String> variables = new LinkedHashMap<>();

    public KeyboardConfig parseConfig(Path file) {
        List<String> lines = readAllLines(file);

        KeyboardConfig config = new KeyboardConfig();

        boolean hasKey = false;
        KeyboardRow row = new KeyboardRow();

        for (String line : lines) {
            line = line.trim();
            if (!hasKey) {
                Matcher propertyDefinitionMatcher = PROPERTY_DEFINITION_PATTERN.matcher(line);
                if (propertyDefinitionMatcher.matches()) {
                    processIntProperty(line, config, propertyDefinitionMatcher);
                } else if (line.startsWith("$")) {
                    processVariable(line);
                } else if (line.equals("Keys:")) {
                    hasKey = true;
                } else if (!line.isEmpty()) {
                    throw new IllegalArgumentException("Unexpected line in config section: " + line);
                }
            } else {
                if (line.isEmpty()) {
                    config.getRows().add(row);
                    row = new KeyboardRow();
                } else {
                    Matcher propertyDefinitionMatcher = PROPERTY_DEFINITION_PATTERN.matcher(line);
                    if (propertyDefinitionMatcher.matches()) {
                        processPropertyForRow(row, line, propertyDefinitionMatcher);
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

    private KeyDefinition parseKeyLine(String line) {
        int firstSpaceIndex = line.indexOf(' ');
        KeyDefinition key = new KeyDefinition();
        key.setText(firstSpaceIndex >= 0 ? line.substring(0, firstSpaceIndex) : line);

        String remainder = firstSpaceIndex >= 0 ? replaceVariables(line.substring(firstSpaceIndex)) : "";
        for (String linePart : remainder.split(" ")) {
            if (linePart.isEmpty()) {
                continue;
            }

            Matcher propertyDefinitionMatcher = PROPERTY_DEFINITION_PATTERN.matcher(linePart);
            if (propertyDefinitionMatcher.matches()) {
                processPropertyForKey(key, line, propertyDefinitionMatcher);
            } else {
                key.getKeys().add(KeyCode.getEntryOrThrow(linePart));
            }
        }

        if (key.getKeys().isEmpty()) {
            throw new IllegalArgumentException("No keyboard key was defined for line: " + line);
        }
        return key;
    }

    private void processIntProperty(String line, KeyboardConfig config, Matcher propertyDefinitionMatcher) {
        String propertyName = propertyDefinitionMatcher.group(1);
        Integer value = Ints.tryParse(propertyDefinitionMatcher.group(2));
        if (value == null) {
            throw new IllegalArgumentException("Invalid value '" + propertyDefinitionMatcher.group(2)
                + "' found, expected integer. Full line: " + line);
        }
        String unitInput = propertyDefinitionMatcher.group(4);
        if (unitInput != null) {
            Unit unit = Unit.fromSymbolOrDefaultIfNull(unitInput);
            if (unit != Unit.PIXEL) {
                throw new IllegalArgumentException("Found invalid unit; expected pixels. Line: " + line);
            }
        }

        switch (propertyName) {
            case "width":
                config.setWidth(value);
                break;
            case "height":
                config.setHeight(value);
                break;
            case "spacing":
                config.setSpacing(value);
                break;
            default:
                throw new IllegalArgumentException("Unknown property '" + propertyName + "' in line: " + line);
        }
    }

    private void processPropertyForKey(KeyDefinition key, String fullLine, Matcher propertyDefinitionMatcher) {
        String propertyName = propertyDefinitionMatcher.group(1);
        BigDecimal value = NumberUtils.parseBigDecimalOrThrow(propertyDefinitionMatcher.group(2),
            () -> "Invalid number for property '" + propertyName + "' in line: " + fullLine);
        String unitSpecifier = propertyDefinitionMatcher.group(4);
        Unit unit = Unit.fromSymbolOrDefaultIfNull(unitSpecifier);

        switch (propertyName) {
            case "width":
                key.setCustomWidth(new ValueWithUnit(value, unit));
                break;
            case "height":
                key.setCustomHeight(new ValueWithUnit(value, unit));
                break;
            case "marginTop":
                key.setMarginTop(new ValueWithUnit(value, unit));
                break;
            case "marginLeft":
                key.setMarginLeft(new ValueWithUnit(value, unit));
                break;
            case "stacked":
                key.setStacked(value.compareTo(BigDecimal.ZERO) != 0);
                break;
            default:
                throw new IllegalArgumentException("Unknown property '" + propertyName + "' in line: " + fullLine);
        }
    }

    private void processPropertyForRow(KeyboardRow row, String fullLine, Matcher propertyDefinitionMatcher) {
        if (!row.getKeys().isEmpty()) {
            throw new IllegalArgumentException(
                "Unexpected row property; properties should come before keys. Line: " + fullLine);
        }

        String propertyName = propertyDefinitionMatcher.group(1);
        BigDecimal value = NumberUtils.parseBigDecimalOrThrow(propertyDefinitionMatcher.group(2),
            () -> "Invalid number for property '" + propertyName + "' in line: " + fullLine);
        String unitSpecifier = propertyDefinitionMatcher.group(4);
        Unit unit = Unit.fromSymbolOrDefaultIfNull(unitSpecifier);

        switch (propertyName) {
            case "marginTop":
                row.setMarginTop(new ValueWithUnit(value, unit));
                break;
            case "marginLeft":
                row.setMarginLeft(new ValueWithUnit(value, unit));
                break;
            default:
                throw new IllegalArgumentException("Unknown row property '" + propertyName + "' in line: " + fullLine);
        }
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
