package ch.jalu.nohboardconfiggen.keycode;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Test for {@link KeyboardLayout}.
 */
class KeyboardLayoutTest {

    private static final int TOTAL_BASIC_KEYCODES = 157;

    @Test
    void shouldCreateLayoutWithoutRegion() {
        // given / when
        KeyboardLayout layout = KeyboardLayout.create(null);

        // then
        assertThat(getKeyCodeMap(layout), aMapWithSize(TOTAL_BASIC_KEYCODES));

        assertThat(layout.getKeyCodeOrThrow("A"), equalTo(65));
        assertThat(layout.getKeyCodeOrThrow("+"), equalTo(187));
        assertThat(layout.getKeyCodeOrThrow("numpad3"), equalTo(99));
        assertThat(layout.getKeyCodeOrThrow("PageUp"), equalTo(33));
    }

    @Test
    void shouldCreateLayoutForAllRegions() {
        // given / when
        for (KeyboardRegion region : KeyboardRegion.values()) {
            KeyboardLayout layout = KeyboardLayout.create(region);

            // then
            assertThat(getKeyCodeMap(layout).size(), greaterThan(TOTAL_BASIC_KEYCODES));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Integer> getKeyCodeMap(KeyboardLayout layout) {
        try {
            Field mapField = KeyboardLayout.class.getDeclaredField("keyNameToCode");
            mapField.setAccessible(true);
            return (Map) mapField.get(layout);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get key code map", e);
        }
    }
}