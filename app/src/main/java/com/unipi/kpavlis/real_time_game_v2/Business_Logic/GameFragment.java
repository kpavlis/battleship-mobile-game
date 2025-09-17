package com.unipi.kpavlis.real_time_game_v2.Business_Logic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.kpavlis.real_time_game_v2.Models.Opponent_Card;
import com.unipi.kpavlis.real_time_game_v2.Models.Opponent_User;
import com.unipi.kpavlis.real_time_game_v2.Models.User;
import com.unipi.kpavlis.real_time_game_v2.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class GameFragment extends Fragment {

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private final ArrayList<ValueEventListener> onResume_listeners = new ArrayList<>();
    private final ArrayList<ValueEventListener> onBattle_listeners = new ArrayList<>();

    private MediaPlayer sound_request_received;

    private final String current_user_id = User.getInstance().getUser_id();

    // Variables to store the data of the opponents
    private final ArrayList<String> user_ids = new ArrayList<>();
    private final ArrayList<String> first_names = new ArrayList<>();
    private final ArrayList<String> last_names = new ArrayList<>();
    private final ArrayList<Integer> levels = new ArrayList<>();
    private final List<Opponent_Card> opponent_cards = new ArrayList<>();


    private LinearLayout opponents_container;
    private LinearLayout supporting_elements;
    private LinearLayout no_opponents_layout;

    private Button refresh_opponents_button;

    private RecyclerView opponents_recycler_view;
    private Opponents_Recycle_Adapter adapter;

    private TextView update_time;


    private AlertDialog dialog_message = null;
    private AlertDialog request_dialog = null;
    private String status = "battle";
    private String opponent_id = "";
    private String player_2_external = "";


    public GameFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("GameFragment", "onCreate called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("GameFragment", "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        database = FirebaseDatabase.getInstance();

        sound_request_received = MediaPlayer.create(getActivity(), R.raw.battle_v1);


        opponents_recycler_view = view.findViewById(R.id.available_opponents_recycler_view);
        opponents_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new Opponents_Recycle_Adapter(getContext(), opponent_cards);

        opponents_recycler_view.setAdapter(adapter);

        supporting_elements = view.findViewById(R.id.supporting_elements);

        update_time = view.findViewById(R.id.update_time);

        refresh_opponents_button = view.findViewById(R.id.refresh_opponents_button);

        //Set the click listener for the refresh button which will refresh the available opponents
        refresh_opponents_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_ids.clear();
                first_names.clear();
                last_names.clear();
                levels.clear();
                opponents_container.removeAllViews();
                opponents_recycler_view.setVisibility(View.GONE);
                no_opponents_layout.setVisibility(View.GONE);
                supporting_elements.setVisibility(View.VISIBLE);
                refresh_opponents_button.setEnabled(false);
                find_opponents();

            }
        });

        opponents_container = view.findViewById(R.id.available_opponents_container);
        no_opponents_layout = view.findViewById(R.id.no_opponents_available);

        return view;
    }

    // This method is used to find the available opponents for the current user
    private void find_opponents(){
        DatabaseReference user_reference = database.getReference("Users");
        DatabaseReference online_reference = database.getReference("Games");

        int current_user_level = User.getInstance().getLevel();

        //Retrieve the users data from the database
        user_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalChildren = (int) snapshot.getChildrenCount();
                CountDownLatch latch = new CountDownLatch(totalChildren);
                for(DataSnapshot user : snapshot.getChildren()){
                    String user_id = user.child("user_id").getValue().toString();
                    String first_name = user.child("first_name").getValue().toString();
                    String last_name = user.child("last_name").getValue().toString();
                    int level = Integer.parseInt(user.child("level").getValue().toString());

                    //Check if the user is not the current user and if the level of the user is less than or equal to the current user's level + 1
                    if(!user_id.equals(current_user_id) && level <= current_user_level + 1){

                        //Check if the user is online
                        online_reference.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    boolean is_online = snapshot.child("is_available").getValue(Boolean.class);
                                    if (is_online) {
                                        user_ids.add(user_id);
                                        first_names.add(first_name);
                                        last_names.add(last_name);
                                        levels.add(level);
                                    }
                                }
                                latch.countDown();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("GameFragment", "Error: " + error.getMessage());
                            }
                        });
                    } else{

                        latch.countDown();
                    }
                }

                // Wait for all the children to be processed and the latch to count down to zero then update the UI
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            latch.await();
                            Log.d("GameFragment", "Total children: " + totalChildren);
                            Log.d("First_names", first_names.toString());
                            Log.d("Last_names", last_names.toString());
                            Log.d("Levels", levels.toString());

                            if(getActivity()!=null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        supporting_elements.setVisibility(View.GONE);
                                        opponents_recycler_view.setVisibility(View.VISIBLE);
                                        fill_the_UI();
                                        refresh_opponents_button.setEnabled(true);
                                    }
                                });
                            }

                        } catch (InterruptedException e) {
                            Log.d("GameFragment", "Latch interrupted: " + e.getMessage());
                        }
                    }
                }).start();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // This method is used to fill the UI with the available opponents
    private void fill_the_UI(){

        update_time.setText(current_date_time());

        boolean is_empty = user_ids.isEmpty();

        if (is_empty) {
            no_opponents_layout.setVisibility(View.VISIBLE);
            opponents_recycler_view.setVisibility(View.GONE);
            supporting_elements.setVisibility(View.GONE);
            return;
        }

        opponent_cards.clear();

        for(int i = 0; i < first_names.size(); i++){

            opponent_cards.add(new Opponent_Card(first_names.get(i) + " " + last_names.get(i), levels.get(i), user_ids.get(i), this));
        }

        adapter.updateOpponentCards(opponent_cards);


    }

    // This method is used to request a battle with the selected opponent when the user clicks on the request button
    public void request_battle(View view){
        //If the user is not connected to the internet don't proceed and show a toast message
        if (SystemOperations.internetConnectionAvailability(getActivity())) {
            status = "request";

            Button button = (Button) view;
            opponent_id = button.getTag().toString();
            reference = database.getReference("Games").child(opponent_id);

            //Check if the opponent is available
            reference.child("is_available").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {

                        database.getReference("Games").child(current_user_id).child("is_available").setValue(false);

                        reference.child("player_2").setValue(current_user_id);
                        reference.child("request_response").setValue("");
                        reference.child("player_1_message").setValue("");
                        reference.child("player_2_message").setValue("");


                        // Create the AlertDialog to show the waiting for response message and the cancel button logic if you want to cancel the request
                        AlertDialog opponent_alert = new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.waiting_for_response))
                                .setMessage(getString(R.string.you_sent_battle_request) + " " + first_names.get(user_ids.indexOf(opponent_id)) + " " + last_names.get(user_ids.indexOf(opponent_id)))
                                .setIcon(android.R.drawable.presence_away)
                                .setCancelable(false)
                                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ValueEventListener temp = new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists() && snapshot.getValue(String.class).equals(current_user_id)) {
                                                    reference.child("player_2").setValue("");

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        };
                                        reference.child("player_2").addListenerForSingleValueEvent(temp);
                                        database.getReference("Games").child(current_user_id).child("is_available").setValue(true);
                                        status = "battle";
                                        request_dialog = null;
                                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                        for (ValueEventListener selected_listener : onBattle_listeners) {
                                            reference.child("request_response").removeEventListener(selected_listener);
                                        }
                                        onBattle_listeners.clear();
                                    }
                                })
                                .create();


                        if (opponent_alert.getWindow() != null) {

                            opponent_alert.getWindow().setBackgroundDrawableResource(R.drawable.request_alert_dialog_background);
                        }

                        request_dialog = opponent_alert;
                        request_dialog.show();
                        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        //Refreshing the game fields
                        database.getReference("Games").child(opponent_id).child("player_1_response").setValue("");
                        database.getReference("Games").child(opponent_id).child("player_2_response").setValue("");
                        database.getReference("Games").child(opponent_id).child("player_1_message").setValue("");
                        database.getReference("Games").child(opponent_id).child("player_2_message").setValue("");

                        //Adding the listener to the request_response field to check if the opponent accepted or declined the request
                        reference.child("request_response").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                onBattle_listeners.add(this);
                                //Handle the response from the opponent on different cases
                                if (snapshot.exists()) {
                                    DatabaseReference current_user_reference = database.getReference("Games").child(current_user_id);
                                    String response = snapshot.getValue(String.class);
                                    //User accepted the request
                                    if (response.startsWith("a") && response.substring(1).equals(current_user_id)) {
                                        reference.child("request_response").removeEventListener(this);
                                        Toast.makeText(getActivity(), getString(R.string.request_approved), Toast.LENGTH_SHORT).show();

                                        reference.child("request_response").setValue("");
                                        current_user_reference.child("is_available").setValue(false);

                                        status = "battle";
                                        request_dialog.dismiss();
                                        request_dialog = null;
                                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                                        DatabaseReference opponent_reference = database.getReference("Users").child(opponent_id);
                                        opponent_reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    String opponent_first_name = snapshot.child("first_name").getValue(String.class);
                                                    String opponent_last_name = snapshot.child("last_name").getValue(String.class);
                                                    int opponent_level = snapshot.child("level").getValue(Integer.class);
                                                    List<String> opponent_ships = (List<String>) snapshot.child("ships").getValue();



                                                    Opponent_User.setInstance(new Opponent_User(opponent_id, opponent_first_name, opponent_last_name, opponent_level, 1, opponent_ships.toArray(new String[0])));

                                                    // Start the game going to the BattleshipActivity
                                                    Intent intent = new Intent(getActivity(), BattleshipActivity.class);
                                                    startActivity(intent);


                                                } else {
                                                    Toast.makeText(getActivity(), getString(R.string.opponent_data_not_found), Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.d("GameFragment", "Error: " + error.getMessage());
                                            }
                                        });

                                    //User declined the request
                                    } else if (response.startsWith("d") && response.substring(1).equals(current_user_id)) {
                                        reference.child("request_response").removeEventListener(this);
                                        Toast.makeText(getActivity(), getString(R.string.request_declined), Toast.LENGTH_SHORT).show();
                                        reference.child("request_response").setValue("");
                                        current_user_reference.child("is_available").setValue(true);

                                        status = "battle";

                                        request_dialog.dismiss();
                                        request_dialog = null;
                                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                    }


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("GameFragment", "Error: " + error.getMessage());

                                status = "battle";
                                database.getReference("Games").child(current_user_id).child("is_available").setValue(true);
                                request_dialog.dismiss();
                                request_dialog = null;
                                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.opponent_not_available), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("GameFragment", "Error: " + error.getMessage());
                }
            });
        }else{
            Toast.makeText(getActivity(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }

    }

    // This method is used to get the current date and time in the format dd-MM-yy HH:mm:ss
    private String current_date_time(){
        Date timestamp = new Date();
        SimpleDateFormat timestamp_format = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        return timestamp_format.format(timestamp);
    }


    // This method is used to set the status of the game when the onResume method is called from the activity
    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //Reset the status of the game to battle
            DatabaseReference battle_reference = database.getReference("Games").child(current_user_id);
            battle_reference.child("player_2").setValue("");
            battle_reference.child("request_response").setValue("");
            battle_reference.child("player_1_message").setValue("");
            battle_reference.child("player_2_message").setValue("");
            battle_reference.child("is_available").setValue(true);
            // Set the listener to check if an opponent has sent a battle request
            battle_reference.child("player_2").addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        ValueEventListener listener = this;
                        onResume_listeners.add(listener);
                        String player_2 = snapshot.getValue(String.class);
                        player_2_external = player_2;

                        if (!player_2.equals("")) {

                            DatabaseReference opponent_reference = database.getReference("Users").child(player_2);
                            opponent_reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        //If a valid opponent is found then retrieve the data of the opponent
                                        String opponent_first_name = snapshot.child("first_name").getValue(String.class);
                                        String opponent_last_name = snapshot.child("last_name").getValue(String.class);
                                        int opponent_level = snapshot.child("level").getValue(Integer.class);
                                        List<String> opponent_ships = (List<String>) snapshot.child("ships").getValue();


                                        battle_reference.child("is_available").setValue(false);

                                        play_request_sound();

                                        // Create the AlertDialog to show the received battle request message and the accept/decline buttons
                                        dialog_message = new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.battle_request))
                                                .setMessage(getString(R.string.received_battle_request) + " " + opponent_first_name + " " + opponent_last_name)
                                                .setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Handle accept action
                                                        battle_reference.child("request_response").setValue("a" + player_2);
                                                        battle_reference.child("is_available").setValue(false);
                                                        battle_reference.child("player_2").removeEventListener(listener);

                                                        dialog_message = null;
                                                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                                                        Opponent_User.setInstance(new Opponent_User(player_2, opponent_first_name, opponent_last_name, opponent_level, 2, opponent_ships.toArray(new String[0])));

                                                        // Start the game going to the BattleshipActivity
                                                        Intent intent = new Intent(getActivity(), BattleshipActivity.class);
                                                        startActivity(intent);


                                                    }


                                                })
                                                .setNegativeButton(getString(R.string.decline), new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        // Handle decline action
                                                        battle_reference.child("request_response").setValue("d" + player_2);
                                                        battle_reference.child("player_2").setValue("");
                                                        battle_reference.child("is_available").setValue(true);

                                                        dialog_message = null;
                                                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


                                                    }
                                                })
                                                .setIcon(android.R.drawable.star_big_on)
                                                .setCancelable(false)
                                                .create();

                                        if (dialog_message.getWindow() != null) {

                                            dialog_message.getWindow().setBackgroundDrawableResource(R.drawable.request_alert_dialog_background);
                                        }

                                        dialog_message.show();

                                        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                                    }else{
                                        Toast.makeText(getActivity(), getString(R.string.opponent_data_not_found), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d("GameFragment", "Error: " + error.getMessage());

                                }
                            });



                        }else{
                            if (dialog_message != null) {
                                dialog_message.dismiss();
                                battle_reference.child("is_available").setValue(true);
                                Toast.makeText(getActivity(), getString(R.string.opponent_canceled_request), Toast.LENGTH_SHORT).show();
                                dialog_message = null;
                                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("GameFragment", "Error: " + error.getMessage());
                    battle_reference.child("is_available").setValue(true);
                }
            });

            user_ids.clear();
            first_names.clear();
            last_names.clear();
            levels.clear();
            opponents_container.removeAllViews();
            opponents_recycler_view.setVisibility(View.GONE);
            no_opponents_layout.setVisibility(View.GONE);
            supporting_elements.setVisibility(View.VISIBLE);
            refresh_opponents_button.setEnabled(false);
            find_opponents();
    }

    // This method is used to play the sound when a battle request is received
    private void play_request_sound(){
        if(sound_request_received.isPlaying()){

            sound_request_received.pause();
            sound_request_received.seekTo(0);
            sound_request_received.start();

        }else{

            sound_request_received.start();

        }
    }


    // This method is used to cancel an active battle request if the user causes the fragment to pause
    @Override
    public void onPause() {

        Log.d("GameFragment - PAUSE", "onPause called");
        reference = database.getReference("Games").child(current_user_id);
        reference.child("is_available").setValue(false);
        for (ValueEventListener listener : onResume_listeners) {
            reference.child("player_2").removeEventListener(listener);
        }
        onResume_listeners.clear();
        reference = database.getReference("Games").child(opponent_id);
        for (ValueEventListener listener : onBattle_listeners) {
            reference.child("request_response").removeEventListener(listener);
        }
        onBattle_listeners.clear();

        Log.d("GameFragment", "onPause called");

        if (dialog_message != null) {
            Log.d("GameFragment - !!!", "Dialog message is not null");
            database.getReference("Games").child(current_user_id).child("request_response").setValue("d" + player_2_external);
            dialog_message.dismiss();
            dialog_message = null;
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (request_dialog != null) {
            Log.d("GameFragment - !!!", "Request dialog is not null");
            ValueEventListener temp = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists() && snapshot.getValue(String.class).equals(current_user_id)){
                        reference.child("player_2").setValue("");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            database.getReference("Games").child(opponent_id).child("player_2").addListenerForSingleValueEvent(temp);
            request_dialog.dismiss();
            request_dialog = null;
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        super.onPause();

    }


}