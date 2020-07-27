package com.gmail.specifikarma.optimumklus.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gmail.specifikarma.optimumklus.UI.form.Form;
import com.gmail.specifikarma.optimumklus.networking.Settings;
import com.gmail.specifikarma.optimumklus.networking.Util;

public class Preferences {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;


    public static boolean load(String key, Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, false);
    }

    public static Form loadForm(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Util.getGsonParser().fromJson(sharedPreferences.getString("FORM", ""), Form.class);
    }

    public static Settings loadSettings(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Util.getGsonParser().fromJson(sharedPreferences.getString("SETTINGS", ""), Settings.class);
    }

    public static void save(String key, boolean text, Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
        editor.putBoolean(key, text);
        editor.apply();
    }

    public static void save(String key, String text, Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
        editor.putString(key, text);
        editor.apply();
    }
}
