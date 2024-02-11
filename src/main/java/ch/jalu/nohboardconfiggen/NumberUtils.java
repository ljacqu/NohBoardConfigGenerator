package ch.jalu.nohboardconfiggen;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Supplier;

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
        return roundToInt(resultPrecise);
    }

    /**
     * Rounds the given BigDecimal to an integer and returns it as int.
     *
     * @param value the value to round
     * @return properly rounded integer
     */
    public static int roundToInt(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    /**
     * Converts the given String to a BigDecimal, throwing an error if not possible.
     *
     * @param number the string to convert
     * @param errorSupplier error message supplier in case the string is invalid
     * @return the string as BigDecimal
     */
    public static BigDecimal parseBigDecimalOrThrow(String number, Supplier<String> errorSupplier) {
        try {
            return new BigDecimal(number);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorSupplier.get());
        }
    }
}
