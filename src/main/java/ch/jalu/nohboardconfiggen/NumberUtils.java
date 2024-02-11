package ch.jalu.nohboardconfiggen;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilities for numbers.
 */
public final class NumberUtils {

    private NumberUtils() {
    }

    /**
     * Returns whether the given value is not null and not zero.
     *
     * @param value the value to inspect (nullable)
     * @return true if value is not null and different from zero
     */
    public static boolean notNullAndNotZero(BigDecimal value) {
        return value != null && BigDecimal.ZERO.compareTo(value) != 0;
    }

    /**
     * Multiplies the two numbers and returns the result as an int, rounding decimals to the nearest integer.
     *
     * @param a value to multiply
     * @param b value to multiply
     * @return a*b as a rounded integer
     */
    public static int multiply(int a, BigDecimal b) {
        BigDecimal resultPrecise = BigDecimal.valueOf(a).multiply(b);
        return resultPrecise.setScale(0, RoundingMode.HALF_UP).intValueExact();
    }
}
