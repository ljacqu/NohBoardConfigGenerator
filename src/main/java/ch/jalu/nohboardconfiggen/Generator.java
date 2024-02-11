package ch.jalu.nohboardconfiggen;

import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Generator {

    private final DefinitionParser parser = new DefinitionParser();

    public static void main(String... args) {
        new Generator().generateConfig();
    }

    public void generateConfig() {
        Path file = Paths.get("./src/test/resources/conf_sample.txt");
        KeyboardConfig config = parser.parseConfig(file);

    }

}
