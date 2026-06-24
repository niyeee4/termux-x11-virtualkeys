package com.termux.x11.virtualkeys;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class VirtualKeysManager {
    private Context context;
    private ArrayList<VirtualKeysProfile> profiles = new ArrayList<>();
    private VirtualKeysProfile currentProfile = null;

    public VirtualKeysManager(Context context) {
        this.context = context;
        ensureDirectoryExists();
        scanProfiles();
    }

    private void ensureDirectoryExists() {
        File dir = new File(context.getFilesDir(), "virtualkeys");
        if (!dir.exists()) dir.mkdirs();
    }

    public ArrayList<VirtualKeysProfile> getProfiles() {
        return profiles;
    }

    public VirtualKeysProfile getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(VirtualKeysProfile profile) {
        this.currentProfile = profile;
    }

    public void scanProfiles() {
        profiles.clear();
        File dir = new File(context.getFilesDir(), "virtualkeys");
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".icp"));
        if (files != null) {
            for (File f : files) {
                String name = f.getName().substring(0, f.getName().length() - 4);
                try {
                    VirtualKeysProfile p = VirtualKeysProfile.load(context, name);
                    if (p != null) {
                        profiles.add(p);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    public VirtualKeysProfile createProfile(String name) {
        VirtualKeysProfile p = new VirtualKeysProfile(context, name);
        p.save();
        profiles.add(p);
        return p;
    }

    public VirtualKeysProfile duplicateProfile(VirtualKeysProfile source, String newName) throws Exception {
        File sourceFile = source.getFile();
        File newFile = new File(context.getFilesDir(), "virtualkeys/" + newName + ".icp");
        copyFile(sourceFile, newFile);
        VirtualKeysProfile p = VirtualKeysProfile.load(context, newName);
        profiles.add(p);
        return p;
    }

    public void renameProfile(VirtualKeysProfile profile, String newName) throws Exception {
        File oldFile = profile.getFile();
        File newFile = new File(context.getFilesDir(), "virtualkeys/" + newName + ".icp");
        if (oldFile.exists()) {
            oldFile.renameTo(newFile);
        }
        profile.setName(newName);
        profile.save();
    }

    public void deleteProfile(VirtualKeysProfile profile) {
        profile.delete();
        profiles.remove(profile);
        if (currentProfile == profile) {
            currentProfile = null;
        }
    }

    public VirtualKeysProfile importProfile(Uri uri) throws Exception {
        InputStream is = context.getContentResolver().openInputStream(uri);
        if (is == null) throw new IOException("Cannot open input stream");
        String name = "imported_" + System.currentTimeMillis();
        VirtualKeysProfile profile = VirtualKeysProfile.loadFromUri(context, name, is);
        is.close();
        if (!profile.validate()) {
            throw new IOException("Invalid profile: no valid elements found");
        }
        File targetFile = new File(context.getFilesDir(), "virtualkeys/" + name + ".icp");
        targetFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(targetFile);
        fos.write(profile.toJSONObject().toString(2).getBytes(StandardCharsets.UTF_8));
        fos.close();
        profiles.add(profile);
        return profile;
    }

    public void exportProfile(VirtualKeysProfile profile, Uri uri) throws Exception {
        if (profile == null || profile.getElements().isEmpty()) {
            throw new IOException("Nothing to export: profile is empty");
        }
        OutputStream os = context.getContentResolver().openOutputStream(uri);
        if (os == null) throw new IOException("Cannot open output stream");
        String content = profile.toJSONObject().toString(2);
        os.write(content.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    public void deployDefaultProfiles() {
        File dir = new File(context.getFilesDir(), "virtualkeys");
        String[] defaults = {"default"};
        for (String name : defaults) {
            File f = new File(dir, name + ".icp");
            if (!f.exists()) {
                try {
                    InputStream is = context.getAssets().open("virtualkeys/" + name + ".icp");
                    OutputStream os = new FileOutputStream(f);
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = is.read(buffer)) > 0) {
                        os.write(buffer, 0, read);
                    }
                    is.close();
                    os.close();
                } catch (Exception e) {
                    try {
                        VirtualKeysProfile p = new VirtualKeysProfile(context, name);
                        p.save();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private void copyFile(File src, File dst) throws IOException {
        FileInputStream is = new FileInputStream(src);
        FileOutputStream os = new FileOutputStream(dst);
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) > 0) {
            os.write(buf, 0, n);
        }
        is.close();
        os.close();
    }
}
