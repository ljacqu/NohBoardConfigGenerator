package ch.jalu.nohboardconfiggen.definition;

/**
 * Unit in which a distance (e.g. for margins) can be defined.
 */
public enum Unit {

    KEY("k"),

    PIXEL("px");

    private final String symbol;

    Unit(String symbol) {
        this.symbol = symbol;
    }

    public static Unit fromSymbolOrDefaultIfNull(String symbol) {
        if (symbol == null) {
            return Unit.KEY;
        }
        for (Unit unit : values()) {
            if (unit.symbol.equalsIgnoreCase(symbol)) {
                return unit;
            }
        }

        throw new IllegalStateException("Unknown unit '" + symbol + "'. Supported units: keys (k), pixels (px)");
    }

}
