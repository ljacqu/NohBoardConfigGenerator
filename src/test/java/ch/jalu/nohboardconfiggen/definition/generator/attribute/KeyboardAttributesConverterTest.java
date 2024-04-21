package ch.jalu.nohboardconfiggen.definition.generator.attribute;

import ch.jalu.nohboardconfiggen.definition.KeyboardConfig;
import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link KeyboardAttributesConverter}.
 */
class KeyboardAttributesConverterTest {

    @Test
    void shouldConvertAttributes() {
        // given
        KeyboardConfig keyboardConfig = new KeyboardConfig();

        // when
        KeyboardAttributesConverter.processAttribute(keyboardConfig, new Attribute("height", "30px"));
        KeyboardAttributesConverter.processAttribute(keyboardConfig, new Attribute("width", "32"));
        KeyboardAttributesConverter.processAttribute(keyboardConfig, new Attribute("spacing", "4"));

        // then
        assertThat(keyboardConfig.getHeight(), equalTo(30));
        assertThat(keyboardConfig.getWidth(), equalTo(32));
        assertThat(keyboardConfig.getSpacing(), equalTo(4));
    }

    @Test
    void shouldThrowForUnknownAttribute() {
        // given / when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> KeyboardAttributesConverter.processAttribute(new KeyboardConfig(), new Attribute("opacity", "0.5")));

        // then
        assertThat(ex.getMessage(), equalTo("Unknown keyboard attribute: opacity"));
    }

    @Test
    void shouldIgnoreKeyboardAttribute() {
        // given
        KeyboardConfig keyboardConfig = new KeyboardConfig();

        // when
        KeyboardAttributesConverter.processAttribute(keyboardConfig, new Attribute("keyboard", "nl"));

        // then - no exception
    }

    @Test
    void shouldThrowForUnitIfNotEqualToPixels() {
        // given / when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> KeyboardAttributesConverter.processAttribute(new KeyboardConfig(), new Attribute("width", "1k")));

        // then
        assertThat(ex.getMessage(), equalTo("Invalid value for keyboard attribute 'width'. Expected units in pixel, but got: k"));
    }
}