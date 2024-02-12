package ch.jalu.nohboardconfiggen.keycode;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * https://learn.microsoft.com/en-us/windows/win32/inputdev/virtual-key-codes?redirectedfrom=MSDN
 * http://www.kbdlayout.info/features/virtualkeys/
 */
public class KeyboardLayout {

    private final Map<String, Integer> keyNameToCode = new HashMap<>();

    /**
     * Temporary way of getting a layout.
     *
     * @return keyboard layout
     */
    public static KeyboardLayout create(KeyboardRegion region) {
        KeyboardLayout layout = new KeyboardLayout();
        BasicKeyCodes.addKeys(layout);
        if (region != null) {
            KeyCodeExtension.addExtensions(layout, region);
        }
        return layout;
    }

    public int getKeyCodeOrThrow(String text) {
        Integer result = keyNameToCode.get(text.toLowerCase(Locale.ROOT));
        if (result == null) {
            throw new IllegalStateException("Unknown key '" + text + "'");
        }
        return result;
    }

    KeyboardLayout add(int code, String name) {
        if (keyNameToCode.put(name.toLowerCase(Locale.ROOT), code) != null) {
            throw new IllegalArgumentException("Name '" + name + "' was already registered");
        }
        return this;
    }

    KeyboardLayout add(int code, String... names) {
        for (String name : names) {
            if (keyNameToCode.put(name.toLowerCase(Locale.ROOT), code) != null) {
                throw new IllegalStateException("Name '" + name + "' was already registered");
            }
        }
        return this;
    }
}
