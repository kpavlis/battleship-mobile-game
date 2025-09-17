package com.unipi.kpavlis.real_time_game_v2.Models;

public class Opponent_User {

    private String id;
    private String first_name;
    private String last_name;
    private int level;
    private int opponent_selection;
    private String[] ships;
    private static Opponent_User instance;

    // Singleton object class for opponent user data
    public static Opponent_User getInstance(){
        if (instance == null){
            instance = new Opponent_User();
        }
        return instance;
    }

    public static void setInstance(Opponent_User opponent_user){
        instance = opponent_user;
    }

    public Opponent_User() {
        // Default constructor
    }

    public Opponent_User(String id, String first_name, String last_name, int level, int opponent_selection, String[] ships) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.level = level;
        this.opponent_selection = opponent_selection;
        this.ships = ships;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }
    public String getFirst_name() {
        return first_name;
    }
    public String getLast_name() {
        return last_name;
    }
    public int getLevel() {
        return level;
    }
    public int getOpponent_selection() {
        return opponent_selection;
    }
    public String[] getShips() {
        return ships;
    }
}
