package com.unipi.kpavlis.real_time_game_v2.Business_Logic;

import static java.lang.Character.getNumericValue;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.kpavlis.real_time_game_v2.Models.Opponent_User;
import com.unipi.kpavlis.real_time_game_v2.Models.User;
import com.unipi.kpavlis.real_time_game_v2.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import android.media.MediaPlayer;

public class BattleshipActivity extends BaseActivity {

    private FirebaseDatabase database;

    private GridLayout action_game_grid;
    private GridLayout secondary_game_grid;
    private LinearLayout active_player;
    private LinearLayout inactive_player;
    private LinearLayout opponent_responds;



    private final ArrayList<String> pressed_buttons = new ArrayList<>();
    private final HashMap<String, Integer> pressed_buttons_map = new HashMap<>();
    private final ArrayList<ImageView> image_ships = new ArrayList<>();

    private final HashMap<String, Integer> receiver_pressed_buttons_map = new HashMap<>();
    private final ArrayList<LinearLayout> available_ships = new ArrayList<>();

    private ValueEventListener eventListener_1;
    private ValueEventListener eventListener_2;

    private ValueEventListener eventListener_1_message;
    private ValueEventListener eventListener_2_message;

    private Boolean alert_dialog_back = false;
    private Boolean finishing = false;
    private Boolean completed_game = false;
    private Boolean user_declined_behavior = false;

    private AlertDialog exit_game;
    private AlertDialog user_termination;
    private AlertDialog user_won;
    private AlertDialog user_lost;

    private int vessels_sunk = 0;
    private final boolean[] first_declined = {false};

    private EditText message;
    private Button message_button;
    private LinearLayout message_layout;
    private LinearLayout down_scroll_layout;
    private LinearLayout bottom_action_bar;

    private Button previous_received_attack_button = null;
    private int previous_received_attack_state = 0;

    private Button previous_made_attack_button = null;
    private int previous_made_attack_state = 0;

    // Media Players for sound effects
    private MediaPlayer hit_sound_effect;
    private MediaPlayer destroy_sound_effect;
    private MediaPlayer miss_sound_effect;
    private MediaPlayer win_sound_effect;
    private MediaPlayer lose_sound_effect;
    private MediaPlayer[] mediaPlayer_collection;

    private TextView opponent_message;

