package ch.jalu.nohboardconfiggen.definition.generator.attribute;

import ch.jalu.nohboardconfiggen.definition.KeyDefinition;
import ch.jalu.nohboardconfiggen.definition.Unit;
import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;
import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link KeyAttributesConverter}.
 */
class KeyAttributesConverterTest {

    @Test
    void shouldConvertAttributes() {
        // given
        KeyDefinition keyModel = new KeyDefinition();

        // when
        KeyAttributesConverter.processAttribute(keyModel, new Attribute("height", "30px"));
        KeyAttributesConverter.processAttribute(keyModel, new Attribute("width", "0.75"));
        KeyAttributesConverter.processAttribute(keyModel, new Attribute("marginLeft", "20px"));
        KeyAttributesConverter.processAttribute(keyModel, new Attribute("marginTop", "0.5"));
        KeyAttributesConverter.processAttribute(keyModel, new Attribute("id", "250"));
        KeyAttributesConverter.processAttribute(keyModel, new Attribute("stacked", "true"));

        // then
        assertThat(keyModel.getCustomHeight(), equalTo(new ValueWithUnit(BigDecimal.valueOf(30), Unit.PIXEL)));
        assertThat(keyModel.getCustomWidth(), equalTo(new ValueWithUnit(new BigDecimal("0.75"), Unit.KEY)));
        assertThat(keyModel.getMarginLeft(), equalTo(new ValueWithUnit(BigDecimal.valueOf(20), Unit.PIXEL)));
        assertThat(keyModel.getMarginTop(), equalTo(new ValueWithUnit(new BigDecimal("0.5"), Unit.KEY)));
        assertThat(keyModel.getId(), equalTo(250));
        assertThat(keyModel.isStacked(), equalTo(true));
    }

    @Test
    void shouldThrowForUnknownAttribute() {
        // given / when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> KeyAttributesConverter.processAttribute(new KeyDefinition(), new Attribute("opacity", "0.5")));

        // then
        assertThat(ex.getMessage(), equalTo("Unknown key attribute: opacity"));
    }
}