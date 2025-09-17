package com.unipi.kpavlis.real_time_game_v2.Models;

import com.unipi.kpavlis.real_time_game_v2.Business_Logic.GameFragment;

public class Opponent_Card {
    private final String full_name;
    private final int level;
    private final String opponent_id;
    private final GameFragment gameFragment;

    public Opponent_Card(String full_name, int level, String opponent_id, GameFragment gameFragment) {
        this.full_name = full_name;
        this.level = level;
        this.opponent_id = opponent_id;
        this.gameFragment = gameFragment;
    }

    // Getters
    public String getFull_name() {
        return full_name;
    }


    public int getLevel() {
        return level;
    }

    public String getOpponent_id() {
        return opponent_id;
    }

    public GameFragment getGameFragment() {
        return gameFragment;
    }


}