    //Support Arrays
    private final int[] row_calc= {1, 1, 1, -1, -1, -1, 0, 0};
    private final int[] col_calc = {1, 0, -1, 1, 0, -1, 1, -1};
    private final int delayed_time = 2100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_battleship);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        database = FirebaseDatabase.getInstance();

        //Initialize the sound effects
        hit_sound_effect = MediaPlayer.create(this, R.raw.hit_ship);
        destroy_sound_effect = MediaPlayer.create(this, R.raw.destroy_ship);
        miss_sound_effect = MediaPlayer.create(this, R.raw.water_splash_1);
        win_sound_effect = MediaPlayer.create(this, R.raw.win_game);
        lose_sound_effect = MediaPlayer.create(this, R.raw.lose_game);
        mediaPlayer_collection = new MediaPlayer[]{hit_sound_effect, destroy_sound_effect, miss_sound_effect, win_sound_effect, lose_sound_effect};

        active_player = findViewById(R.id.your_turn);
        inactive_player = findViewById(R.id.opponent_turn);
        opponent_responds = findViewById(R.id.opponent_responding);


        for(String ship : Opponent_User.getInstance().getShips()){
            pressed_buttons_map.put(ship, 0);
        }

        for (String ship : User.getInstance().getShips_list()){
            receiver_pressed_buttons_map.put(ship, 0);
        }

        //Initialize the ships variables
        image_ships.add(findViewById(R.id.attack_2));
        image_ships.add(findViewById(R.id.attack_3));
        image_ships.add(findViewById(R.id.attack_4));
        image_ships.add(findViewById(R.id.attack_5));
        image_ships.add(findViewById(R.id.attack_6));

        available_ships.add(findViewById(R.id.my_ship_2));
        available_ships.add(findViewById(R.id.my_ship_3));
        available_ships.add(findViewById(R.id.my_ship_4));
        available_ships.add(findViewById(R.id.my_ship_5));
        available_ships.add(findViewById(R.id.my_ship_6));



        //Initialize the players messaging communication components
        message = findViewById(R.id.message);
        message_button = findViewById(R.id.message_button);
        message_layout = findViewById(R.id.message_box_layout);
        down_scroll_layout = findViewById(R.id.down_battleship_layout);
        bottom_action_bar = findViewById(R.id.bottom_action_bar);
        opponent_message = findViewById(R.id.opponent_message);

        opponent_message.setMovementMethod(new android.text.method.ScrollingMovementMethod());


        //Set the message_box view
        message.post(() -> {
            int layout_height = message_layout.getHeight();

            LinearLayout.LayoutParams parameters = (LinearLayout.LayoutParams) bottom_action_bar.getLayoutParams();
            parameters.topMargin = -layout_height/2;
            bottom_action_bar.setLayoutParams(parameters);

            LinearLayout.LayoutParams scrollview_parameters = (LinearLayout.LayoutParams) down_scroll_layout.getLayoutParams();
            scrollview_parameters.bottomMargin = layout_height/2 + 55;
            down_scroll_layout.setLayoutParams(scrollview_parameters);
        });


        //Set the message sending button logic
        message_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message_text = message.getText().toString();
                if (message_text.equals("")){
                    Toast.makeText(BattleshipActivity.this, getString(R.string.empty_message), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!SystemOperations.internetConnectionAvailability(BattleshipActivity.this)){
                    Toast.makeText(BattleshipActivity.this, getString(R.string.no_internet_no_message), Toast.LENGTH_SHORT).show();
                    return;
                }
                // Check if the opponent is available and if he has read all the previous messages and if he has sends the message
                if (Opponent_User.getInstance().getOpponent_selection() == 1){
                    database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getValue(String.class).equals(User.getInstance().getUser_id())) {
                                database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2_message").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists() && snapshot.getValue(String.class).equals("")){
                                            database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2_message").setValue(message_text);
                                            message.setText("");
                                        } else{
                                            Toast.makeText(BattleshipActivity.this, getString(R.string.message_no_sent_internet), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }else {
                    database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_message").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getValue(String.class).equals("")) {
                                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_message").setValue(message_text);
                                message.setText("");
                            } else{
                                Toast.makeText(BattleshipActivity.this, getString(R.string.message_no_sent_internet), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });
                }


            }
        });

        //Sets special handle for the back button to finish the activity and then return back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!alert_dialog_back && !completed_game && !user_declined_behavior) {
                    alert_dialog_back = true;
                    exit_game = new AlertDialog.Builder(BattleshipActivity.this)
                            .setTitle(getString(R.string.exit))
                            .setIcon(android.R.drawable.presence_busy)
                            .setMessage(getString(R.string.exit_question))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Opponent_User.getInstance().getOpponent_selection() == 2) {
                                        //Cleaning the game fields & listeners
                                        database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_response").removeEventListener(eventListener_2);
                                        database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_message").removeEventListener(eventListener_2_message);
                                        database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_response").setValue("d" + Opponent_User.getInstance().getId());
                                        database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_response").setValue("");
                                        database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_message").setValue("");
                                        database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_message").setValue("");
                                        database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_response").setValue("");

                                    }else{
                                        database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_response").removeEventListener(eventListener_1);
                                        database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_message").removeEventListener(eventListener_1_message);
                                        database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists() && snapshot.getValue(String.class).equals(User.getInstance().getUser_id())){

                                                    database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2_response").setValue("d" + User.getInstance().getUser_id());
                                                    database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2_response").setValue("");

                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                    finishing = true;
                                    finish();

                                }
                            })
                            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alert_dialog_back = false;
                                }
                            }).create();

                    if (exit_game.getWindow() != null) {

                        exit_game.getWindow().setBackgroundDrawableResource(R.drawable.leave_game_alert_dialog_background);
                    }

                    exit_game.show();
                }


            }
        });

        action_game_grid = findViewById(R.id.action_game_grid);
        secondary_game_grid = findViewById(R.id.secondary_game_grid);
        action_game_grid.setRowCount(11);
        action_game_grid.setColumnCount(11);
        secondary_game_grid.setRowCount(11);
        secondary_game_grid.setColumnCount(11);

        //PRIMARY GRID - Make attacks
        buttons_grid_creation(action_game_grid, (int)getResources().getDimension(R.dimen.battleship_activity_panel_button_size_big), 8);


        //SECONDARY GRID - Received attacks
        buttons_grid_creation(secondary_game_grid, (int)getResources().getDimension(R.dimen.battleship_activity_panel_button_size_small), 4);

        //Set the user ships to be displayed on the secondary grid
        for (String position : User.getInstance().getShips_list()) {
            for (int position_counter = 0; position_counter < position.length(); position_counter += 2) {
                int row_temp = getNumericValue(position.charAt(position_counter));
                int col_temp = getNumericValue(position.charAt(position_counter + 1));

                Button selected_button = (Button)secondary_game_grid.getChildAt((row_temp + 1) * secondary_game_grid.getColumnCount()+ col_temp);

                selected_button.setTextColor(Color.TRANSPARENT);
                selected_button.setText("X");

                selected_button.setBackgroundResource(R.drawable.battleship_my_ships);
            }
        }


        // Check if the user is the first player or the second player
        if(Opponent_User.getInstance().getOpponent_selection() == 1) {
            //Player 1

            //Enable the buttons
            active_player.setVisibility(View.VISIBLE);
            for (int i = 0; i < action_game_grid.getChildCount(); i++) {
                View button_child = action_game_grid.getChildAt(i);
                if (button_child instanceof Button) {
                    ((Button) button_child).setEnabled(true);
                }
            }

            //Set the listeners for the opponent response
            database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_response")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            eventListener_1 = this;
                            if (snapshot.exists()){
                                String received_message = snapshot.getValue(String.class);
                                if (snapshot.getValue(String.class).equals("")) {
                                    database.getReference().child(Opponent_User.getInstance().getId()).child("player_2").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists() && !snapshot.getValue(String.class).equals(User.getInstance().getUser_id())) {
                                                Toast.makeText(BattleshipActivity.this, getString(R.string.out_of_the_game), Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                    //Do nothing

                                    // Check if the opponent won the game
                                }else if(snapshot.getValue(String.class).startsWith("w")){
                                    if (snapshot.getValue(String.class).substring(1).equals(User.getInstance().getUser_id())) {
                                        //Player 1 won the game

                                        User.getInstance().setLosses(User.getInstance().getLosses() + 1);
                                        User.getInstance().updateLosses();

                                        User.getInstance().setWin_rate((double) User.getInstance().getWins() / (User.getInstance().getWins() + User.getInstance().getLosses()));
                                        User.getInstance().updateWinRate();

                                        if(exit_game != null && exit_game.isShowing()){
                                            exit_game.dismiss();
                                        }

                                        database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_response").removeEventListener(eventListener_1);
                                        database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_message").removeEventListener(eventListener_1_message);
                                        database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists() && snapshot.getValue(String.class).equals(User.getInstance().getUser_id())){
                                                    database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2_response").setValue("");
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        completed_game = true;

                                        sound_player(lose_sound_effect);

                                        // Appear the alert dialog to notify the user that he lost
                                        user_lost = new AlertDialog.Builder(BattleshipActivity.this)
                                                .setTitle(getString(R.string.lost_game))
                                                .setMessage(getString(R.string.opponent_won_game))
                                                .setIcon(android.R.drawable.ic_delete)
                                                .setCancelable(false)
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                })
                                                .create();

                                        if (user_lost.getWindow() != null) {

                                            user_lost.getWindow().setBackgroundDrawableResource(R.drawable.lost_game_alert_dialog_background);
                                        }

                                        user_lost.show();

                                    }

                                    // Check if the opponent left the game
                                }else if (snapshot.getValue(String.class).startsWith("d")) {
                                    if (snapshot.getValue(String.class).substring(1).equals(User.getInstance().getUser_id())) {
                                        //Player 1 left the game
                                        database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_response").removeEventListener(eventListener_1);
                                        Toast.makeText(BattleshipActivity.this, getString(R.string.opponent_left_game), Toast.LENGTH_SHORT).show();
                                        first_declined[0] = true;
                                        finish();

                                    }else {
                                        Toast.makeText(BattleshipActivity.this, getString(R.string.opponent_not_available_exit), Toast.LENGTH_SHORT).show();
                                    }

                                    // Check if the opponent make a move (attack)
                                }else{
                                    //Player 1 has responded

                                    database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                if (snapshot.getValue(String.class).equals(User.getInstance().getUser_id())){

                                                    inactive_player.setVisibility(View.GONE);
                                                    opponent_responds.setVisibility(View.VISIBLE);
                                                    active_player.setVisibility(View.GONE);

                                                    int row_temp = getNumericValue(received_message.charAt(0));
                                                    int col_temp = getNumericValue(received_message.charAt(1));


                                                    ArrayList<String> pressed_buttons_temp = new ArrayList<>(pressed_buttons);


                                                    if(previous_received_attack_button != null) {
                                                        if (previous_received_attack_state == 1) {
                                                            previous_received_attack_button.setBackgroundResource(R.drawable.game_receive_attack_ship);
                                                        } else {
                                                            previous_received_attack_button.setBackgroundResource(R.drawable.game_receive_attack);
                                                        }
                                                    }

                                                    int local_counter = 0;
                                                    //Check if the opponent hit a ship
                                                    for (String myShip : User.getInstance().getShips_list()) {
                                                        for (int position_counter = 0; position_counter < myShip.length(); position_counter += 2) {
                                                            int row_temp_1 = getNumericValue(myShip.charAt(position_counter));
                                                            int col_temp_1 = getNumericValue(myShip.charAt(position_counter+1));
                                                            if (row_temp_1 == row_temp && col_temp_1 == col_temp) {
                                                                Button selected_button = (Button)secondary_game_grid.getChildAt((row_temp+1) * secondary_game_grid.getColumnCount()+col_temp);
                                                                selected_button.setBackgroundResource(R.drawable.game_receive_attack_ship_highlight);

                                                                previous_received_attack_button = selected_button;
                                                                previous_received_attack_state = 1;


                                                                receiver_pressed_buttons_map.put(myShip, receiver_pressed_buttons_map.get(myShip) + 1);
                                                                if(receiver_pressed_buttons_map.get(myShip) == myShip.length()/2){

                                                                    // Play the sound effect
                                                                    sound_player(destroy_sound_effect);

                                                                    Toast.makeText(BattleshipActivity.this, getString(R.string.opponent_destroyed_ship), Toast.LENGTH_SHORT).show();
                                                                    available_ships.get(local_counter).setVisibility(View.GONE);

                                                                    for(int destroyed_counter = 0; destroyed_counter < myShip.length(); destroyed_counter += 2){
                                                                        int selected_row = getNumericValue(myShip.charAt(destroyed_counter));
                                                                        int selected_col = getNumericValue(myShip.charAt(destroyed_counter + 1));

                                                                        disable_buttons(secondary_game_grid, selected_row, selected_col);
                                                                    }

                                                                }else{

                                                                    // Play the sound effect
                                                                    sound_player(hit_sound_effect);

                                                                    Toast.makeText(BattleshipActivity.this, getString(R.string.opponent_hit_ship), Toast.LENGTH_SHORT).show();
                                                                }

                                                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                                    inactive_player.setVisibility(View.GONE);
                                                                    opponent_responds.setVisibility(View.GONE);
                                                                    active_player.setVisibility(View.VISIBLE);

                                                                    int row;
                                                                    int col;
                                                                    //Re-enable the buttons
                                                                    for (int i = 0; i < action_game_grid.getChildCount(); i++) {
                                                                        View selected_item = action_game_grid.getChildAt(i);
                                                                        if (selected_item instanceof Button) {
                                                                            selected_item.setEnabled(true);

                                                                            row = (int) selected_item.getTag(R.id.tag_row);
                                                                            col = (int) selected_item.getTag(R.id.tag_col);

                                                                            for(String position : pressed_buttons_temp){
                                                                                if (position.equals("" + row + col)){
                                                                                    pressed_buttons_temp.remove(position);
                                                                                    selected_item.setEnabled(false);
                                                                                    break;
                                                                                }
                                                                            }
                                                                        }

                                                                    }

                                                                }, delayed_time);

                                                                return;
                                                            }
                                                        }

                                                        local_counter += 1;
                                                    }

                                                    sound_player(miss_sound_effect);

                                                    Button selected_button = (Button)secondary_game_grid.getChildAt((row_temp + 1) * secondary_game_grid.getColumnCount()+col_temp);
                                                    selected_button.setBackgroundResource(R.drawable.game_receive_attack_highlight);

                                                    previous_received_attack_button = selected_button;
                                                    previous_received_attack_state = 0;

                                                    Toast.makeText(BattleshipActivity.this, getString(R.string.no_ship_attack), Toast.LENGTH_SHORT).show();

                                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                        inactive_player.setVisibility(View.GONE);
                                                        opponent_responds.setVisibility(View.GONE);
                                                        active_player.setVisibility(View.VISIBLE);

                                                        int row;
                                                        int col;
                                                        //Re-enable the buttons
                                                        for (int i = 0; i < action_game_grid.getChildCount(); i++) {
                                                            View selected_item = action_game_grid.getChildAt(i);
                                                            if (selected_item instanceof Button) {
                                                                selected_item.setEnabled(true);

                                                                row = (int) selected_item.getTag(R.id.tag_row);
                                                                col = (int) selected_item.getTag(R.id.tag_col);

                                                                for(String position : pressed_buttons_temp){
                                                                    if (position.equals("" + row + col)){
                                                                        pressed_buttons_temp.remove(position);
                                                                        selected_item.setEnabled(false);
                                                                        break;
                                                                    }
                                                                }
                                                            }

                                                        }

                                                    }, delayed_time);


                                                }else{
                                                    if(!first_declined[0]) {
                                                        Toast.makeText(BattleshipActivity.this, getString(R.string.opponent_isnt_available), Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            //Set the listeners for the opponent message
            database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_message")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            eventListener_1_message = this;
                            if (snapshot.exists()){
                                String received_message = snapshot.getValue(String.class);
                                if (snapshot.getValue(String.class).equals("")) {
                                    //Do nothing
                                    //Disable the received message
                                }else{
                                    database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists() && snapshot.getValue(String.class).equals(User.getInstance().getUser_id())) {
                                                opponent_message.setGravity(Gravity.START);
                                                opponent_message.setText("[ 🕗 " + current_date_time() + "] " + Opponent_User.getInstance().getFirst_name() + " " + Opponent_User.getInstance().getLast_name() + " " + getString(R.string.said) + " " + received_message);
                                                database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_message").setValue("");

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }else{
            //Player 2
            inactive_player.setVisibility(View.VISIBLE);
            //Set the listener for the opponent response
            database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_response")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            eventListener_2 = this;
                            if (snapshot.exists()){
                                String received_message = snapshot.getValue(String.class);
                                if (snapshot.getValue(String.class).equals("")) {
                                    //Do nothing

                                    //Check if the opponent won the game
                                }else if (snapshot.getValue(String.class).startsWith("w")){
                                    //Player 2 won the game
                                    database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists() && snapshot.getValue(String.class).equals(Opponent_User.getInstance().getId())) {
                                                User.getInstance().setLosses(User.getInstance().getLosses() + 1);
                                                User.getInstance().updateLosses();

                                                User.getInstance().setWin_rate((double) User.getInstance().getWins() / (User.getInstance().getWins() + User.getInstance().getLosses()));
                                                User.getInstance().updateWinRate();

                                                if(exit_game != null && exit_game.isShowing()){
                                                    exit_game.dismiss();
                                                }

                                                //Cleaning the game fields & listeners
                                                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_response").removeEventListener(eventListener_2);
                                                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_message").removeEventListener(eventListener_2_message);

                                                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_response").setValue("");
                                                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_message").setValue("");
                                                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_message").setValue("");
                                                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_response").setValue("");

                                                completed_game = true;

                                                sound_player(lose_sound_effect);

                                                user_lost = new AlertDialog.Builder(BattleshipActivity.this)
                                                        .setTitle(getString(R.string.lost_game))
                                                        .setMessage(getString(R.string.opponent_won_game))
                                                        .setIcon(android.R.drawable.ic_delete)
                                                        .setCancelable(false)
                                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                finish();
                                                            }
                                                        })
                                                        .create();

                                                if (user_lost.getWindow() != null) {

                                                    user_lost.getWindow().setBackgroundDrawableResource(R.drawable.lost_game_alert_dialog_background);
                                                }

                                                user_lost.show();

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                    //Check if the opponent left the game
                                }else if (snapshot.getValue(String.class).startsWith("d")) {
                                    //Player 2 left the game
                                    database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists() && snapshot.getValue(String.class).equals(Opponent_User.getInstance().getId())) {
                                                Toast.makeText(BattleshipActivity.this, getString(R.string.opponent_left_game), Toast.LENGTH_SHORT).show();
                                                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_response").removeEventListener(eventListener_2);
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                    // Check if the opponent made a move (attack)
                                }else{
                                    //Player 2 has responded

                                    database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists() && Objects.equals(snapshot.getValue(String.class), Opponent_User.getInstance().getId())) {
                                                inactive_player.setVisibility(View.GONE);
                                                opponent_responds.setVisibility(View.VISIBLE);
                                                active_player.setVisibility(View.GONE);

                                                int row_temp = getNumericValue(received_message.charAt(0));
                                                int col_temp = getNumericValue(received_message.charAt(1));


                                                ArrayList<String> pressed_buttons_temp = new ArrayList<>(pressed_buttons);


                                                if(previous_received_attack_button != null) {
                                                    if (previous_received_attack_state == 1) {
                                                        previous_received_attack_button.setBackgroundResource(R.drawable.game_receive_attack_ship);
                                                    } else {
                                                        previous_received_attack_button.setBackgroundResource(R.drawable.game_receive_attack);
                                                    }
                                                }

                                                int local_counter = 0;
                                                //Check if the opponent hit a ship
                                                for (String myShip : User.getInstance().getShips_list()) {
                                                    for (int counter = 0; counter < myShip.length(); counter += 2) {
                                                        int row_temp_1 = getNumericValue(myShip.charAt(counter));
                                                        int col_temp_1 = getNumericValue(myShip.charAt(counter+1));
                                                        if (row_temp_1 == row_temp && col_temp_1 == col_temp) {
                                                            Button selected_button = (Button)secondary_game_grid.getChildAt((row_temp + 1)*secondary_game_grid.getColumnCount()+col_temp);
                                                            selected_button.setBackgroundResource(R.drawable.game_receive_attack_ship_highlight);
                                                            previous_received_attack_button = selected_button;
                                                            previous_received_attack_state = 1;
                                                            //Toast.makeText(BattleshipActivity.this, "The opponent hit a ship", Toast.LENGTH_SHORT).show();

                                                            receiver_pressed_buttons_map.put(myShip, receiver_pressed_buttons_map.get(myShip) + 1);
                                                            if(receiver_pressed_buttons_map.get(myShip) == myShip.length()/2){

                                                                // Play the sound effect
                                                                sound_player(destroy_sound_effect);

                                                                Toast.makeText(BattleshipActivity.this, getString(R.string.opponent_destroyed_ship), Toast.LENGTH_SHORT).show();
                                                                available_ships.get(local_counter).setVisibility(View.GONE);

                                                                for(int destroyed_counter = 0; destroyed_counter < myShip.length(); destroyed_counter += 2){
                                                                    int selected_row = getNumericValue(myShip.charAt(destroyed_counter));
                                                                    int selected_col = getNumericValue(myShip.charAt(destroyed_counter + 1));

                                                                    disable_buttons(secondary_game_grid, selected_row, selected_col);
                                                                }

                                                            }else{

                                                                // Play the sound effect
                                                                sound_player(hit_sound_effect);

                                                                Toast.makeText(BattleshipActivity.this, getString(R.string.opponent_hit_ship), Toast.LENGTH_SHORT).show();
                                                            }

                                                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                                inactive_player.setVisibility(View.GONE);
                                                                opponent_responds.setVisibility(View.GONE);
                                                                active_player.setVisibility(View.VISIBLE);

                                                                int row;
                                                                int col;

                                                                //Re-enable the buttons
                                                                for (int i = 0; i < action_game_grid.getChildCount(); i++) {
                                                                    View selected_item = action_game_grid.getChildAt(i);
                                                                    if (selected_item instanceof Button){
                                                                        selected_item.setEnabled(true);

                                                                        row = (int) selected_item.getTag(R.id.tag_row);
                                                                        col = (int) selected_item.getTag(R.id.tag_col);

                                                                        for(String position : pressed_buttons_temp){
                                                                            if (position.equals("" + row + col)){
                                                                                pressed_buttons_temp.remove(position);
                                                                                selected_item.setEnabled(false);
                                                                                break;
                                                                            }
                                                                        }
                                                                    }

                                                                }

                                                            }, delayed_time);

                                                            return;
                                                        }
                                                    }

                                                    local_counter += 1;

                                                }

                                                // Play the sound effect
                                                sound_player(miss_sound_effect);

                                                Button selected_button = (Button)secondary_game_grid.getChildAt((row_temp+1)*secondary_game_grid.getColumnCount()+col_temp);
                                                selected_button.setBackgroundResource(R.drawable.game_receive_attack_highlight);

                                                previous_received_attack_button = selected_button;
                                                previous_received_attack_state = 0;

                                                Toast.makeText(BattleshipActivity.this, getString(R.string.no_ship_attack), Toast.LENGTH_SHORT).show();

                                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                    inactive_player.setVisibility(View.GONE);
                                                    opponent_responds.setVisibility(View.GONE);
                                                    active_player.setVisibility(View.VISIBLE);

                                                    int row;
                                                    int col;

                                                    //Re-enable the buttons
                                                    for (int i = 0; i < action_game_grid.getChildCount(); i++) {
                                                        View selected_item = action_game_grid.getChildAt(i);
                                                        if (selected_item instanceof Button){
                                                            selected_item.setEnabled(true);

                                                            row = (int) selected_item.getTag(R.id.tag_row);
                                                            col = (int) selected_item.getTag(R.id.tag_col);

                                                            for(String position : pressed_buttons_temp){
                                                                if (position.equals("" + row + col)){
                                                                    pressed_buttons_temp.remove(position);
                                                                    selected_item.setEnabled(false);
                                                                    break;
                                                                }
                                                            }
                                                        }

                                                    }

                                                }, delayed_time);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });

            //Set the listener for the opponent message
            database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_message").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    eventListener_2_message = this;
                    if (snapshot.exists()){
                        String received_message = snapshot.getValue(String.class);
                        if (snapshot.getValue(String.class).equals("")) {
                            //Do nothing
                            // Show the message
                        }else{
                            opponent_message.setGravity(Gravity.START);
                            opponent_message.setText("[ 🕗 " + current_date_time() + "] "  + Opponent_User.getInstance().getFirst_name() + " " + Opponent_User.getInstance().getLast_name() + " " + getString(R.string.said) + " " + received_message);
                            database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_message").setValue("");


                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    // This method is called when the user clicks on a button in the action game grid to make an attack
    private void ship_attack_click(View view) {
        Button targeted_button = (Button) view;
        int row = (int) targeted_button.getTag(R.id.tag_row);
        int col = (int) targeted_button.getTag(R.id.tag_col);

        // Disable the buttons
        for (int i = 0; i < action_game_grid.getChildCount(); i++) {
            View selected_item = action_game_grid.getChildAt(i);
            if (selected_item instanceof Button) {
                ((Button) selected_item).setEnabled(false);
            }
        }

        // Check if it is the first or second player
        if (Opponent_User.getInstance().getOpponent_selection() == 1){
            active_player.setVisibility(View.GONE);
            opponent_responds.setVisibility(View.GONE);
            inactive_player.setVisibility(View.VISIBLE);
            database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        if (Objects.equals(snapshot.getValue(String.class), User.getInstance().getUser_id())){

                            if(previous_made_attack_button != null) {
                                if (previous_made_attack_state == 1) {
                                    previous_made_attack_button.setBackgroundResource(R.drawable.game_make_attack_successful);
                                } else {
                                    previous_made_attack_button.setBackgroundResource(R.drawable.game_make_attack_unsuccessful);
                                }
                            }

                            // Check if with the attack the user won the game
                            for (int i = 0; i < Opponent_User.getInstance().getShips().length; i++) {
                                for (int position_counter = 0; position_counter < Opponent_User.getInstance().getShips()[i].length(); position_counter += 2) {
                                    int row_temp = getNumericValue(Opponent_User.getInstance().getShips()[i].charAt(position_counter));
                                    int col_temp = getNumericValue(Opponent_User.getInstance().getShips()[i].charAt(position_counter + 1));
                                    if(row_temp == row-1 && col_temp == col-1){
                                        vessels_sunk += 1;
                                        if(vessels_sunk == 20) {
                                            int gained_points = Opponent_User.getInstance().getLevel() * 10;
                                            int my_new_points = User.getInstance().getPoints() + gained_points;

                                            // User level up check
                                            if (my_new_points - User.getInstance().getLevel() * 100 >= 100) {
                                                User.getInstance().setLevel(User.getInstance().getLevel() + 1);
                                                User.getInstance().updateLevel();
                                            }
                                            User.getInstance().setPoints(my_new_points);
                                            User.getInstance().updatePoints();

                                            targeted_button.setBackgroundResource(R.drawable.game_make_attack_successful_highlight);

                                            database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_response").removeEventListener(eventListener_1);
                                            database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_message").removeEventListener(eventListener_1_message);

                                            database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2_response").setValue("w" + User.getInstance().getUser_id());
                                            database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2_response").setValue("");



                                            User.getInstance().setWins(User.getInstance().getWins() + 1);
                                            User.getInstance().updateWins();

                                            User.getInstance().setWin_rate((double) User.getInstance().getWins() / (User.getInstance().getWins() + User.getInstance().getLosses()));
                                            User.getInstance().updateWinRate();

                                            completed_game = true;

                                            sound_player(win_sound_effect);

                                            //Creating the congratulations dialog
                                            user_won = new AlertDialog.Builder(BattleshipActivity.this)
                                                    .setTitle(getString(R.string.won_game))
                                                    .setMessage(getString(R.string.congratulations_won))
                                                    .setIcon(android.R.drawable.star_on)
                                                    .setCancelable(false)
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            finish();
                                                        }
                                                    })
                                                    .create();

                                            if (user_won.getWindow() != null) {

                                                user_won.getWindow().setBackgroundDrawableResource(R.drawable.won_game_alert_dialog_background);
                                            }

                                            user_won.show();

                                            return;
                                        }
                                        targeted_button.setBackgroundResource(R.drawable.game_make_attack_successful_highlight);

                                        previous_made_attack_button = targeted_button;
                                        previous_made_attack_state = 1;
                                        //Toast.makeText(BattleshipActivity.this, "You hit a ship", Toast.LENGTH_SHORT).show();

                                        pressed_buttons_map.put(Opponent_User.getInstance().getShips()[i], pressed_buttons_map.get(Opponent_User.getInstance().getShips()[i]) + 1);

                                        //Check if a single ship is destroyed
                                        if(pressed_buttons_map.get(Opponent_User.getInstance().getShips()[i]) == Opponent_User.getInstance().getShips()[i].length()/2){

                                            // Play the sound effect
                                            sound_player(destroy_sound_effect);

                                            Toast.makeText(BattleshipActivity.this, getString(R.string.you_destroyed_ship), Toast.LENGTH_SHORT).show();
                                            image_ships.get(i).setVisibility(View.VISIBLE);

                                            for(int destroyed_counter = 0; destroyed_counter < Opponent_User.getInstance().getShips()[i].length(); destroyed_counter += 2){
                                                int selected_row = getNumericValue(Opponent_User.getInstance().getShips()[i].charAt(destroyed_counter));
                                                int selected_col = getNumericValue(Opponent_User.getInstance().getShips()[i].charAt(destroyed_counter + 1));

                                                disable_buttons(action_game_grid, selected_row, selected_col);
                                            }

                                        }else{

                                            // Play the sound effect
                                            sound_player(hit_sound_effect);

                                            Toast.makeText(BattleshipActivity.this, getString(R.string.you_hit_ship), Toast.LENGTH_SHORT).show();
                                        }

                                        database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2_response").setValue("" + (row-1) + (col-1));
                                        pressed_buttons.add("" + row + col);
                                        return;
                                    }
                                }
                            }

                            // Play the sound effect
                            sound_player(miss_sound_effect);

                            database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2_response").setValue("" + (row-1) + (col-1));

                            targeted_button.setBackgroundResource(R.drawable.game_make_attack_unsuccessful_highlight);

                            previous_made_attack_button = targeted_button;
                            previous_made_attack_state = 0;

                            // Insert the last pressed button
                            pressed_buttons.add("" + row + col);

                        }else{

                            Toast.makeText(BattleshipActivity.this, getString(R.string.opponent_not_available_exit), Toast.LENGTH_SHORT).show();
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else{
            active_player.setVisibility(View.GONE);
            opponent_responds.setVisibility(View.GONE);
            inactive_player.setVisibility(View.VISIBLE);

            if(previous_made_attack_button != null) {
                if (previous_made_attack_state == 1) {
                    previous_made_attack_button.setBackgroundResource(R.drawable.game_make_attack_successful);

                } else {

                    previous_made_attack_button.setBackgroundResource(R.drawable.game_make_attack_unsuccessful);

                }
            }

            // Check if with the attack the user won the game
            for (int i = 0; i < Opponent_User.getInstance().getShips().length; i++) {
                for (int position_counter = 0; position_counter < Opponent_User.getInstance().getShips()[i].length(); position_counter += 2) {
                    int row_temp = getNumericValue(Opponent_User.getInstance().getShips()[i].charAt(position_counter));
                    int col_temp = getNumericValue(Opponent_User.getInstance().getShips()[i].charAt(position_counter + 1));

                    if(row_temp == row-1 && col_temp == col-1){
                        vessels_sunk += 1;
                        if (vessels_sunk == 20){
                            int gained_points = Opponent_User.getInstance().getLevel() * 10;
                            int my_new_points = User.getInstance().getPoints() + gained_points;

                            // User level up check
                            if (my_new_points - User.getInstance().getLevel() *100 >= 100){
                                User.getInstance().setLevel(User.getInstance().getLevel() +1);
                                User.getInstance().updateLevel();
                            }
                            User.getInstance().setPoints(my_new_points);
                            User.getInstance().updatePoints();

                            targeted_button.setBackgroundResource(R.drawable.game_make_attack_successful_highlight);



                            //Cleaning the game fields & listeners
                            database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_response").removeEventListener(eventListener_2);
                            database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_message").removeEventListener(eventListener_2_message);
                            database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_response").setValue("w" + Opponent_User.getInstance().getId());
                            database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_response").setValue("");
                            database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_message").setValue("");
                            database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_message").setValue("");
                            database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_response").setValue("");

                            User.getInstance().setWins(User.getInstance().getWins()+1);
                            User.getInstance().updateWins();

                            User.getInstance().setWin_rate((double) User.getInstance().getWins() / (User.getInstance().getWins() + User.getInstance().getLosses()));
                            User.getInstance().updateWinRate();

                            completed_game = true;

                            sound_player(win_sound_effect);

                            //Creating the congratulations dialog
                            user_won = new AlertDialog.Builder(BattleshipActivity.this)
                                    .setTitle("You Won the Game")
                                    .setMessage(getString(R.string.congratulations_won))
                                    .setIcon(android.R.drawable.star_on)
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .create();

                            if (user_won.getWindow() != null) {

                                user_won.getWindow().setBackgroundDrawableResource(R.drawable.won_game_alert_dialog_background);
                            }

                            user_won.show();

                            return;
                        }
                        targeted_button.setBackgroundResource(R.drawable.game_make_attack_successful_highlight);

                        previous_made_attack_button = targeted_button;
                        previous_made_attack_state = 1;

                        pressed_buttons_map.put(Opponent_User.getInstance().getShips()[i], pressed_buttons_map.get(Opponent_User.getInstance().getShips()[i])+ 1);

                        if(pressed_buttons_map.get(Opponent_User.getInstance().getShips()[i]) == Opponent_User.getInstance().getShips()[i].length()/2){

                            // Play the sound effect
                            sound_player(destroy_sound_effect);

                            Toast.makeText(BattleshipActivity.this, getString(R.string.you_destroyed_ship), Toast.LENGTH_SHORT).show();
                            image_ships.get(i).setVisibility(View.VISIBLE);

                            for(int destroyed_counter = 0; destroyed_counter < Opponent_User.getInstance().getShips()[i].length(); destroyed_counter += 2){
                                int selected_row = getNumericValue(Opponent_User.getInstance().getShips()[i].charAt(destroyed_counter));
                                int selected_col = getNumericValue(Opponent_User.getInstance().getShips()[i].charAt(destroyed_counter + 1));

                                disable_buttons(action_game_grid, selected_row, selected_col);
                            }

                        }else{

                            // Play the sound effect
                            sound_player(hit_sound_effect);

                            Toast.makeText(BattleshipActivity.this, getString(R.string.you_hit_ship), Toast.LENGTH_SHORT).show();
                        }

                        database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_response").setValue("" + (row-1) + (col-1));
                        pressed_buttons.add("" + row + col);
                        return;
                    }
                }
            }

            // Play the sound effect
            sound_player(miss_sound_effect);

            database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_response").setValue("" + (row-1) + (col-1));

            targeted_button.setBackgroundResource(R.drawable.game_make_attack_unsuccessful_highlight);

            previous_made_attack_button = targeted_button;
            previous_made_attack_state = 0;

            // Insert the last pressed button
            pressed_buttons.add("" + row + col);
        }

    }

    // This method is called to create the buttons in the grid layout
    private void buttons_grid_creation(GridLayout gridLayout, int width_height,  int margins){
        // Adding top text_views
        for (int col_number = 1 ; col_number <= 10 ; col_number++ ) {
            TextView horizontal_textView = new TextView(this);

            GridLayout.LayoutParams textView_Parameters = new GridLayout.LayoutParams();
            textView_Parameters.columnSpec = GridLayout.spec(col_number);
            textView_Parameters.rowSpec = GridLayout.spec(0);
            textView_Parameters.setGravity(Gravity.CENTER);
            horizontal_textView.setLayoutParams(textView_Parameters);
            horizontal_textView.setTextSize(getResources().getDimension(R.dimen.battleship_activity_panel_text_size));

            horizontal_textView.setText(String.valueOf((char)('A'-1+ col_number))) ;

            gridLayout.addView(horizontal_textView);

        }

        // Adding left text_views
        for (int row_number = 1; row_number <= 10; row_number++) {

            TextView vertical_textView = new TextView(this);

            GridLayout.LayoutParams textView_Parameters = new GridLayout.LayoutParams();
            textView_Parameters.rowSpec = GridLayout.spec(row_number);
            textView_Parameters.columnSpec = GridLayout.spec(0);
            textView_Parameters.setGravity(Gravity.CENTER);
            vertical_textView.setLayoutParams(textView_Parameters);
            vertical_textView.setTextSize(getResources().getDimension(R.dimen.battleship_activity_panel_text_size));

            vertical_textView.setText(String.valueOf(row_number));

            gridLayout.addView(vertical_textView);


            // Adding the buttons to each row
            for (int col_number = 1; col_number <= 10; col_number++) {
                Button panel_button = new Button(this);

                panel_button.setTag(R.id.tag_row, row_number);
                panel_button.setTag(R.id.tag_col, col_number);
                panel_button.setEnabled(false);


                GridLayout.LayoutParams button_Parameters = new GridLayout.LayoutParams();
                button_Parameters.width = width_height;
                button_Parameters.height = width_height;
                button_Parameters.setMargins(margins, margins, margins, margins);
                button_Parameters.columnSpec = GridLayout.spec(col_number);
                button_Parameters.rowSpec = GridLayout.spec(row_number);
                panel_button.setLayoutParams(button_Parameters);


                panel_button.setBackgroundResource(R.drawable.default_button);

                if (gridLayout == action_game_grid) {
                    panel_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ship_attack_click(v);
                        }
                    });
                }

                gridLayout.addView(panel_button);
            }
        }
    }

    // This method is called to disable the buttons around a specified button (especially when the user makes an attack)
    private void disable_buttons(GridLayout panel, int selected_row, int selected_col) {
        for (int i = 0; i < col_calc.length; i++){
            int new_selected_row = selected_row + row_calc[i];
            int new_selected_col = selected_col + col_calc[i];

            if( new_selected_row >= 0 && new_selected_row < 10 && new_selected_col >= 0 && new_selected_col <10 ){
                Button selected_button = (Button) panel.getChildAt((new_selected_row+1) * panel.getColumnCount() +new_selected_col);
                if (selected_button != null){
                    if(selected_button.getBackground().getConstantState().equals(getDrawable(R.drawable.default_button).getConstantState())){

                        if (panel == action_game_grid) {

                            selected_button.setBackgroundResource(R.drawable.game_make_attack_unsuccessful);
                            pressed_buttons.add("" + (new_selected_row+1) + (new_selected_col+1));
                        }else {

                            selected_button.setBackgroundResource(R.drawable.game_receive_attack);

                        }

                    }
                }
            }
        }


    }

    // This method controls the sound players
    private void sound_player(MediaPlayer m_player){

        for (MediaPlayer other_player : mediaPlayer_collection){
            if(other_player.isPlaying() && other_player != m_player){
                other_player.pause();
                other_player.seekTo(0);
            }
        }

        if (m_player.isPlaying()) {

            m_player.pause();
            m_player.seekTo(0);
            m_player.start();

        }else {
            m_player.start();

        }

    }

    // This method is called when the activity is resumed showing the user termination dialog if it is needed
    public void onResume() {
        super.onResume();
        Log.d("BattleshipActivity", "onResume");
        if (user_declined_behavior && user_termination == null) {

            if(exit_game != null && exit_game.isShowing()){
                exit_game.dismiss();
            }

            user_termination = new AlertDialog.Builder(BattleshipActivity.this)
                    .setTitle(getString(R.string.game_termination))
                    .setMessage(getString(R.string.game_termination_descr))
                    .setIcon(android.R.drawable.ic_delete)
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create();

            if (user_termination.getWindow() != null) {

                user_termination.getWindow().setBackgroundDrawableResource(R.drawable.lost_game_alert_dialog_background);
            }


            user_termination.show();
        }
    }

    // This method gets the current date and time
    private String current_date_time(){
        Date timestamp = new Date();
        SimpleDateFormat timestamp_format = new SimpleDateFormat("HH:mm:ss");
        return timestamp_format.format(timestamp);
    }

    // This method is called when the activity is paused and cancels the active game cleaning the game fields and listeners
    @Override
    public void onPause() {
        super.onPause();
        if (!finishing && !completed_game && !user_declined_behavior) {
            if (Opponent_User.getInstance().getOpponent_selection() == 2) {
                //Cleaning the game fields & listeners
                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_response").removeEventListener(eventListener_2);
                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_message").removeEventListener(eventListener_2_message);
                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_response").setValue("d" + Opponent_User.getInstance().getId());
                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_response").setValue("");
                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_message").setValue("");
                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_2_message").setValue("");
                database.getReference("Games").child(User.getInstance().getUser_id()).child("player_1_response").setValue("");
            } else {
                database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_response").removeEventListener(eventListener_1);
                database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_1_message").removeEventListener(eventListener_1_message);
                database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue(String.class).equals(User.getInstance().getUser_id())) {
                            database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2_response").setValue("d" + User.getInstance().getUser_id());
                            database.getReference("Games").child(Opponent_User.getInstance().getId()).child("player_2_response").setValue("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }


            user_declined_behavior = true;


            Log.d("BattleshipActivity", "onStop");

        }


    }



}