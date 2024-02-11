package ch.jalu.nohboardconfiggen;

import ch.jalu.nohboardconfiggen.config.NohbConfiguration;
import ch.jalu.nohboardconfiggen.config.NohboardConfigGenerator;
import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Generator {

    private final DefinitionParser parser = new DefinitionParser();

    public static void main(String... args) {
        new Generator().generateConfig();
    }

    public void generateConfig() {
        Path file = Paths.get("./src/test/resources/conf_sample2.txt");
        KeyboardConfig config = parser.parseConfig(file);
        System.out.println(config);

        NohboardConfigGenerator generator = new NohboardConfigGenerator();
        NohbConfiguration result = generator.generate(config);
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(result));
    }

}
