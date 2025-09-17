package com.unipi.kpavlis.real_time_game_v2.Business_Logic;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.kpavlis.real_time_game_v2.Models.User;
import com.unipi.kpavlis.real_time_game_v2.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class StatisticsFragment extends Fragment {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Variables to store the top 3 winners, losers and best_quality names and counters
    private final String[] top_3_winners_names = new String[3];
    private final String[] top_3_winners_counter = new String[3];
    private final String[] top_3_losers_names = new String[3];
    private final String[] top_3_losers_counter = new String[3];
    private final String[] top_3_best_quality_names = new String[3];
    private final String[] top_3_best_quality_counter = new String[3];

    private TextView statistics_textView;
    private TextView games_textView;
    private TextView wins_textView;
    private TextView losses_textView;
    private TextView best_quality_textView;
    private TextView level_textView;
    private TextView points_textView;
    private ProgressBar progressBar;
    private Button wins_button;
    private Button losses_button;
    private Button best_quality_button;

    // Variables for Global Statistics
    private LinearLayout global_statistics_loading_panel;
    private LinearLayout global_statistics_panel;
    private TextView rank_statistics_textView;
    private TextView first_place_textView;
    private TextView second_place_textView;
    private TextView third_place_textView;
    private TextView first_place_counter_textView;
    private TextView second_place_counter_textView;
    private TextView third_place_counter_textView;


    public StatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("StatisticsFragment", "onCreate called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);


        // Initialize the top 3 winners and losers names and counters
        for(int i = 0; i < 3; i++){
            top_3_winners_names[i] = "-";
            top_3_winners_counter[i] = "-";
            top_3_losers_names[i] = "-";
            top_3_losers_counter[i] = "-";
            top_3_best_quality_names[i] = "-";
            top_3_best_quality_counter[i] = "-";
        }

        statistics_textView = view.findViewById(R.id.statistics_text);
        statistics_textView.setText("-  " + User.getInstance().getFirst_name() + " " + User.getInstance().getLast_name() + " " + getString(R.string.statistics_descr_text) + "  -");

        // Initialize the statistics text views and set their values
        games_textView = view.findViewById(R.id.games);
        games_textView.setText(String.valueOf(User.getInstance().getWins() + User.getInstance().getLosses()));


        wins_textView = view.findViewById(R.id.wins);
        wins_textView.setText(String.valueOf(User.getInstance().getWins()));


        losses_textView = view.findViewById(R.id.losses);
        losses_textView.setText(String.valueOf(User.getInstance().getLosses()));


        best_quality_textView = view.findViewById(R.id.win_rate);
        best_quality_textView.setText((int)(User.getInstance().getWin_rate() * 100) + "%");


        level_textView = view.findViewById(R.id.level);
        level_textView.setText(String.valueOf(User.getInstance().getLevel()));


        points_textView = view.findViewById(R.id.points);
        points_textView.setText(String.valueOf(User.getInstance().getPoints()));


        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setProgress((User.getInstance().getPoints() - User.getInstance().getLevel() * 100));

        wins_button = view.findViewById(R.id.wins_statistics_button);
        losses_button = view.findViewById(R.id.losses_statistics_button);
        best_quality_button = view.findViewById(R.id.best_quality_statistics_button);

        wins_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wins_selection();
            }
        });

        losses_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                losses_selection();
            }
        });

        best_quality_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                best_quality_selection();
            }
        });

        // Global Statistics variables initialization
        rank_statistics_textView = view.findViewById(R.id.rank_statistics);
        first_place_textView = view.findViewById(R.id.first_place_name);
        first_place_counter_textView = view.findViewById(R.id.first_place_counter);
        second_place_textView = view.findViewById(R.id.second_place_name);
        second_place_counter_textView = view.findViewById(R.id.second_place_counter);
        third_place_textView = view.findViewById(R.id.third_place_name);
        third_place_counter_textView = view.findViewById(R.id.third_place_counter);

        global_statistics_loading_panel = view.findViewById(R.id.global_statistics_loading_panel);
        global_statistics_panel = view.findViewById(R.id.global_statistics_panel);


        CountDownLatch latch = new CountDownLatch(3); // We will wait for 3 database calls to finish

        // Fetching the top 3 winners from the database
        database.getReference("Users").orderByChild("wins").limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 2;
                List<DataSnapshot> elements = new ArrayList<>();
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    elements.add(user);
                }
                for (int j = elements.size() - 1; j >= 0; j--) {
                    DataSnapshot user = elements.get(j);
                    String first_name = user.child("first_name").getValue(String.class);
                    String last_name = user.child("last_name").getValue(String.class);
                    int wins = user.child("wins").getValue(Integer.class);
                    top_3_winners_names[i] = first_name + " " + last_name;
                    top_3_winners_counter[i] = String.valueOf(wins);
                    i--;
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("StatisticsFragment", "Database error: " + databaseError.getMessage());
                latch.countDown();
            }
        });

        // Fetching the top 3 losers from the database
        database.getReference("Users").orderByChild("losses").limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 2;
                List<DataSnapshot> elements = new ArrayList<>();
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    elements.add(user);
                }
                for (int j = elements.size() - 1; j >= 0; j--) {
                    DataSnapshot user = elements.get(j);
                    String first_name = user.child("first_name").getValue(String.class);
                    String last_name = user.child("last_name").getValue(String.class);
                    int losses = user.child("losses").getValue(Integer.class);
                    top_3_losers_names[i] = first_name + " " + last_name;
                    top_3_losers_counter[i] = String.valueOf(losses);
                    i--;
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("StatisticsFragment", "Database error: " + databaseError.getMessage());
                latch.countDown();
            }
        });

        // Fetching the top 3 best quality from the database
        database.getReference("Users").orderByChild("win_rate").limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 2;
                List<DataSnapshot> elements = new ArrayList<>();
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    elements.add(user);
                }
                for (int j = elements.size() - 1; j >= 0; j--) {
                    DataSnapshot user = elements.get(j);
                    String first_name = user.child("first_name").getValue(String.class);
                    String last_name = user.child("last_name").getValue(String.class);
                    double wins = user.child("win_rate").getValue(Double.class);
                    top_3_best_quality_names[i] = first_name + " " + last_name;
                    top_3_best_quality_counter[i] = (int)(wins*100) + "%";
                    i--;
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("StatisticsFragment", "Database error: " + databaseError.getMessage());
                latch.countDown();
            }
        });

        // Wait for both database calls to finish before updating the UI
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();

                    if(getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                first_place_textView.setText(top_3_best_quality_names[2]);
                                first_place_counter_textView.setText(top_3_best_quality_counter[2]);
                                second_place_textView.setText(top_3_best_quality_names[1]);
                                second_place_counter_textView.setText(top_3_best_quality_counter[1]);
                                third_place_textView.setText(top_3_best_quality_names[0]);
                                third_place_counter_textView.setText(top_3_best_quality_counter[0]);
                                global_statistics_loading_panel.setVisibility(View.GONE);
                                global_statistics_panel.setVisibility(View.VISIBLE);

                            }
                        });
                    }

                } catch (InterruptedException e) {
                    Log.d("StatisticsFragment", "Latch interrupted: " + e.getMessage());
                }
            }
        }).start();

        return view;
    }

    // Method that is called when the user selects the Success (%) button
    private void best_quality_selection() {
        losses_button.setEnabled(true);
        wins_button.setEnabled(true);
        best_quality_button.setEnabled(false);

        rank_statistics_textView.setText(R.string.statistics_top_3_best_quality);
        if(getContext() != null) {
            first_place_counter_textView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
            second_place_counter_textView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
            third_place_counter_textView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        }

        first_place_textView.setText(top_3_best_quality_names[2]);
        first_place_counter_textView.setText(top_3_best_quality_counter[2]);
        second_place_textView.setText(top_3_best_quality_names[1]);
        second_place_counter_textView.setText(top_3_best_quality_counter[1]);
        third_place_textView.setText(top_3_best_quality_names[0]);
        third_place_counter_textView.setText(top_3_best_quality_counter[0]);


    }

    // Method that is called when the user selects the wins button
    private void wins_selection() {
        losses_button.setEnabled(true);
        wins_button.setEnabled(false);
        best_quality_button.setEnabled(true);

        rank_statistics_textView.setText(R.string.statistics_top_3_winners);
        if(getContext() != null) {
            first_place_counter_textView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
            second_place_counter_textView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
            third_place_counter_textView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        }

        first_place_textView.setText(top_3_winners_names[2]);
        first_place_counter_textView.setText(top_3_winners_counter[2]);
        second_place_textView.setText(top_3_winners_names[1]);
        second_place_counter_textView.setText(top_3_winners_counter[1]);
        third_place_textView.setText(top_3_winners_names[0]);
        third_place_counter_textView.setText(top_3_winners_counter[0]);


    }

    // Method that is called when the user selects the losses button
    private void losses_selection() {
        wins_button.setEnabled(true);
        losses_button.setEnabled(false);
        best_quality_button.setEnabled(true);

        rank_statistics_textView.setText(R.string.statistics_top_3_losers);
        if(getContext() != null) {
            first_place_counter_textView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
            second_place_counter_textView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
            third_place_counter_textView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }

        first_place_textView.setText(top_3_losers_names[2]);
        first_place_counter_textView.setText(top_3_losers_counter[2]);
        second_place_textView.setText(top_3_losers_names[1]);
        second_place_counter_textView.setText(top_3_losers_counter[1]);
        third_place_textView.setText(top_3_losers_names[0]);
        third_place_counter_textView.setText(top_3_losers_counter[0]);


    }

}