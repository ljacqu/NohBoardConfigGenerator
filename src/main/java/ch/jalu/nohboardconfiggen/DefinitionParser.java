package ch.jalu.nohboardconfiggen;

import ch.jalu.nohboardconfiggen.definition.KeyCode;
import ch.jalu.nohboardconfiggen.definition.KeyDefinition;
import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.KeyboardRow;
import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class DefinitionParser {

    private static final Pattern PROPERTY_DEFINITION_PATTERN = Pattern.compile("(\\w+)=(\\d+(\\.\\d+)?)");

    public KeyboardConfig parseConfig(Path file) {
        List<String> lines = readAllLines(file);

        KeyboardConfig config = new KeyboardConfig();

        boolean hasKey = false;
        KeyboardRow row = new KeyboardRow();

        for (String line : lines) {
            line = line.trim();
            if (!hasKey) {
                if (PROPERTY_DEFINITION_PATTERN.matcher(line).matches()) {
                    processConfigLine(line, config);
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
                    row.getKeys().add(parseKeyLine(line));
                }
            }
        }
        config.getRows().add(row);

        // todo: find better way to add rows
        config.getRows().removeIf(configRow -> configRow.getKeys().isEmpty());
        if (config.getRows().isEmpty()) {
            throw new IllegalArgumentException("No rows defined for keyboard");
        }
        return config;
    }

    private KeyDefinition parseKeyLine(String line) {
        String[] lineParts = line.split(" ");
        KeyDefinition key = new KeyDefinition();
        key.setText(lineParts[0]);
        for (int i = 1; i < lineParts.length; ++i) {
            if (PROPERTY_DEFINITION_PATTERN.matcher(lineParts[i]).matches()) {
                processConfigStatement(lineParts[i], key, line);
            } else {
                key.getKeys().add(KeyCode.getEntryOrThrow(lineParts[i]));
            }
        }
        if (key.getKeys().isEmpty()) {
            throw new IllegalArgumentException("No keyboard key was defined for key: " + line);
        }
        return key;
    }

    private void processConfigLine(String line, KeyboardConfig config) {
        Property<Integer> property = parseIntConfigValue(line);
        switch (property.name()) {
            case "width":
                config.setWidth(property.value());
                break;
            case "height":
                config.setHeight(property.value());
                break;
            case "space":
                config.setSpace(property.value());
                break;
            default:
                throw new IllegalArgumentException("Unsupported property '" + property.name()
                    + "', from line: " + line);
        }
    }

    private void processConfigStatement(String textToParse, KeyDefinition key, String fullLine) {
        Property<BigDecimal> property = parseBigDecimalConfigValue(textToParse);
        switch (property.name()) {
            case "width":
                key.setCustomWidth(property.value);
                break;
            case "height":
                key.setCustomHeight(property.value);
                break;
            default:
                throw new IllegalArgumentException("Unknown property '" + property.name
                    + ", from line: " + fullLine);
        }
    }

    private Property<Integer> parseIntConfigValue(String line) {
        String[] parts = line.split("=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid line: " + line);
        }
        Integer num = Ints.tryParse(parts[1]);
        if (num == null) {
            throw new IllegalArgumentException("Expected numerical value in line: " + line);
        }
        return new Property<>(parts[0], num);
    }

    private Property<BigDecimal> parseBigDecimalConfigValue(String line) {
        String[] parts = line.split("=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid line: " + line);
        }
        try {
            return new Property<>(parts[0], new BigDecimal(parts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expected numerical value in line: " + line);
        }
    }

    private static List<String> readAllLines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + file, e);
        }
    }

    private record Property<T>(String name, T value) {

    }
}
