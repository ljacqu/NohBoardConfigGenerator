package ch.jalu.nohboardconfiggen;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link NumberUtils}.
 */
class NumberUtilsTest {

    @Test
    @SuppressWarnings("ConstantValue") // it's a test, IntelliJ
    void shouldDetermineIfValueIsNotNullAndNotZero() {
        // given / when / then
        assertTrue(NumberUtils.notNullAndNotZero(BigDecimal.ONE));
        assertTrue(NumberUtils.notNullAndNotZero(new BigDecimal("-0.2")));
        assertTrue(NumberUtils.notNullAndNotZero(new BigDecimal("0.01")));

        assertFalse(NumberUtils.notNullAndNotZero(BigDecimal.ZERO));
        assertFalse(NumberUtils.notNullAndNotZero(null));
        assertFalse(NumberUtils.notNullAndNotZero(new BigDecimal("0.000")));
    }

    @Test
    void shouldMultiply() {
        // given / when / then
        assertThat(NumberUtils.multiply(3, BigDecimal.TEN), equalTo(30));
        assertThat(NumberUtils.multiply(40, new BigDecimal("-0.2")), equalTo(-8));
        assertThat(NumberUtils.multiply(3, new BigDecimal("0.5")), equalTo(2));
    }

    @Test
    void shouldRoundToInt() {
        // given / when / then
        assertThat(NumberUtils.roundToInt(new BigDecimal("0.4")), equalTo(0));
        assertThat(NumberUtils.roundToInt(new BigDecimal("0.5")), equalTo(1));
        assertThat(NumberUtils.roundToInt(new BigDecimal("-2.8")), equalTo(-3));
        assertThat(NumberUtils.roundToInt(new BigDecimal("-1.4")), equalTo(-1));
    }

    @Test
    void shouldParseToBigDecimal() {
        // given / when / then
        assertThat(NumberUtils.parseBigDecimalOrThrow("320", () -> "should not be called"), comparesEqualTo(new BigDecimal("320")));
        assertThat(NumberUtils.parseBigDecimalOrThrow("-2.4", () -> "should not be called"), comparesEqualTo(new BigDecimal("-2.4")));
    }

    @Test
    void shouldThrowIfStringCannotBeParsed() {
        // given / when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> NumberUtils.parseBigDecimalOrThrow("T3", () -> "An invalid value was given"));

        // then
        assertThat(ex.getMessage(), equalTo("An invalid value was given"));
    }
}