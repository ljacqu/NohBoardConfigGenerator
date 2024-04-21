package ch.jalu.nohboardconfiggen.definition.generator.attribute;

import ch.jalu.nohboardconfiggen.definition.KeyboardRow;
import ch.jalu.nohboardconfiggen.definition.Unit;
import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;
import ch.jalu.nohboardconfiggen.definition.parser.element.Attribute;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link RowAttributesConverter}.
 */
class RowAttributesConverterTest {

    @Test
    void shouldConvertAttributes() {
        // given
        KeyboardRow rowModel = new KeyboardRow();

        // when
        RowAttributesConverter.processAttribute(rowModel, new Attribute("marginLeft", "20px"));
        RowAttributesConverter.processAttribute(rowModel, new Attribute("marginTop", "0.5"));

        // then
        assertThat(rowModel.getMarginLeft(), equalTo(new ValueWithUnit(BigDecimal.valueOf(20), Unit.PIXEL)));
        assertThat(rowModel.getMarginTop(), equalTo(new ValueWithUnit(new BigDecimal("0.5"), Unit.KEY)));
    }

    @Test
    void shouldThrowForUnknownAttribute() {
        // given / when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> RowAttributesConverter.processAttribute(new KeyboardRow(), new Attribute("opacity", "0.5")));

        // then
        assertThat(ex.getMessage(), equalTo("Unknown row attribute: opacity"));
    }
}