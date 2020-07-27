package com.specifikarma.optimumklus.UI.contact;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.specifikarma.optimumklus.R;
import com.specifikarma.optimumklus.UI.contact.importVCF.AndroidCustomFieldScribe;
import com.specifikarma.optimumklus.UI.contact.importVCF.ContactOperations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import pl.droidsonroids.gif.GifImageButton;

public class AddContact extends Fragment {
    private GifImageButton contact;
    private boolean isOpened;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private List<String> permissionsList;

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadPreferences();
        requiredPermissions();

        contact = view.findViewById(R.id.contact);

        if (permissionsList.size() == 0) contact.setBackgroundResource(R.drawable.contact_static);

        if (!isOpened) {
            contact.post(new Runnable() {
                @Override
                public void run() {
                    contact.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(getContext())
                                    .setView(getLayoutInflater().inflate(R.layout.contact_dialog, null))
                                    .setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setPositiveButton(permissionsList.size() != 0 ? getString(R.string.next) + " ➤" : getString(R.string.find_me), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (permissionsList.size() != 0) {
                                                        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                                                        requiredPermissions();
                                                    }
                                                }
                                            }.run();
                                        }
                                    }).create().show();
                        }
                    });
                }
            });
        } else {
            contact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted

                    if (!isOpened) {
                        Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                        copyAssets("Klusjesman_Ivan.vcf");
                        importCard();
                        savePreferences("IS_OPENED", true);
                        contact.setBackgroundResource(R.drawable.contact_static);
                        contact.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    // Permission Denied
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requiredPermissions() {
        permissionsList = new ArrayList<String>();
        checkPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE);
        checkPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        checkPermission(permissionsList, Manifest.permission.WRITE_CONTACTS);
    }

    private boolean checkPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    private void loadPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        isOpened = sharedPreferences.getBoolean("IS_OPENED", false);
    }

    private void savePreferences(String KEY, boolean status) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPreferences.edit();
        editor.putBoolean(KEY, status);
        editor.apply();
    }

    private void importCard() {
        File vcardFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/contactCard/" + "Klusjesman_Ivan.vcf");

        VCardReader reader = null;
        try {
            reader = new VCardReader(vcardFile);
            reader.registerScribe(new AndroidCustomFieldScribe());

            ContactOperations operations = new ContactOperations(getContext());
            VCard vcard = null;
            while ((vcard = reader.readNext()) != null) {
                operations.insertContact(vcard);
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(getContext(), "FileNotFoundException", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(getContext(), "IOException", Toast.LENGTH_SHORT).show();
        } catch (OperationApplicationException e) {
            Toast.makeText(getContext(), "OperationApplicationException", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (RemoteException e) {
            Toast.makeText(getContext(), "RemoteException", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                Toast.makeText(getContext(), "IOException", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void copyAssets(String filename) {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/contactCard";
        File dir = new File(dirPath);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        AssetManager assetManager = getContext().getAssets();

        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open(filename);
            File outFile = new File(dirPath, filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);


        } catch (IOException e) {
            Toast.makeText(getContext(), "FAIL", Toast.LENGTH_SHORT).show();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}