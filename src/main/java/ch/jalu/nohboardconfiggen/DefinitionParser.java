package ch.jalu.nohboardconfiggen;

import ch.jalu.nohboardconfiggen.definition.KeyCode;
import ch.jalu.nohboardconfiggen.definition.KeyDefinition;
import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.KeyboardRow;
import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.UncheckedIOException;
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
                if (line.isEmpty()) {
                    continue;
                } else if (line.startsWith("width=") || line.startsWith("height=") || line.startsWith("space=")) {
                    Object[] propertyAndValue = parseConfigLine(line);
                    switch ((String) propertyAndValue[0]) {
                        case "width":
                            config.setWidth((int) propertyAndValue[1]);
                            break;
                        case "height":
                            config.setHeight((int) propertyAndValue[1]);
                            break;
                        case "space":
                            config.setSpace((int) propertyAndValue[1]);
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported property '" + propertyAndValue[0]
                                + "', from line: " + line);
                    }
                } else if (line.equals("Keys:")) {
                    hasKey = true;
                    continue;
                } else {
                    throw new IllegalArgumentException("Unexpected line in config section: " + line);
                }
            } else {
                if (line.isEmpty()) {
                    config.getRows().add(row);
                    row = new KeyboardRow();
                } else {
                    String[] lineParts = line.split(" ");
                    KeyDefinition key = new KeyDefinition();
                    key.setText(lineParts[0]);
                    for (int i = 1; i < lineParts.length; ++i) {
                        if (PROPERTY_DEFINITION_PATTERN.matcher(lineParts[i]).matches()) {
                            // TODO: Configuration
                        } else {
                            key.getKeys().add(KeyCode.getEntryOrThrow(lineParts[i]));
                        }
                    }
                    if (key.getKeys().isEmpty()) {
                        throw new IllegalArgumentException("No keyboard key was defined for key: " + line);
                    }

                    row.getKeys().add(key);
                }
            }
        }

        // todo: find better way to add rows
        config.getRows().removeIf(configRow -> configRow.getKeys().isEmpty());
        if (config.getRows().isEmpty()) {
            throw new IllegalArgumentException("No rows defined for keyboard");
        }
        return config;
    }

    private Object[] parseConfigLine(String line) {
        String[] parts = line.split("=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid line: " + line);
        }
        Integer num = Ints.tryParse(parts[1]);
        if (num == null) {
            throw new IllegalArgumentException("Expected numerical value in line: " + line);
        }
        return new Object[]{ parts[0], num };
    }

    private static List<String> readAllLines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + file, e);
        }
    }
}
