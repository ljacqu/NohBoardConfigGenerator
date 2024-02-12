package ch.jalu.nohboardconfiggen.keycode;

/**
 * Adds all key codes that are the same for all Windows Latin keyboard layouts.
 */
final class BasicKeyCodes {

    private BasicKeyCodes() {
    }

    static void addKeys(KeyboardLayout layout) {
        addEnterAndOtherBasicControlKeys(layout);
        addHomeBlockAndArrowKeys(layout);
        add09Keys(layout);
        addAzKeys(layout);
        addWinAndContextKeys(layout);
        addNumPadKeys(layout);
        addFunctionKeys(layout);
        addControlKeys(layout);
    }

    private static void addEnterAndOtherBasicControlKeys(KeyboardLayout layout) {
        layout
            .add(8, "Backspace", "Back")
            .add(9, "Tab")
            // 10-11 reserved
            .add(12, "Clear") // Not sure what this is, Google also not very helpful
            .add(13, "Enter", "Return")
            // 14-15 unassigned
            // 16-18 supposedly Shift/Ctrl/Alt, but those seem to always be mapped by 160 and onwards
            .add(19, "Pause")
            .add(20, "CapsLock", "Caps")
            // 21-26 IME keys (Asian keyboards)
            .add(27, "Esc", "Escape")
            // 28-31 more IME keys
            .add(32, "Space");
    }

    private static void addHomeBlockAndArrowKeys(KeyboardLayout layout) {
        layout
            .add(33, "PageUp")
            .add(34, "PageDown")
            .add(35, "End")
            .add(36, "Home")
            .add(37, "Left", "ArrowLeft")
            .add(38, "Up", "ArrowUp")
            .add(39, "Right", "ArrowRight")
            .add(40, "Down", "ArrowDown")
            // 41 select
            // 42 print
            // 43 execute
            .add(44, "PrintScreen", "PrtScrn", "PrntScr")
            .add(45, "Insert", "Ins")
            .add(46, "Delete", "Del")
            .add(47, "Help"); // Not sure if relevant
    }

    private static void add09Keys(KeyboardLayout layout) {
        layout
            .add(48, "0")
            .add(49, "1")
            .add(50, "2")
            .add(51, "3")
            .add(52, "4")
            .add(53, "5")
            .add(54, "6")
            .add(55, "7")
            .add(56, "8")
            .add(57, "9");
            // 58-64 are undefined
    }

    private static void addAzKeys(KeyboardLayout layout) {
        layout
            .add(65, "A")
            .add(66, "B")
            .add(67, "C")
            .add(68, "D")
            .add(69, "E")
            .add(70, "F")
            .add(71, "G")
            .add(72, "H")
            .add(73, "I")
            .add(74, "J")
            .add(75, "K")
            .add(76, "L")
            .add(77, "M")
            .add(78, "N")
            .add(79, "O")
            .add(80, "P")
            .add(81, "Q")
            .add(82, "R")
            .add(83, "S")
            .add(84, "T")
            .add(85, "U")
            .add(86, "V")
            .add(87, "W")
            .add(88, "X")
            .add(89, "Y")
            .add(90, "Z");
    }

    private static void addWinAndContextKeys(KeyboardLayout layout) {
        layout
            .add(91, "LWin", "LeftWin", "LeftWindows")
            .add(92, "RWin", "RightWin", "RightWindows") // unconfirmed
            .add(93, "Context", "ContextMenu", "Apps");
            // 94 - reserved
            // 95 - sleep key
    }

    private static void addNumPadKeys(KeyboardLayout layout) {
        layout
            .add(96, "NumPad0", "num0")
            .add(97, "NumPad1", "num1")
            .add(98, "NumPad2", "num2")
            .add(99, "NumPad3", "num3")
            .add(100, "NumPad4", "num4")
            .add(101, "NumPad5", "num5")
            .add(102, "NumPad6", "num6")
            .add(103, "NumPad7", "num7")
            .add(104, "NumPad8", "num8")
            .add(105, "NumPad9", "num9")
            .add(106, "NumPad*", "num*")
            .add(107, "NumPad+", "num+")
            .add(108, "NumPad,", "num,") // unconfirmed
            .add(109, "NumPad-", "num-")
            .add(110, "NumPad.", "num.")
            .add(111, "NumPad/", "num/");
    }

    private static void addFunctionKeys(KeyboardLayout layout) {
        layout
            .add(112, "F1")
            .add(113, "F2")
            .add(114, "F3")
            .add(115, "F4")
            .add(116, "F5")
            .add(117, "F6")
            .add(118, "F7")
            .add(119, "F8")
            .add(120, "F9")
            .add(121, "F10")
            .add(122, "F11")
            .add(123, "F12")
            .add(124, "F13")
            .add(125, "F14")
            .add(126, "F15")
            .add(127, "F16")
            .add(128, "F17")
            .add(129, "F18")
            .add(130, "F19")
            .add(131, "F20")
            .add(132, "F21")
            .add(133, "F22")
            .add(134, "F23")
            .add(135, "F24");
            // 136-143 reserved
    }

    private static void addControlKeys(KeyboardLayout layout) {
        layout
            .add(144, "Num", "NumLock")
            .add(145, "Scroll", "ScrollLock")
            // 146-150 OEM-specific (?)
            // 151-159 unassigned
            .add(160, "LeftShift", "LShift")
            .add(161, "RightShift", "RShift")
            .add(162, "LeftCtrl", "LCtrl")
            .add(163, "RightCtrl", "RCtrl")
            .add(164, "LeftAlt", "LAlt")
            .add(165, "RightAlt", "RAlt")
            // 166-183 browser/media/start app keys
            // 184-185 reserved
            .add(187, "Plus", "+") // not on all keyboards
            .add(188, "Comma", ",")
            .add(189, "Minus", "-")
            .add(190, "Period", ".");
    }
}
