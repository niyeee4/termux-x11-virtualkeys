package com.termux.x11.virtualkeys;

public interface VirtualKeysInputSender {
    void onKeyEvent(VirtualKeysBinding binding, boolean isDown);
    void onPointerButton(int button, boolean isDown);
    void onPointerMove(int dx, int dy);
}
