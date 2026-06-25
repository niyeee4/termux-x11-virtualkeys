package com.termux.x11.virtualkeys;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class VirtualKeysProfile {
    private final Context context;
    private String name;
    private final ArrayList<VirtualKeysElement> elements = new ArrayList<>();
    private boolean elementsLoaded = false;
    private VirtualKeysView view;
    private int cachedMaxWidth = 0;
    private int cachedMaxHeight = 0;
    private JSONArray elementsJson;

    public VirtualKeysProfile(Context context, String name) {
        this.context = context;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<VirtualKeysElement> getElements() {
        return elements;
    }

    public boolean isElementsLoaded() {
        return elementsLoaded;
    }

    public void setView(VirtualKeysView view) {
        this.view = view;
    }

    public void addElement(VirtualKeysElement element) {
        elements.add(element);
    }

    public void removeElement(VirtualKeysElement element) {
        elements.remove(element);
    }

    public void loadElements(VirtualKeysView view) {
        this.view = view;
        cachedMaxWidth = view.getMaxWidth();
        cachedMaxHeight = view.getMaxHeight();
        elements.clear();

        if (elementsJson == null) {
            elementsLoaded = true;
            return;
        }

        try {
            for (int i = 0; i < elementsJson.length(); i++) {
                JSONObject elementJson = elementsJson.getJSONObject(i);
                VirtualKeysElement element = new VirtualKeysElement(view);
                element.setType(VirtualKeysElement.Type.valueOf(elementJson.getString("type")));
                element.setShape(VirtualKeysElement.Shape.valueOf(elementJson.getString("shape")));
                element.setScale((float) elementJson.getDouble("scale"));
                double ratioX = elementJson.getDouble("x");
                double ratioY = elementJson.getDouble("y");
                element.setRatio(ratioX, ratioY);
                element.setX((int) (ratioX * cachedMaxWidth));
                element.setY((int) (ratioY * cachedMaxHeight));
                element.setToggleSwitch(elementJson.getBoolean("toggleSwitch"));
                element.setText(elementJson.optString("text", ""));
                element.setIconId((byte) elementJson.optInt("iconId", 0));

                if (elementJson.has("range")) {
                    element.setRange(VirtualKeysElement.Range.fromString(elementJson.getString("range")));
                }
                if (elementJson.has("orientation")) {
                    element.setOrientation((byte) elementJson.getInt("orientation"));
                }
                if (elementJson.has("columns")) {
                    element.setColumns(elementJson.getInt("columns"));
                }

                JSONArray bindingsArray = elementJson.getJSONArray("bindings");
                for (int j = 0; j < bindingsArray.length(); j++) {
                    element.setBindingAt(j, VirtualKeysBinding.fromString(bindingsArray.getString(j)));
                }

                elements.add(element);
            }
            elementsLoaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        elements.clear();
        elementsJson = null;
        elementsLoaded = false;
    }

    public void save() {
        File file = getFile();
        file.getParentFile().mkdirs();

        try {
            JSONObject data = new JSONObject();
            data.put("name", name);

            JSONArray elementsArray = new JSONArray();
            for (VirtualKeysElement element : elements) {
                elementsArray.put(element.toJSONObject());
            }
            data.put("elements", elementsArray);

            OutputStream os = new java.io.FileOutputStream(file);
            os.write(data.toString().getBytes(StandardCharsets.UTF_8));
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fromJSON(String jsonString) throws Exception {
        JSONObject json = new JSONObject(jsonString);
        name = json.optString("name", name);
        elementsJson = json.getJSONArray("elements");
        elements.clear();
        for (int i = 0; i < elementsJson.length(); i++) {
            JSONObject el = elementsJson.getJSONObject(i);
            VirtualKeysElement element = new VirtualKeysElement();
            element.setType(VirtualKeysElement.Type.valueOf(el.getString("type")));
            element.setShape(VirtualKeysElement.Shape.valueOf(el.getString("shape")));
            element.setScale((float) el.getDouble("scale"));
            double ratioX = el.getDouble("x");
            double ratioY = el.getDouble("y");
            element.setRatio(ratioX, ratioY);
            if (view != null) {
                element.setX((int) (ratioX * view.getMaxWidth()));
                element.setY((int) (ratioY * view.getMaxHeight()));
            }
            element.setToggleSwitch(el.getBoolean("toggleSwitch"));
            element.setText(el.optString("text", ""));
            element.setIconId((byte) el.optInt("iconId", 0));
            if (el.has("range")) {
                element.setRange(VirtualKeysElement.Range.fromString(el.getString("range")));
            }
            if (el.has("orientation")) {
                element.setOrientation((byte) el.getInt("orientation"));
            }
            if (el.has("columns")) {
                element.setColumns(el.getInt("columns"));
            }
            JSONArray bindingsArray = el.getJSONArray("bindings");
            for (int j = 0; j < bindingsArray.length(); j++) {
                element.setBindingAt(j, VirtualKeysBinding.fromString(bindingsArray.getString(j)));
            }
            elements.add(element);
        }
        elementsLoaded = true;
    }

    public JSONObject toJSONObject() throws Exception {
        JSONObject json = new JSONObject();
        json.put("name", name);
        JSONArray elementsArray = new JSONArray();
        for (VirtualKeysElement element : elements) {
            elementsArray.put(element.toJSONObject());
        }
        json.put("elements", elementsArray);
        return json;
    }

    public static VirtualKeysProfile fromJSON(Context context, String name, JSONObject json) throws Exception {
        String actualName = json.optString("name", name);
        VirtualKeysProfile profile = new VirtualKeysProfile(context, actualName);
        JSONArray elementsArray = json.getJSONArray("elements");
        profile.elementsJson = elementsArray;
        for (int i = 0; i < elementsArray.length(); i++) {
            JSONObject el = elementsArray.getJSONObject(i);
            VirtualKeysElement element = new VirtualKeysElement();
            element.setType(VirtualKeysElement.Type.valueOf(el.getString("type")));
            element.setShape(VirtualKeysElement.Shape.valueOf(el.getString("shape")));
            element.setScale((float) el.getDouble("scale"));
            double ratioX = el.getDouble("x");
            double ratioY = el.getDouble("y");
            element.setRatio(ratioX, ratioY);
            element.setToggleSwitch(el.getBoolean("toggleSwitch"));
            element.setText(el.optString("text", ""));
            element.setIconId((byte) el.optInt("iconId", 0));
            if (el.has("range")) {
                element.setRange(VirtualKeysElement.Range.fromString(el.getString("range")));
            }
            if (el.has("orientation")) {
                element.setOrientation((byte) el.getInt("orientation"));
            }
            if (el.has("columns")) {
                element.setColumns(el.getInt("columns"));
            }
            JSONArray bindingsArray = el.getJSONArray("bindings");
            for (int j = 0; j < bindingsArray.length(); j++) {
                element.setBindingAt(j, VirtualKeysBinding.fromString(bindingsArray.getString(j)));
            }
            profile.elements.add(element);
        }
        profile.elementsLoaded = true;
        return profile;
    }

    public void delete() {
        File file = getFile();
        if (file.exists()) {
            file.delete();
        }
    }

    public File getFile() {
        return new File(context.getFilesDir(), "virtualkeys/" + name + ".icp");
    }

    public boolean validate() {
        if (elements.isEmpty()) return false;
        for (VirtualKeysElement e : elements) {
            if (e.getType() == null) return false;
            if (e.getBindings() == null || e.getBindings().length == 0) return false;
        }
        return true;
    }

    public static VirtualKeysProfile load(Context context, String name) throws Exception {
        File file = new File(context.getFilesDir(), "virtualkeys/" + name + ".icp");
        if (!file.exists()) {
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        JSONObject json = new JSONObject(sb.toString());
        return fromJSON(context, name, json);
    }

    public static VirtualKeysProfile loadFromUri(Context context, String name, InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        JSONObject json = new JSONObject(sb.toString());
        return fromJSON(context, name, json);
    }

    @Override
    public String toString() {
        return name;
    }
}
