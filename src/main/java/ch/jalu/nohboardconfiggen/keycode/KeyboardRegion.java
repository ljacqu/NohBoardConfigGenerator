package ch.jalu.nohboardconfiggen.keycode;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Keyboard layouts.
 * <p>
 * This enum is called <i>region</i> because {@link KeyboardLayout} is a specific
 * implementation of names to key codes for a keyboard layout.
 */
@RequiredArgsConstructor
public enum KeyboardRegion {

    /**
     * Swiss German layout.
     * @see <a href="http://www.kbdlayout.info/kbdsg">KBDSG</a>
     */
    SWISS_GERMAN("de-ch"),

    /**
     * German (Germany) layout.
     * @see <a href="http://www.kbdlayout.info/kbdgr">KBDGR</a>
     */
    GERMAN("de"),

    /**
     * English (UK) layout.
     * @see <a href="http://www.kbdlayout.info/kbduk">KBDUK</a>
     */
    UK("en-gb"),

    /**
     * English (US) layout.
     * @see <a href="http://www.kbdlayout.info/kbdus">KBDUS</a>
     */
    US("en-us"),

    /**
     * French (France) layout.
     * @see <a href="http://www.kbdlayout.info/kbdfrna">KBDFRNA</a>
     */
    FRENCH("fr"),

    /**
     * Dutch (NL) layout.
     * @see <a href="http://www.kbdlayout.info/kbdne">KBDNE</a>
     */
    DUTCH("nl");


    private final String code;

    /**
     * Returns the region matching the given region code, throwing an exception otherwise.
     *
     * @param code the code to look up
     * @return enum entry matching the given code
     */
    public static KeyboardRegion findByCodeOrThrow(String code) {
        String codeLower = code.toLowerCase(Locale.ROOT);

        for (KeyboardRegion region : KeyboardRegion.values()) {
            if (region.code.equals(codeLower)) {
                return region;
            }
        }

        throw new IllegalArgumentException("Keyboard region '" + code + "' is not supported. Available regions: "
          + Arrays.stream(values()).map(region -> region.code).collect(Collectors.joining(", ")));
    }
}
