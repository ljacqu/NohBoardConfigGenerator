package ch.jalu.nohboardconfiggen.definition.generator.attribute;

import ch.jalu.nohboardconfiggen.definition.Unit;
import ch.jalu.nohboardconfiggen.definition.ValueWithUnit;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
final class ValueWithUnitAttributeType implements AttributeType<ValueWithUnit> {

    private final boolean acceptDecimals;

    @Override
    public ValueWithUnit parse(String name, String value) {
        String numberPart = extractNumberPart(value);
        BigDecimal number = parseNumber(numberPart);
        if (number == null) {
            throw new IllegalArgumentException("Invalid value '" + value + "' for attribute '" + name + "'");
        }

        Unit unit = null;
        if (numberPart.length() < value.length()) {
            unit = Unit.fromSymbol(value.substring(numberPart.length()));
        }
        return new ValueWithUnit(number, unit);
    }

    private static String extractNumberPart(String value) {
        int index = 0;
        for (char chr : value.toCharArray()) {
            if (!Character.isDigit(chr) && chr != '.' && chr != '-') {
                break;
            }
            ++index;
        }
        return value.substring(0, index);
    }

    private BigDecimal parseNumber(String value) {
        if (value.isEmpty()) {
            return null;
        }
        try {
            if (acceptDecimals) {
                return new BigDecimal(value);
            } else {
                int v = Integer.parseInt(value);
                return BigDecimal.valueOf(v);
            }
        } catch (NumberFormatException ignore) {
        }
        return null;
    }
}
