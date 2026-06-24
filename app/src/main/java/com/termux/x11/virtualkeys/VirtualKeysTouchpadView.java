package com.termux.x11.virtualkeys;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

public class VirtualKeysTouchpadView extends View {
    public VirtualKeysInputSender inputSender;
    public boolean isPointerButtonLeftEnabled = true;
    private boolean swapMouseButtons = false;
    private boolean simTouchScreen = false;
    private float lastX = 0f;
    private float lastY = 0f;

    public static final float CURSOR_ACCELERATION = 2f;
    public static final float CURSOR_ACCELERATION_THRESHOLD = 4f;

    public VirtualKeysTouchpadView(Context context) {
        super(context);
    }

    public void setSwapMouseButtons() {
        swapMouseButtons = !swapMouseButtons;
    }

    public void setSimTouchScreen() {
        simTouchScreen = !simTouchScreen;
    }

    public float[] computeDeltaPoint(float oldX, float oldY, float newX, float newY) {
        return new float[]{newX - oldX, newY - oldY};
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastX;
                float dy = event.getY() - lastY;
                if (Math.abs(dx) > CURSOR_ACCELERATION_THRESHOLD || Math.abs(dy) > CURSOR_ACCELERATION_THRESHOLD) {
                    if (inputSender != null)
                        inputSender.onPointerMove((int)(dx * CURSOR_ACCELERATION), (int)(dy * CURSOR_ACCELERATION));
                } else {
                    if (inputSender != null)
                        inputSender.onPointerMove((int)dx, (int)dy);
                }
                lastX = event.getX();
                lastY = event.getY();
                return true;
        }
        return true;
    }
}
