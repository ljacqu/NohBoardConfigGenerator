package ch.jalu.nohboardconfiggen.keycode;

import lombok.RequiredArgsConstructor;

import java.util.Locale;

@RequiredArgsConstructor
public enum KeyboardRegion {

    DUTCH("nl"),

    FRENCH("fr"),

    SWISS_GERMAN("de-ch"),

    GERMAN("de"),

    UK("en-gb"),

    US("en-us");

    private final String code;

    KeyboardRegion findByCode(String code) {
        String codeLower = code.toLowerCase(Locale.ROOT);

        for (KeyboardRegion region : KeyboardRegion.values()) {
            if (region.code.equals(codeLower)) {
                return region;
            }
        }
        return null;
    }
}
