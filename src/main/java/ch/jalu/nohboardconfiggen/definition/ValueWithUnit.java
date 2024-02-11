package ch.jalu.nohboardconfiggen.definition;

import ch.jalu.nohboardconfiggen.NumberUtils;

import java.math.BigDecimal;

public record ValueWithUnit(BigDecimal value, Unit unit) {

    public int resolveToPixels(int baseValue) {
        return switch (unit) {
            case KEY -> NumberUtils.multiply(baseValue, value);
            case PIXEL -> NumberUtils.roundToInt(value);
        };
    }
}
