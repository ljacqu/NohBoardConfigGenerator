package ch.jalu.nohboardconfiggen.definition.generator.attribute;

import ch.jalu.nohboardconfiggen.definition.Unit;
import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link ValueWithUnitAttributeType}.
 */
class ValueWithUnitAttributeTypeTest {

    @Test
    void shouldParseWithDecimals() {
        // given
        ValueWithUnitAttributeType decimalAttributeType = new ValueWithUnitAttributeType(true);

        // when
        ValueWithUnit result30d2px = decimalAttributeType.parse("attr", "30.2px");
        ValueWithUnit result30px = decimalAttributeType.parse("attr", "30px");
        ValueWithUnit resultM45 = decimalAttributeType.parse("attr", "-45");
        ValueWithUnit resultM0d5k = decimalAttributeType.parse("attr", "-0.5k");

        // then
        assertThat(result30d2px, equalTo(valueWithUnit("30.2", Unit.PIXEL)));
        assertThat(result30px, equalTo(valueWithUnit("30", Unit.PIXEL)));
        assertThat(resultM45, equalTo(valueWithUnit("-45", null)));
        assertThat(resultM0d5k, equalTo(valueWithUnit("-0.5", Unit.KEY)));
    }

    @Test
    void shouldParseWithoutDecimals() {
        // given
        ValueWithUnitAttributeType intAttributeType = new ValueWithUnitAttributeType(false);

        // when
        ValueWithUnit result30px = intAttributeType.parse("attr", "30px");
        ValueWithUnit result2k = intAttributeType.parse("attr", "2k");
        ValueWithUnit resultM45 = intAttributeType.parse("attr", "-45");

        // then
        assertThat(result30px, equalTo(valueWithUnit("30", Unit.PIXEL)));
        assertThat(result2k, equalTo(valueWithUnit("2", Unit.KEY)));
        assertThat(resultM45, equalTo(valueWithUnit("-45", null)));
    }

    @Test
    void shouldThrowForMalformedNumber() {
        // given
        ValueWithUnitAttributeType intAttributeType = new ValueWithUnitAttributeType(false);

        // when
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> intAttributeType.parse("attr", "3..5"));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> intAttributeType.parse("attr", "3.px"));
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
            () -> intAttributeType.parse("attr", "0-2k"));
        IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class,
            () -> intAttributeType.parse("attr", "px"));
        IllegalArgumentException ex5 = assertThrows(IllegalArgumentException.class,
            () -> intAttributeType.parse("attr", ""));

        // then
        assertThat(ex1.getMessage(), equalTo("Invalid value '3..5' for attribute 'attr'"));
        assertThat(ex2.getMessage(), equalTo("Invalid value '3.px' for attribute 'attr'"));
        assertThat(ex3.getMessage(), equalTo("Invalid value '0-2k' for attribute 'attr'"));
        assertThat(ex4.getMessage(), equalTo("Invalid value 'px' for attribute 'attr'"));
        assertThat(ex5.getMessage(), equalTo("Invalid value '' for attribute 'attr'"));
    }

    @Test
    void shouldThrowForInvalidUnit() {
        // given
        ValueWithUnitAttributeType decimalAttributeType = new ValueWithUnitAttributeType(true);

        // when
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> decimalAttributeType.parse("attr", "3boop"));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> decimalAttributeType.parse("attr", "-2k2"));
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
            () -> decimalAttributeType.parse("attr", "1 px"));

        // then
        assertThat(ex1.getMessage(), equalTo("Unknown unit 'boop'. Supported units: keys (k), pixels (px)"));
        assertThat(ex2.getMessage(), equalTo("Unknown unit 'k2'. Supported units: keys (k), pixels (px)"));
        assertThat(ex3.getMessage(), equalTo("Unknown unit ' px'. Supported units: keys (k), pixels (px)"));
    }

    @Test
    void shouldThrowForDecimalsWhenIntegerIsExpected() {
        // given
        ValueWithUnitAttributeType intAttributeType = new ValueWithUnitAttributeType(false);

        // when
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> intAttributeType.parse("attr", "3.5"));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> intAttributeType.parse("attr", "2.0px"));

        // then
        assertThat(ex1.getMessage(), equalTo("Invalid value '3.5' for attribute 'attr'"));
        assertThat(ex2.getMessage(), equalTo("Invalid value '2.0px' for attribute 'attr'"));
    }

    private static ValueWithUnit valueWithUnit(String value, Unit unit) {
        return new ValueWithUnit(new BigDecimal(value), unit);
    }
}