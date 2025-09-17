package com.unipi.kpavlis.real_time_game_v2;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.unipi.kpavlis.real_time_game_v2.Business_Logic.SystemOperations;

public class App extends Application {

    //To be checked again
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(SystemOperations.setLocale(base, SystemOperations.getLanguage(base)));

        // Set the application theme based on the user's preference when the app starts
        int theme_code = SystemOperations.get_Appearance_Theme(base);
        if(theme_code == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if(theme_code == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if(theme_code == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

    }



}
