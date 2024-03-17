package ch.jalu.nohboardconfiggen;

import ch.jalu.nohboardconfiggen.config.NohbConfiguration;
import ch.jalu.nohboardconfiggen.config.NohboardConfigGenerator;
import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.generator.KeyboardModelGenerator;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Generator {

    private final KeyboardModelGenerator modelGenerator = new KeyboardModelGenerator();

    public static void main(String... args) {
        new Generator().generateConfig();
    }

    public NohbConfiguration generateConfig(Path modelFile) {
        ch.jalu.nohboardconfiggen.definition.parser.DefinitionParser parser =
            new ch.jalu.nohboardconfiggen.definition.parser.DefinitionParser();

        List<String> lines;
        try {
            lines = Files.readAllLines(modelFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file '" + modelFile + "'", e);
        }
        parser.parse(lines);

        KeyboardConfig keyboardModel = modelGenerator.generate(parser.getAttributes(), parser.getKeyRows());
        NohboardConfigGenerator generator = new NohboardConfigGenerator();
        return generator.generate(keyboardModel);
    }

    private void generateConfig() {
        Path file = Paths.get("./src/test/resources/testconfigs/tr3.txt");
        NohbConfiguration result = generateConfig(file);
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(result));
    }
}
