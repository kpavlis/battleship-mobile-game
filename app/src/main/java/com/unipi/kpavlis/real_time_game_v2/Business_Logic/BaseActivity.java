package com.unipi.kpavlis.real_time_game_v2.Business_Logic;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        //Set the application display language when each activity starts
        super.attachBaseContext(SystemOperations.setLocale(base, SystemOperations.getLanguage(base)));

    }


}
