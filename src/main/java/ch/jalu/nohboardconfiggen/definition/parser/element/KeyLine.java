package ch.jalu.nohboardconfiggen.definition.parser.element;

import java.util.List;

/**
 * Parsed definition of one NohBoard key. Not named {@code KeyDefinition} to avoid using the same name as
 * the converted element.
 *
 * @param displayText text of the key
 * @param keys key bindings
 * @param attributes attributes
 * @see ch.jalu.nohboardconfiggen.definition.KeyDefinition
 */
public record KeyLine(String displayText, List<KeyNameSet> keys, List<Attribute> attributes)
    implements KeyboardLineParseResult {

}
