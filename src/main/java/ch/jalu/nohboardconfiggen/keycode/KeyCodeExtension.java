package ch.jalu.nohboardconfiggen.keycode;

final class KeyCodeExtension {

    private static final int VK_OEM_1 = 0xBA; // 186
    private static final int VK_OEM_2 = 0xBF; // 191
    private static final int VK_OEM_3 = 0xC0; // 192
    private static final int VK_OEM_4 = 0xDB; // 219
    private static final int VK_OEM_5 = 0xDC; // 220
    private static final int VK_OEM_6 = 0xDD; // 221
    private static final int VK_OEM_7 = 0xDE; // 222
    private static final int VK_OEM_8 = 0xDF; // 223
    private static final int VK_OEM_102 = 0xE2; // 226

    private KeyCodeExtension() {
    }

    static void addExtensions(KeyboardLayout layout, KeyboardRegion region) {
        switch (region) {
            case DUTCH -> {
                layout
                    .add(VK_OEM_1, "*")
                    .add(VK_OEM_2, "°")
                    .add(VK_OEM_3, "´")
                    .add(VK_OEM_4, "/")
                    .add(VK_OEM_5, "<")
                    .add(VK_OEM_6, "¨")
                    .add(VK_OEM_7, "@")
                    // No VK_OEM_8
                    .add(VK_OEM_102, "]");
            }
            case FRENCH -> {
                layout
                    .add(VK_OEM_1, ";")
                    .add(VK_OEM_2, ":")
                    .add(VK_OEM_3, "@")
                    .add(VK_OEM_4, "/")
                    .add(VK_OEM_5, "*")
                    .add(VK_OEM_6, "^")
                    .add(VK_OEM_7, "'")
                    // No VK_OEM_8
                    .add(VK_OEM_102, "<");
            }
            case SWISS_GERMAN -> {
                layout
                    .add(VK_OEM_1, "ü")
                    .add(VK_OEM_2, "§")
                    .add(VK_OEM_3, "¨")
                    .add(VK_OEM_4, "'")
                    .add(VK_OEM_5, "ä")
                    .add(VK_OEM_6, "^")
                    .add(VK_OEM_7, "ö")
                    .add(VK_OEM_8, "$")
                    .add(VK_OEM_102, "<");
            }
            case GERMAN -> {
                layout
                    .add(VK_OEM_1, "ü")
                    .add(VK_OEM_2, "#")
                    .add(VK_OEM_3, "ö")
                    .add(VK_OEM_4, "ß")
                    .add(VK_OEM_5, "^")
                    .add(VK_OEM_6, "´")
                    .add(VK_OEM_7, "ä")
                    // No VK_OEM_8
                    .add(VK_OEM_102, "<");
            }
            case UK -> {
                layout
                    .add(VK_OEM_1, ";")
                    .add(VK_OEM_2, "/")
                    .add(VK_OEM_3, "'")
                    .add(VK_OEM_4, "[")
                    .add(VK_OEM_5, "\\")
                    .add(VK_OEM_6, "]")
                    .add(VK_OEM_7, "#")
                    .add(VK_OEM_8, "`")
                    .add(VK_OEM_102, "<");
            }
            case US -> {
                layout
                    .add(VK_OEM_1, ";")
                    .add(VK_OEM_2, "/")
                    .add(VK_OEM_3, "`")
                    .add(VK_OEM_4, "[")
                    .add(VK_OEM_5, "\\", "Right\\")
                    .add(VK_OEM_6, "]")
                    .add(VK_OEM_7, "'")
                    // No VK_OEM_8
                    .add(VK_OEM_102, "Left\\"); // most US keyboards physically don't have this key
            }
            default -> throw new IllegalStateException("Unsupported layout: " + layout);
        }
    }
}
