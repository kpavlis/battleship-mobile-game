package com.unipi.kpavlis.real_time_game_v2.Business_Logic;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.kpavlis.real_time_game_v2.Models.User;
import com.unipi.kpavlis.real_time_game_v2.R;

public class StartActivity extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private Button log_in;
    private Button sign_up;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        log_in = findViewById(R.id.login_button);
        sign_up = findViewById(R.id.sign_up_button);
        progressBar = findViewById(R.id.start_progress_ring);

        //Check if the user is already logged in - proceed with fast log in
        if (user != null) {
            ScrollView scrollView = findViewById(R.id.scroll);
            LinearLayout layout = findViewById(R.id.main);
            scrollView.removeView(layout);

            ConstraintLayout constraintLayout = new ConstraintLayout(this);
            ProgressBar progressBar = new ProgressBar(this);
            progressBar.setId(R.id.auto_logIn_progressBar);
            ConstraintLayout.LayoutParams progressBarParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            progressBarParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            progressBarParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            progressBarParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            progressBarParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            constraintLayout.addView(progressBar, progressBarParams);

            //Check if the user has an internet connection
            if(!SystemOperations.internetConnectionAvailability(this)){
                TextView InternetConnectionTextView = new TextView(this);
                InternetConnectionTextView.setText(R.string.log_in_no_internet);
                InternetConnectionTextView.setTextSize(17);
                ConstraintLayout.LayoutParams textViewParams = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
                textViewParams.topToBottom = R.id.auto_logIn_progressBar;
                textViewParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                textViewParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                textViewParams.topMargin = 30;
                constraintLayout.addView(InternetConnectionTextView, textViewParams);
            }

            ConstraintLayout.LayoutParams constraintLayoutParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
            );
            scrollView.addView(constraintLayout, constraintLayoutParams);


            database = FirebaseDatabase.getInstance();
            reference = database.getReference("Users").child(user.getUid());

            //Read the user data from the database and check if the device id is the same as the one in the database
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User app_user = snapshot.getValue(User.class);

                    if (app_user != null) {
                        User.setInstance(app_user);

                        if (User.getInstance().getDevice_id().equals(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))) {
                            Intent intent = new Intent(StartActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            auth.signOut();
                            Toast.makeText(StartActivity.this, getString(R.string.other_device_id), Toast.LENGTH_SHORT).show();
                            scrollView.removeView(constraintLayout);
                            scrollView.addView(layout);

                        }
                    } else {
                        auth.signOut();
                        Toast.makeText(StartActivity.this, getString(R.string.no_user_data), Toast.LENGTH_SHORT).show();
                        scrollView.removeView(constraintLayout);
                        scrollView.addView(layout);
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (!SystemOperations.internetConnectionAvailability(StartActivity.this)) {
                        Toast.makeText(StartActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                        scrollView.removeView(constraintLayout);
                        scrollView.addView(layout);
                    }else {
                        Toast.makeText(StartActivity.this, getString(R.string.no_user_data_read), Toast.LENGTH_SHORT).show();
                    }
                    Log.d("Error", error.getMessage());
                }
            });


        }
    }


    //The classic login procedure
     public void sign_in(View view) {

        EditText email = findViewById(R.id.editText_email);
        EditText password = findViewById(R.id.editText_password);

        //Check it the fields are filled and then proceed with the login
        if(!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            log_in.setEnabled(false);
            sign_up.setEnabled(false);

            //The Firebase Authentication procedure
            auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {//successful log in
                        user = auth.getCurrentUser();
                        database = FirebaseDatabase.getInstance();
                        reference = database.getReference("Users").child(user.getUid());

                        //Read the user data from the database and if that exist check if the device id is the same as the one in the database
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User app_user = snapshot.getValue(User.class);
                                //Log.d("User", app_user.email);
                                //Log.d("Ships", app_user.ships.toString());
                                if (app_user != null) {
                                    User.setInstance(app_user);
                                    if (User.getInstance().getDevice_id().equals("")) {
                                        User.getInstance().setDevice_id(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                                        User.getInstance().updateDeviceID();

                                        Intent intent = new Intent(StartActivity.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else if (User.getInstance().getDevice_id().equals(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))) {
                                        Intent intent = new Intent(StartActivity.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        auth.signOut();
                                        Toast.makeText(StartActivity.this, getString(R.string.other_device_id), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                        log_in.setEnabled(true);
                                        sign_up.setEnabled(true);

                                    }

                                } else {
                                    Toast.makeText(StartActivity.this, getString(R.string.no_user_data_found), Toast.LENGTH_SHORT).show();

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(StartActivity.this, getString(R.string.no_user_data_read), Toast.LENGTH_SHORT).show();

                            }
                        });
                    } else {
                        Toast.makeText(StartActivity.this, getString(R.string.invalid_username_password), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        log_in.setEnabled(true);
                        sign_up.setEnabled(true);
                    }
                }
            });
        }else{
            Toast.makeText(this, getString(R.string.fill_fields), Toast.LENGTH_SHORT).show();
        }
    }

    //Open the sign up activity
    public void sign_up(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);

    }



}