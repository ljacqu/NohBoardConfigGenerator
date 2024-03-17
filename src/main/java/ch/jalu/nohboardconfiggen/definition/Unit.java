package ch.jalu.nohboardconfiggen.definition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Unit in which a distance (e.g. for margins) can be defined.
 */
@Getter
@RequiredArgsConstructor
public enum Unit {

    KEY("k"),

    PIXEL("px");

    private final String symbol;

    public static Unit fromSymbol(String symbol) {
        for (Unit unit : values()) {
            if (unit.symbol.equalsIgnoreCase(symbol)) {
                return unit;
            }
        }

        throw new IllegalStateException("Unknown unit '" + symbol + "'. Supported units: keys (k), pixels (px)");
    }
}
