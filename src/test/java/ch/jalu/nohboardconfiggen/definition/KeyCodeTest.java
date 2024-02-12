package ch.jalu.nohboardconfiggen.definition;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KeyCodeTest {

    @Test
    void shouldHaveUniqueNamesCaseInsensitive() {
        // given
        Set<String> names = new HashSet<>();

        // when / then
        for (KeyCode keyCode : KeyCode.values()) {
            boolean changed = names.add(keyCode.name().toLowerCase(Locale.ROOT));
            if (keyCode.getAltName() != null) {
                changed |= names.add(keyCode.getAltName().toLowerCase(Locale.ROOT));
            }

            if (!changed) {
                fail("Key " + keyCode + " has either name or alt name that was already used");
            }
        }
    }

    @Test
    void shouldHaveUniqueKeyCodes() {
        // given
        Set<Integer> codes = new HashSet<>();

        // when / then
        for (KeyCode keyCode : KeyCode.values()) {
            if (!codes.add(keyCode.getCode())) {
                fail("Key " + keyCode + " has a code that was already used");
            }
        }
    }
}