package ch.jalu.nohboardconfiggen.definition.generator.attribute;

import ch.jalu.nohboardconfiggen.definition.KeyDefinition;
import ch.jalu.nohboardconfiggen.definition.Unit;
import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;
import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;

public final class KeyAttributes {

    private KeyAttributes() {
    }

    public static void processAttribute(KeyDefinition keyModel, Attribute attribute) {
        switch (attribute.name()) {
            case "height":
                keyModel.setCustomHeight(parseNumberWithOptionalUnit(attribute));
                break;
            case "width":
                keyModel.setCustomWidth(parseNumberWithOptionalUnit(attribute));
                break;
            case "marginTop":
                keyModel.setMarginTop(parseNumberWithOptionalUnit(attribute));
                break;
            case "marginLeft":
                keyModel.setMarginLeft(parseNumberWithOptionalUnit(attribute));
                break;
            case "id":
                keyModel.setId(AttributeType.INTEGER.parse(attribute));
                break;
            case "stacked":
                keyModel.setStacked(AttributeType.BOOLEAN.parse(attribute));
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
