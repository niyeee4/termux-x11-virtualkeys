package com.termux.x11.virtualkeys;

import android.graphics.Rect;

public class RangeScroller {
    private final VirtualKeysView view;
    private final VirtualKeysElement element;
    private float scrollOffset = 0f;
    private final int[] rangeIndex = new int[]{0, 26};
    private float lastTouchX = 0f;
    private float lastTouchY = 0f;
    private boolean isDragging = false;
    private int lastActivatedIndex = -1;

    public RangeScroller(VirtualKeysView view, VirtualKeysElement element) {
        this.view = view;
        this.element = element;
    }

    public float getElementSize() {
        return view.getSnappingSize() * 4f * element.getScale();
    }

    public float getScrollOffset() {
        return scrollOffset;
    }

    public int[] getRangeIndex() {
        return rangeIndex;
    }

    public void handleTouchDown(VirtualKeysElement element, float x, float y) {
        lastTouchX = x;
        lastTouchY = y;
        isDragging = true;
        lastActivatedIndex = -1;
        updateRangeIndex(element, x, y);
    }

    public void handleTouchMove(VirtualKeysElement element, float x, float y) {
        if (!isDragging) return;

        float delta = element.getOrientation() == 0 ? x - lastTouchX : y - lastTouchY;

        scrollOffset -= delta;
        lastTouchX = x;
        lastTouchY = y;

        updateRangeIndex(element, x, y);
        view.invalidate();
    }

    public void handleTouchUp() {
        if (lastActivatedIndex >= 0) {
            VirtualKeysBinding binding = getBindingForRangeIndex(element.getRange(), lastActivatedIndex);
            if (binding != VirtualKeysBinding.NONE) {
                view.handleInputEvent(binding, false);
            }
            lastActivatedIndex = -1;
        }
        isDragging = false;
    }

