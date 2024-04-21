package ch.jalu.nohboardconfiggen.definition.generator.attribute;

import ch.jalu.nohboardconfiggen.definition.KeyboardRow;
import ch.jalu.nohboardconfiggen.definition.Unit;
import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;
import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;

/**
 * Converts and sets attributes to key row definitions.
 */
public final class RowAttributesConverter {

    private RowAttributesConverter() {
    }

    public static void processAttribute(KeyboardRow rowModel, Attribute attribute) {
        switch (attribute.name()) {
            case "marginTop":
                rowModel.setMarginTop(parseNumberWithOptionalUnit(attribute));
                break;
            case "marginLeft":
                rowModel.setMarginLeft(parseNumberWithOptionalUnit(attribute));
                break;
            default:
                throw new IllegalArgumentException("Unknown key attribute: " + attribute.name());
        }
    }

    private static ValueWithUnit parseNumberWithOptionalUnit(Attribute attribute) {
        ValueWithUnit valueWithUnit = AttributeType.NUMBER_WITH_UNIT.parse(attribute);
        if (valueWithUnit.unit() == null) {
            return new ValueWithUnit(valueWithUnit.value(), Unit.KEY);
        }
        return valueWithUnit;
    }
}
