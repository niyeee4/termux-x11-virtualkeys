package com.termux.x11.virtualkeys;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.util.ArrayList;

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
            importLauncher.launch(intent);
        });

        view.findViewById(R.id.vk_bt_export).setOnClickListener(v -> {
            if (currentProfile == null) {
                Toast.makeText(getContext(), "No profile selected", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE, currentProfile.getName() + ".icp");
            exportLauncher.launch(intent);
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
