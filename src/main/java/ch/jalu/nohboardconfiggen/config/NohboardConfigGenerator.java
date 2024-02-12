package ch.jalu.nohboardconfiggen.config;

import ch.jalu.nohboardconfiggen.ConfigHelper;
import ch.jalu.nohboardconfiggen.definition.KeyCode;
import ch.jalu.nohboardconfiggen.definition.KeyDefinition;
import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.KeyboardRow;
import ch.jalu.nohboardconfiggen.definition.Unit;
import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;

import java.math.BigDecimal;
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
            xCurrentCell = KEYBOARD_SURFACE_MARGIN;
            if (row.getMarginLeft() != null) {
                xCurrentCell += row.getMarginLeft().resolveToPixels(config.getWidth());
            }

            int yMaxInCurrentRow = 0;
            if (row.getMarginTop() != null) {
                yCurrentRowTop += row.getMarginTop().resolveToPixels(config.getHeight());
            }

            NohbCoords topLeftPosition = null;
            NohbCoords bottomRightPosition = null;
            for (KeyDefinition keyDefinition : row.getKeys()) {
                if (keyDefinition.isStacked()) {
                    if (bottomRightPosition == null) {
                        throw new IllegalStateException("Stacked key may not be first in row");
                    }
                    topLeftPosition = calculateTopLeftPosition(topLeftPosition.getX(),
                        bottomRightPosition.getY() + config.getSpacing(), keyDefinition, config);
                } else {
                    topLeftPosition = calculateTopLeftPosition(xCurrentCell, yCurrentRowTop, keyDefinition, config);
                }

                NohbElement element = new NohbElement();
                element.setTexts(keyDefinition.getText());
                element.setBoundaries(calculateBounds(topLeftPosition, config, keyDefinition));
                element.setTextPosition(ConfigHelper.calculateCenterTextPosition(element.getBoundaries()));
                List<NohbElement> elementsForKey = generateElementsForAllKeys(element, keyDefinition.getKeys());
                elements.addAll(elementsForKey);

                bottomRightPosition = element.getBoundaries().get(MAX_BOUNDARY_INDEX);
                yMaxInCurrentRow = Math.max(yMaxInCurrentRow, bottomRightPosition.getY());
                xCurrentCell = bottomRightPosition.getX() + config.getSpacing();
            }
            yCurrentRowTop = yMaxInCurrentRow + config.getSpacing(); // todo: what if a key should go down two rows? :/
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
                xCurrentCell += marginLeft.value().intValue() * config.getSpacing();
            }
        }

        int yTopLeftCurrentCell = yCurrentRowTop;
        ValueWithUnit marginTop = keyDefinition.getMarginTop();
        if (marginTop != null) {
            yTopLeftCurrentCell += marginTop.resolveToPixels(config.getHeight());
            if (marginTop.unit() == Unit.KEY) {
                yTopLeftCurrentCell += marginTop.value().intValue() * config.getSpacing();
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
        int leftX = topLeftPosition.getX();
        int topY = topLeftPosition.getY();
        int rightX = leftX + calculateKeySize(key.getCustomWidth(), config.getWidth(), config.getSpacing());
        int bottomY = topY + calculateKeySize(key.getCustomHeight(), config.getHeight(), config.getSpacing());

        // The order of the bounds is relevant; NohBoard has them in the following order:
        // 0  1
        // 3  2
        return List.of(
            new NohbCoords(leftX, topY),
            new NohbCoords(rightX, topY),
            new NohbCoords(rightX, bottomY),
            new NohbCoords(leftX, bottomY));
    }

    private int calculateKeySize(ValueWithUnit customSize, int baseSize, int spacing) {
        if (customSize == null) {
            return baseSize;
        }

        int size = customSize.resolveToPixels(baseSize);
        // e.g. if width is set to 2, we need to add one spacing to it, so it spans the whole two keys
        if (customSize.unit() == Unit.KEY && customSize.value().compareTo(BigDecimal.ONE) > 0) {
            size += customSize.value().subtract(BigDecimal.ONE).intValue() * spacing;
        }
        return size;
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
