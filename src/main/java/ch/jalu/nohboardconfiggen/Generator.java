package ch.jalu.nohboardconfiggen;

import ch.jalu.nohboardconfiggen.config.NohbConfiguration;
import ch.jalu.nohboardconfiggen.config.NohboardConfigGenerator;
import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.generator.KeyboardModelGenerator;
import ch.jalu.nohboardconfiggen.definition.parser.DefinitionParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Generator {

    private final KeyboardModelGenerator modelGenerator = new KeyboardModelGenerator();

    public NohbConfiguration generateConfig(Path modelFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(modelFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file '" + modelFile + "'", e);
        }

        DefinitionParser parser = new DefinitionParser();
        parser.parse(lines);

        KeyboardConfig keyboardModel = modelGenerator.generate(parser.buildAttributes(), parser.getKeyRows());
        NohboardConfigGenerator generator = new NohboardConfigGenerator();
        return generator.generate(keyboardModel);
    }
}
