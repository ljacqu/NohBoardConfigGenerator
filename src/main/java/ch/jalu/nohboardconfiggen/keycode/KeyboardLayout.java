package ch.jalu.nohboardconfiggen.keycode;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Instance that represents a keyboard layout. Allows to translate key names to key codes
 * that are appropriate for the keyboard layout the instance represents.
 *
 * @see <a href="https://learn.microsoft.com/en-us/windows/win32/inputdev/virtual-key-codes?redirectedfrom=MSDN">Microsoft virtual key codes</a>
 * @see <a href="http://www.kbdlayout.info/features/virtualkeys/">kbdlayout.info</a>
 */
public class KeyboardLayout {

    private final Map<String, Integer> keyNameToCode = new HashMap<>();

    /**
     * Creates a keyboard layout instance appropriate for the given region. If the region is null,
     * no region-specific keys are mapped (e.g. A-Z, 0-9 will be present).
     *
     * @param region the region (keyboard layout) to use, or null for only general bindings
     * @return keyboard layout with names to codes appropriate for the given region
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
