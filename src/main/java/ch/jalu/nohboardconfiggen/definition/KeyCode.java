package ch.jalu.nohboardconfiggen.definition;

import lombok.Getter;

import java.util.Locale;

public enum KeyCode {

    BACKSPACE(8, "Back"),
    TAB(9),
    CLEAR(12),
    ENTER(13),
    PAUSE(19),
    CAPS_LOCK(20, "Caps"),
    ESCAPE(27, "Esc"),
    SPACE(32),

    PAGE_UP(33, "PageUp"),
    PAGE_DOWN(34, "PageDown"),
    END(35),
    HOME(36),

    LEFT(37, "ArrowLeft"),
    UP(38, "ArrowUp"),
    RIGHT(39, "ArrowRight"),
    DOWN(40, "ArrowDown"),

    PRINT_SCREEN(44, "PrtScrn"),
    INSERT(45),
    DELETE(46, "Del"),

    KEY_0(48, "0"),
    KEY_1(49, "1"),
    KEY_2(50, "2"),
    KEY_3(51, "3"),
    KEY_4(52, "4"),
    KEY_5(53, "5"),
    KEY_6(54, "6"),
    KEY_7(55, "7"),
    KEY_8(56, "8"),
    KEY_9(57, "9"),

    A(65),
    B(66),
    C(67),
    D(68),
    E(69),
    F(70),
    G(71),
    H(72),
    I(73),
    J(74),
    K(75),
    L(76),
    M(77),
    N(78),
    O(79),
    P(80),
    Q(81),
    R(82),
    S(83),
    T(84),
    U(85),
    V(86),
    W(87),
    X(88),
    Y(89),
    Z(90),
    WINDOWS(91, "Win"),
    BACK_SLASH(92), // unconfirmed
    CONTEXT_MENU(93),

    NUMPAD0(96, "num0"),
    NUMPAD1(97, "num1"),
    NUMPAD2(98, "num2"),
    NUMPAD3(99, "num3"),
    NUMPAD4(100, "num4"),
    NUMPAD5(101, "num5"),
    NUMPAD6(102, "num6"),
    NUMPAD7(103, "num7"),
    NUMPAD8(104, "num8"),
    NUMPAD9(105, "num9"),
    NUMPAD_ASTERISK(106, "num*"),
    NUMPAD_PLUS(107, "num+"),
    NUMPAD_COMMA(108, "num,"), // unconfirmed
    NUMPAD_MINUS(109, "num-"),
    NUMPAD_DOT(110, "num."),
    NUMPAD_SLASH(111, "num/"),

    F1(112),
    F2(113),
    F3(114),
    F4(115),
    F5(116),
    F6(117),
    F7(118),
    F8(119),
    F9(120),
    F10(121),
    F11(122),
    F12(123),
    F13(124),
    F14(125),
    F15(126),
    F16(127),
    F17(128),
    F18(129),
    F19(130),
    F20(131),
    F21(132),
    F22(133),
    F23(134),
    F24(135),

    NUM_LOCK(144),
    SCROLL_LOCK(145),
    AMPERSAND(150, "&"), // unconfirmed
    ASTERISK(151, "*"), // unconfirmed
    DOUBLE_QUOTE(152, "\""), // unconfirmed
    LESS(153, "<"), // unconfirmed

    LEFT_SHIFT(160, "LShift"),
    RIGHT_SHIFT(161, "RShift"),
    LEFT_CTRL(162, "LCtrl"),
    RIGHT_CTRL(163, "RCtrl"),
    LEFT_ALT(164, "LAlt"),
    RIGHT_ALT(165, "RAlt"),
    SEMI_COLON(186, ";"), // todo check with US keyboard
    EQUALS(187, "="), // todo check with US keyboard
    COMMA(188, ","),
    MINUS(189, "-"),
    PERIOD(190, "."),
    SLASH(191, "/"), // § has the same code on de-CH, think about separating by keyboard layouts
    BACK_QUOTE(192, "`"), // todo check with US keyboard
    LEFT_BRACE(219, "["), // todo check with US keyboard
    RIGHT_BRACE(221, "]"), // todo check with US keyboard
    QUOTE(222, "'"),
    AT(512, "@"), // unconfirmed
    COLON(513, ":"), // unconfirmed
    CIRCUMFLEX(514, "^"), // unconfirmed
    DOLLAR(515, "$"), // unconfirmed
    EURO(516, "€"), // unconfirmed
    EXCLAMATION_MARK(517, "!"), // unconfirmed
    INVERTED_EXCLAMATION_MARK(518, "¡"), // unconfirmed
    LEFT_PARENTHESIS(519, "("), // unconfirmed
    NUMBER_SIGN(520, "#"), // unconfirmed
    PLUS(521, "+"), // unconfirmed
    RIGHT_PARENTHESIS(522, ")"), // unconfirmed
    UNDERSCORE(523), // unconfirmed

    NUMPAD_ENTER(1025);

    @Getter
    private final int code;
    private final String altName;

    KeyCode(int code) {
        this(code, null);
    }

    KeyCode(int code, String altName) {
        this.code = code;
        this.altName = altName;
    }

    String getAltName() {
        return altName;
    }

    public static KeyCode getEntryOrThrow(String keyName) {
        String text = keyName.toUpperCase(Locale.ROOT);
        try {
            return valueOf(text);
        } catch (IllegalArgumentException ignore) {
        }

        for (KeyCode value : values()) {
            if (value.altName != null && value.altName.equalsIgnoreCase(keyName)) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown key '" + keyName + "'");
    }
}
