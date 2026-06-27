package com.termux.x11.virtualkeys;

import android.view.KeyEvent;

public enum VirtualKeysBinding {
    NONE(0, true, false), // keyboard

    KEY_ESC(1, true, false),
    KEY_ESCAPE(1, true, false),
    KEY_F1(59, true, false),
    KEY_F2(60, true, false),
    KEY_F3(61, true, false),
    KEY_F4(62, true, false),
    KEY_F5(63, true, false),
    KEY_F6(64, true, false),
    KEY_F7(65, true, false),
    KEY_F8(66, true, false),
    KEY_F9(67, true, false),
    KEY_F10(68, true, false),
    KEY_F11(69, true, false),
    KEY_F12(70, true, false),
    KEY_GRAVE(41, true, false),
    KEY_1(2, true, false),
    KEY_2(3, true, false),
    KEY_3(4, true, false),
    KEY_4(5, true, false),
    KEY_5(6, true, false),
    KEY_6(7, true, false),
    KEY_7(8, true, false),
    KEY_8(9, true, false),
    KEY_9(10, true, false),
    KEY_0(11, true, false),
    KEY_MINUS(12, true, false),
    KEY_EQUALS(13, true, false),
    KEY_BKSP(14, true, false),
    KEY_BACKSPACE(14, true, false),
    KEY_TAB(15, true, false),
    KEY_Q(16, true, false),
    KEY_W(17, true, false),
    KEY_E(18, true, false),
    KEY_R(19, true, false),
    KEY_T(20, true, false),
    KEY_Y(21, true, false),
    KEY_U(22, true, false),
    KEY_I(23, true, false),
    KEY_O(24, true, false),
    KEY_P(25, true, false),
    KEY_BRACKET_LEFT(26, true, false),
    KEY_BRACKET_RIGHT(27, true, false),
    KEY_BACKSLASH(43, true, false),
    KEY_CAPITAL(58, true, false),
    KEY_CAPS_LOCK(58, true, false),
    KEY_A(30, true, false),
    KEY_S(31, true, false),
    KEY_D(32, true, false),
    KEY_F(33, true, false),
    KEY_G(34, true, false),
    KEY_H(35, true, false),
    KEY_J(36, true, false),
    KEY_K(37, true, false),
    KEY_L(38, true, false),
    KEY_SEMICOLON(39, true, false),
    KEY_APOSTROPHE(40, true, false),
    KEY_ENTER(28, true, false),
    KEY_SHIFT_L(42, true, false),
    KEY_LSHIFT(42, true, false),
    KEY_SHIFT_R(54, true, false),
    KEY_RSHIFT(54, true, false),
    KEY_Z(44, true, false),
    KEY_X(45, true, false),
    KEY_C(46, true, false),
    KEY_V(47, true, false),
    KEY_B(48, true, false),
    KEY_N(49, true, false),
    KEY_M(50, true, false),
    KEY_COMMA(51, true, false),
    KEY_PERIOD(52, true, false),
    KEY_SLASH(53, true, false),
    KEY_CTRL_L(29, true, false),
    KEY_LCTRL(29, true, false),
    KEY_LCONTROL(29, true, false),
    KEY_CTRL_R(157, true, false),
    KEY_RCTRL(157, true, false),
    KEY_RCONTROL(157, true, false),
    KEY_LWIN(125, true, false),
    KEY_LMENU(56, true, false),
    KEY_LALT(56, true, false),
    KEY_ALT_L(56, true, false),
    KEY_SPACE(57, true, false),
    KEY_RMENU(126, true, false),
    KEY_RALT(126, true, false),
    KEY_ALT_R(126, true, false),
    KEY_RWIN(127, true, false),
    KEY_UP(72, true, false),
    KEY_DOWN(80, true, false),
    KEY_LEFT(105, true, false),
    KEY_RIGHT(106, true, false),
    KEY_INSERT(110, true, false),
    KEY_HOME(102, true, false),
    KEY_END(107, true, false),
    KEY_PGUP(104, true, false),
    KEY_PAGEUP(104, true, false),
    KEY_PGDN(109, true, false),
    KEY_PAGEDOWN(109, true, false),
    KEY_DELETE(111, true, false),
    KEY_DEL(111, true, false),
    KEY_PRTSCN(127, true, false),
    KEY_PRINT(127, true, false),
    KEY_SCROLL_LOCK(70, true, false),
    KEY_PAUSE(197, true, false),
    NUMPAD_0(82, true, false),
    KEY_KP_0(82, true, false),
    NUMPAD_1(79, true, false),
    KEY_KP_1(79, true, false),
    NUMPAD_2(80, true, false),
    KEY_KP_2(80, true, false),
    NUMPAD_3(81, true, false),
    KEY_KP_3(81, true, false),
    NUMPAD_4(75, true, false),
    KEY_KP_4(75, true, false),
    NUMPAD_5(76, true, false),
    KEY_KP_5(76, true, false),
    NUMPAD_6(77, true, false),
    KEY_KP_6(77, true, false),
    NUMPAD_7(71, true, false),
    KEY_KP_7(71, true, false),
    NUMPAD_8(72, true, false),
    KEY_KP_8(72, true, false),
    NUMPAD_9(73, true, false),
    KEY_KP_9(73, true, false),
    NUMPAD_DECIMAL(83, true, false),
    KEY_KP_DECIMAL(83, true, false),
    NUMPAD_DIVIDE(84, true, false),
    KEY_KP_DIVIDE(84, true, false),
    NUMPAD_MULTIPLY(85, true, false),
    KEY_KP_MULTIPLY(85, true, false),
    NUMPAD_MINUS(86, true, false),
    KEY_KP_SUBTRACT(86, true, false),
    NUMPAD_PLUS(87, true, false),
    KEY_KP_ADD(87, true, false),
    NUMPAD_ENTER(156, true, false),
    KEY_KP_ENTER(156, true, false),
    KEY_NUM_LOCK(69, true, false),
    KEY_NUMLOCK(69, true, false),
    KEY_SCROLL(151, true, false),

