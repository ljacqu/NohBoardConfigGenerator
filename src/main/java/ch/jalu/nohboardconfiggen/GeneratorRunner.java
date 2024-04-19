package ch.jalu.nohboardconfiggen;

import ch.jalu.nohboardconfiggen.config.NohbConfiguration;
import ch.jalu.nohboardconfiggen.config.NohboardConfigExporter;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class GeneratorRunner {

    private final Generator generator = new Generator();
    private final NohboardConfigExporter exporter = new NohboardConfigExporter();

    private GeneratorRunner() {
    }

    public static void main(String... args) {
        new GeneratorRunner().outputOrExportConfig();
    }

    private void outputOrExportConfig() {
        Path input = Paths.get("./src/test/resources/testconfigs/tr3.txt");
        // Set output file to immediately write to a file to continuously update the keyboard
        Path output = null; // Paths.get("Downloads/NohBoard/keyboards/TR/tr3hw/keyboard.json");
        NohbConfiguration result = generator.generateConfig(input);
        System.out.println(exporter.toJson(result));
        if (output != null) {
            exporter.export(result, output);
        }
    }
}