    private VirtualKeysBinding getBindingForRangeIndex(VirtualKeysElement.Range range, int index) {
        VirtualKeysElement.Range currentRange = range != null ? range : VirtualKeysElement.Range.FROM_A_TO_Z;
        switch (currentRange) {
            case FROM_A_TO_Z:
                switch (index) {
                    case 0: return VirtualKeysBinding.KEY_A;
                    case 1: return VirtualKeysBinding.KEY_B;
                    case 2: return VirtualKeysBinding.KEY_C;
                    case 3: return VirtualKeysBinding.KEY_D;
                    case 4: return VirtualKeysBinding.KEY_E;
                    case 5: return VirtualKeysBinding.KEY_F;
                    case 6: return VirtualKeysBinding.KEY_G;
                    case 7: return VirtualKeysBinding.KEY_H;
                    case 8: return VirtualKeysBinding.KEY_I;
                    case 9: return VirtualKeysBinding.KEY_J;
                    case 10: return VirtualKeysBinding.KEY_K;
                    case 11: return VirtualKeysBinding.KEY_L;
                    case 12: return VirtualKeysBinding.KEY_M;
                    case 13: return VirtualKeysBinding.KEY_N;
                    case 14: return VirtualKeysBinding.KEY_O;
                    case 15: return VirtualKeysBinding.KEY_P;
                    case 16: return VirtualKeysBinding.KEY_Q;
                    case 17: return VirtualKeysBinding.KEY_R;
                    case 18: return VirtualKeysBinding.KEY_S;
                    case 19: return VirtualKeysBinding.KEY_T;
                    case 20: return VirtualKeysBinding.KEY_U;
                    case 21: return VirtualKeysBinding.KEY_V;
                    case 22: return VirtualKeysBinding.KEY_W;
                    case 23: return VirtualKeysBinding.KEY_X;
                    case 24: return VirtualKeysBinding.KEY_Y;
                    case 25: return VirtualKeysBinding.KEY_Z;
                    default: return VirtualKeysBinding.NONE;
                }
            case DIGITS:
                switch (index) {
                    case 0: return VirtualKeysBinding.KEY_0;
                    case 1: return VirtualKeysBinding.KEY_1;
                    case 2: return VirtualKeysBinding.KEY_2;
                    case 3: return VirtualKeysBinding.KEY_3;
                    case 4: return VirtualKeysBinding.KEY_4;
                    case 5: return VirtualKeysBinding.KEY_5;
                    case 6: return VirtualKeysBinding.KEY_6;
                    case 7: return VirtualKeysBinding.KEY_7;
                    case 8: return VirtualKeysBinding.KEY_8;
                    case 9: return VirtualKeysBinding.KEY_9;
                    default: return VirtualKeysBinding.NONE;
                }
            case FUNCTION_KEYS:
                switch (index) {
                    case 0: return VirtualKeysBinding.KEY_F1;
                    case 1: return VirtualKeysBinding.KEY_F2;
                    case 2: return VirtualKeysBinding.KEY_F3;
                    case 3: return VirtualKeysBinding.KEY_F4;
                    case 4: return VirtualKeysBinding.KEY_F5;
                    case 5: return VirtualKeysBinding.KEY_F6;
                    case 6: return VirtualKeysBinding.KEY_F7;
                    case 7: return VirtualKeysBinding.KEY_F8;
                    case 8: return VirtualKeysBinding.KEY_F9;
                    case 9: return VirtualKeysBinding.KEY_F10;
                    case 10: return VirtualKeysBinding.KEY_F11;
                    case 11: return VirtualKeysBinding.KEY_F12;
                    default: return VirtualKeysBinding.NONE;
                }
            case NUMPAD_DIGITS:
                switch (index) {
                    case 0: return VirtualKeysBinding.NUMPAD_0;
                    case 1: return VirtualKeysBinding.NUMPAD_1;
                    case 2: return VirtualKeysBinding.NUMPAD_2;
                    case 3: return VirtualKeysBinding.NUMPAD_3;
                    case 4: return VirtualKeysBinding.NUMPAD_4;
                    case 5: return VirtualKeysBinding.NUMPAD_5;
                    case 6: return VirtualKeysBinding.NUMPAD_6;
                    case 7: return VirtualKeysBinding.NUMPAD_7;
                    case 8: return VirtualKeysBinding.NUMPAD_8;
                    case 9: return VirtualKeysBinding.NUMPAD_9;
                    default: return VirtualKeysBinding.NONE;
                }
            default:
                return VirtualKeysBinding.NONE;
        }
    }

    private void updateRangeIndex(VirtualKeysElement element, float x, float y) {
        VirtualKeysElement.Range range = element.getRange() != null ? element.getRange() : VirtualKeysElement.Range.FROM_A_TO_Z;
        float elementSize = getElementSize();
        Rect box = element.getBoundingBox();

        float position = element.getOrientation() == 0 ?
            (x - box.left + scrollOffset) / elementSize :
            (y - box.top + scrollOffset) / elementSize;

        int visibleCount = 5;
        int centerIndex = Math.max(0, Math.min((int) position, range.max - 1));
        int startIndex = Math.max(0, centerIndex - visibleCount / 2);
        int endIndex = Math.min(startIndex + visibleCount, range.max);

        rangeIndex[0] = startIndex;
        rangeIndex[1] = endIndex;

        if (centerIndex != lastActivatedIndex) {
            if (lastActivatedIndex >= 0) {
                VirtualKeysBinding prevBinding = getBindingForRangeIndex(range, lastActivatedIndex);
                if (prevBinding != VirtualKeysBinding.NONE) {
                    view.handleInputEvent(prevBinding, false);
                }
            }

            VirtualKeysBinding binding = getBindingForRangeIndex(range, centerIndex);
            if (binding != VirtualKeysBinding.NONE) {
                view.handleInputEvent(binding, true);
                lastActivatedIndex = centerIndex;
            }
        }
    }
}
