package com.unipi.kpavlis.real_time_game_v2.Models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class User {

    public String user_id;
    public String first_name;
    public String last_name;
    public String email;
    public List<String> ships;
    public int level;
    public int points;
    public int wins;
    public int losses;
    public String device_id;
    public double win_rate;
    private static User instance;

    public User() {
        //Default constructor
    }

    public User(String user_id, String first_name, String last_name, String email, List<String> ships, int level, int points, int wins, int losses, String device_id, double win_rate) {
        this.user_id = user_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.ships = ships;
        this.level = level;
        this.points = points;
        this.wins = wins;
        this.losses = losses;
        this.device_id = device_id;
        this.win_rate = win_rate;

    }


    //Singleton object class for user data
    public static User getInstance(){
        if (instance == null){
            instance = new User();
        }
        return instance;
    }

    //Set the instance of the user
    public static void setInstance(User user){
        instance = user;
    }

    //Update the user data (first_name, last_name, email, ships) in the database
    public void updateUser() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Users").child(this.user_id);


        reference.child("first_name").setValue(this.first_name);
        reference.child("last_name").setValue(this.last_name);
        reference.child("email").setValue(this.email);
        reference.child("ships").setValue(this.ships);
    }

    //Update the user device id in the database
    public void updateDeviceID() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Users").child(this.user_id);
        reference.child("device_id").setValue(this.device_id);
    }

    //Update the user level in the database
    public void updateLevel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Users").child(this.user_id);
        reference.child("level").setValue(this.level);
    }

    //Update the user points in the database
    public void updatePoints() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Users").child(this.user_id);
        reference.child("points").setValue(this.points);
    }

    //Update the user wins in the database
    public void updateWins() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Users").child(this.user_id);
        reference.child("wins").setValue(this.wins);
    }

    //Update the user losses in the database
    public void updateLosses() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Users").child(this.user_id);
        reference.child("losses").setValue(this.losses);
    }

    //Update the user win rate in the database
    public void updateWinRate() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Users").child(this.user_id);
        reference.child("win_rate").setValue(this.win_rate);
    }


    //Getters and Setters
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String[] getShips_list() {
        return ships.toArray(new String[0]);
    }

    public void setShips_list(String[] ships) {
        this.ships = Arrays.asList(ships);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public double getWin_rate() {
        return win_rate;
    }
    public void setWin_rate(double win_rate) {
        this.win_rate = win_rate;
    }


}
