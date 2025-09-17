package com.unipi.kpavlis.real_time_game_v2.Business_Logic;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.unipi.kpavlis.real_time_game_v2.Models.User;
import com.unipi.kpavlis.real_time_game_v2.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpActivity extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private Button sign_up;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set a specific behavior for the back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        sign_up = findViewById(R.id.sign_up_button);
        progressBar = findViewById(R.id.sign_up_progress_ring);
    }

    // This method is called when the user clicks the sign-up button and submits the registration form
    public void submit_sign_up(View view) {

        EditText emailText = findViewById(R.id.editTextEmailAddress);
        EditText passwordText = findViewById(R.id.editTextPassword2);
        EditText firstNameText = findViewById(R.id.editTextFirstName);
        EditText surNameText = findViewById(R.id.editTextSurName);
        //Initialization of the ships positions
        List<String> ships_positions = Arrays.asList("5969", "778797", "08182838", "2232425262", "253545556575");

        //Check if the required fields are filled and the proceed with the registration
        if (!emailText.getText().toString().isEmpty()
                && !firstNameText.getText().toString().isEmpty()
                && !surNameText.getText().toString().isEmpty()
                && !passwordText.getText().toString().isEmpty()){
            sign_up.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            //Register user using Firebase Authentication service
            auth.createUserWithEmailAndPassword(emailText.getText().toString(),passwordText.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>(){

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task){
                            if(task.isSuccessful()){
                                user = auth.getCurrentUser();
                                String uid = user.getUid();
                                //If the user is registered successfully to the Authentication service, we proceed to register the user in the Firebase Realtime Database
                                User app_user = new User(uid,firstNameText.getText().toString(), surNameText.getText().toString(), emailText.getText().toString(), ships_positions, 1, 100, 0, 0, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), 0.0);
                                register_user_to_db(uid,firstNameText.getText().toString(), surNameText.getText().toString(), emailText.getText().toString(), ships_positions, app_user, 1, 100,0,0, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), 0.0);

                            }
                            else{
                                Toast.makeText(SignUpActivity.this, getString(R.string.failed_registration), Toast.LENGTH_SHORT).show();
                                sign_up.setEnabled(true);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        }else{
            Toast.makeText(SignUpActivity.this, getString(R.string.fill_fields), Toast.LENGTH_SHORT).show();
        }

    }

    // This method is called to register the user in the Firebase Realtime Database, creating two new nodes for each user
    public void register_user_to_db(String user_id,String first_name,String last_name,String email, List<String> ships, User app_user, int level, int points, int wins, int losses, String device_id, double win_rate){
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        //The User node contains the user's data
        Map<String,Object> newUser = new HashMap<>();
        newUser.put("user_id",user_id);
        newUser.put("first_name",first_name);
        newUser.put("last_name",last_name);
        newUser.put("email",email);
        newUser.put("ships", ships);
        newUser.put("level",level);
        newUser.put("points",points);
        newUser.put("wins",wins);
        newUser.put("losses",losses);
        newUser.put("device_id",device_id);
        newUser.put("win_rate",win_rate);

        //The Game node contains the user's game data
        Map<String,Object> newUser_Game = new HashMap<>();
        newUser_Game.put("is_available",false);
        newUser_Game.put("player_1",user_id);
        newUser_Game.put("player_2","");
        newUser_Game.put("player_1_message","");
        newUser_Game.put("player_2_message","");
        newUser_Game.put("player_1_response","");
        newUser_Game.put("player_2_response","");
        newUser_Game.put("request_response","");

        //Adding the nodes to the database
        reference.child("Users").child(user_id).setValue(newUser).addOnCompleteListener(
                task_add_user -> {
                    if(task_add_user.isSuccessful()){
                        reference.child("Games").child(user_id).setValue(newUser_Game).addOnCompleteListener(
                                task_add_user_game -> {
                                    if(task_add_user_game.isSuccessful()){
                                        //Since the user is registered successfully to the database, we proceed to set the user instance in the User class
                                        User.setInstance(app_user);

                                        //Proceed to the HomeActivity the registration is successful
                                        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                    }else{

                                        reference.child("Users").child("/" + user_id).removeValue().addOnCompleteListener(
                                                task_remove_user -> {
                                                    if (!task_remove_user.isSuccessful()) {
                                                        Log.e("SignUpActivity", "Failed to remove user from database" );
                                                    }
                                                }
                                        );
                                        // Delete the user data from authentication service if sth goes wrong with the sign up
                                        if (user != null) {
                                            user.delete().addOnCompleteListener(deleteTask -> {
                                                if (!deleteTask.isSuccessful()) {
                                                    Log.e("SignUpActivity", "Failed to delete user from authentication service");
                                                }
                                            });
                                        }
                                        Toast.makeText(SignUpActivity.this, getString(R.string.failed_registration), Toast.LENGTH_SHORT).show();
                                        sign_up.setEnabled(true);
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                        );


                    } else {
                        // Delete the user data from authentication service if something goes wrong with the sign up
                        if (user != null) {
                            user.delete().addOnCompleteListener(deleteTask -> {
                                if (!deleteTask.isSuccessful()) {
                                    Log.e("SignUpActivity", "Failed to delete user from authentication service");
                                }
                            });
                        }
                        Toast.makeText(SignUpActivity.this, getString(R.string.failed_registration), Toast.LENGTH_SHORT).show();
                        sign_up.setEnabled(true);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
        );

    }
}