package ch.jalu.nohboardconfiggen.definition.parser.element;

/**
 * Represents an attribute (= property to configure some keyboard element). The value is kept as String
 * and processed later, e.g. to convert it to the right type.
 *
 * @param name attribute name
 * @param value the attribute's value
 */
public record Attribute(String name, String value) {

}
