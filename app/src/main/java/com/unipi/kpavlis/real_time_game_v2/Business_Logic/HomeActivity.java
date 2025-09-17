package com.unipi.kpavlis.real_time_game_v2.Business_Logic;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.unipi.kpavlis.real_time_game_v2.R;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);


        // Set up the bottom navigation view with the NavController
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_bar);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);


    }

}