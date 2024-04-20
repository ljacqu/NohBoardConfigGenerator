package ch.jalu.nohboardconfiggen.definition.parser.element;

import java.util.Set;

/**
 * Represents one key binding for a Nohboard key. If there are multiple entries in {@link #keys}, it means
 * that all keys must be pressed.
 * <p>
 * Not called {@code KeyBinding} to avoid using the same name as the converted element.
 *
 * @param keys the key names
 * @see ch.jalu.nohboardconfiggen.definition.KeyBinding
 */
public record KeyNameSet(Set<String> keys) {

    public KeyNameSet(String... key) {
        this(Set.of(key));
    }
}
