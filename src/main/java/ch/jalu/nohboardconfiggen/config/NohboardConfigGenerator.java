package ch.jalu.nohboardconfiggen.config;

import ch.jalu.nohboardconfiggen.ConfigHelper;
import ch.jalu.nohboardconfiggen.definition.KeyCode;
import ch.jalu.nohboardconfiggen.definition.KeyDefinition;
import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.KeyboardRow;
import ch.jalu.nohboardconfiggen.definition.Unit;
import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;

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
        int xCurrentCell;
        int yCurrentRowTop = KEYBOARD_SURFACE_MARGIN;
        for (KeyboardRow row : config.getRows()) {
            int yMaxInCurrentRow = 0;
            xCurrentCell = KEYBOARD_SURFACE_MARGIN;
            for (KeyDefinition keyDefinition : row.getKeys()) {
                NohbCoords topLeftPosition =
                    calculateTopLeftPosition(xCurrentCell, yCurrentRowTop, keyDefinition, config);

                NohbElement element = new NohbElement();
                element.setTexts(keyDefinition.getText());
                element.setBoundaries(calculateBounds(topLeftPosition, config, keyDefinition));
                element.setTextPosition(ConfigHelper.calculateCenterTextPosition(element.getBoundaries()));
                List<NohbElement> elementsForKey = generateElementsForAllKeys(element, keyDefinition.getKeys());
                elements.addAll(elementsForKey);

                NohbCoords maxBoundary = element.getBoundaries().get(MAX_BOUNDARY_INDEX);
                yMaxInCurrentRow = Math.max(yMaxInCurrentRow, maxBoundary.getY());
                xCurrentCell = maxBoundary.getX() + config.getSpace();
            }
            yCurrentRowTop = yMaxInCurrentRow + config.getSpace(); // todo: what if a key should go down two rows? :/
        }

        int id = 1;
        for (NohbElement element : elements) {
            element.setId(id);
            ++id;
        }
        return elements;
    }

    private NohbCoords calculateTopLeftPosition(int xCurrentCell, int yCurrentRowTop,
                                                KeyDefinition keyDefinition, KeyboardConfig config) {
        ValueWithUnit marginLeft = keyDefinition.getMarginLeft();
        if (marginLeft != null) {
            xCurrentCell += marginLeft.resolveToPixels(config.getWidth());
            if (marginLeft.unit() == Unit.KEY) {
                xCurrentCell += marginLeft.value().intValue() * config.getSpace();
            }
        }

        int yTopLeftCurrentCell = yCurrentRowTop;
        ValueWithUnit marginTop = keyDefinition.getMarginTop();
        if (marginTop != null) {
            yTopLeftCurrentCell += marginTop.resolveToPixels(config.getHeight());
            if (marginTop.unit() == Unit.KEY) {
                yTopLeftCurrentCell += marginTop.value().intValue() * config.getSpace();
            }
        }

        return new NohbCoords(xCurrentCell, yTopLeftCurrentCell);
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

    private List<NohbCoords> calculateBounds(NohbCoords topLeftPosition, KeyboardConfig config, KeyDefinition key) {
        int xIncrement = key.getCustomWidth() != null
            ? key.getCustomWidth().resolveToPixels(config.getWidth())
            : config.getWidth();
        int yIncrement = key.getCustomHeight() != null
            ? key.getCustomHeight().resolveToPixels(config.getHeight())
            : config.getHeight();

        int leftX = topLeftPosition.getX();
        int topY = topLeftPosition.getY();
        int rightX = leftX + xIncrement;
        int bottomY = topY + yIncrement;

        // The order of the bounds is relevant; NohBoard has them in the following order:
        // 0  1
        // 3  2
        return List.of(
            new NohbCoords(leftX, topY),
            new NohbCoords(rightX, topY),
            new NohbCoords(rightX, bottomY),
            new NohbCoords(leftX, bottomY));
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
