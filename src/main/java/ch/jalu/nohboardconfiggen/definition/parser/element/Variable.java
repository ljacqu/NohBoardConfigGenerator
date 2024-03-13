package ch.jalu.nohboardconfiggen.definition.parser.element;

import java.util.List;

public interface Variable {

    String name();

    record ValueVariable(String name, String value) implements Variable {

    }

    record AttributeVariable(String name, List<Attribute> attributes) implements Variable {

    }
}
