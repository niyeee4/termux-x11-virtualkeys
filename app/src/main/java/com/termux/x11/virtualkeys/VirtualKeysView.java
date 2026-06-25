package com.termux.x11.virtualkeys;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.preference.PreferenceManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.SparseArray;
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
    private static final float TAP_DISTANCE_THRESHOLD = 25f;
    private static final long TAP_TIME_THRESHOLD_MS = 350;

    private SparseArray<PointF> touchDownPos = new SparseArray<>();
    private SparseArray<Long> touchDownTime = new SparseArray<>();
    private SparseArray<Boolean> pointerOnElement = new SparseArray<>();
    private int freePointerCount = 0;
    private boolean twoFingerScrollActive = false;
    private float twoFingerLastCenterX;
    private float twoFingerLastCenterY;
    private int tapCount = 0;
    private float scrollAccumulatorX = 0f;
    private float scrollAccumulatorY = 0f;
    private static final float SCROLL_THRESHOLD = 20f;
    private boolean leftButtonHeld = false;
    private boolean rightButtonHeld = false;
    private Runnable holdDetector = null;
    private static final long HOLD_DELAY_MS = 450;
    private static final float HOLD_MOVEMENT_THRESHOLD = 15f;

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
            } else if (binding == VirtualKeysBinding.MOUSE_SCROLL_UP) {
                if (isDown && inputSender != null) {
                    int speed = PreferenceManager.getDefaultSharedPreferences(getContext())
                        .getInt("mouseScrollSpeed", 5);
                    inputSender.onScroll(0f, -120f * speed);
                }
            } else if (binding == VirtualKeysBinding.MOUSE_SCROLL_DOWN) {
                if (isDown && inputSender != null) {
                    int speed = PreferenceManager.getDefaultSharedPreferences(getContext())
                        .getInt("mouseScrollSpeed", 5);
                    inputSender.onScroll(0f, 120f * speed);
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
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean vkInputMode = prefs.getBoolean("touchGestures", false);

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
                    pointerOnElement.put(pointerId, handledByElement);
                    if (!handledByElement) {
                        touchDownPos.put(pointerId, new PointF(x, y));
                        touchDownTime.put(pointerId, SystemClock.uptimeMillis());
                        freePointerCount++;
                        if (mousePointerId == -1) {
                            mousePointerId = pointerId;
                            mouseLastX = x;
                            mouseLastY = y;
                        }
                        if (freePointerCount == 1) {
                            scheduleHoldDetector();
                        }
                        if (freePointerCount == 2) {
                            twoFingerLastCenterX = computeFreeCenterX(event);
                            twoFingerLastCenterY = computeFreeCenterY(event);
                            twoFingerScrollActive = false;
                            scheduleHoldDetector();
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    // Process element touch moves (handleTouchMove returns false for BUTTON,
                    // so we cannot rely on its return value to update pointerOnElement)
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        float x = event.getX(i);
                        float y = event.getY(i);
                        int id = event.getPointerId(i);
                        for (VirtualKeysElement element : profile.getElements()) {
                            element.handleTouchMove(id, x, y);
                        }
                    }
                    // Compute free count from pointerOnElement (set at DOWN, cleared at UP)
                    // Do NOT re-evaluate from handleTouchMove — BUTTON elements return false even when captured
                    freePointerCount = 0;
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        int id = event.getPointerId(i);
                        Boolean onEl = pointerOnElement.get(id, null);
                        if (onEl != null && !onEl) {
                            freePointerCount++;
                        }
                    }
                    // Update centers when transitioning to 2 free pointers
                    if (freePointerCount == 2 && !twoFingerScrollActive) {
                        twoFingerLastCenterX = computeFreeCenterX(event);
                        twoFingerLastCenterY = computeFreeCenterY(event);
                        scrollAccumulatorX = 0f;
                        scrollAccumulatorY = 0f;
                    }
                    if (freePointerCount >= 2 && !vkInputMode && !rightButtonHeld && !leftButtonHeld) {
                        // Cancel hold if any free finger moved beyond threshold (user is moving, not holding)
                        for (int pi = 0; pi < event.getPointerCount(); pi++) {
                            int pid = event.getPointerId(pi);
                            Boolean onEl = pointerOnElement.get(pid, null);
                            if (onEl != null && !onEl) {
                                PointF dp = touchDownPos.get(pid);
                                if (dp != null) {
                                    float fd = (float) Math.sqrt(
                                        Math.pow(event.getX(pi) - dp.x, 2) + Math.pow(event.getY(pi) - dp.y, 2));
                                    if (fd >= HOLD_MOVEMENT_THRESHOLD) {
                                        cancelHoldDetector();
                                        break;
                                    }
                                }
                            }
                        }
                        float centerX = computeFreeCenterX(event);
                        float centerY = computeFreeCenterY(event);
                        float dx = centerX - twoFingerLastCenterX;
                        float dy = centerY - twoFingerLastCenterY;
                        twoFingerLastCenterX = centerX;
                        twoFingerLastCenterY = centerY;
                        if (freePointerCount > 2) {
                            twoFingerLastCenterX = centerX;
                            twoFingerLastCenterY = centerY;
                            scrollAccumulatorX = 0f;
                            scrollAccumulatorY = 0f;
                            cancelHoldDetector();
                            break;
                        }
                        scrollAccumulatorX += dx;
                        scrollAccumulatorY += dy;
                        if (Math.abs(scrollAccumulatorY) >= SCROLL_THRESHOLD && inputSender != null) {
                            float dist = scrollAccumulatorY;
                            scrollAccumulatorY = 0f;
                            twoFingerScrollActive = true;
                            cancelHoldDetector();
                            inputSender.onScroll(0f, dist);
                        }
                        if (Math.abs(scrollAccumulatorX) >= SCROLL_THRESHOLD && inputSender != null) {
                            float dist = scrollAccumulatorX;
                            scrollAccumulatorX = 0f;
                            twoFingerScrollActive = true;
                            cancelHoldDetector();
                            inputSender.onScroll(dist, 0f);
                        }
                    } else if (freePointerCount == 1) {
                        for (int i = 0; i < event.getPointerCount(); i++) {
                            int id = event.getPointerId(i);
                            Boolean onEl = pointerOnElement.get(id, null);
                            if (onEl != null && !onEl && id == mousePointerId) {
                                float x = event.getX(i);
                                float y = event.getY(i);
                                float dx = x - mouseLastX;
                                float dy = y - mouseLastY;
                                if (dx != 0f || dy != 0f) {
                                    // Cancel hold if finger moved beyond threshold (user is moving cursor, not holding)
                                    if (!leftButtonHeld) {
                                        PointF downPos = touchDownPos.get(id);
                                        if (downPos != null) {
                                            float totalDist = (float) Math.sqrt(
                                                Math.pow(x - downPos.x, 2) + Math.pow(y - downPos.y, 2));
                                            if (totalDist >= HOLD_MOVEMENT_THRESHOLD) {
                                                cancelHoldDetector();
                                            }
                                        }
                                    }
                                    boolean relativeMouse = prefs.getBoolean("relativeMouse", false);
                                    int sendDx;
                                    int sendDy;
                                    if (relativeMouse && (Math.abs(dx) > CURSOR_ACCELERATION_THRESHOLD || Math.abs(dy) > CURSOR_ACCELERATION_THRESHOLD)) {
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
                                break;
                            }
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL: {
                    cancelHoldDetector();
                    for (VirtualKeysElement element : profile.getElements()) {
                        element.handleTouchUp(pointerId);
                    }
                    if (pointerId == mousePointerId) {
                        mousePointerId = -1;
                    }
                    Boolean wasOnElement = pointerOnElement.get(pointerId, false);
                    if (!wasOnElement) {
                        freePointerCount = Math.max(0, freePointerCount - 1);
                        if (!vkInputMode && !twoFingerScrollActive && !rightButtonHeld && !leftButtonHeld) {
                            PointF downPos = touchDownPos.get(pointerId);
                            Long downTime = touchDownTime.get(pointerId);
                            if (downPos != null && downTime != null) {
                                int idx = event.findPointerIndex(pointerId);
                                if (idx >= 0) {
                                    float upX = event.getX(idx);
                                    float upY = event.getY(idx);
                                    float dist = (float) Math.sqrt(
                                        Math.pow(upX - downPos.x, 2) + Math.pow(upY - downPos.y, 2));
                                    long dur = SystemClock.uptimeMillis() - downTime;
                                    if (dist < TAP_DISTANCE_THRESHOLD && dur < TAP_TIME_THRESHOLD_MS) {
                                        tapCount++;
                                    }
                                }
                            }
                        }
                    }
                    pointerOnElement.remove(pointerId);
                    touchDownPos.remove(pointerId);
                    touchDownTime.remove(pointerId);
                    boolean allPointersReleased = actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL || (freePointerCount == 0 && pointerOnElement.size() == 0);
                    if (allPointersReleased) {
                        if (leftButtonHeld) {
                            if (inputSender != null) {
                                inputSender.onPointerButton(1, false);
                            }
                            leftButtonHeld = false;
                        }
                        if (rightButtonHeld) {
                            if (inputSender != null) {
                                inputSender.onPointerButton(3, false);
                            }
                            rightButtonHeld = false;
                        }
                        if (!leftButtonHeld && !rightButtonHeld && !vkInputMode && !twoFingerScrollActive && inputSender != null) {
                            if (tapCount == 1) {
                                inputSender.onPointerButton(1, true);
                                inputSender.onPointerButton(1, false);
                            } else if (tapCount >= 2) {
                                inputSender.onPointerButton(3, true);
                                inputSender.onPointerButton(3, false);
                            }
                        }
                        mousePointerId = -1;
                        freePointerCount = 0;
                        twoFingerScrollActive = false;
                        leftButtonHeld = false;
                        rightButtonHeld = false;
                        scrollAccumulatorX = 0f;
                        scrollAccumulatorY = 0f;
                        tapCount = 0;
                        touchDownPos.clear();
                        touchDownTime.clear();
                        pointerOnElement.clear();
                    }
                    break;
                }
            }
            return true;
        }
        return false;
    }

    private void scheduleHoldDetector() {
        cancelHoldDetector();
        holdDetector = () -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean touchGesturesOn = prefs.getBoolean("touchGestures", false);
            if (!touchGesturesOn && inputSender != null) {
                if (freePointerCount >= 2 && !twoFingerScrollActive && !rightButtonHeld && !leftButtonHeld) {
                    rightButtonHeld = true;
                    inputSender.onPointerButton(3, true);
                } else if (freePointerCount == 1 && !leftButtonHeld) {
                    leftButtonHeld = true;
                    inputSender.onPointerButton(1, true);
                }
            }
        };
        postDelayed(holdDetector, HOLD_DELAY_MS);
    }

    private void cancelHoldDetector() {
        if (holdDetector != null) {
            removeCallbacks(holdDetector);
            holdDetector = null;
        }
    }

    private float computeFreeCenterX(MotionEvent event) {
        float sum = 0;
        int count = 0;
        for (int i = 0; i < event.getPointerCount(); i++) {
            int id = event.getPointerId(i);
            Boolean onEl = pointerOnElement.get(id, null);
            if (onEl != null && !onEl) {
                sum += event.getX(i);
                count++;
            }
        }
        return count > 0 ? sum / count : event.getX();
    }

    private float computeFreeCenterY(MotionEvent event) {
        float sum = 0;
        int count = 0;
        for (int i = 0; i < event.getPointerCount(); i++) {
            int id = event.getPointerId(i);
            Boolean onEl = pointerOnElement.get(id, null);
            if (onEl != null && !onEl) {
                sum += event.getY(i);
                count++;
            }
        }
        return count > 0 ? sum / count : event.getY();
    }
}
