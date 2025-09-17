package com.unipi.kpavlis.real_time_game_v2.Business_Logic;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class SystemOperations {
    private static SharedPreferences preferences;
    private static final String location_tag = "display_language";

    // Check if there is internet connection currently available
    public static boolean internetConnectionAvailability(Activity activity) {
        ConnectivityManager connectivity_Manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities network_Capabilities = connectivity_Manager.getNetworkCapabilities(connectivity_Manager.getActiveNetwork());
        if (network_Capabilities == null) {
            return false;
        }
        return network_Capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

    }

    //Update the shared preferences with the selected language
    public static void set_display_language(Activity activity, String selected_language) {
        preferences = activity.getSharedPreferences(location_tag, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(location_tag, selected_language);
        editor.apply();

        activity.recreate();

    }

    //Sets the new locale / display language for the application
    public static Context setLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        config.setLocale(locale);
        context = context.createConfigurationContext(config);

        return context;
    }

    //Get the selected language from the shared preferences
    public static String getLanguage(Context context) {
        preferences = context.getSharedPreferences("display_language", Context.MODE_PRIVATE);
        String selected_language = preferences.getString("display_language", "empty");

        Log.d("SystemOperations", "Selected language: " + selected_language);

        if(!selected_language.equals("empty")) {

            return selected_language;

        }
        return context.getResources().getConfiguration().getLocales().get(0).getLanguage();
    }

    //Set the theme of the application
    public static void set_Appearance_Theme(int theme_code, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("theme_code", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        if(theme_code == 0) {
            editor.putInt("theme_code", 0);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            Log.d("DEFAULT_THEME", "Default theme selected");
        } else if(theme_code == 1) {
            editor.putInt("theme_code", 1);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Log.d("LIGHT_THEME", "Light theme selected");
        } else if(theme_code == 2) {
            editor.putInt("theme_code", 2);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Log.d("DARK_THEME", "Dark theme selected");
        }

        editor.apply();


    }

    //Get the theme of the application from the shared preferences
    public static int get_Appearance_Theme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("theme_code", Context.MODE_PRIVATE);
        return preferences.getInt("theme_code", 0);
    }





}
