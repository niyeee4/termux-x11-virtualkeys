package com.termux.x11.virtualkeys;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;

import java.io.InputStream;

public class VirtualKeysView extends View {
    public VirtualKeysInputSender inputSender;
    public VirtualKeysProfile profile;
    public boolean editMode;
    public boolean showTouchscreenControls = true;
    public float overlayOpacity = 0.4f;
    public VirtualKeysElement selectedElement;
    public int cursorX;
    public int cursorY;
    public boolean moveCursor;
    public float offsetX;
    public float offsetY;
    public int mousePointerId = -1;
    public float mouseLastX;
    public float mouseLastY;
    public Paint paint;
    public Path path;
    public Bitmap[] icons = new Bitmap[17];
    public VirtualKeysTouchpadView touchpadView;

    private boolean readyToDraw = false;
    private Vibrator vibrator;
    private VibrationEffect vibrationEffect;
    private int lastMaxWidth;
    private int lastMaxHeight;

    private static final float CURSOR_ACCELERATION = 2f;
    private static final float CURSOR_ACCELERATION_THRESHOLD = 4f;

    public VirtualKeysView(Context context) {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        path = new Path();
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setBackgroundColor(Color.TRANSPARENT);
        try {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
        } catch (Exception e) {
            vibrator = null;
        }
    }

    public int getSnappingSize() {
        if (getWidth() > 0) {
            return Math.max(getWidth(), getHeight()) / 100;
        }
        return 10;
    }

    public int getMaxWidth() {
        int ss = getSnappingSize();
        if (ss > 0) {
            return Math.round((float) getWidth() / ss) * ss;
        }
        return getWidth();
    }

    public int getMaxHeight() {
        int ss = getSnappingSize();
        if (ss > 0) {
            return Math.round((float) getHeight() / ss) * ss;
        }
        return getHeight();
    }

    public Paint getPaint() {
        return paint;
    }

    public Path getPath() {
        return path;
    }

    public int getPrimaryColor() {
        return Color.argb((int) (overlayOpacity * 255), 255, 255, 255);
    }

    public int getSecondaryColor() {
        return Color.argb((int) (overlayOpacity * 255), 2, 119, 189);
    }

