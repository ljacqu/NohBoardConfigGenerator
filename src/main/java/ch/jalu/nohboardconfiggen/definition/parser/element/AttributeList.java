package ch.jalu.nohboardconfiggen.definition.parser.element;

import java.util.List;

public record AttributeList(List<Attribute> attributes) implements KeyboardLineParseResult {
}