    MOUSE_LEFT_BUTTON(0, false, true),
    MOUSE_RIGHT_BUTTON(0, false, true),
    MOUSE_MIDDLE_BUTTON(0, false, true),
    MOUSE_MOVE_UP(0, false, true),
    MOUSE_MOVE_DOWN(0, false, true),
    MOUSE_MOVE_LEFT(0, false, true),
    MOUSE_MOVE_RIGHT(0, false, true),
    MOUSE_SCROLL_UP(0, false, true),
    MOUSE_SCROLL_DOWN(0, false, true),
    MOUSE_LEFT_RIGHT(0, false, true),
    MOUSE_TOUCHMODE_SWITCH(0, false, true);

    private final int keycode;
    private final boolean keyboard;
    private final boolean mouse;

    VirtualKeysBinding(int keycode, boolean keyboard, boolean mouse) {
        this.keycode = keycode;
        this.keyboard = keyboard;
        this.mouse = mouse;
    }

    public int keycode() { return keycode; }
    public boolean isKeyboard() { return keyboard; }
    public boolean isMouse() { return mouse; }

    public boolean isMouseMove() {
        return this == MOUSE_MOVE_UP || this == MOUSE_MOVE_DOWN ||
               this == MOUSE_MOVE_LEFT || this == MOUSE_MOVE_RIGHT;
    }

    public Integer getPointerButton() {
        switch (this) {
            case MOUSE_LEFT_BUTTON: return 1;
            case MOUSE_MIDDLE_BUTTON: return 2;
            case MOUSE_RIGHT_BUTTON: return 3;
            case MOUSE_SCROLL_UP: return 4;
            case MOUSE_SCROLL_DOWN: return 5;
            default: return null;
        }
    }

