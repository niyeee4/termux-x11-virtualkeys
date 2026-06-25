package com.termux.x11.virtualkeys;

import android.graphics.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class VirtualKeysElement {
    public static final float STICK_DEAD_ZONE = 0.15f;
    public static final float DPAD_DEAD_ZONE = 0.3f;
    public static final float STICK_SENSITIVITY = 3.0f;
    public static final float TRACKPAD_MIN_SPEED = 0.8f;
    public static final float TRACKPAD_MAX_SPEED = 20.0f;
    public static final byte TRACKPAD_ACCELERATION_THRESHOLD = 4;
    public static final short BUTTON_MIN_TIME_TO_KEEP_PRESSED = 300;

    public enum Type {
        BUTTON, D_PAD, RANGE_BUTTON, STICK, TRACKPAD;

        public static String[] names() {
            return new String[]{"BUTTON", "D-PAD", "RANGE-BUTTON", "STICK", "TRACKPAD"};
        }
    }

    public enum Shape {
        CIRCLE, RECT, ROUND_RECT, SQUARE;

        public static String[] names() {
            return new String[]{"CIRCLE", "RECT", "ROUND RECT", "SQUARE"};
        }
    }

    public enum Range {
        FROM_A_TO_Z((byte) 26),
        DIGITS((byte) 10),
        FUNCTION_KEYS((byte) 12),
        NUMPAD_DIGITS((byte) 10);

        public final byte max;

        Range(byte max) {
            this.max = max;
        }

        public static String[] names() {
            return new String[]{"FROM A TO Z", "DIGITS", "FUNCTION KEYS", "NUMPAD DIGITS"};
        }

        public static Range fromString(String name) {
            switch (name) {
                case "FROM_A_TO_Z":
                case "A-Z":
                    return FROM_A_TO_Z;
                case "FROM_0_TO_9":
                case "0-9":
                case "DIGITS":
                    return DIGITS;
                case "FROM_F1_TO_F12":
                case "F1-F12":
                case "FUNCTION_KEYS":
                    return FUNCTION_KEYS;
                case "FROM_NP0_TO_NP9":
                case "NP0-NP9":
                case "NUMPAD_DIGITS":
                    return NUMPAD_DIGITS;
                default:
                    return null;
            }
        }
    }

    private VirtualKeysView view;
    private Type type = Type.BUTTON;
    private Shape shape = Shape.CIRCLE;
    private VirtualKeysBinding[] bindings = new VirtualKeysBinding[]{VirtualKeysBinding.NONE, VirtualKeysBinding.NONE, VirtualKeysBinding.NONE, VirtualKeysBinding.NONE};
    private float scale = 1.0f;
    private int x = 0;
    private int y = 0;
    private boolean isSelected = false;
    private boolean isToggleSwitch = false;
    private String text = "";
    private byte iconId = 0;
    private Range range = null;
    private byte orientation = 0;
    private int columns = 5;

    private int currentPointerId = -1;
    private Rect boundingBox = new Rect();
    private boolean boundingBoxNeedsUpdate = true;
    private double ratioX = -1.0;
    private double ratioY = -1.0;
    private boolean[] states = new boolean[]{false, false, false, false};
    private PointF currentPosition = null;
    private Long touchTime = null;
    private RangeScroller scroller = null;
    private CubicBezierInterpolator interpolator = null;
    private boolean toggleActive = false;

    public VirtualKeysElement(VirtualKeysView view) {
        this.view = view;
    }

    public VirtualKeysElement() {
    }

    public void setView(VirtualKeysView view) {
        this.view = view;
    }

    public VirtualKeysView getView() {
        return view;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        if (this.type != type) {
            this.type = type;
            reset();
        }
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        if (this.shape != shape) {
            this.shape = shape;
            boundingBoxNeedsUpdate = true;
        }
    }

    public VirtualKeysBinding[] getBindings() {
        return bindings;
    }

    public void setBindings(VirtualKeysBinding[] bindings) {
        this.bindings = bindings;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        if (this.scale != scale) {
            this.scale = scale;
            boundingBoxNeedsUpdate = true;
        }
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        if (this.x != x) {
            this.x = x;
            boundingBoxNeedsUpdate = true;
        }
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        if (this.y != y) {
            this.y = y;
            boundingBoxNeedsUpdate = true;
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isToggleSwitch() {
        return isToggleSwitch;
    }

    public void setToggleSwitch(boolean toggleSwitch) {
        isToggleSwitch = toggleSwitch;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte getIconId() {
        return iconId;
    }

    public void setIconId(byte iconId) {
        this.iconId = iconId;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public byte getOrientation() {
        return orientation;
    }

    public void setRatio(double ratioX, double ratioY) {
        this.ratioX = ratioX;
        this.ratioY = ratioY;
    }

    public void setOrientation(byte orientation) {
        if (this.orientation != orientation) {
            this.orientation = orientation;
            boundingBoxNeedsUpdate = true;
        }
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        if (this.columns != columns) {
            this.columns = Math.max(1, columns);
            boundingBoxNeedsUpdate = true;
        }
    }

    private void reset() {
        text = "";
        iconId = 0;
        range = null;
        scroller = null;
        toggleActive = false;
        columns = 5;
        orientation = 0;
        boundingBoxNeedsUpdate = true;
    }

    public void initDefaultBindings() {
        switch (type) {
            case D_PAD:
            case STICK:
                bindings = new VirtualKeysBinding[]{VirtualKeysBinding.KEY_W, VirtualKeysBinding.KEY_D, VirtualKeysBinding.KEY_S, VirtualKeysBinding.KEY_A};
                break;
            case TRACKPAD:
                bindings = new VirtualKeysBinding[]{VirtualKeysBinding.MOUSE_MOVE_UP, VirtualKeysBinding.MOUSE_MOVE_RIGHT, VirtualKeysBinding.MOUSE_MOVE_DOWN, VirtualKeysBinding.MOUSE_MOVE_LEFT};
                break;
            case RANGE_BUTTON:
                scroller = new RangeScroller(view, this);
                break;
        }
    }

    public int getBindingCount() {
        return bindings.length;
    }

    public void setBindingCount(int count) {
        bindings = new VirtualKeysBinding[count];
        Arrays.fill(bindings, VirtualKeysBinding.NONE);
        Arrays.fill(states, false);
        boundingBoxNeedsUpdate = true;
    }

    public VirtualKeysBinding getBindingAt(int index) {
        if (index < bindings.length) {
            return bindings[index];
        }
        return VirtualKeysBinding.NONE;
    }

    public void setBindingAt(int index, VirtualKeysBinding binding) {
        if (index >= bindings.length) {
            int oldLength = bindings.length;
            VirtualKeysBinding[] newBindings = new VirtualKeysBinding[index + 1];
            System.arraycopy(bindings, 0, newBindings, 0, oldLength);
            for (int i = oldLength; i < newBindings.length; i++) {
                newBindings[i] = VirtualKeysBinding.NONE;
            }
            bindings = newBindings;
            Arrays.fill(states, false);
            boundingBoxNeedsUpdate = true;
        }
        bindings[index] = binding;
    }

    public void setBinding(VirtualKeysBinding binding) {
        Arrays.fill(bindings, binding);
    }

    public Rect getBoundingBox() {
        if (boundingBoxNeedsUpdate) {
            computeBoundingBox();
        }
        return boundingBox;
    }

    private void computeBoundingBox() {
        int snappingSize = view.getSnappingSize();
        int halfWidth = 0;
        int halfHeight = 0;

        switch (type) {
            case BUTTON:
                switch (shape) {
                    case RECT:
                    case ROUND_RECT:
                        halfWidth = snappingSize * 4;
                        halfHeight = snappingSize * 2;
                        break;
                    case SQUARE:
                        halfWidth = (int) (snappingSize * 2.5f);
                        halfHeight = (int) (snappingSize * 2.5f);
                        break;
                    case CIRCLE:
                        halfWidth = snappingSize * 3;
                        halfHeight = snappingSize * 3;
                        break;
                }
                break;
            case D_PAD:
                halfWidth = snappingSize * 7;
                halfHeight = snappingSize * 7;
                break;
            case TRACKPAD:
            case STICK:
                halfWidth = snappingSize * 6;
                halfHeight = snappingSize * 6;
                break;
            case RANGE_BUTTON:
                halfWidth = (columns * 4 * snappingSize) / 2;
                halfHeight = snappingSize * 2;
                if (orientation == 1) {
                    int tmp = halfWidth;
                    halfWidth = halfHeight;
                    halfHeight = tmp;
                }
                break;
        }

        halfWidth = (int) (halfWidth * scale);
        halfHeight = (int) (halfHeight * scale);
        boundingBox.set(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight);
        boundingBoxNeedsUpdate = false;
    }

    public boolean containsPoint(float px, float py) {
        return getBoundingBox().contains((int) (px + 0.5f), (int) (py + 0.5f));
    }

    public void draw(Canvas canvas) {
        int snappingSize = view.getSnappingSize();
        Paint paint = view.getPaint();
        int primaryColor = view.getPrimaryColor();

        paint.setColor((isSelected || toggleActive) ? view.getSecondaryColor() : primaryColor);
        paint.setStyle(Paint.Style.STROKE);
        float strokeWidth = snappingSize * 0.25f;
        paint.setStrokeWidth(strokeWidth);
        Rect box = getBoundingBox();

        switch (type) {
            case BUTTON:
                drawButton(canvas, paint, box, primaryColor, strokeWidth);
                break;
            case D_PAD:
                drawDPad(canvas, paint, box);
                break;
            case RANGE_BUTTON:
                drawRangeButton(canvas, paint, box, strokeWidth);
                break;
            case STICK:
                drawStick(canvas, paint, box, primaryColor, strokeWidth);
                break;
            case TRACKPAD:
                drawTrackpad(canvas, paint, box, strokeWidth);
                break;
        }
    }

    private void drawButton(Canvas canvas, Paint paint, Rect box, int primaryColor, float strokeWidth) {
        float cx = box.exactCenterX();
        float cy = box.exactCenterY();

        switch (shape) {
            case CIRCLE:
                canvas.drawCircle(cx, cy, box.width() * 0.5f, paint);
                break;
            case RECT:
                canvas.drawRect(box, paint);
                break;
            case ROUND_RECT:
                float radius = box.height() * 0.5f;
                canvas.drawRoundRect(box.left, box.top, box.right, box.bottom, radius, radius, paint);
                break;
            case SQUARE:
                int snappingSize = view.getSnappingSize();
                radius = snappingSize * 0.75f * scale;
                canvas.drawRoundRect(box.left, box.top, box.right, box.bottom, radius, radius, paint);
                break;
        }

        if (isToggleSwitch && toggleActive) {
            int savedColor = paint.getColor();
            int savedAlpha = paint.getAlpha();
            Paint.Style savedStyle = paint.getStyle();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFFFFFF);
            paint.setAlpha(50);

            switch (shape) {
                case CIRCLE:
                    canvas.drawCircle(cx, cy, box.width() * 0.5f, paint);
                    break;
                case RECT:
                    canvas.drawRect(box, paint);
                    break;
                case ROUND_RECT:
                    canvas.drawRoundRect(box.left, box.top, box.right, box.bottom,
                        box.height() * 0.5f, box.height() * 0.5f, paint);
                    break;
                case SQUARE:
                    canvas.drawRoundRect(box.left, box.top, box.right, box.bottom,
                        view.getSnappingSize() * 0.75f * scale,
                        view.getSnappingSize() * 0.75f * scale, paint);
                    break;
            }

            paint.setColor(savedColor);
            paint.setAlpha(savedAlpha);
            paint.setStyle(savedStyle);
        }

        if (iconId > 0) {
            drawIcon(canvas, cx, cy, box.width(), box.height());
        } else {
            String displayText = getDisplayText();
            paint.setTextSize(Math.min(
                getTextSizeForWidth(paint, displayText, box.width() - strokeWidth * 2),
                view.getSnappingSize() * 2 * scale
            ));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(primaryColor);
            canvas.drawText(
                displayText, (float) x,
                y - (paint.descent() + paint.ascent()) * 0.5f,
                paint
            );
        }
    }

    private void drawIcon(Canvas canvas, float cx, float cy, float width, float height) {
        Paint paint = view.getPaint();
        Bitmap icon = view.getIcon(iconId);
        if (icon == null) return;

        paint.setColorFilter(view.getColorFilter());
        int margin = (int) (view.getSnappingSize() * (shape == Shape.CIRCLE || shape == Shape.SQUARE ? 2.0f : 1.0f) * scale);
        int halfSize = (int) ((Math.min(width, height) - margin) * 0.5f);

        Rect srcRect = new Rect(0, 0, icon.getWidth(), icon.getHeight());
        Rect dstRect = new Rect(
            (int) (cx - halfSize),
            (int) (cy - halfSize),
            (int) (cx + halfSize),
            (int) (cy + halfSize)
        );

        canvas.drawBitmap(icon, srcRect, dstRect, paint);
        paint.setColorFilter(null);
    }

    private void drawDPad(Canvas canvas, Paint paint, Rect box) {
        float cx = box.exactCenterX();
        float cy = box.exactCenterY();
        int snappingSize = view.getSnappingSize();
        float offsetX = snappingSize * 2 * scale;
        float offsetY = snappingSize * 3 * scale;
        float start = snappingSize * scale;

        Path path = view.getPath();
        path.reset();

        path.moveTo(cx, cy - start);
        path.lineTo(cx - offsetX, cy - offsetY);
        path.lineTo(cx - offsetX, box.top);
        path.lineTo(cx + offsetX, box.top);
        path.lineTo(cx + offsetX, cy - offsetY);
        path.close();

        path.moveTo(cx - start, cy);
        path.lineTo(cx - offsetY, cy - offsetX);
        path.lineTo(box.left, cy - offsetX);
        path.lineTo(box.left, cy + offsetX);
        path.lineTo(cx - offsetY, cy + offsetX);
        path.close();

        path.moveTo(cx, cy + start);
        path.lineTo(cx - offsetX, cy + offsetY);
        path.lineTo(cx - offsetX, box.bottom);
        path.lineTo(cx + offsetX, box.bottom);
        path.lineTo(cx + offsetX, cy + offsetY);
        path.close();

        path.moveTo(cx + start, cy);
        path.lineTo(cx + offsetY, cy - offsetX);
        path.lineTo(box.right, cy - offsetX);
        path.lineTo(box.right, cy + offsetX);
        path.lineTo(cx + offsetY, cy + offsetX);
        path.close();

        canvas.drawPath(path, paint);

        float indicatorSize = snappingSize * 0.75f * scale;
        path.reset();
        path.moveTo(cx, cy - indicatorSize);
        path.lineTo(cx + indicatorSize, cy);
        path.lineTo(cx, cy + indicatorSize);
        path.lineTo(cx - indicatorSize, cy);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRangeButton(Canvas canvas, Paint paint, Rect box, float strokeWidth) {
        int snappingSize = view.getSnappingSize();
        float radius = snappingSize * 0.75f * scale;

        float elementSize = snappingSize * 4 * scale;
        Range currentRange = range != null ? range : Range.FROM_A_TO_Z;
        float scrollOffset = scroller != null ? scroller.getScrollOffset() : 0f;
        int maxItems = currentRange.max & 0xFF;

        if (orientation == 0) {
            float lineTop = box.top + strokeWidth * 0.5f;
            float lineBottom = box.bottom - strokeWidth * 0.5f;

            canvas.drawRoundRect(box.left, box.top, box.right, box.bottom, radius, radius, paint);

            canvas.save();
            Path clipPath = view.getPath();
            clipPath.reset();
            clipPath.addRoundRect(box.left, box.top, box.right, box.bottom, radius, radius, Path.Direction.CW);
            canvas.clipPath(clipPath);

            float positionAtLeftEdge = scrollOffset / elementSize;
            int leftVirtualIndex = (int) Math.floor(positionAtLeftEdge);
            float fractionalOffset = positionAtLeftEdge - leftVirtualIndex;
            int baseIndex = ((leftVirtualIndex % maxItems) + maxItems) % maxItems;

            int itemsToDraw = (int) Math.ceil(box.width() / elementSize) + 2;
            float startX = box.left - fractionalOffset * elementSize;

            for (int i = 0; i < itemsToDraw; i++) {
                int index = (baseIndex + i) % maxItems;
                float itemX = startX + i * elementSize;

                paint.setStyle(Paint.Style.STROKE);
                if (itemX > box.left && itemX < box.right) {
                    canvas.drawLine(itemX, lineTop, itemX, lineBottom, paint);
                }

                String text = getRangeTextForIndex(currentRange, index);
                if (itemX + elementSize > box.left && itemX < box.right) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(view.getPrimaryColor());
                    paint.setTextSize(Math.min(
                        getTextSizeForWidth(paint, text, elementSize - strokeWidth * 2),
                        snappingSize * 2 * scale
                    ));
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(
                        text,
                        itemX + elementSize * 0.5f,
                        y - (paint.descent() + paint.ascent()) * 0.5f,
                        paint
                    );
                }
            }

            canvas.restore();
        } else {
            float lineLeft = box.left + strokeWidth * 0.5f;
            float lineRight = box.right - strokeWidth * 0.5f;

            canvas.drawRoundRect(box.left, box.top, box.right, box.bottom, radius, radius, paint);

            canvas.save();
            Path clipPath = view.getPath();
            clipPath.reset();
            clipPath.addRoundRect(box.left, box.top, box.right, box.bottom, radius, radius, Path.Direction.CW);
            canvas.clipPath(clipPath);

            float positionAtTopEdge = scrollOffset / elementSize;
            int topVirtualIndex = (int) Math.floor(positionAtTopEdge);
            float fractionalOffset = positionAtTopEdge - topVirtualIndex;
            int baseIndex = ((topVirtualIndex % maxItems) + maxItems) % maxItems;

            int itemsToDraw = (int) Math.ceil(box.height() / elementSize) + 2;
            float startY = box.top - fractionalOffset * elementSize;

            for (int i = 0; i < itemsToDraw; i++) {
                int index = (baseIndex + i) % maxItems;
                float itemY = startY + i * elementSize;

                paint.setStyle(Paint.Style.STROKE);
                if (itemY > box.top && itemY < box.bottom) {
                    canvas.drawLine(lineLeft, itemY, lineRight, itemY, paint);
                }

                String text = getRangeTextForIndex(currentRange, index);
                if (itemY + elementSize > box.top && itemY < box.bottom) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(view.getPrimaryColor());
                    paint.setTextSize(Math.min(
                        getTextSizeForWidth(paint, text, box.width() - strokeWidth * 2),
                        snappingSize * 2 * scale
                    ));
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(
                        text,
                        (float) x,
                        itemY + elementSize * 0.5f - (paint.descent() + paint.ascent()) * 0.5f,
                        paint
                    );
                }
            }

            canvas.restore();
        }
    }

    private void drawStick(Canvas canvas, Paint paint, Rect box, int primaryColor, float strokeWidth) {
        float cx = box.exactCenterX();
        float cy = box.exactCenterY();
        int snappingSize = view.getSnappingSize();
        int oldColor = paint.getColor();

        canvas.drawCircle(cx, cy, box.height() * 0.5f, paint);

        float thumbX = currentPosition != null ? currentPosition.x : cx;
        float thumbY = currentPosition != null ? currentPosition.y : cy;
        float thumbRadius = snappingSize * 3.5f * scale;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(50, 255, 255, 255));
        canvas.drawCircle(thumbX, thumbY, thumbRadius, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(oldColor);
        canvas.drawCircle(thumbX, thumbY, thumbRadius + strokeWidth * 0.5f, paint);
    }

    private void drawTrackpad(Canvas canvas, Paint paint, Rect box, float strokeWidth) {
        float radius = box.height() * 0.15f;
        canvas.drawRoundRect(box.left, box.top, box.right, box.bottom, radius, radius, paint);

        float offset = strokeWidth * 2.5f;
        float innerStrokeWidth = strokeWidth * 2;
        float innerHeight = box.height() - offset * 2;
        float innerRadius = (innerHeight / box.height()) * radius - (innerStrokeWidth * 0.5f + strokeWidth * 0.5f);

        paint.setStrokeWidth(innerStrokeWidth);
        canvas.drawRoundRect(box.left + offset, box.top + offset, box.right - offset, box.bottom - offset, innerRadius, innerRadius, paint);
        paint.setStrokeWidth(strokeWidth);
    }

    private String getDisplayText() {
        if (!text.isEmpty()) {
            return text;
        }

        VirtualKeysBinding binding = getBindingAt(0);
        String displayText = binding.toString()
            .replace("NUMPAD ", "NP")
            .replace("BUTTON ", "");

        if (displayText.length() > 7) {
            String[] parts = displayText.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    sb.append(part.charAt(0));
                }
            }
            displayText = (binding.isMouse() ? "M" : "") + sb.toString();
        }
        return displayText;
    }

    private float getTextSizeForWidth(Paint paint, String text, float desiredWidth) {
        float testTextSize = 48f;
        paint.setTextSize(testTextSize);
        return testTextSize * desiredWidth / paint.measureText(text);
    }

    private String getRangeTextForIndex(Range range, int index) {
        switch (range) {
            case FROM_A_TO_Z:
                return String.valueOf((char) ('A' + index));
            case DIGITS:
                return String.valueOf(index);
            case FUNCTION_KEYS:
                return "F" + (index + 1);
            case NUMPAD_DIGITS:
                return "NP" + index;
            default:
                return "";
        }
    }

    public boolean handleTouchDown(int pointerId, float px, float py) {
        if (currentPointerId == -1 && containsPoint(px, py)) {
            currentPointerId = pointerId;

            switch (type) {
                case BUTTON:
                    if (isToggleSwitch) {
                        if (!toggleActive) {
                            sendAllBindings(true);
                            toggleActive = true;
                        } else {
                            sendAllBindings(false);
                            toggleActive = false;
                        }
                        view.invalidate();
                        return true;
                    }
                    if (isKeepButtonPressedAfterMinTime()) {
                        touchTime = System.currentTimeMillis();
                    }
                    sendAllBindings(true);
                    return true;
                case RANGE_BUTTON:
                    if (scroller == null) {
                        scroller = new RangeScroller(view, this);
                    }
                    scroller.handleTouchDown(px, py);
                    return true;
                case TRACKPAD:
                    if (currentPosition == null) {
                        currentPosition = new PointF();
                    }
                    currentPosition.set(px, py);
                    return handleTouchMove(pointerId, px, py);
                case D_PAD:
                case STICK:
                    return handleTouchMove(pointerId, px, py);
            }
        }
        return false;
    }

    public boolean handleTouchMove(int pointerId, float px, float py) {
        if (pointerId == currentPointerId && (type == Type.D_PAD || type == Type.STICK || type == Type.TRACKPAD)) {
            float deltaX;
            float deltaY;
            Rect box = getBoundingBox();
            float radius = box.width() * 0.5f;

            if (type == Type.TRACKPAD) {
                VirtualKeysTouchpadView touchpadView = view.getTouchpadView();
                if (currentPosition == null) {
                    currentPosition = new PointF();
                }
                float[] deltaPoint;
                if (touchpadView != null) {
                    deltaPoint = touchpadView.computeDeltaPoint(currentPosition.x, currentPosition.y, px, py);
                } else {
                    deltaPoint = new float[]{px - currentPosition.x, py - currentPosition.y};
                }
                deltaX = deltaPoint[0];
                deltaY = deltaPoint[1];
                currentPosition.set(px, py);
            } else {
                float localX = px - box.left;
                float localY = py - box.top;
                float offsetX = localX - radius;
                float offsetY = localY - radius;

                float distance = (float) Math.sqrt(
                    (radius - localX) * (radius - localX) +
                    (radius - localY) * (radius - localY)
                );
                if (distance > radius) {
                    float angle = (float) Math.atan2(offsetY, offsetX);
                    offsetX = (float) (Math.cos(angle) * radius);
                    offsetY = (float) (Math.sin(angle) * radius);
                }

                deltaX = clamp(offsetX / radius, -1f, 1f);
                deltaY = clamp(offsetY / radius, -1f, 1f);
            }

            switch (type) {
                case STICK: {
                    if (currentPosition == null) {
                        currentPosition = new PointF();
                    }
                    currentPosition.x = box.left + deltaX * radius + radius;
                    currentPosition.y = box.top + deltaY * radius + radius;

                    boolean[] newStates = new boolean[]{
                        deltaY <= -STICK_DEAD_ZONE,
                        deltaX >= STICK_DEAD_ZONE,
                        deltaY >= STICK_DEAD_ZONE,
                        deltaX <= -STICK_DEAD_ZONE
                    };

                    for (int i = 0; i <= 3; i++) {
                        float value = (i == 1 || i == 3) ? deltaX : deltaY;
                        VirtualKeysBinding binding = getBindingAt(i);

                        if (binding.isGamepad()) {
                            float adjustedValue = clamp(
                                Math.max(0f, Math.abs(value) - 0.01f) * Math.signum(value) * STICK_SENSITIVITY,
                                -1f, 1f
                            );
                            view.handleInputEvent(binding, true, adjustedValue);
                            states[i] = true;
                        } else {
                            boolean state = binding.isMouseMove() ? (newStates[i] || newStates[(i + 2) % 4]) : newStates[i];
                            view.handleInputEvent(binding, state, value);
                            states[i] = state;
                        }
                    }
                    view.invalidate();
                    break;
                }
                case TRACKPAD: {
                    boolean[] newStates = new boolean[]{
                        deltaY <= -TRACKPAD_MIN_SPEED,
                        deltaX >= TRACKPAD_MIN_SPEED,
                        deltaY >= TRACKPAD_MIN_SPEED,
                        deltaX <= -TRACKPAD_MIN_SPEED
                    };
                    int cursorDx = 0;
                    int cursorDy = 0;

                    for (int i = 0; i <= 3; i++) {
                        float value = (i == 1 || i == 3) ? deltaX : deltaY;
                        VirtualKeysBinding binding = getBindingAt(i);

                        if (binding.isGamepad()) {
                            if (Math.abs(value) > TRACKPAD_ACCELERATION_THRESHOLD) {
                                view.handleInputEvent(binding, true, value * STICK_SENSITIVITY);
                            }
                            states[i] = true;
                        } else {
                            if (Math.abs(value) > 4) {
                                if (binding == VirtualKeysBinding.MOUSE_MOVE_LEFT || binding == VirtualKeysBinding.MOUSE_MOVE_RIGHT) {
                                    cursorDx = Math.round(value);
                                } else if (binding == VirtualKeysBinding.MOUSE_MOVE_UP || binding == VirtualKeysBinding.MOUSE_MOVE_DOWN) {
                                    cursorDy = Math.round(value);
                                } else {
                                    view.handleInputEvent(binding, newStates[i], value);
                                    states[i] = newStates[i];
                                }
                            }
                        }
                    }

                    if (cursorDx != 0 || cursorDy != 0) {
                        view.injectPointerMove(cursorDx, cursorDy);
                    }
                    break;
                }
                case D_PAD: {
                    boolean[] newStates = new boolean[]{
                        deltaY <= -DPAD_DEAD_ZONE,
                        deltaX >= DPAD_DEAD_ZONE,
                        deltaY >= DPAD_DEAD_ZONE,
                        deltaX <= -DPAD_DEAD_ZONE
                    };

                    for (int i = 0; i <= 3; i++) {
                        float value = (i == 1 || i == 3) ? deltaX : deltaY;
                        VirtualKeysBinding binding = getBindingAt(i);
                        boolean state = binding.isMouseMove() ? (newStates[i] || newStates[(i + 2) % 4]) : newStates[i];
                        view.handleInputEvent(binding, state, value);
                        states[i] = state;
                    }
                    break;
                }
            }
            return true;
        } else if (pointerId == currentPointerId && type == Type.RANGE_BUTTON) {
            if (scroller != null) {
                scroller.handleTouchMove(px, py);
            }
            return true;
        }
        return false;
    }

    public boolean handleTouchUp(int pointerId) {
        if (pointerId == currentPointerId) {
            switch (type) {
                case BUTTON:
                    if (isToggleSwitch) {
                        break;
                    }
                    if (isKeepButtonPressedAfterMinTime() && touchTime != null) {
                        isSelected = (System.currentTimeMillis() - touchTime) > BUTTON_MIN_TIME_TO_KEEP_PRESSED;
                        if (!isSelected) {
                            sendAllBindings(false);
                        }
                        touchTime = null;
                        view.invalidate();
                    } else {
                        sendAllBindings(false);
                    }
                    break;
                case RANGE_BUTTON:
                case D_PAD:
                case STICK:
                case TRACKPAD:
                    for (int i = 0; i < states.length; i++) {
                        if (states[i]) {
                            view.handleInputEvent(getBindingAt(i), false);
                        }
                        states[i] = false;
                    }

                    if (type == Type.RANGE_BUTTON && scroller != null) {
                        scroller.handleTouchUp();
                    } else if (type == Type.STICK) {
                        view.invalidate();
                    }

                    currentPosition = null;
                    break;
            }
            currentPointerId = -1;
            return true;
        }
        return false;
    }

    public void forceRelease() {
        currentPointerId = -1;
        currentPosition = null;
        touchTime = null;
        if (type == Type.RANGE_BUTTON) {
            scroller = null;
        }
        if (type == Type.BUTTON) {
            if (isToggleSwitch) {
                if (toggleActive) {
                    sendAllBindings(false);
                    toggleActive = false;
                }
            } else {
                sendAllBindings(false);
            }
        } else {
            for (int i = 0; i < states.length; i++) {
                if (states[i]) {
                    view.handleInputEvent(getBindingAt(i), false);
                }
                states[i] = false;
            }
        }
        isSelected = false;
        if (view != null) view.invalidate();
    }

    private void sendAllBindings(boolean isDown) {
        for (int i = 0; i < bindings.length; i++) {
            VirtualKeysBinding b = bindings[i];
            if (b != null && b != VirtualKeysBinding.NONE) {
                view.handleInputEvent(b, isDown);
            }
        }
    }

    private boolean isKeepButtonPressedAfterMinTime() {
        VirtualKeysBinding binding = getBindingAt(0);
        return binding == VirtualKeysBinding.GAMEPAD_BUTTON_THUMBL || binding == VirtualKeysBinding.GAMEPAD_BUTTON_THUMBR;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type.name());
            json.put("shape", shape.name());
            json.put("scale", (double) scale);
            if (view != null) {
                json.put("x", (double) x / Math.max(view.getMaxWidth(), 1));
                json.put("y", (double) y / Math.max(view.getMaxHeight(), 1));
            } else if (ratioX >= 0.0 && ratioY >= 0.0) {
                json.put("x", ratioX);
                json.put("y", ratioY);
            } else {
                json.put("x", x / 1000.0);
                json.put("y", y / 1000.0);
            }
            json.put("toggleSwitch", isToggleSwitch);
            json.put("text", text);
            json.put("iconId", (int) iconId);

            JSONArray bindingsArray = new JSONArray();
            for (VirtualKeysBinding binding : bindings) {
                bindingsArray.put(binding.name());
            }
            json.put("bindings", bindingsArray);

            if (type == Type.RANGE_BUTTON && range != null) {
                json.put("range", range.name());
                json.put("columns", columns);
                if (orientation != 0) {
                    json.put("orientation", (int) orientation);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    static class CubicBezierInterpolator {
        private float mX1 = 0f, mY1 = 0f, mX2 = 0f, mY2 = 0f;
        private int mSamples = 200;
        private float[] mCurve = new float[0];

        void set(float x1, float y1, float x2, float y2) {
            if (x1 != mX1 || y1 != mY1 || x2 != mX2 || y2 != mY2) {
                mX1 = x1;
                mY1 = y1;
                mX2 = x2;
                mY2 = y2;
                mCurve = new float[mSamples];
                for (int i = 0; i < mSamples; i++) {
                    float t = (float) i / (mSamples - 1);
                    mCurve[i] = sampleCurveY(sampleCurveX(t));
                }
            }
        }

        float getInterpolation(float t) {
            if (t <= 0f) return 0f;
            if (t >= 1f) return 1f;
            int position = (int) (t * (mSamples - 1));
            int nextPosition = Math.min(position + 1, mSamples - 1);
            float between = (t * (mSamples - 1)) - position;
            return mCurve[position] + (mCurve[nextPosition] - mCurve[position]) * between;
        }

        private float sampleCurveX(float t) {
            return (float) ((1 - t) * (1 - t) * (1 - t) * 0 +
                3 * (1 - t) * (1 - t) * t * mX1 +
                3 * (1 - t) * t * t * mX2 +
                t * t * t * 1);
        }

        private float sampleCurveY(float t) {
            return (float) ((1 - t) * (1 - t) * (1 - t) * 0 +
                3 * (1 - t) * (1 - t) * t * mY1 +
                3 * (1 - t) * t * t * mY2 +
                t * t * t * 1);
        }
    }
}
