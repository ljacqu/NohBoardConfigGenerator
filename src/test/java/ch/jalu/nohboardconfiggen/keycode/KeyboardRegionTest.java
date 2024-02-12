package ch.jalu.nohboardconfiggen.keycode;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link KeyboardRegion}.
 */
class KeyboardRegionTest {

    @Test
    void shouldReturnRegion() {
        // given / when / then
        assertThat(KeyboardRegion.findByCodeOrThrow("fr"), equalTo(KeyboardRegion.FRENCH));
        assertThat(KeyboardRegion.findByCodeOrThrow("NL"), equalTo(KeyboardRegion.DUTCH));
        assertThat(KeyboardRegion.findByCodeOrThrow("de-CH"), equalTo(KeyboardRegion.SWISS_GERMAN));
    }

    @Test
    void shouldThrowWithListOfAvailableCodesForUnknownCode() {
        // given / when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> KeyboardRegion.findByCodeOrThrow("cy"));

        // then
        assertThat(ex.getMessage(), equalTo("Keyboard region 'cy' is not supported. Available regions: de-ch, de, en-gb, en-us, fr, nl"));
    }
}