    public int toAndroidKeyCode() {
        switch (this) {
            case NONE: return 0;
            case KEY_ESC: case KEY_ESCAPE: return KeyEvent.KEYCODE_ESCAPE;
            case KEY_F1: return KeyEvent.KEYCODE_F1;
            case KEY_F2: return KeyEvent.KEYCODE_F2;
            case KEY_F3: return KeyEvent.KEYCODE_F3;
            case KEY_F4: return KeyEvent.KEYCODE_F4;
            case KEY_F5: return KeyEvent.KEYCODE_F5;
            case KEY_F6: return KeyEvent.KEYCODE_F6;
            case KEY_F7: return KeyEvent.KEYCODE_F7;
            case KEY_F8: return KeyEvent.KEYCODE_F8;
            case KEY_F9: return KeyEvent.KEYCODE_F9;
            case KEY_F10: return KeyEvent.KEYCODE_F10;
            case KEY_F11: return KeyEvent.KEYCODE_F11;
            case KEY_F12: return KeyEvent.KEYCODE_F12;
            case KEY_GRAVE: return KeyEvent.KEYCODE_GRAVE;
            case KEY_1: return KeyEvent.KEYCODE_1;
            case KEY_2: return KeyEvent.KEYCODE_2;
            case KEY_3: return KeyEvent.KEYCODE_3;
            case KEY_4: return KeyEvent.KEYCODE_4;
            case KEY_5: return KeyEvent.KEYCODE_5;
            case KEY_6: return KeyEvent.KEYCODE_6;
            case KEY_7: return KeyEvent.KEYCODE_7;
            case KEY_8: return KeyEvent.KEYCODE_8;
            case KEY_9: return KeyEvent.KEYCODE_9;
            case KEY_0: return KeyEvent.KEYCODE_0;
            case KEY_MINUS: return KeyEvent.KEYCODE_MINUS;
            case KEY_EQUALS: return KeyEvent.KEYCODE_EQUALS;
            case KEY_BKSP: case KEY_BACKSPACE: return KeyEvent.KEYCODE_DEL;
            case KEY_TAB: return KeyEvent.KEYCODE_TAB;
            case KEY_Q: return KeyEvent.KEYCODE_Q;
            case KEY_W: return KeyEvent.KEYCODE_W;
            case KEY_E: return KeyEvent.KEYCODE_E;
            case KEY_R: return KeyEvent.KEYCODE_R;
            case KEY_T: return KeyEvent.KEYCODE_T;
            case KEY_Y: return KeyEvent.KEYCODE_Y;
            case KEY_U: return KeyEvent.KEYCODE_U;
            case KEY_I: return KeyEvent.KEYCODE_I;
            case KEY_O: return KeyEvent.KEYCODE_O;
            case KEY_P: return KeyEvent.KEYCODE_P;
            case KEY_BRACKET_LEFT: return KeyEvent.KEYCODE_LEFT_BRACKET;
            case KEY_BRACKET_RIGHT: return KeyEvent.KEYCODE_RIGHT_BRACKET;
            case KEY_BACKSLASH: return KeyEvent.KEYCODE_BACKSLASH;
            case KEY_CAPITAL: case KEY_CAPS_LOCK: return KeyEvent.KEYCODE_CAPS_LOCK;
            case KEY_A: return KeyEvent.KEYCODE_A;
            case KEY_S: return KeyEvent.KEYCODE_S;
            case KEY_D: return KeyEvent.KEYCODE_D;
            case KEY_F: return KeyEvent.KEYCODE_F;
            case KEY_G: return KeyEvent.KEYCODE_G;
            case KEY_H: return KeyEvent.KEYCODE_H;
            case KEY_J: return KeyEvent.KEYCODE_J;
            case KEY_K: return KeyEvent.KEYCODE_K;
            case KEY_L: return KeyEvent.KEYCODE_L;
            case KEY_SEMICOLON: return KeyEvent.KEYCODE_SEMICOLON;
            case KEY_APOSTROPHE: return KeyEvent.KEYCODE_APOSTROPHE;
            case KEY_ENTER: return KeyEvent.KEYCODE_ENTER;
            case KEY_SHIFT_L: case KEY_LSHIFT: return KeyEvent.KEYCODE_SHIFT_LEFT;
            case KEY_SHIFT_R: case KEY_RSHIFT: return KeyEvent.KEYCODE_SHIFT_RIGHT;
            case KEY_Z: return KeyEvent.KEYCODE_Z;
            case KEY_X: return KeyEvent.KEYCODE_X;
            case KEY_C: return KeyEvent.KEYCODE_C;
            case KEY_V: return KeyEvent.KEYCODE_V;
            case KEY_B: return KeyEvent.KEYCODE_B;
            case KEY_N: return KeyEvent.KEYCODE_N;
            case KEY_M: return KeyEvent.KEYCODE_M;
            case KEY_COMMA: return KeyEvent.KEYCODE_COMMA;
            case KEY_PERIOD: return KeyEvent.KEYCODE_PERIOD;
            case KEY_SLASH: return KeyEvent.KEYCODE_SLASH;
            case KEY_CTRL_L: case KEY_LCTRL: case KEY_LCONTROL: return KeyEvent.KEYCODE_CTRL_LEFT;
            case KEY_CTRL_R: case KEY_RCTRL: case KEY_RCONTROL: return KeyEvent.KEYCODE_CTRL_RIGHT;
            case KEY_LWIN: return KeyEvent.KEYCODE_META_LEFT;
            case KEY_LMENU: case KEY_LALT: case KEY_ALT_L: return KeyEvent.KEYCODE_ALT_LEFT;
            case KEY_SPACE: return KeyEvent.KEYCODE_SPACE;
            case KEY_RMENU: case KEY_RALT: case KEY_ALT_R: return KeyEvent.KEYCODE_ALT_RIGHT;
            case KEY_RWIN: return KeyEvent.KEYCODE_META_RIGHT;
            case KEY_UP: return KeyEvent.KEYCODE_DPAD_UP;
            case KEY_DOWN: return KeyEvent.KEYCODE_DPAD_DOWN;
            case KEY_LEFT: return KeyEvent.KEYCODE_DPAD_LEFT;
            case KEY_RIGHT: return KeyEvent.KEYCODE_DPAD_RIGHT;
            case KEY_INSERT: return KeyEvent.KEYCODE_INSERT;
            case KEY_HOME: return KeyEvent.KEYCODE_MOVE_HOME;
            case KEY_END: return KeyEvent.KEYCODE_MOVE_END;
            case KEY_PGUP: case KEY_PAGEUP: return KeyEvent.KEYCODE_PAGE_UP;
            case KEY_PGDN: case KEY_PAGEDOWN: return KeyEvent.KEYCODE_PAGE_DOWN;
            case KEY_DELETE: case KEY_DEL: return KeyEvent.KEYCODE_FORWARD_DEL;
            case KEY_PRTSCN: case KEY_PRINT: return KeyEvent.KEYCODE_SYSRQ;
            case KEY_SCROLL_LOCK: return KeyEvent.KEYCODE_SCROLL_LOCK;
            case KEY_PAUSE: return KeyEvent.KEYCODE_BREAK;
            case KEY_NUM_LOCK: case KEY_NUMLOCK: return KeyEvent.KEYCODE_NUM_LOCK;
            case KEY_KP_0: case NUMPAD_0: return KeyEvent.KEYCODE_NUMPAD_0;
            case KEY_KP_1: case NUMPAD_1: return KeyEvent.KEYCODE_NUMPAD_1;
            case KEY_KP_2: case NUMPAD_2: return KeyEvent.KEYCODE_NUMPAD_2;
            case KEY_KP_3: case NUMPAD_3: return KeyEvent.KEYCODE_NUMPAD_3;
            case KEY_KP_4: case NUMPAD_4: return KeyEvent.KEYCODE_NUMPAD_4;
            case KEY_KP_5: case NUMPAD_5: return KeyEvent.KEYCODE_NUMPAD_5;
            case KEY_KP_6: case NUMPAD_6: return KeyEvent.KEYCODE_NUMPAD_6;
            case KEY_KP_7: case NUMPAD_7: return KeyEvent.KEYCODE_NUMPAD_7;
            case KEY_KP_8: case NUMPAD_8: return KeyEvent.KEYCODE_NUMPAD_8;
            case KEY_KP_9: case NUMPAD_9: return KeyEvent.KEYCODE_NUMPAD_9;
            case KEY_KP_DIVIDE: case NUMPAD_DIVIDE: return KeyEvent.KEYCODE_NUMPAD_DIVIDE;
            case KEY_KP_MULTIPLY: case NUMPAD_MULTIPLY: return KeyEvent.KEYCODE_NUMPAD_MULTIPLY;
            case KEY_KP_SUBTRACT: case NUMPAD_MINUS: return KeyEvent.KEYCODE_NUMPAD_SUBTRACT;
            case KEY_KP_ADD: case NUMPAD_PLUS: return KeyEvent.KEYCODE_NUMPAD_ADD;
            case KEY_KP_DECIMAL: case NUMPAD_DECIMAL: return KeyEvent.KEYCODE_NUMPAD_DOT;
            case KEY_KP_ENTER: case NUMPAD_ENTER: return KeyEvent.KEYCODE_NUMPAD_ENTER;
            default: return 0;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case NONE: return "NONE";
            case KEY_SHIFT_L: case KEY_LSHIFT: return "L SHIFT";
            case KEY_SHIFT_R: case KEY_RSHIFT: return "R SHIFT";
            case KEY_CTRL_L: case KEY_LCTRL: case KEY_LCONTROL: return "L CTRL";
            case KEY_CTRL_R: case KEY_RCTRL: case KEY_RCONTROL: return "R CTRL";
            case KEY_ALT_L: case KEY_LALT: case KEY_LMENU: return "L ALT";
            case KEY_ALT_R: case KEY_RALT: case KEY_RMENU: return "R ALT";
            case KEY_BRACKET_LEFT: return "[";
            case KEY_BRACKET_RIGHT: return "]";
            case KEY_BACKSLASH: return "\\";
            case KEY_SLASH: return "/";
            case KEY_SEMICOLON: return ";";
            case KEY_COMMA: return ",";
            case KEY_PERIOD: return ".";
            case KEY_APOSTROPHE: return "'";
            case KEY_MINUS: return "-";
            case KEY_EQUALS: return "=";
            case KEY_GRAVE: return "`";
            case KEY_KP_0: case NUMPAD_0: return "NP 0";
            case KEY_KP_1: case NUMPAD_1: return "NP 1";
            case KEY_KP_2: case NUMPAD_2: return "NP 2";
            case KEY_KP_3: case NUMPAD_3: return "NP 3";
            case KEY_KP_4: case NUMPAD_4: return "NP 4";
            case KEY_KP_5: case NUMPAD_5: return "NP 5";
            case KEY_KP_6: case NUMPAD_6: return "NP 6";
            case KEY_KP_7: case NUMPAD_7: return "NP 7";
            case KEY_KP_8: case NUMPAD_8: return "NP 8";
            case KEY_KP_9: case NUMPAD_9: return "NP 9";
            case KEY_KP_DIVIDE: case NUMPAD_DIVIDE: return "NP /";
            case KEY_KP_MULTIPLY: case NUMPAD_MULTIPLY: return "NP *";
            case KEY_KP_SUBTRACT: case NUMPAD_MINUS: return "NP -";
            case KEY_KP_ADD: case NUMPAD_PLUS: return "NP +";
            case KEY_KP_DECIMAL: case NUMPAD_DECIMAL: return "NP .";
            case KEY_KP_ENTER: case NUMPAD_ENTER: return "NP ENTER";
            case KEY_CAPS_LOCK: case KEY_CAPITAL: return "CAPS LOCK";
            case KEY_NUM_LOCK: case KEY_NUMLOCK: return "NUM LOCK";
            case KEY_SCROLL_LOCK: case KEY_SCROLL: return "SCROLL LOCK";
            case KEY_ESC: case KEY_ESCAPE: return "ESC";
            case KEY_ENTER: return "ENTER";
            case KEY_TAB: return "TAB";
            case KEY_SPACE: return "SPACE";
            case KEY_BKSP: case KEY_BACKSPACE: return "BKSP";
            case KEY_DELETE: case KEY_DEL: return "DELETE";
            case KEY_INSERT: return "INSERT";
            case KEY_HOME: return "HOME";
            case KEY_END: return "END";
            case KEY_PGUP: case KEY_PAGEUP: return "PG UP";
            case KEY_PGDN: case KEY_PAGEDOWN: return "PG DN";
            case KEY_UP: return "UP";
            case KEY_DOWN: return "DOWN";
            case KEY_LEFT: return "LEFT";
            case KEY_RIGHT: return "RIGHT";
            case KEY_PRTSCN: case KEY_PRINT: return "PRINT";
            case KEY_PAUSE: return "PAUSE";
            case MOUSE_LEFT_BUTTON: return "L MB";
            case MOUSE_RIGHT_BUTTON: return "R MB";
            case MOUSE_MIDDLE_BUTTON: return "M MB";
            case MOUSE_MOVE_UP: return "M UP";
            case MOUSE_MOVE_DOWN: return "M DOWN";
            case MOUSE_MOVE_LEFT: return "M LEFT";
            case MOUSE_MOVE_RIGHT: return "M RIGHT";
            case MOUSE_SCROLL_UP: return "M SC UP";
            case MOUSE_SCROLL_DOWN: return "M SC DOWN";
            default: return name().replace("KEY_", "").replace("NUMPAD_", "");
        }
    }

