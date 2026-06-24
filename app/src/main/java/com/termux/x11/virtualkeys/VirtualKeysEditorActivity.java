package com.termux.x11.virtualkeys;

import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Arrays;

public class VirtualKeysEditorActivity extends AppCompatActivity implements View.OnClickListener {
    private VirtualKeysView inputControlsView;
    private VirtualKeysProfile profile;

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
        View view = LayoutInflater.from(this).inflate(com.termux.x11.R.layout.virtual_keys_element_settings, null);

        Runnable updateLayout = new Runnable() {
            @Override
            public void run() {
                VirtualKeysElement.Type type = element.getType();
                view.findViewById(com.termux.x11.R.id.LLShape).setVisibility(
                    type == VirtualKeysElement.Type.BUTTON ? View.VISIBLE : View.GONE);
                view.findViewById(com.termux.x11.R.id.CBToggleSwitch).setVisibility(
                    type == VirtualKeysElement.Type.BUTTON ? View.VISIBLE : View.GONE);
                view.findViewById(com.termux.x11.R.id.LLCustomTextIcon).setVisibility(
                    type == VirtualKeysElement.Type.BUTTON ? View.VISIBLE : View.GONE);
                view.findViewById(com.termux.x11.R.id.LLRangeOptions).setVisibility(
                    type == VirtualKeysElement.Type.RANGE_BUTTON ? View.VISIBLE : View.GONE);
                loadBindingSpinners(element, view.findViewById(com.termux.x11.R.id.LLBindings));
            }
        };

        loadTypeSpinner(element, view.findViewById(com.termux.x11.R.id.SType), updateLayout);

        Spinner shapeSpinner = view.findViewById(com.termux.x11.R.id.SShape);
        shapeSpinner.setVisibility(element.getType() == VirtualKeysElement.Type.BUTTON ? View.VISIBLE : View.GONE);
        loadShapeSpinner(element, shapeSpinner);

        Spinner rangeSpinner = view.findViewById(com.termux.x11.R.id.SRange);
        rangeSpinner.setVisibility(element.getType() == VirtualKeysElement.Type.RANGE_BUTTON ? View.VISIBLE : View.GONE);
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

        TextView tvScale = view.findViewById(com.termux.x11.R.id.TVScale);
        SeekBar sbScale = view.findViewById(com.termux.x11.R.id.SBScale);
        sbScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvScale.setText(progress + "%");
                if (fromUser) {
                    element.setScale(progress / 100.0f);
                    if (profile != null) profile.save();
                    inputControlsView.invalidate();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        sbScale.setProgress((int) (element.getScale() * 100));

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

        LinearLayout llIconList = view.findViewById(com.termux.x11.R.id.LLIconList);
        llIconList.setVisibility(element.getType() == VirtualKeysElement.Type.BUTTON ? View.VISIBLE : View.GONE);
        loadIcons(llIconList, element.getIconId());

        updateLayout.run();

        PopupWindow popupWindow = showPopupWindow(anchorView, view, dpToPx(340), 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                String text = etCustomText.getText().toString().trim();
                byte iconId = 0;
                for (int i = 0; i < llIconList.getChildCount(); i++) {
                    View child = llIconList.getChildAt(i);
                    if (child.isSelected()) {
                        Object tag = child.getTag();
                        if (tag instanceof Byte) {
                            iconId = (Byte) tag;
                        } else if (tag instanceof Integer) {
                            iconId = ((Integer) tag).byteValue();
                        }
                        break;
                    }
                }
                element.setText(text);
                element.setIconId(iconId);
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

        contentView.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int contentHeight = contentView.getMeasuredHeight();

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        int screenHeight = getWindow().getAttributes().height;
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
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeNames));
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
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, shapeNames));
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
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rangeNames));
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

    private void loadBindingSpinners(VirtualKeysElement element, LinearLayout container) {
        container.removeAllViews();
        switch (element.getType()) {
            case BUTTON:
                loadBindingSpinner(element, container, 0, "Binding");
                break;
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

    private void loadBindingSpinner(VirtualKeysElement element, LinearLayout container, int index, String title) {
        float density = getResources().getDisplayMetrics().density;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, (int) (8 * density), 0, (int) (8 * density));
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(row, rowParams);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(14);
        row.addView(tvTitle, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Spinner sBindingType = new Spinner(this);
        String[] typeEntries = {"Keyboard", "Mouse", "Gamepad"};
        sBindingType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeEntries));
        row.addView(sBindingType, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Spinner sBinding = new Spinner(this);
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
                    case 2:
                        bindingEntries = VirtualKeysBinding.gamepadBindingLabels();
                        break;
                    default:
                        bindingEntries = VirtualKeysBinding.keyboardBindingLabels();
                        break;
                }
                sBinding.setAdapter(new ArrayAdapter<>(VirtualKeysEditorActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, bindingEntries));
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
        } else if (selectedBinding.isGamepad()) {
            typePosition = 2;
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
                    case 2:
                        binding = VirtualKeysBinding.gamepadBindings()[position];
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

    private void loadIcons(LinearLayout parent, byte selectedId) {
        byte[] iconIds = new byte[0];
        try {
            String[] filenames = getAssets().list("inputcontrols/icons/");
            if (filenames != null) {
                iconIds = new byte[filenames.length];
                for (int i = 0; i < filenames.length; i++) {
                    String name = filenames[i];
                    int dotIndex = name.indexOf('.');
                    String numPart = dotIndex > 0 ? name.substring(0, dotIndex) : name;
                    try {
                        iconIds[i] = Byte.parseByte(numPart);
                    } catch (NumberFormatException e) {
                        iconIds[i] = 0;
                    }
                }
            }
        } catch (IOException e) {
        }

        Arrays.sort(iconIds);

        float density = getResources().getDisplayMetrics().density;
        int size = (int) (40 * density);
        int margin = (int) (2 * density);
        int padding = (int) (4 * density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, 0, margin, 0);

        GradientDrawable iconBackground = new GradientDrawable();
        iconBackground.setShape(GradientDrawable.RECTANGLE);
        iconBackground.setCornerRadius(4 * density);
        iconBackground.setStroke((int) (1 * density), 0xFF888888);
        iconBackground.setColor(0x33FFFFFF);

        for (byte id : iconIds) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params);
            imageView.setPadding(padding, padding, padding, padding);
            imageView.setBackground(iconBackground);
            imageView.setTag(id);
            imageView.setSelected(id == selectedId);

            final ImageView iv = imageView;
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        View child = parent.getChildAt(i);
                        child.setSelected(false);
                        child.setScaleX(1.0f);
                        child.setScaleY(1.0f);
                    }
                    iv.setSelected(true);
                    iv.setScaleX(1.2f);
                    iv.setScaleY(1.2f);
                }
            });

            try {
                java.io.InputStream is = getAssets().open("inputcontrols/icons/" + id + ".png");
                imageView.setImageBitmap(BitmapFactory.decodeStream(is));
                is.close();
            } catch (IOException e) {
            }

            parent.addView(imageView);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
