package com.termux.x11.virtualkeys;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.termux.x11.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.json.JSONObject;

public class VirtualKeysFragment extends Fragment {
    private VirtualKeysManager manager;
    private VirtualKeysProfile currentProfile;
    private Spinner sProfile;

    private static final String PREF_CURRENT_PROFILE = "vk_current_profile";

    private ActivityResultLauncher<Intent> importLauncher;
    private ActivityResultLauncher<Intent> exportLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = new VirtualKeysManager(requireContext());

        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            VirtualKeysProfile imported = manager.importProfile(result.getData().getData());
                            if (imported != null) {
                                currentProfile = imported;
                                saveCurrentProfile();
                                loadProfileSpinner();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && currentProfile != null) {
                        try {
                            manager.exportProfile(currentProfile, result.getData().getData());
                            Toast.makeText(getContext(), "Export successful", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.virtual_keys_fragment, container, false);
        sProfile = view.findViewById(R.id.vk_s_profile);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String profileName = prefs.getString(PREF_CURRENT_PROFILE, null);
        if (profileName != null) {
            for (VirtualKeysProfile p : manager.getProfiles()) {
                if (p.getName().equals(profileName)) {
                    currentProfile = p;
                    break;
                }
            }
        }

        loadProfileSpinner();

        view.findViewById(R.id.vk_bt_add_profile).setOnClickListener(v -> {
            showInputDialog("Profile name", null, name -> {
                currentProfile = manager.createProfile(name);
                saveCurrentProfile();
                loadProfileSpinner();
            });
        });

        view.findViewById(R.id.vk_bt_rename_profile).setOnClickListener(v -> {
            if (currentProfile == null) {
                Toast.makeText(getContext(), "No profile selected", Toast.LENGTH_SHORT).show();
                return;
            }
            showInputDialog("New name", currentProfile.getName(), name -> {
                try {
                    manager.renameProfile(currentProfile, name);
                    loadProfileSpinner();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Rename failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        view.findViewById(R.id.vk_bt_duplicate_profile).setOnClickListener(v -> {
            if (currentProfile == null) {
                Toast.makeText(getContext(), "No profile selected", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                currentProfile = manager.duplicateProfile(currentProfile, currentProfile.getName() + "_copy");
                saveCurrentProfile();
                loadProfileSpinner();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Duplicate failed", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.vk_bt_delete_profile).setOnClickListener(v -> {
            if (currentProfile == null) {
                Toast.makeText(getContext(), "No profile selected", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete profile")
                    .setMessage("Delete '" + currentProfile.getName() + "'?")
                    .setPositiveButton("Delete", (d, which) -> {
                        manager.deleteProfile(currentProfile);
                        currentProfile = manager.getProfiles().isEmpty() ? null : manager.getProfiles().get(0);
                        saveCurrentProfile();
                        loadProfileSpinner();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        view.findViewById(R.id.vk_bt_import).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Uri x11ProfilesUri = DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    "primary:Download/x11profiles");
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, x11ProfilesUri);
            }
            importLauncher.launch(intent);
        });

        view.findViewById(R.id.vk_bt_export).setOnClickListener(v -> {
            if (currentProfile == null) {
                Toast.makeText(getContext(), "No profile selected", Toast.LENGTH_SHORT).show();
                return;
            }
            exportProfile(currentProfile);
        });

        view.findViewById(R.id.vk_bt_open_editor).setOnClickListener(v -> {
            if (currentProfile == null) {
                Toast.makeText(getContext(), "No profile selected", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), VirtualKeysEditorActivity.class);
            intent.putExtra("profile_name", currentProfile.getName());
            startActivity(intent);
        });

        return view;
    }

    private void exportProfile(VirtualKeysProfile profile) {
        byte[] bytes;
        String name;
        try {
            JSONObject json = profile.toJSONObject();
            if (json == null) throw new Exception("null JSON");
            bytes = json.toString(2).getBytes(StandardCharsets.UTF_8);
            name = profile.getName();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Export failed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try direct /sdcard/Download/x11profiles/ first
        try {
            File profilesDir = new File(Environment.getExternalStorageDirectory(), "Download/x11profiles");
            profilesDir.mkdirs();
            String filename = resolveFilename(profilesDir, name, ".icp");
            File outFile = new File(profilesDir, filename);
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(bytes);
            fos.close();
            Toast.makeText(getContext(), "Exported to /sdcard/Download/x11profiles/" + filename,
                Toast.LENGTH_SHORT).show();
            return;
        } catch (Exception ignored) {}

        // Fall back to MediaStore with valid RELATIVE_PATH "Download/x11profiles"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (exportViaMediaStore(bytes, name))
                return;
        }

        // Last resort: SAF
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, name + ".icp");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri x11ProfilesUri = DocumentsContract.buildDocumentUri(
                "com.android.externalstorage.documents",
                "primary:Download/x11profiles");
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, x11ProfilesUri);
        }
        exportLauncher.launch(intent);
    }

    private String resolveFilename(File dir, String baseName, String extension) {
        String filename = baseName + extension;
        File f = new File(dir, filename);
        int counter = 1;
        while (f.exists()) {
            filename = baseName + " (" + counter + ")" + extension;
            f = new File(dir, filename);
            counter++;
        }
        return filename;
    }

    private boolean exportViaMediaStore(byte[] bytes, String name) {
        String ext = ".icp";
        for (int counter = 0; counter < 1000; counter++) {
            try {
                String displayName = counter == 0
                    ? name + ext
                    : name + " (" + counter + ")" + ext;

                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, displayName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/x11profiles");
                }
                Uri uri = requireContext().getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null)
                    continue;

                // Check if MediaStore auto-renamed (e.g. name.icp → name.icp (1))
                try (Cursor c = requireContext().getContentResolver().query(
                        uri, new String[]{MediaStore.Downloads.DISPLAY_NAME},
                        null, null, null)) {
                    if (c != null && c.moveToFirst()) {
                        String actualName = c.getString(0);
                        if (!actualName.equals(displayName)) {
                            requireContext().getContentResolver().delete(uri, null, null);
                            continue;
                        }
                    }
                }

                try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                    if (os != null)
                        os.write(bytes);
                }
                Toast.makeText(getContext(), "Exported to Download/x11profiles/" + displayName,
                    Toast.LENGTH_SHORT).show();
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private void loadProfileSpinner() {
        ArrayList<String> names = new ArrayList<>();
        int selected = 0;
        ArrayList<VirtualKeysProfile> profiles = manager.getProfiles();
        for (int i = 0; i < profiles.size(); i++) {
            names.add(profiles.get(i).getName());
            if (profiles.get(i) == currentProfile) selected = i;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
        sProfile.setAdapter(adapter);
        sProfile.setSelection(selected, false);
        sProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < profiles.size()) {
                    currentProfile = profiles.get(position);
                    saveCurrentProfile();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void saveCurrentProfile() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.edit().putString(PREF_CURRENT_PROFILE, currentProfile != null ? currentProfile.getName() : null).apply();
    }

    private void showInputDialog(String title, String initialValue, java.util.function.Consumer<String> onResult) {
        EditText editText = new EditText(requireContext());
        editText.setText(initialValue);
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(editText)
                .setPositiveButton("OK", (d, which) -> {
                    String value = editText.getText().toString().trim();
                    if (!value.isEmpty()) onResult.accept(value);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
