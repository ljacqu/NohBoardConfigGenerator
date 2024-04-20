package ch.jalu.nohboardconfiggen.definition.generator.attribute;

import com.google.common.primitives.Ints;

/**
 * Provides simple attribute type implementations.
 */
final class SimpleAttributeTypes {

    private SimpleAttributeTypes() {
    }

    static AttributeType<Boolean> createBooleanAttributeType() {
        return new AttributeType<>() {

            @Override
            public Boolean parse(String name, String value) {
                return switch (value) {
                    case "true" -> true;
                    case "false" -> false;
                    default -> throw new IllegalArgumentException(
                        "Invalid value '" + value + "' for attribute '" + name + "': expected a boolean");
                };
            }
        };
    }

    static AttributeType<Integer> createIntegerAttributeType() {
        return new AttributeType<>() {

            @Override
            public Integer parse(String name, String value) {
                Integer number = Ints.tryParse(value);
                if (number == null) {
                    throw new IllegalArgumentException("Invalid value '" + value + "' for attribute '"
                        + name + "': expected an integer");
                }
                return number;
            }
        };
    }
}
