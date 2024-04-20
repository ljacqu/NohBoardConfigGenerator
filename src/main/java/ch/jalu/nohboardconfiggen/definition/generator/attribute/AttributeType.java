package ch.jalu.nohboardconfiggen.definition.generator.attribute;

import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;
import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;

/**
 * Attribute type to convert parsed String values to the appropriate type.
 *
 * @param <T> the type
 */
public interface AttributeType<T> {

    /** Integer with optional unit, e.g. "120px" or "120". */
    AttributeType<ValueWithUnit> INTEGER_WITH_UNIT = new ValueWithUnitAttributeType(false);
    /** Number (optionally with decimals) with an optional unit, e.g. "-2.5" or "30.5k". */
    AttributeType<ValueWithUnit> NUMBER_WITH_UNIT = new ValueWithUnitAttributeType(true);

    /** Boolean type (true or false). */
    AttributeType<Boolean> BOOLEAN = SimpleAttributeTypes.createBooleanAttributeType();
    /** Integer value (without units). */
    AttributeType<Integer> INTEGER = SimpleAttributeTypes.createIntegerAttributeType();

    default T parse(Attribute attribute) {
        return parse(attribute.name(), attribute.value());
    }

    T parse(String name, String value);

}
