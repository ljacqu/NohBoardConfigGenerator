package ch.jalu.nohboardconfiggen.definition.generator.attribute;

import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.Unit;
import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;
import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;

/**
 * Converts and sets top-level attributes.
 */
public final class KeyboardAttributesConverter {

    private KeyboardAttributesConverter() {
    }

    public static void processAttribute(KeyboardConfig keyboardConfig, Attribute attribute) {
        switch (attribute.name()) {
            case "spacing":
                keyboardConfig.setSpacing(parsePixelProperty(attribute));
                break;
            case "width":
                keyboardConfig.setWidth(parsePixelProperty(attribute));
                break;
            case "height":
                keyboardConfig.setHeight(parsePixelProperty(attribute));
                break;
            case "keyboard":
                // Ignore
                break;
            default:
                throw new IllegalArgumentException("Unknown keyboard attribute: " + attribute.name());
        }
    }

    private static int parsePixelProperty(Attribute attribute) {
        ValueWithUnit valueWithUnit = AttributeType.INTEGER_WITH_UNIT.parse(attribute.name(), attribute.value());
        if (valueWithUnit.unit() == null || valueWithUnit.unit() == Unit.PIXEL) {
            return valueWithUnit.value().intValue();
        }
        throw new IllegalArgumentException("Invalid value for keyboard attribute '"
            + attribute.name() + "'. Expected units in pixel, but got: " + valueWithUnit.unit().getSymbol());
    }
}