    public ColorFilter getColorFilter() {
        return new PorterDuffColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_IN);
    }

    public Bitmap getIcon(byte id) {
        int index = id & 0xFF;
        if (icons[index] == null) {
            try {
                InputStream inputStream = getContext().getAssets().open("inputcontrols/icons/" + id + ".png");
                icons[index] = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
            }
        }
        return icons[index];
    }

    public void handleInputEvent(VirtualKeysBinding binding, boolean isDown, float value) {
        if (binding.isGamepad()) {
        } else if (binding.isMouse()) {
            if (binding.isMouseMove()) {
                int dx = 0;
                int dy = 0;
                if (binding == VirtualKeysBinding.MOUSE_MOVE_LEFT) dx = -10;
                else if (binding == VirtualKeysBinding.MOUSE_MOVE_RIGHT) dx = 10;
                else if (binding == VirtualKeysBinding.MOUSE_MOVE_UP) dy = -10;
                else if (binding == VirtualKeysBinding.MOUSE_MOVE_DOWN) dy = 10;
                if (isDown && (dx != 0 || dy != 0) && inputSender != null) {
                    inputSender.onPointerMove(dx, dy);
                }
            } else {
                Integer button = binding.getPointerButton();
                if (button != null && inputSender != null) {
                    inputSender.onPointerButton(button, isDown);
                }
            }
        } else if (binding.isKeyboard()) {
            if (inputSender != null) {
                inputSender.onKeyEvent(binding, isDown);
            }
        }
    }

    public void handleInputEvent(VirtualKeysBinding binding, boolean isDown) {
        handleInputEvent(binding, isDown, 0f);
    }

    public void injectPointerMove(int dx, int dy) {
        if (inputSender != null) {
            inputSender.onPointerMove(dx, dy);
        }
    }

    public void releaseAllKeys() {
        if (profile != null) {
            for (VirtualKeysElement element : profile.getElements()) {
                element.forceRelease();
            }
        }
    }

    public VirtualKeysTouchpadView getTouchpadView() {
        return touchpadView;
    }

    public void setEditMode(boolean mode) {
        editMode = mode;
    }

    public void setProfile(VirtualKeysProfile profile) {
        this.profile = profile;
        deselectAllElements();
        if (getWidth() > 0 && getHeight() > 0) {
            reloadElements();
        }
    }

    public VirtualKeysElement getSelectedElement() {
        return selectedElement;
    }

    public boolean addElement() {
        if (editMode && profile != null) {
            VirtualKeysElement element = new VirtualKeysElement(this);
            element.setX(cursorX);
            element.setY(cursorY);
            element.initDefaultBindings();
            profile.addElement(element);
            profile.save();
            selectElement(element);
            return true;
        }
        return false;
    }

    public boolean removeElement() {
        if (editMode && selectedElement != null && profile != null) {
            profile.removeElement(selectedElement);
            selectedElement = null;
            profile.save();
            invalidate();
            return true;
        }
        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0 && profile != null) {
            reloadElements();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (profile != null && getWidth() > 0 && getHeight() > 0) {
            reloadElements();
        }
    }

    private void reloadElements() {
        if (profile != null) {
            VirtualKeysElement selected = selectedElement;
            profile.loadElements(this);
            if (selected != null) {
                VirtualKeysElement newSelected = null;
                for (VirtualKeysElement e : profile.getElements()) {
                    if (e == selected) {
                        newSelected = e;
                        break;
                    }
                }
                selectElement(newSelected);
            }
        }
        invalidate();
    }

    private void deselectAllElements() {
        selectedElement = null;
        if (profile != null) {
            for (VirtualKeysElement e : profile.getElements()) {
                e.setSelected(false);
            }
        }
    }

    private void selectElement(VirtualKeysElement element) {
        deselectAllElements();
        if (element != null) {
            selectedElement = element;
            element.setSelected(true);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            readyToDraw = false;
            return;
        }
        readyToDraw = true;
        if (editMode) {
            drawGrid(canvas);
            drawCursor(canvas);
        }
        if (profile != null) {
            if (!profile.isElementsLoaded()) {
                reloadElements();
            }
            if (showTouchscreenControls) {
                for (VirtualKeysElement element : profile.getElements()) {
                    element.draw(canvas);
                }
            }
        }
        super.onDraw(canvas);
    }

    private void drawGrid(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        int ss = getSnappingSize();
        paint.setStrokeWidth(ss * 0.0625f);
        paint.setColor(Color.BLACK);
        canvas.drawColor(Color.BLACK);
        paint.setAntiAlias(false);
        paint.setColor(Color.rgb(48, 48, 48));
        int w = getMaxWidth();
        int h = getMaxHeight();
        for (int i = 0; i <= w; i += ss) {
            canvas.drawLine(i, 0, i, h, paint);
        }
        for (int i = 0; i <= h; i += ss) {
            canvas.drawLine(0, i, w, i, paint);
        }
        float cx = roundTo(w * 0.5f, ss);
        float cy = roundTo(h * 0.5f, ss);
        paint.setColor(Color.rgb(66, 66, 66));
        for (int i = 0; i <= w; i += ss * 2) {
            canvas.drawLine(cx, i, cx, i + ss, paint);
        }
        for (int i = 0; i <= h; i += ss * 2) {
            canvas.drawLine(i, cy, i + ss, cy, paint);
        }
        paint.setAntiAlias(true);
    }

    private void drawCursor(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(getSnappingSize() * 0.0625f);
        paint.setColor(Color.rgb(198, 40, 40));
        paint.setAntiAlias(false);
        canvas.drawLine(0, cursorY, getMaxWidth(), cursorY, paint);
        canvas.drawLine(cursorX, 0, cursorX, getMaxHeight(), paint);
        paint.setAntiAlias(true);
    }

    private VirtualKeysElement intersectElement(float x, float y) {
        if (profile != null) {
            for (VirtualKeysElement element : profile.getElements()) {
                if (element.containsPoint(x, y)) return element;
            }
        }
        return null;
    }

    private int roundTo(float value, int snapping) {
        return Math.round(value / snapping) * snapping;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (editMode && readyToDraw) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    float x = event.getX();
                    float y = event.getY();
                    VirtualKeysElement element = intersectElement(x, y);
                    moveCursor = true;
                    if (element != null) {
                        offsetX = x - element.getX();
                        offsetY = y - element.getY();
                        moveCursor = false;
                    }
                    selectElement(element);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (selectedElement != null) {
                        selectedElement.setX(roundTo(event.getX() - offsetX, getSnappingSize()));
                        selectedElement.setY(roundTo(event.getY() - offsetY, getSnappingSize()));
                        invalidate();
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    if (selectedElement != null && profile != null) {
                        profile.save();
                    }
                    if (moveCursor) {
                        cursorX = roundTo(event.getX(), getSnappingSize());
                        cursorY = roundTo(event.getY(), getSnappingSize());
                    }
                    invalidate();
                    break;
                }
            }
            return true;
        }

        if (!editMode && profile != null && showTouchscreenControls) {
            int actionIndex = event.getActionIndex();
            int pointerId = event.getPointerId(actionIndex);
            int actionMasked = event.getActionMasked();

            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    float x = event.getX(actionIndex);
                    float y = event.getY(actionIndex);
                    boolean handledByElement = false;
                    for (VirtualKeysElement element : profile.getElements()) {
                        if (element.handleTouchDown(pointerId, x, y)) {
                            if (vibrator != null && vibrationEffect != null) {
                                try {
                                    vibrator.vibrate(vibrationEffect);
                                } catch (SecurityException e) {
                                    vibrator = null;
                                }
                            }
                            handledByElement = true;
                            break;
                        }
                    }
                    if (!handledByElement && mousePointerId == -1) {
                        mousePointerId = pointerId;
                        mouseLastX = x;
                        mouseLastY = y;
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        float x = event.getX(i);
                        float y = event.getY(i);
                        int id = event.getPointerId(i);
                        boolean handledByElement = false;
                        for (VirtualKeysElement element : profile.getElements()) {
                            if (element.handleTouchMove(id, x, y)) {
                                handledByElement = true;
                                break;
                            }
                        }
                        if (!handledByElement && id == mousePointerId) {
                            float dx = x - mouseLastX;
                            float dy = y - mouseLastY;
                            if (dx != 0f || dy != 0f) {
                                int sendDx;
                                int sendDy;
                                if (Math.abs(dx) > CURSOR_ACCELERATION_THRESHOLD || Math.abs(dy) > CURSOR_ACCELERATION_THRESHOLD) {
                                    sendDx = (int) (dx * CURSOR_ACCELERATION);
                                    sendDy = (int) (dy * CURSOR_ACCELERATION);
                                } else {
                                    sendDx = (int) dx;
                                    sendDy = (int) dy;
                                }
                                if ((sendDx != 0 || sendDy != 0) && inputSender != null) {
                                    inputSender.onPointerMove(sendDx, sendDy);
                                }
                            }
                            mouseLastX = x;
                            mouseLastY = y;
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL: {
                    for (VirtualKeysElement element : profile.getElements()) {
                        element.handleTouchUp(pointerId);
                    }
                    if (pointerId == mousePointerId) {
                        mousePointerId = -1;
                    }
                    if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
                        mousePointerId = -1;
                    }
                    break;
                }
            }
            return true;
        }
        return false;
    }
}
