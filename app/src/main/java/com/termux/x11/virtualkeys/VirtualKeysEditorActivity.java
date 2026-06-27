package com.termux.x11.virtualkeys;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VirtualKeysEditorActivity extends AppCompatActivity implements View.OnClickListener {
    private VirtualKeysView inputControlsView;
    private VirtualKeysProfile profile;
    private Context lightContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        setContentView(com.termux.x11.R.layout.virtual_keys_editor_activity);

        inputControlsView = new VirtualKeysView(this);
        inputControlsView.setEditMode(true);
        inputControlsView.overlayOpacity = 0.6f;

        String profileName = getIntent().getStringExtra("profile_name");
        if (profileName == null) {
            Toast.makeText(this, com.termux.x11.R.string.no_profile_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            profile = VirtualKeysProfile.load(this, profileName);
        } catch (Exception e) {
            profile = null;
        }

        if (profile == null) {
            Toast.makeText(this, com.termux.x11.R.string.no_profile_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(com.termux.x11.R.id.vk_tv_profile_name)).setText(profile.getName());
        inputControlsView.setProfile(profile);

        FrameLayout container = findViewById(com.termux.x11.R.id.FLContainer);
        container.addView(inputControlsView, 0);

        findViewById(com.termux.x11.R.id.vk_bt_add_element).setOnClickListener(this);
        findViewById(com.termux.x11.R.id.vk_bt_remove_element).setOnClickListener(this);
        findViewById(com.termux.x11.R.id.vk_bt_element_settings).setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (profile != null) {
            inputControlsView.setProfile(profile);
        }
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == com.termux.x11.R.id.vk_bt_add_element) {
            if (!inputControlsView.addElement()) {
                Toast.makeText(this, com.termux.x11.R.string.no_profile_selected, Toast.LENGTH_SHORT).show();
            }
        } else if (id == com.termux.x11.R.id.vk_bt_remove_element) {
            if (!inputControlsView.removeElement()) {
                Toast.makeText(this, com.termux.x11.R.string.no_control_element_selected, Toast.LENGTH_SHORT).show();
            }
        } else if (id == com.termux.x11.R.id.vk_bt_element_settings) {
            VirtualKeysElement selectedElement = inputControlsView.getSelectedElement();
            if (selectedElement != null) {
                try {
                    showControlElementSettings(v, selectedElement);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, com.termux.x11.R.string.no_control_element_selected, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showControlElementSettings(View anchorView, VirtualKeysElement element) {
        lightContext = new ContextThemeWrapper(this, com.termux.x11.R.style.AppTheme_Light);
        View view = LayoutInflater.from(lightContext).inflate(com.termux.x11.R.layout.virtual_keys_element_settings, null);

        Runnable[] updateLayoutRef = new Runnable[1];
        Runnable updateLayout = new Runnable() {
            @Override
            public void run() {
                VirtualKeysElement.Type type = element.getType();
                boolean isButton = type == VirtualKeysElement.Type.BUTTON;
                boolean isRange = type == VirtualKeysElement.Type.RANGE_BUTTON;
                view.findViewById(com.termux.x11.R.id.LLShape).setVisibility(
                    isButton ? View.VISIBLE : View.GONE);
                view.findViewById(com.termux.x11.R.id.SShape).setVisibility(
                    isButton ? View.VISIBLE : View.GONE);
                view.findViewById(com.termux.x11.R.id.CBToggleSwitch).setVisibility(
                    isButton ? View.VISIBLE : View.GONE);
                view.findViewById(com.termux.x11.R.id.LLCustomText).setVisibility(
                    isButton ? View.VISIBLE : View.GONE);
                view.findViewById(com.termux.x11.R.id.LLRangeOptions).setVisibility(
                    isRange ? View.VISIBLE : View.GONE);
                view.findViewById(com.termux.x11.R.id.SRange).setVisibility(
                    isRange ? View.VISIBLE : View.GONE);
                loadBindingSpinners(element, view.findViewById(com.termux.x11.R.id.LLBindings), updateLayoutRef[0]);
            }
        };
        updateLayoutRef[0] = updateLayout;

        loadTypeSpinner(element, view.findViewById(com.termux.x11.R.id.SType), updateLayout);

        Spinner shapeSpinner = view.findViewById(com.termux.x11.R.id.SShape);
        loadShapeSpinner(element, shapeSpinner);

        Spinner rangeSpinner = view.findViewById(com.termux.x11.R.id.SRange);
        loadRangeSpinner(element, rangeSpinner);

        RadioGroup rgOrientation = view.findViewById(com.termux.x11.R.id.RGOrientation);
        rgOrientation.check(element.getOrientation() == 1 ? com.termux.x11.R.id.RBVertical : com.termux.x11.R.id.RBHorizontal);
        rgOrientation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                element.setOrientation((byte) (checkedId == com.termux.x11.R.id.RBVertical ? 1 : 0));
                if (profile != null) profile.save();
                inputControlsView.invalidate();
            }
        });

        EditText etColumns = view.findViewById(com.termux.x11.R.id.ETColumns);
        etColumns.setText(String.valueOf(element.getColumns()));
        etColumns.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                int value;
                try {
                    value = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    return;
                }
                value = Math.max(1, Math.min(20, value));
                element.setColumns(value);
                if (profile != null) profile.save();
                inputControlsView.invalidate();
            }
        });

        EditText etScale = view.findViewById(com.termux.x11.R.id.ETScale);
        etScale.setText(String.valueOf((int) (element.getScale() * 100)));
        etScale.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                int value;
                try {
                    value = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    return;
                }
                value = Math.max(0, Math.min(300, value));
                element.setScale(value / 100.0f);
                if (profile != null) profile.save();
                inputControlsView.invalidate();
            }
        });

        CheckBox cbToggleSwitch = view.findViewById(com.termux.x11.R.id.CBToggleSwitch);
        cbToggleSwitch.setChecked(element.isToggleSwitch());
        cbToggleSwitch.setVisibility(element.getType() == VirtualKeysElement.Type.BUTTON ? View.VISIBLE : View.GONE);
        cbToggleSwitch.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                element.setToggleSwitch(isChecked);
                if (profile != null) profile.save();
            }
        });

        EditText etCustomText = view.findViewById(com.termux.x11.R.id.ETCustomText);
        etCustomText.setText(element.getText());
        etCustomText.setVisibility(element.getType() == VirtualKeysElement.Type.BUTTON ? View.VISIBLE : View.GONE);

        updateLayout.run();

        PopupWindow popupWindow = showPopupWindow(anchorView, view, dpToPx(340), 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                String text = etCustomText.getText().toString().trim();
                element.setText(text);
                if (profile != null) profile.save();
                inputControlsView.invalidate();
            }
        });
    }

    private PopupWindow showPopupWindow(View anchorView, View contentView, int width, int heightOffset) {
        PopupWindow popupWindow = new PopupWindow(
            contentView,
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        );
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE));
        popupWindow.setElevation(dpToPx(8));

        contentView.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int contentHeight = contentView.getMeasuredHeight();

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        android.graphics.Point size = new android.graphics.Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenHeight = size.y;
        int anchorY = location[1];

        int spaceBelow = screenHeight - anchorY - anchorView.getHeight();
        int spaceAbove = anchorY;

        boolean useAbove = contentHeight > spaceBelow && contentHeight < spaceAbove;

        int yOffset;
        if (useAbove) {
            yOffset = -(anchorView.getHeight() + contentHeight + dpToPx(heightOffset));
        } else {
            yOffset = dpToPx(heightOffset);
        }

        popupWindow.showAtLocation(
            anchorView,
            Gravity.TOP | Gravity.START,
            location[0],
            useAbove ? anchorY + yOffset : anchorY + anchorView.getHeight() + yOffset
        );
        return popupWindow;
    }

    private void loadTypeSpinner(VirtualKeysElement element, Spinner spinner, Runnable callback) {
        String[] typeNames = VirtualKeysElement.Type.names();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(lightContext, android.R.layout.simple_spinner_item, typeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(element.getType().ordinal(), false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                element.setType(VirtualKeysElement.Type.values()[position]);
                if (profile != null) profile.save();
                callback.run();
                inputControlsView.invalidate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadShapeSpinner(VirtualKeysElement element, Spinner spinner) {
        String[] shapeNames = VirtualKeysElement.Shape.names();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(lightContext, android.R.layout.simple_spinner_item, shapeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(element.getShape().ordinal(), false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                element.setShape(VirtualKeysElement.Shape.values()[position]);
                if (profile != null) profile.save();
                inputControlsView.invalidate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadRangeSpinner(VirtualKeysElement element, Spinner spinner) {
        String[] rangeNames = VirtualKeysElement.Range.names();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(lightContext, android.R.layout.simple_spinner_item, rangeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (element.getRange() != null) {
            spinner.setSelection(element.getRange().ordinal(), false);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                element.setRange(VirtualKeysElement.Range.values()[position]);
                if (profile != null) profile.save();
                inputControlsView.invalidate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadBindingSpinners(VirtualKeysElement element, LinearLayout container, Runnable onChanged) {
        container.removeAllViews();
        switch (element.getType()) {
            case BUTTON: {
                int slotsToShow = 1;
                for (int i = 1; i < 4; i++) {
                    if (element.getBindingAt(i) != VirtualKeysBinding.NONE) {
                        slotsToShow = i + 1;
                    }
                }
                for (int i = 0; i < slotsToShow; i++) {
                    float density = getResources().getDisplayMetrics().density;
                    LinearLayout row = new LinearLayout(lightContext);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(0, (int) (4 * density), 0, (int) (4 * density));
                    LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    container.addView(row, rowParams);

                    LinearLayout spinnerColumn = new LinearLayout(lightContext);
                    spinnerColumn.setOrientation(LinearLayout.VERTICAL);
                    spinnerColumn.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    row.addView(spinnerColumn);

                    String title = "Key " + (i + 1);
                    loadBindingSpinnerInline(element, spinnerColumn, i, title);

                    if (i > 0) {
                        android.widget.Button removeBtn = new android.widget.Button(lightContext);
                        removeBtn.setText("X");
                        removeBtn.setTextSize(10);
                        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                            (int) (36 * density), (int) (36 * density));
                        btnParams.gravity = android.view.Gravity.CENTER_VERTICAL;
                        btnParams.setMargins((int) (4 * density), 0, 0, 0);
                        removeBtn.setLayoutParams(btnParams);
                        final int idx = i;
                        removeBtn.setOnClickListener(v -> {
                            element.setBindingAt(idx, VirtualKeysBinding.NONE);
                            if (profile != null) profile.save();
                            onChanged.run();
                        });
                        row.addView(removeBtn);
                    }
                }
                final int nextSlot = slotsToShow;
                if (nextSlot < 4) {
                    android.widget.Button addBtn = new android.widget.Button(lightContext);
                    addBtn.setText("+ Add Key");
                    addBtn.setTextSize(12);
                    addBtn.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    addBtn.setOnClickListener(v -> {
                        element.setBindingAt(nextSlot, VirtualKeysBinding.KEY_A);
                        if (profile != null) profile.save();
                        onChanged.run();
                    });
                    container.addView(addBtn);
                }
                break;
            }
            case D_PAD:
            case STICK:
            case TRACKPAD:
                loadBindingSpinner(element, container, 0, "Up");
                loadBindingSpinner(element, container, 1, "Right");
                loadBindingSpinner(element, container, 2, "Down");
                loadBindingSpinner(element, container, 3, "Left");
                break;
        }
    }

    private void loadBindingSpinnerInline(VirtualKeysElement element, LinearLayout container, int index, String title) {
        float density = getResources().getDisplayMetrics().density;

        TextView tvTitle = new TextView(lightContext);
        tvTitle.setText(title);
        tvTitle.setTextSize(12);
        container.addView(tvTitle, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Spinner sBindingType = new Spinner(lightContext);
        String[] typeEntries = {"Keyboard", "Mouse"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(lightContext, android.R.layout.simple_spinner_item, typeEntries);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sBindingType.setAdapter(typeAdapter);
        container.addView(sBindingType, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Spinner sBinding = new Spinner(lightContext);
        container.addView(sBinding, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Runnable update = () -> {
            String[] bindingEntries;
            switch (sBindingType.getSelectedItemPosition()) {
                case 0:
                    bindingEntries = VirtualKeysBinding.keyboardBindingLabels();
                    break;
                case 1:
                    bindingEntries = VirtualKeysBinding.mouseBindingLabels();
                    break;
                default:
                    bindingEntries = VirtualKeysBinding.keyboardBindingLabels();
                    break;
            }
            ArrayAdapter<String> bindingAdapter = new ArrayAdapter<>(lightContext,
                android.R.layout.simple_spinner_item, bindingEntries);
            bindingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sBinding.setAdapter(bindingAdapter);
            setSpinnerSelectionFromValue(sBinding, element.getBindingAt(index).toString());
        };

        sBindingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                update.run();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        VirtualKeysBinding selectedBinding = element.getBindingAt(index);
        int typePosition;
        if (selectedBinding.isKeyboard()) {
            typePosition = 0;
        } else if (selectedBinding.isMouse()) {
            typePosition = 1;
        } else {
            typePosition = 0;
        }
        sBindingType.setSelection(typePosition, false);

        sBinding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VirtualKeysBinding binding;
                switch (sBindingType.getSelectedItemPosition()) {
                    case 0:
                        binding = VirtualKeysBinding.keyboardBindings()[position];
                        break;
                    case 1:
                        binding = VirtualKeysBinding.mouseBindings()[position];
                        break;
                    default:
                        binding = VirtualKeysBinding.NONE;
                        break;
                }
                if (binding != element.getBindingAt(index)) {
                    element.setBindingAt(index, binding);
                    if (profile != null) profile.save();
                    inputControlsView.invalidate();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        update.run();
    }

    private void loadBindingSpinner(VirtualKeysElement element, LinearLayout container, int index, String title) {
        float density = getResources().getDisplayMetrics().density;

        LinearLayout row = new LinearLayout(lightContext);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, (int) (8 * density), 0, (int) (8 * density));
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(row, rowParams);

        TextView tvTitle = new TextView(lightContext);
        tvTitle.setText(title);
        tvTitle.setTextSize(14);
        row.addView(tvTitle, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Spinner sBindingType = new Spinner(lightContext);
        String[] typeEntries = {"Keyboard", "Mouse"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(lightContext, android.R.layout.simple_spinner_item, typeEntries);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sBindingType.setAdapter(typeAdapter);
        row.addView(sBindingType, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Spinner sBinding = new Spinner(lightContext);
        row.addView(sBinding, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Runnable update = new Runnable() {
            @Override
            public void run() {
                String[] bindingEntries;
                switch (sBindingType.getSelectedItemPosition()) {
                    case 0:
                        bindingEntries = VirtualKeysBinding.keyboardBindingLabels();
                        break;
                    case 1:
                        bindingEntries = VirtualKeysBinding.mouseBindingLabels();
                        break;
                    default:
                        bindingEntries = VirtualKeysBinding.keyboardBindingLabels();
                        break;
                }
                ArrayAdapter<String> bindingAdapter = new ArrayAdapter<>(lightContext,
                    android.R.layout.simple_spinner_item, bindingEntries);
                bindingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sBinding.setAdapter(bindingAdapter);
                setSpinnerSelectionFromValue(sBinding, element.getBindingAt(index).toString());
            }
        };

        sBindingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                update.run();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        VirtualKeysBinding selectedBinding = element.getBindingAt(index);
        int typePosition;
        if (selectedBinding.isKeyboard()) {
            typePosition = 0;
        } else if (selectedBinding.isMouse()) {
            typePosition = 1;
        } else {
            typePosition = 0;
        }
        sBindingType.setSelection(typePosition, false);

        sBinding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VirtualKeysBinding binding;
                switch (sBindingType.getSelectedItemPosition()) {
                    case 0:
                        binding = VirtualKeysBinding.keyboardBindings()[position];
                        break;
                    case 1:
                        binding = VirtualKeysBinding.mouseBindings()[position];
                        break;
                    default:
                        binding = VirtualKeysBinding.NONE;
                        break;
                }
                if (binding != element.getBindingAt(index)) {
                    element.setBindingAt(index, binding);
                    if (profile != null) profile.save();
                    inputControlsView.invalidate();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        update.run();
    }

    private void setSpinnerSelectionFromValue(Spinner spinner, String value) {
        android.widget.SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i, false);
                return;
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
