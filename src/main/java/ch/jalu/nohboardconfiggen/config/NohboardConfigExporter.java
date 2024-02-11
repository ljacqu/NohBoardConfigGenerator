package ch.jalu.nohboardconfiggen.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class NohboardConfigExporter {

    private final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    public String toJson(NohbConfiguration config) {
        return gson.toJson(config);
    }

    public void export(NohbConfiguration config, Path fileToWriteTo) {
        try (FileWriter writer = new FileWriter(fileToWriteTo.toFile(), StandardCharsets.UTF_8)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write to '" + fileToWriteTo + "'", e);
        }
    }
}
