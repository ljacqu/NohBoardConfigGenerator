package ch.jalu.nohboardconfiggen.config;

import ch.jalu.nohboardconfiggen.ConfigHelper;
import ch.jalu.nohboardconfiggen.definition.KeyCode;
import ch.jalu.nohboardconfiggen.definition.KeyDefinition;
import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NohboardConfigGenerator {

    private static final int MAX_BOUNDARY_INDEX = 2;
    private static final int KEYBOARD_SURFACE_MARGIN = 5;

    public NohbConfiguration generate(KeyboardConfig config) {
        NohbConfiguration nohbConfiguration = new NohbConfiguration();
        nohbConfiguration.setElements(generateElements(config));
        setHeightAndWidth(nohbConfiguration);
        return nohbConfiguration;
    }

    private List<NohbElement> generateElements(KeyboardConfig config) {
        List<NohbElement> elements = new ArrayList<>();

        // x is width, y is height
        int curX;
        int curY = KEYBOARD_SURFACE_MARGIN;
        for (KeyboardRow row : config.getRows()) {
            int maxY = 0;
            curX = KEYBOARD_SURFACE_MARGIN;
            for (KeyDefinition keyDefinition : row.getKeys()) {
                NohbElement element = new NohbElement();
                element.setTexts(keyDefinition.getText());
                element.setBoundaries(calculateBounds(curX, curY, config, keyDefinition));
                element.setTextPosition(ConfigHelper.calculateCenterTextPosition(element.getBoundaries()));
                List<NohbElement> elementsForKey = generateElementsForAllKeys(element, keyDefinition.getKeys());
                elements.addAll(elementsForKey);

                NohbCoords maxBoundary = element.getBoundaries().get(MAX_BOUNDARY_INDEX);
                maxY = Math.max(maxY, maxBoundary.getY());
                curX = maxBoundary.getX() + config.getSpace();
            }
            curY = maxY + config.getSpace(); // todo: what if a key should go down two rows? :/
        }

        int id = 1;
        for (NohbElement element : elements) {
            element.setId(id);
            ++id;
        }
        return elements;
    }

    private List<NohbElement> generateElementsForAllKeys(NohbElement template, Set<KeyCode> keys) {
        if (keys.size() == 1) {
            template.setKeyCodes(keys.stream().map(KeyCode::getCode).toList());
            return List.of(template);
        }

        List<NohbElement> elements = new ArrayList<>(keys.size());
        for (KeyCode key : keys) {
            NohbElement element = new NohbElement(template);
            element.setKeyCodes(List.of(key.getCode()));
            elements.add(element);
        }
        return elements;
    }

    private List<NohbCoords> calculateBounds(int curX, int curY, KeyboardConfig config, KeyDefinition key) {
        int xIncr = config.getWidth();
        int yIncr = config.getHeight(); // todo: respect key def

        NohbCoords coords1 = new NohbCoords(curX, curY);
        NohbCoords coords2 = new NohbCoords(curX + xIncr, curY);
        NohbCoords coords3 = new NohbCoords(curX + xIncr, curY + yIncr);
        NohbCoords coords4 = new NohbCoords(curX, curY + yIncr);
        return List.of(coords1, coords2, coords3, coords4);
    }

    private void setHeightAndWidth(NohbConfiguration config) {
        int maxX = 0;
        int maxY = 0;

        for (NohbElement element : config.getElements()) {
            NohbCoords maxBoundary = element.getBoundaries().get(MAX_BOUNDARY_INDEX);
            maxX = Math.max(maxX, maxBoundary.getX());
            maxY = Math.max(maxY, maxBoundary.getY());
        }

        config.setWidth(maxX + KEYBOARD_SURFACE_MARGIN);
        config.setHeight(maxY + KEYBOARD_SURFACE_MARGIN);
    }
}
