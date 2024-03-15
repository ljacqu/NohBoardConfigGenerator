package ch.jalu.nohboardconfiggen.definition.parser.element;

import java.util.List;

public record KeyLine(String displayText, List<KeyNameSet> keys, List<Attribute> attributes) {

}
