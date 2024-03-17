package ch.jalu.nohboardconfiggen.definition.generator.attribute;

import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;
import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;

public interface AttributeType<T> {

    AttributeType<ValueWithUnit> INTEGER_WITH_UNIT = new ValueWithUnitAttributeType(false);
    AttributeType<ValueWithUnit> NUMBER_WITH_UNIT = new ValueWithUnitAttributeType(true);

    AttributeType<Boolean> BOOLEAN = SimpleAttributeTypes.createBooleanAttributeType();
    AttributeType<Integer> INTEGER = SimpleAttributeTypes.createIntegerAttributeType();

    default T parse(Attribute attribute) {
        return parse(attribute.name(), attribute.value());
    }

    T parse(String name, String value);

}
