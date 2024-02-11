package ch.jalu.nohboardconfiggen;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}