    public static VirtualKeysBinding fromString(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            switch (name) {
                case "KEY_ESC": return KEY_ESC;
                case "KEY_BKSP": case "KEY_BACKSPACE": return KEY_BKSP;
                case "KEY_SHIFT": case "KEY_LSHIFT": return KEY_SHIFT_L;
                case "KEY_RSHIFT": return KEY_SHIFT_R;
                case "KEY_CTRL": case "KEY_LCTRL": case "KEY_LCONTROL": return KEY_CTRL_L;
                case "KEY_RCTRL": case "KEY_RCONTROL": return KEY_CTRL_R;
                case "KEY_ALT": case "KEY_LALT": return KEY_ALT_L;
                case "KEY_RALT": return KEY_ALT_R;
                case "KEY_DELETE": return KEY_DEL;
                case "KEY_PAGEUP": case "KEY_PG_UP": return KEY_PGUP;
                case "KEY_PAGEDOWN": case "KEY_PG_DOWN": return KEY_PGDN;
                case "KEY_PRINT": return KEY_PRTSCN;
                case "KEY_CAPS": return KEY_CAPITAL;
                case "KEY_NUMLOCK": return KEY_NUM_LOCK;
                case "KEY_SCROLL": return KEY_SCROLL_LOCK;
                case "KEY_KP0": case "NUMPAD_0": return NUMPAD_0;
                case "KEY_KP1": case "NUMPAD_1": return NUMPAD_1;
                case "KEY_KP2": case "NUMPAD_2": return NUMPAD_2;
                case "KEY_KP3": case "NUMPAD_3": return NUMPAD_3;
                case "KEY_KP4": case "NUMPAD_4": return NUMPAD_4;
                case "KEY_KP5": case "NUMPAD_5": return NUMPAD_5;
                case "KEY_KP6": case "NUMPAD_6": return NUMPAD_6;
                case "KEY_KP7": case "NUMPAD_7": return NUMPAD_7;
                case "KEY_KP8": case "NUMPAD_8": return NUMPAD_8;
                case "KEY_KP9": case "NUMPAD_9": return NUMPAD_9;
                case "KEY_KP_DIVIDE": return NUMPAD_DIVIDE;
                case "KEY_KP_MULTIPLY": return NUMPAD_MULTIPLY;
                case "KEY_KP_SUBTRACT": return NUMPAD_MINUS;
                case "KEY_KP_ADD": return NUMPAD_PLUS;
                case "KEY_KP_DECIMAL": return NUMPAD_DECIMAL;
                case "KEY_KP_ENTER": return NUMPAD_ENTER;
                default: return NONE;
            }
        }
    }

    private static VirtualKeysBinding[] keyboardCache = null;
    private static VirtualKeysBinding[] mouseCache = null;
    private static String[] keyboardLabelCache = null;
    private static String[] mouseLabelCache = null;

    private static void buildCaches() {
        if (keyboardCache != null) return;
        java.util.ArrayList<VirtualKeysBinding> kb = new java.util.ArrayList<>();
        java.util.ArrayList<VirtualKeysBinding> mb = new java.util.ArrayList<>();
        java.util.HashSet<String> seenKeyboardLabels = new java.util.HashSet<>();
        for (VirtualKeysBinding b : values()) {
            if (b.isKeyboard()) {
                if (seenKeyboardLabels.add(b.toString())) {
                    kb.add(b);
                }
            } else if (b.isMouse()) {
                mb.add(b);
            }
        }
        java.util.Collections.sort(kb, (x, y) -> compareKeyboardLabels(x.toString(), y.toString()));
        keyboardCache = kb.toArray(new VirtualKeysBinding[0]);
        mouseCache = mb.toArray(new VirtualKeysBinding[0]);
        keyboardLabelCache = new String[keyboardCache.length];
        for (int i = 0; i < keyboardCache.length; i++) keyboardLabelCache[i] = keyboardCache[i].toString();
        mouseLabelCache = new String[mouseCache.length];
        for (int i = 0; i < mouseCache.length; i++) mouseLabelCache[i] = mouseCache[i].toString();
    }

    private static int compareKeyboardLabels(String a, String b) {
        int ga = keyboardGroup(a);
        int gb = keyboardGroup(b);
        if (ga != gb) return Integer.compare(ga, gb);
        return a.compareTo(b);
    }

    private static int keyboardGroup(String label) {
        if (label.equals("NONE")) return 0;
        if (label.length() == 1) {
            char c = label.charAt(0);
            if (c >= 'A' && c <= 'Z') return 1;
            if (c >= '0' && c <= '9') return 2;
            return 3;
        }
        if (label.startsWith("F") && label.length() <= 3) {
            try {
                Integer.parseInt(label.substring(1));
                return 6;
            } catch (NumberFormatException e) {}
        }
        if (label.startsWith("L ") || label.startsWith("R ")) {
            if (label.contains("CTRL") || label.contains("SHIFT") || label.contains("ALT")) return 7;
        }
        if (label.equals("LWIN") || label.equals("RWIN")) return 7;
        if (label.equals("TAB") || label.equals("ENTER") || label.equals("ESC") ||
            label.equals("SPACE") || label.equals("BKSP") || label.equals("DELETE")) return 8;
        if (label.equals("UP") || label.equals("DOWN") || label.equals("LEFT") || label.equals("RIGHT")) return 9;
        if (label.equals("HOME") || label.equals("END") || label.equals("PG UP") ||
            label.equals("PG DN") || label.equals("INSERT") || label.equals("PRINT") ||
            label.equals("PAUSE")) return 10;
        if (label.equals("CAPS LOCK") || label.equals("NUM LOCK") || label.equals("SCROLL LOCK")) return 11;
        if (label.startsWith("NP ")) return 12;
        return 13;
    }

    public static VirtualKeysBinding[] keyboardBindings() { buildCaches(); return keyboardCache; }
    public static VirtualKeysBinding[] mouseBindings() { buildCaches(); return mouseCache; }
    public static String[] keyboardBindingLabels() { buildCaches(); return keyboardLabelCache; }
    public static String[] mouseBindingLabels() { buildCaches(); return mouseLabelCache; }
}
