package ch.jalu.nohboardconfiggen.definition.parser.element;

import java.util.Set;

public record KeyNameSet(Set<String> keys) {

    public KeyNameSet(String... key) {
        this(Set.of(key));
    }
}
