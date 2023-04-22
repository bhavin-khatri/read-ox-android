package com.example.readox.utils;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager{
        // Shared Preferences
        SharedPreferences pref;
        SharedPreferences.Editor editor;

        Context context;
        private static final String KEY_THEME = "customTheme";

    public PreferenceManager(Context context) {
        this.context = context;
    }

    public void setCustomTheme(int theme) {
            editor.putInt(KEY_THEME,theme);
            editor.commit();
        }

        public int getCustomTheme() {
            return pref.getInt(KEY_THEME,0);
        }
    }

