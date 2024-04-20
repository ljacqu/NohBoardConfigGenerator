package ch.jalu.nohboardconfiggen.definition.parser.element;

import java.util.List;

/**
 * Variable definition.
 */
public interface Variable {

    /**
     * @return variable name (without initial "$")
     */
    String name();

    /**
     * Definition of a variable with a text value.
     *
     * @param name variable name (without initial "$")
     * @param value the value
     */
    record ValueVariable(String name, String value) implements Variable {

    }

    /**
     * Definition of a variable with attributes as value.
     *
     * @param name variable name (without initial "$")
     * @param attributes the attributes which this variable has as value
     */
    record AttributeVariable(String name, List<Attribute> attributes) implements Variable {

    }
}
