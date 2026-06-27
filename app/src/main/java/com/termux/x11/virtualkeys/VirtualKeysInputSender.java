package com.termux.x11.virtualkeys;

public interface VirtualKeysInputSender {
    void onKeyEvent(VirtualKeysBinding binding, boolean isDown);
    default void onKeyEvent(VirtualKeysBinding binding, boolean isDown, float value) {
        onKeyEvent(binding, isDown);
    }
    void onPointerButton(int button, boolean isDown);
    void onPointerMove(int dx, int dy);
    void onScroll(float distanceX, float distanceY);
}
