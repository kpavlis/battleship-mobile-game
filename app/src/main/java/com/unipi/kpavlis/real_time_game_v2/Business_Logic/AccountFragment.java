package com.unipi.kpavlis.real_time_game_v2.Business_Logic;

import static java.lang.Character.getNumericValue;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.unipi.kpavlis.real_time_game_v2.Models.User;
import com.unipi.kpavlis.real_time_game_v2.R;

import java.util.Arrays;


public class AccountFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;

    private GridLayout grid_Layout_Panel;


    private Toast invalid_selection_toast;
    private MediaPlayer sound_invalid_selection;

    private TextView ship_type_textView;
    private TextView ship_position_textView;
    private TextView default_theme_description;

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText nameEditText;
    private EditText surnameEditText;
    private boolean fieldsEnabled = false;
    private boolean shipsEditEnabled = false;

    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private RadioButton radioButton3;
    private RadioButton radioButton4;
    private RadioButton radioButton5;


    private RadioButton radio_english_language;
    private RadioButton radio_greek_language;
    private RadioButton radio_italian_language;

    private RadioButton radio_horizontal_position;
    private RadioButton radio_vertical_position;

    private RadioButton radio_default_theme;
    private RadioButton radio_light_theme;
    private RadioButton radio_dark_theme;

    private int selected_radio = 1;
    private boolean is_theme_description_visible = false;

    private Button relocate_button;
    private Button edit_ships_button;
    private Button edit_user_button;
    private Button sign_out_button;
    private Button delete_account_button;

    private CheckBox change_password_checkBox;

    private boolean is_vertical_selected = false;
    private String[] ships_positions = {"5969", "778797", "08182838", "2232425262", "253545556575"};

    //Support Arrays
    private final int[] row_calc= {1, 1, 1, -1, -1, -1, 0, 0};
    private final int[] col_calc = {1, 0, -1, 1, 0, -1, 1, -1};


    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ships_positions = User.getInstance().getShips_list();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Variables initialization
        invalid_selection_toast = Toast.makeText(getContext(), getString(R.string.invalid_ship_position), Toast.LENGTH_SHORT);
        sound_invalid_selection = MediaPlayer.create(getContext(), R.raw.settings_invalid_1);

        ship_type_textView = view.findViewById(R.id.ship_type_text_view);
        ship_position_textView = view.findViewById(R.id.ship_position_text_view);

        emailEditText = view.findViewById(R.id.email_edit_text);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        nameEditText = view.findViewById(R.id.name_edit_text);
        surnameEditText = view.findViewById(R.id.surname_edit_text);
        emailEditText.setText(User.getInstance().getEmail());
        nameEditText.setText(User.getInstance().getFirst_name());
        surnameEditText.setText(User.getInstance().getLast_name());

        radioButton1 = view.findViewById(R.id.radio_ship_type_1);
        radioButton2 = view.findViewById(R.id.radio_ship_type_2);
        radioButton3 = view.findViewById(R.id.radio_ship_type_3);
        radioButton4 = view.findViewById(R.id.radio_ship_type_4);
        radioButton5 = view.findViewById(R.id.radio_ship_type_5);

        radio_english_language = view.findViewById(R.id.radio_english_language);
        radio_greek_language = view.findViewById(R.id.radio_greek_language);
        radio_italian_language = view.findViewById(R.id.radio_italian_language);

        radio_default_theme = view.findViewById(R.id.radio_default_mode);
        radio_light_theme = view.findViewById(R.id.radio_light_mode);
        radio_dark_theme = view.findViewById(R.id.radio_dark_mode);
        default_theme_description = view.findViewById(R.id.default_theme_description);

        radio_horizontal_position = view.findViewById(R.id.radio_horizontal_position);
        radio_vertical_position = view.findViewById(R.id.radio_vertical_position);

        relocate_button = view.findViewById(R.id.relocate_button);
        edit_ships_button = view.findViewById(R.id.edit_ships_button);
        edit_user_button = view.findViewById(R.id.edit_button);
        sign_out_button = view.findViewById(R.id.sign_out_button);
        delete_account_button = view.findViewById(R.id.delete_account_button);

        change_password_checkBox = view.findViewById(R.id.change_password_checkbox);

        // Set the radios state for ships positions
        if(selected_radio == 1) {
            radioButton1.setChecked(true);
        } else if(selected_radio == 2) {
            radioButton2.setChecked(true);
        } else if(selected_radio == 3) {
            radioButton3.setChecked(true);
        } else if(selected_radio == 4) {
            radioButton4.setChecked(true);
        } else if(selected_radio == 5) {
            radioButton5.setChecked(true);
        }

        if(is_vertical_selected) {
            radio_vertical_position.setChecked(true);
        }else{
            radio_horizontal_position.setChecked(true);
        }


        if(getActivity() != null) {
            String lang = getActivity().getResources().getConfiguration().getLocales().get(0).getLanguage();
            int theme_code = SystemOperations.get_Appearance_Theme(getActivity());

            //Language settings
            if(lang.equals("el")) {
                radio_greek_language.setChecked(true);
                radio_english_language.setChecked(false);
                radio_italian_language.setChecked(false);
            } else if(lang.equals("it")) {
                radio_greek_language.setChecked(false);
                radio_english_language.setChecked(false);
                radio_italian_language.setChecked(true);
            } else {
                radio_greek_language.setChecked(false);
                radio_english_language.setChecked(true);
                radio_italian_language.setChecked(false);
            }

            //App Theme settings
            if (theme_code == 0) {
                radio_default_theme.setChecked(true);
                radio_light_theme.setChecked(false);
                radio_dark_theme.setChecked(false);
                default_theme_description.setVisibility(View.VISIBLE);
                is_theme_description_visible = true;
            } else if (theme_code == 1) {
                radio_default_theme.setChecked(false);
                radio_light_theme.setChecked(true);
                radio_dark_theme.setChecked(false);
                default_theme_description.setVisibility(View.INVISIBLE);
                is_theme_description_visible = false;
            } else {
                radio_default_theme.setChecked(false);
                radio_light_theme.setChecked(false);
                radio_dark_theme.setChecked(true);
                default_theme_description.setVisibility(View.INVISIBLE);
                is_theme_description_visible = false;
            }
        }


        relocate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relocate(v);
            }
        });


        edit_ships_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_ships(v);
            }
        });



        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sign_out(v);
            }
        });


        edit_user_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_fields(v);
            }
        });

        // Delete Account button handler
        delete_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog to confirm account deletion
                AlertDialog delete_alert = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.delete_account)
                        .setMessage(R.string.delete_account_text)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            delete_account(v);
                        })
                        .setNegativeButton(R.string.no, null)
                        .create();

                if (delete_alert.getWindow() != null) {

                    delete_alert.getWindow().setBackgroundDrawableResource(R.drawable.lost_game_alert_dialog_background);
                }

                delete_alert.show();
            }
        });


        grid_Layout_Panel = view.findViewById(R.id.game_grid);
        grid_Layout_Panel.setRowCount(11); // Declaring the number of rows
        grid_Layout_Panel.setColumnCount(11); // Declaring the number of columns

        // Creating the grid layout for the ships relocation
        // Adding top text_views
        for (int col_number = 1 ; col_number <= 10 ; col_number++ ) {
            TextView horizontal_textView = new TextView(getActivity());

            GridLayout.LayoutParams textView_Parameters = new GridLayout.LayoutParams();
            textView_Parameters.columnSpec = GridLayout.spec(col_number);
            textView_Parameters.rowSpec = GridLayout.spec(0);
            textView_Parameters.setGravity(Gravity.CENTER);
            horizontal_textView.setLayoutParams(textView_Parameters);
            horizontal_textView.setTextSize(getResources().getDimension(R.dimen.account_fragment_panel_text_size));

            horizontal_textView.setText(String.valueOf((char)('A' - 1 + col_number))) ;

            grid_Layout_Panel.addView(horizontal_textView);
        }

        // Adding left text_views
        for (int row_number = 1; row_number <= 10; row_number++) {

            TextView vertical_textView = new TextView(getActivity());

            GridLayout.LayoutParams textView_Parameters = new GridLayout.LayoutParams();
            textView_Parameters.rowSpec = GridLayout.spec(row_number);
            textView_Parameters.columnSpec = GridLayout.spec(0);
            textView_Parameters.setGravity(Gravity.CENTER);
            vertical_textView.setLayoutParams(textView_Parameters);
            vertical_textView.setTextSize(getResources().getDimension(R.dimen.account_fragment_panel_text_size));

            vertical_textView.setText(String.valueOf(row_number));

            grid_Layout_Panel.addView(vertical_textView);


        // Adding the buttons to each row
            for (int col_number = 1; col_number <= 10; col_number++) {
                Button panel_button = new Button(getActivity());

                panel_button.setTag(R.id.tag_row, row_number);
                panel_button.setTag(R.id.tag_col, col_number);
                panel_button.setEnabled(false);


                GridLayout.LayoutParams button_Parameters = new GridLayout.LayoutParams();
                button_Parameters.width = (int)getResources().getDimension(R.dimen.account_fragment_panel_button_size);
                button_Parameters.height = (int)getResources().getDimension(R.dimen.account_fragment_panel_button_size);
                button_Parameters.setMargins(8, 8, 8, 8);
                button_Parameters.columnSpec = GridLayout.spec(col_number);
                button_Parameters.rowSpec = GridLayout.spec(row_number);
                panel_button.setLayoutParams(button_Parameters);


                panel_button.setBackgroundResource(R.drawable.default_button);

                panel_button.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        ship_button_click(v);
                    }
                });


                grid_Layout_Panel.addView(panel_button);
            }
        }


        for (String position : ships_positions) {
            for (int ship_counter = 0; ship_counter < position.length(); ship_counter += 2) {

                int targeted_row = getNumericValue(position.charAt(ship_counter));
                int targeted_col = getNumericValue(position.charAt(ship_counter+1));


                Button selected_button = (Button)grid_Layout_Panel.getChildAt((targeted_row +1) * grid_Layout_Panel.getColumnCount() + targeted_col);

                if(selected_button != null){
                    selected_button.setTextColor(Color.TRANSPARENT);
                    selected_button.setBackgroundResource(R.drawable.ship_button);
                    selected_button.setText("X");
                }
            }
        }


        // Ship Type - Radio Buttons settings
        radioButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_radio = 1;
                ship_radio_selected();
            }
        });
        radioButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_radio = 2;
                ship_radio_selected();
            }
        });
        radioButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_radio = 3;
                ship_radio_selected();
            }
        });
        radioButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_radio = 4;
                ship_radio_selected();
            }
        });
        radioButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_radio = 5;
                ship_radio_selected();
            }
        });


        // Ship Orientation - Radio Buttons settings
        radio_horizontal_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_vertical_selected = false;
            }
        });
        radio_vertical_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_vertical_selected = true;
            }
        });


        // App Language - Radio Buttons settings
        radio_english_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    SystemOperations.set_display_language(getActivity(), "en");

                }
            }
        });
        radio_greek_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    SystemOperations.set_display_language(getActivity(), "el");
                }
            }
        });
        radio_italian_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    SystemOperations.set_display_language(getActivity(), "it");
                }
            }
        });


        // App Appearance (Theme) - Radio Buttons settings
        radio_default_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    SystemOperations.set_Appearance_Theme(0, getActivity());
                    default_theme_description.setVisibility(View.VISIBLE);
                    is_theme_description_visible = true;

                }
            }
        });
        radio_light_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    SystemOperations.set_Appearance_Theme(1, getActivity());
                    default_theme_description.setVisibility(View.INVISIBLE);
                    is_theme_description_visible = false;
                }

            }
        });
        radio_dark_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    SystemOperations.set_Appearance_Theme(2, getActivity());
                    default_theme_description.setVisibility(View.INVISIBLE);
                    is_theme_description_visible = false;
                }

            }
        });


        // Password update checkbox
        change_password_checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    passwordEditText.setEnabled(true);
                }else{
                    passwordEditText.setEnabled(false);
                }
            }
        });


        return view;
    }


    // This method is called when the settings fragment is recreated to save the state of the fragment
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", emailEditText.getText().toString());
        outState.putString("password", passwordEditText.getText().toString());
        outState.putString("name", nameEditText.getText().toString());
        outState.putString("surname", surnameEditText.getText().toString());
        outState.putStringArray("ships_positions", ships_positions);
        outState.putBoolean("shipsEditEnabled", shipsEditEnabled);
        outState.putBoolean("fieldsEnabled", fieldsEnabled);
        outState.putBoolean("relocate_enabled", relocate_button.isEnabled());
        outState.putBoolean("is_vertical_selected", is_vertical_selected);
        outState.putInt("selected_radio", selected_radio);
        outState.putBoolean("is_theme_description_visible", is_theme_description_visible);


    }

    // This method is called when the settings fragment is recreated to restore the state of the fragment
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            emailEditText.setText(savedInstanceState.getString("email"));
            passwordEditText.setText(savedInstanceState.getString("password"));
            nameEditText.setText(savedInstanceState.getString("name"));
            surnameEditText.setText(savedInstanceState.getString("surname"));
            ships_positions = savedInstanceState.getStringArray("ships_positions");
            shipsEditEnabled = savedInstanceState.getBoolean("shipsEditEnabled");
            fieldsEnabled = savedInstanceState.getBoolean("fieldsEnabled");
            relocate_button.setEnabled(savedInstanceState.getBoolean("relocate_enabled"));
            is_vertical_selected = savedInstanceState.getBoolean("is_vertical_selected");
            selected_radio = savedInstanceState.getInt("selected_radio");
            is_theme_description_visible = savedInstanceState.getBoolean("is_theme_description_visible");


            if(is_theme_description_visible){
                default_theme_description.setVisibility(View.VISIBLE);
            }else{
                default_theme_description.setVisibility(View.INVISIBLE);
            }
            Log.d("OnViewStateRestored", "Called");


            //Enable the edit ships process
            if (shipsEditEnabled) {
                for (int i = 0; i < grid_Layout_Panel.getChildCount(); i++) {
                    View child = grid_Layout_Panel.getChildAt(i);
                    if (child instanceof Button) {
                        ((Button) child).setEnabled(true);
                    }
                }
                radioButton1.setEnabled(true);
                radioButton2.setEnabled(true);
                radioButton3.setEnabled(true);
                radioButton4.setEnabled(true);
                radioButton5.setEnabled(true);


                ship_type_textView.setEnabled(true);
                ship_position_textView.setEnabled(true);

                radio_horizontal_position.setEnabled(true);
                radio_vertical_position.setEnabled(true);

                edit_ships_button.setText(getString(R.string.save_changes));

            }

            //Enable the edit user data process
            if(fieldsEnabled){
                nameEditText.setEnabled(true);
                surnameEditText.setEnabled(true);
                change_password_checkBox.setEnabled(true);
                if(change_password_checkBox.isChecked()){
                    passwordEditText.setEnabled(true);
                }
                edit_user_button.setText(getString(R.string.save));
            }


            // Set the previous ships positions
            for (int i = 0; i < grid_Layout_Panel.getChildCount(); i++) {
                View child = grid_Layout_Panel.getChildAt(i);
                if (child instanceof Button) {
                    ((Button) child).setText("");
                    ((Button)child).setBackgroundResource(R.drawable.default_button);
                }
            }
            for (String position : ships_positions) {
                for (int counter = 0; counter < position.length(); counter += 2) {
                    int row = getNumericValue(position.charAt(counter));
                    int col = getNumericValue(position.charAt(counter + 1));
                    Button button = (Button) grid_Layout_Panel.getChildAt((row + 1) * grid_Layout_Panel.getColumnCount() + col);
                    button.setText("X");
                    button.setTextColor(Color.TRANSPARENT);
                    button.setBackgroundResource(R.drawable.ship_button);
                }
            }

            ship_radio_selected();
        }


    }

    // This method is called when the edit fields button is clicked
    private void edit_fields(View view) {
        Button edit_button = view.findViewById(R.id.edit_button);

        if(!SystemOperations.internetConnectionAvailability(getActivity())){
            Toast.makeText(getContext(), getString(R.string.no_internet_connection_settings), Toast.LENGTH_SHORT).show();
            Log.d("INTERNET", "No internet connection");
            return;
        }

        if(fieldsEnabled){
            fieldsEnabled = false;
            passwordEditText.setEnabled(false);
            nameEditText.setEnabled(false);
            surnameEditText.setEnabled(false);
            change_password_checkBox.setEnabled(false);

            if (change_password_checkBox.isChecked()){
                if(user != null){
                    String new_password = passwordEditText.getText().toString();
                    user.updatePassword(new_password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("PASSWORD", "Password updated");
                        }else {
                            Log.e("PASSWORD", "Password update failed", task.getException());
                        }
                    });
                }
            }
            change_password_checkBox.setChecked(false);
            passwordEditText.setText("");


            edit_button.setText(getString(R.string.edit));
            User.getInstance().setFirst_name(nameEditText.getText().toString());
            User.getInstance().setLast_name(surnameEditText.getText().toString());
            User.getInstance().updateUser();
            return;
        }

        nameEditText.setEnabled(true);
        surnameEditText.setEnabled(true);
        fieldsEnabled = true;
        change_password_checkBox.setEnabled(true);
        edit_button.setText(getString(R.string.save));

    }


    // This method is called when the user wants to randomly relocate the position of the ships pressing the relocate button
    private void relocate(View view) {

        if(!SystemOperations.internetConnectionAvailability(getActivity())){
            Toast.makeText(getContext(), getString(R.string.no_internet_connection_settings), Toast.LENGTH_SHORT).show();
            Log.d("INTERNET", "No internet connection");
            return;
        }

        if(shipsEditEnabled) return;

        for (int i = 0; i < grid_Layout_Panel.getChildCount(); i++) {
            View child_element = grid_Layout_Panel.getChildAt(i);
            if (child_element instanceof Button) {
                ((Button) child_element).setText("");
                ((Button)child_element).setBackgroundResource(R.drawable.default_button);
            }
        }

        Arrays.fill(ships_positions, "");


        int random_row;
        int random_col;

        boolean check; // check if the random positions is already occupied
        int orientation; // 0 for horizontal, 1 for vertical

        for (int i =3; i <= 7; i++){
            int attempts = 0;

            orientation = (int) (Math.random()* 2);

            if (orientation == 0) {
                do {
                    random_row = 1 + (int) (Math.random() * (((10) - 1) +1));
                    random_col = 1 + (int) (Math.random() * (((10 -i + 2) - 1) + 1));


                    check = false;

                    for (int j = random_col; j < random_col + i - 1; j++) {
                        Button selected_button = (Button) grid_Layout_Panel.getChildAt(random_row * grid_Layout_Panel.getColumnCount() + j - 1);
                        attempts++;
                        if (selected_button.getText().equals("X")) {
                            check = true;
                            break;
                        }

                        // Check neighbor buttons
                        if (random_row > 1) {
                            Button top_selected_Button = (Button)grid_Layout_Panel.getChildAt((random_row - 1) * grid_Layout_Panel.getColumnCount() + j - 1);
                            if (top_selected_Button.getText().equals("X")) {
                                check = true;
                                break;

                            }
                        }
                        if (random_row < 10) {
                            Button bottom_selected_Button = (Button)grid_Layout_Panel.getChildAt((random_row + 1) * grid_Layout_Panel.getColumnCount() + j - 1);
                            if (bottom_selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }

                        if (j > 1) {
                            Button left_selected_Button = (Button)grid_Layout_Panel.getChildAt(random_row * grid_Layout_Panel.getColumnCount() + j - 2);
                            if (left_selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }

                        if (j < 10) {
                            Button right_selected_Button = (Button) grid_Layout_Panel.getChildAt(random_row * grid_Layout_Panel.getColumnCount() +j);
                            if (right_selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }

                        }

                        // Check diagonal neighbor buttons
                        if (random_row > 1 && j > 1) {
                            Button top_left_selected_Button = (Button) grid_Layout_Panel.getChildAt((random_row - 1) * grid_Layout_Panel.getColumnCount() + j - 2);
                            if (top_left_selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }

                        if (random_row > 1 && j < 10) {
                            Button top_right_selected_Button = (Button) grid_Layout_Panel.getChildAt((random_row - 1) * grid_Layout_Panel.getColumnCount() + j);
                            if (top_right_selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }
                        if (random_row < 10 && j > 1) {
                            Button bottom_left_selected_Button = (Button)grid_Layout_Panel.getChildAt((random_row + 1) * grid_Layout_Panel.getColumnCount() + j - 2);
                            if (bottom_left_selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }

                        if (random_row < 10 && j < 10) {
                            Button bottom_right_selected_Button = (Button) grid_Layout_Panel.getChildAt((random_row + 1) * grid_Layout_Panel.getColumnCount() + j);
                            if (bottom_right_selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }
                    }

                    if (attempts > 50) {
                        break;
                    }

                } while (check);

                for (int j = random_col; j < random_col + i - 1; j++) {
                    Button selected_button = (Button) grid_Layout_Panel.getChildAt(random_row * grid_Layout_Panel.getColumnCount() + j - 1);
                    selected_button.setText("X");
                    selected_button.setTextColor(Color.TRANSPARENT);
                    selected_button.setBackgroundResource(R.drawable.ship_button);
                    ships_positions[i-3] = ships_positions[i-3] + String.valueOf(random_row-1) + String.valueOf(j-1);
                }

            }else{

                do {
                    random_row = 1 + (int) (Math.random() * (((10 - i + 2) - 1) +1));
                    random_col = 1 + (int) (Math.random() * (((10) - 1) + 1));

                    check = false;

                    for (int j = random_row; j < random_row + i - 1; j++) {
                        Button selected_button = (Button) grid_Layout_Panel.getChildAt(j * grid_Layout_Panel.getColumnCount() + random_col - 1);
                        attempts++;
                        if (selected_button.getText().equals("X")) {
                            check = true;
                            break;
                        }

                        // Check neighbor buttons
                        if (random_col > 1) {
                            Button left_Selected_Button = (Button) grid_Layout_Panel.getChildAt(j * grid_Layout_Panel.getColumnCount() + random_col -2);
                            if (left_Selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }

                        }
                        if (random_col < 10) {
                            Button right_Selected_Button = (Button) grid_Layout_Panel.getChildAt(j * grid_Layout_Panel.getColumnCount() + random_col);
                            if (right_Selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }

                        if (j > 1) {
                            Button top_Selected_Button = (Button) grid_Layout_Panel.getChildAt((j - 1) * grid_Layout_Panel.getColumnCount() + random_col - 1);
                            if (top_Selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }
                        if (j < 10) {
                            Button bottom_Selected_Button = (Button)grid_Layout_Panel.getChildAt((j + 1) * grid_Layout_Panel.getColumnCount() + random_col -1);
                            if (bottom_Selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }

                        // Check diagonal neighbor buttons
                        if (random_col > 1 && j > 1) {
                            Button top_left_selected_Button = (Button) grid_Layout_Panel.getChildAt((j -1) * grid_Layout_Panel.getColumnCount() + random_col - 2);
                            if (top_left_selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }

                        if (random_col < 10 && j > 1) {
                            Button top_right_selected_Button = (Button) grid_Layout_Panel.getChildAt((j - 1) * grid_Layout_Panel.getColumnCount() + random_col);
                            if (top_right_selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }
                        if (random_col > 1 && j < 10) {
                            Button bottom_left_Selected_Button = (Button)grid_Layout_Panel.getChildAt((j +1) * grid_Layout_Panel.getColumnCount() + random_col - 2);
                            if (bottom_left_Selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }
                        if (random_col < 10 && j < 10) {
                            Button bottom_right_Selected_Button = (Button) grid_Layout_Panel.getChildAt((j +1) * grid_Layout_Panel.getColumnCount()+ random_col);
                            if (bottom_right_Selected_Button.getText().equals("X")) {
                                check = true;
                                break;
                            }
                        }
                    }
                    if (attempts > 50) {
                        break;
                    }
                } while (check);

                for (int j = random_row; j < random_row + i - 1; j++) {
                    Button button = (Button) grid_Layout_Panel.getChildAt(j * grid_Layout_Panel.getColumnCount() + random_col -1);
                    button.setText("X");
                    button.setTextColor(Color.TRANSPARENT);
                    button.setBackgroundResource(R.drawable.ship_button);
                    ships_positions[i-3] = ships_positions[i-3] + String.valueOf(j-1) + String.valueOf(random_col-1);
                }
            }

            // If exceeded the 50 attempts, relocate the ships again
            if(attempts > 50){
                relocate(view);
            }else{
                User.getInstance().setShips_list(ships_positions);
                User.getInstance().updateUser();
            }



            Log.d("ships_positions", ships_positions[i-3]);


        }
    }

    // This method is called when the user wants to custom relocate the position of the ships pressing the edit button
    private void edit_ships(View v) {

        if(!SystemOperations.internetConnectionAvailability(getActivity())){
            Toast.makeText(getContext(), getString(R.string.no_internet_connection_settings), Toast.LENGTH_SHORT).show();
            Log.d("INTERNET", "No internet connection");
            return;
        }

        Button edit_ship_button = v.findViewById(R.id.edit_ships_button);
        if (shipsEditEnabled) {
            shipsEditEnabled = false;
            edit_ship_button.setText(getString(R.string.custom_edit));
            for (int i = 0; i < grid_Layout_Panel.getChildCount(); i++) {
                View current_button = grid_Layout_Panel.getChildAt(i);
                if (current_button instanceof Button) {

                    ((Button) current_button).setEnabled(false);

                    if(!((Button) current_button).getText().equals("X")){
                        ((Button) current_button).setBackgroundResource(R.drawable.default_button);
                    }else{
                        ((Button) current_button).setBackgroundResource(R.drawable.ship_button);
                    }
                }
            }


            ship_type_textView.setEnabled(false);
            ship_position_textView.setEnabled(false);

            radioButton1.setEnabled(false);
            radioButton2.setEnabled(false);
            radioButton3.setEnabled(false);
            radioButton4.setEnabled(false);
            radioButton5.setEnabled(false);

            radio_horizontal_position.setEnabled(false);
            radio_vertical_position.setEnabled(false);

            relocate_button.setEnabled(true);


            User.getInstance().setShips_list(ships_positions);
            User.getInstance().updateUser();


        }else{

            shipsEditEnabled = true;
            edit_ship_button.setText(getString(R.string.save_changes));
            relocate_button.setEnabled(false);
            for (int i = 0; i < grid_Layout_Panel.getChildCount(); i++) {
                View current_button = grid_Layout_Panel.getChildAt(i);
                if (current_button instanceof Button) {
                    ((Button) current_button).setEnabled(true);
                }
            }

            for (int position_counter = 0; position_counter < ships_positions[selected_radio-1].length(); position_counter += 2) {
                int row_temp = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter));
                int col_temp = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter + 1));

                Button button_temp = (Button) grid_Layout_Panel.getChildAt((row_temp + 1) * grid_Layout_Panel.getColumnCount() + col_temp);
                button_temp.setBackgroundResource(R.drawable.ship_button_selected);
            }

            for(int i = 0; i < ships_positions.length; i++) {
                if(i == selected_radio - 1) continue;

                for (int position_counter = 0; position_counter < ships_positions[i].length(); position_counter += 2) {
                    int row_temp = getNumericValue(ships_positions[i].charAt(position_counter));
                    int col_temp = getNumericValue(ships_positions[i].charAt(position_counter + 1));

                    switch_buttons(grid_Layout_Panel, row_temp, col_temp, false);
                }
            }
            ship_type_textView.setEnabled(true);
            ship_position_textView.setEnabled(true);

            radioButton1.setEnabled(true);
            radioButton2.setEnabled(true);
            radioButton3.setEnabled(true);
            radioButton4.setEnabled(true);
            radioButton5.setEnabled(true);

            radio_horizontal_position.setEnabled(true);
            radio_vertical_position.setEnabled(true);
        }
    }

    // This method is called when the user clicks on a grid button to select a new position for a ship
    private void ship_button_click(View view){
        Button button_selection = (Button) view;
        int button_row = (int) button_selection.getTag(R.id.tag_row);
        int button_col = (int) button_selection.getTag(R.id.tag_col);
        boolean check = false;
        if (selected_radio == 1){
            change_vessels_positions(button_col, button_row, check);

        } else if (selected_radio == 2) {

            change_vessels_positions(button_col, button_row, check);

        } else if (selected_radio == 3) {

            change_vessels_positions(button_col, button_row, check);

        } else if (selected_radio == 4) {

            change_vessels_positions(button_col, button_row, check);

        } else if (selected_radio == 5) {

            change_vessels_positions(button_col, button_row, check);
        }
    }

    // This method is called to change the position of the ships upon user custom selection
    private void change_vessels_positions(int col, int row, boolean check){
        if (!is_vertical_selected){
            if (col <= 10 - selected_radio) {
                for (int position_counter = 0; position_counter < ships_positions[selected_radio-1].length(); position_counter += 2) {
                    int row_temp = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter));
                    int col_temp = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter +1));
                    Button button_temp = (Button) grid_Layout_Panel.getChildAt((row_temp + 1) * grid_Layout_Panel.getColumnCount() + col_temp);
                    button_temp.setText("");
                    button_temp.setBackgroundResource(R.drawable.default_button);
                }
                for (int j = col; j <= col + selected_radio ; j++) {
                    Button button1 = (Button)grid_Layout_Panel.getChildAt(row * grid_Layout_Panel.getColumnCount() + j - 1);

                    if (button1.getText().equals("X")) {
                        check = true;
                    }

                    // Check neighbor buttons
                    if (row > 1) {
                        Button top_Selected_Button = (Button) grid_Layout_Panel.getChildAt((row - 1) * grid_Layout_Panel.getColumnCount() + j - 1);
                        if (top_Selected_Button.getText().equals("X")) {
                            check = true;
                        }
                    }
                    if (row < 10) {
                        Button bottom_Selected_Button = (Button) grid_Layout_Panel.getChildAt((row + 1) * grid_Layout_Panel.getColumnCount() +j - 1);
                        if (bottom_Selected_Button.getText().equals("X")) {
                            check = true;
                        }
                    }
                    if (j > 1) {
                        Button left_Selected_Button = (Button)grid_Layout_Panel.getChildAt(row * grid_Layout_Panel.getColumnCount() + j -2);
                        if (left_Selected_Button.getText().equals("X")) {
                            check = true;
                        }
                    }
                    if (j < 10) {
                        Button right_Selected_Button = (Button)grid_Layout_Panel.getChildAt(row * grid_Layout_Panel.getColumnCount() + j);
                        if (right_Selected_Button.getText().equals("X") ) {
                            check = true;
                        }
                    }

                    // Check diagonal boxes
                    if (row > 1 && j > 1) {
                        Button top_left_selected_Button = (Button)grid_Layout_Panel.getChildAt((row - 1) * grid_Layout_Panel.getColumnCount() + j-2);
                        if (top_left_selected_Button.getText().equals("X")) {
                            check = true;
                        }
                    }
                    if (row > 1 && j < 10) {
                        Button top_right_selected_Button = (Button) grid_Layout_Panel.getChildAt((row - 1) * grid_Layout_Panel.getColumnCount() +j);
                        if (top_right_selected_Button.getText().equals( "X")) {
                            check = true;
                        }
                    }
                    if (row < 10 && j > 1) {
                        Button bottom_left_selected_Button = (Button) grid_Layout_Panel.getChildAt((row + 1) * grid_Layout_Panel.getColumnCount()+j-2);
                        if (bottom_left_selected_Button.getText().equals("X")) {
                            check = true;
                        }
                    }
                    if (row < 10 && j < 10) {
                        Button bottom_right_Selected_Button = (Button)grid_Layout_Panel.getChildAt((row + 1) * grid_Layout_Panel.getColumnCount()+j);
                        if (bottom_right_Selected_Button.getText().equals("X")) {
                            check = true;
                        }
                    }
                }

                if(!check){
                    //Valid ship position
                    ships_positions[selected_radio-1] = "";
                    for (int j = col; j <= col + selected_radio; j++) {
                        Button button2 = (Button) grid_Layout_Panel.getChildAt(row * grid_Layout_Panel.getColumnCount()+j-1);

                        button2.setText("X");
                        button2.setTextColor(Color.TRANSPARENT);
                        button2.setBackgroundResource(R.drawable.ship_button_selected);

                        ships_positions[selected_radio-1] = ships_positions[selected_radio-1] + String.valueOf(row-1) + String.valueOf(j-1);
                    }
                }else{
                    //Invalid ship position
                    for (int position_counter = 0; position_counter < ships_positions[selected_radio-1].length(); position_counter += 2) {
                        int row_temp = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter));
                        int col_temp = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter + 1));

                        Button button3 = (Button)grid_Layout_Panel.getChildAt((row_temp+1) * grid_Layout_Panel.getColumnCount()+col_temp);
                        button3.setText("X");
                        button3.setTextColor(Color.TRANSPARENT);

                        button3.setBackgroundResource(R.drawable.ship_button_selected);
                    }

                    invalid_selection_toast.show();
                    play_invalid_sound();

                }
            }else{

                invalid_selection_toast.show();
                play_invalid_sound();

            }
        }else{
            if (row <= 10 - selected_radio) {
                for (int position_counter = 0; position_counter < ships_positions[selected_radio-1].length(); position_counter += 2) {
                    int row_temp = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter));
                    int col_temp = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter+1));

                    Button button_temp = (Button) grid_Layout_Panel.getChildAt((row_temp + 1) * grid_Layout_Panel.getColumnCount()+col_temp);
                    button_temp.setText("");

                    button_temp.setBackgroundResource(R.drawable.default_button);
                }
                for (int j = row; j <= row + selected_radio; j++) {
                    Button button4 = (Button) grid_Layout_Panel.getChildAt(j * grid_Layout_Panel.getColumnCount() + col - 1);

                    if (button4.getText().equals("X")) {
                        check = true;

                    }

                    // Check neighbor buttons
                    if (col > 1) {
                        Button left_selected_Button = (Button)grid_Layout_Panel.getChildAt(j * grid_Layout_Panel.getColumnCount()+col-2);
                        if (left_selected_Button.getText().equals("X")) {
                            check = true;

                        }
                    }

                    if (col < 10) {
                        Button right_selected_Button = (Button)grid_Layout_Panel.getChildAt(j*grid_Layout_Panel.getColumnCount() + col);
                        if (right_selected_Button.getText().equals("X")) {
                            check = true;
                        }
                    }

                    if (j > 1) {
                        Button top_selected_Button = (Button) grid_Layout_Panel.getChildAt((j-1) * grid_Layout_Panel.getColumnCount()+col-1);
                        if (top_selected_Button.getText().equals("X")) {
                            check = true;
                        }

                    }

                    if (j < 10) {
                        Button bottom_selected_Button = (Button)grid_Layout_Panel.getChildAt((j+1)*grid_Layout_Panel.getColumnCount()+col-1);
                        if (bottom_selected_Button.getText().equals("X")) {

                            check = true;
                        }
                    }

                    // Check diagonal neighbor buttons
                    if (col > 1 && j > 1) {
                        Button top_left_selected_Button = (Button)grid_Layout_Panel.getChildAt((j-1)*grid_Layout_Panel.getColumnCount()+col-2);
                        if (top_left_selected_Button.getText().equals("X")) {
                            check = true;

                        }
                    }
                    if (col < 10 && j > 1) {
                        Button top_right_selected_Button = (Button)grid_Layout_Panel.getChildAt((j - 1) * grid_Layout_Panel.getColumnCount() +col);
                        if (top_right_selected_Button.getText().equals("X")) {
                            check = true;
                        }
                    }
                    if (col > 1 && j < 10) {
                        Button bottom_left_selected_Button = (Button) grid_Layout_Panel.getChildAt((j+1)*grid_Layout_Panel.getColumnCount()+ col - 2);
                        if (bottom_left_selected_Button.getText().equals("X")) {
                            check = true;

                        }
                    }

                    if (col < 10 && j < 10) {
                        Button bottom_right_selected_Button = (Button)grid_Layout_Panel.getChildAt((j + 1) * grid_Layout_Panel.getColumnCount()+col);
                        if (bottom_right_selected_Button.getText().equals("X")) {
                            check = true;

                        }
                    }
                }

                if (!check) {
                    //Valid ship position
                    ships_positions[selected_radio-1] = "";
                    for (int j = row; j <= row + selected_radio; j++) {
                        Button button5 = (Button) grid_Layout_Panel.getChildAt(j * grid_Layout_Panel.getColumnCount() + col-1);

                        button5.setText("X");
                        button5.setBackgroundResource(R.drawable.ship_button_selected);
                        button5.setTextColor(Color.TRANSPARENT);

                        ships_positions[selected_radio-1] = ships_positions[selected_radio-1] + String.valueOf(j - 1) + String.valueOf(col-1);
                    }
                } else {
                    //Invalid ship position
                    for (int position_counter = 0; position_counter < ships_positions[selected_radio-1].length(); position_counter += 2) {
                        int row_temporary = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter));
                        int col_temporary = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter + 1));

                        Button button_temp = (Button) grid_Layout_Panel.getChildAt((row_temporary+1)*grid_Layout_Panel.getColumnCount()+col_temporary);

                        button_temp.setTextColor(Color.TRANSPARENT);
                        button_temp.setText("X");

                        button_temp.setBackgroundResource(R.drawable.ship_button_selected);
                    }


                    invalid_selection_toast.show();
                    play_invalid_sound();
                }

            }else{

                invalid_selection_toast.show();
                play_invalid_sound();

            }
        }

    }

    // This method is called to change the color of the buttons around the selected ship position
    private void switch_buttons(GridLayout panel, int selected_row, int selected_col, boolean to_enable) {
        for (int i = 0; i < col_calc.length; i++){
            int new_selected_row = selected_row + row_calc[i];
            int new_selected_col = selected_col + col_calc[i];

            if(new_selected_row >= 0 && new_selected_row < 10 && new_selected_col >= 0 && new_selected_col <10 ){
                Button selected_button = (Button) panel.getChildAt((new_selected_row+1) * panel.getColumnCount() +new_selected_col);
                if (selected_button != null){
                    if(!selected_button.getText().equals("X")){

                        if (to_enable) {
                            selected_button.setBackgroundResource(R.drawable.default_button);
                        } else {
                            selected_button.setBackgroundResource(R.drawable.game_make_attack_unsuccessful_highlight);
                        }

                    }
                }
            }
        }

    }

    // This method is called when the user selects a ship to edit its position in order to highlight the selected ship at the grid
    private void ship_radio_selected() {
        if(!shipsEditEnabled) return;

        Button button_temp;
        for (int position_counter = 0; position_counter < ships_positions[selected_radio-1].length(); position_counter += 2) {
            int row_temp = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter));
            int col_temp = getNumericValue(ships_positions[selected_radio-1].charAt(position_counter + 1));

            switch_buttons(grid_Layout_Panel, row_temp, col_temp, true);

            button_temp = (Button) grid_Layout_Panel.getChildAt((row_temp + 1) * grid_Layout_Panel.getColumnCount() + col_temp);
            button_temp.setBackgroundResource(R.drawable.ship_button_selected);
        }

        for(int i = 0; i < ships_positions.length; i++) {
            if(i == selected_radio - 1) continue;

            for (int position_counter = 0; position_counter < ships_positions[i].length(); position_counter += 2) {
                int row_temp = getNumericValue(ships_positions[i].charAt(position_counter));
                int col_temp = getNumericValue(ships_positions[i].charAt(position_counter + 1));

                switch_buttons(grid_Layout_Panel, row_temp, col_temp, false);

                button_temp = (Button) grid_Layout_Panel.getChildAt((row_temp + 1) * grid_Layout_Panel.getColumnCount() + col_temp);
                button_temp.setBackgroundResource(R.drawable.ship_button);
            }
        }
    }

    // This method is handles the sound of invalid ship position selection
    private void play_invalid_sound(){
        if(sound_invalid_selection.isPlaying()){

            sound_invalid_selection.pause();
            sound_invalid_selection.seekTo(0);
            sound_invalid_selection.start();

        }else{

            sound_invalid_selection.start();

        }
    }

    // This method is called when the user clicks on the sign out button
    private void sign_out(View view) {

        if(SystemOperations.internetConnectionAvailability(getActivity())) {

            auth.signOut();

            User.getInstance().setDevice_id("");
            User.getInstance().updateDeviceID();

            if (getActivity() == null) return;


            Intent intent = new Intent(getActivity(), StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            getActivity().finish();

        }else {

            Toast.makeText(getContext(), getString(R.string.sign_out_no_internet), Toast.LENGTH_SHORT).show();
            Log.d("INTERNET", "No internet connection");
        }

    }

    // This method is called when the user has confirmed the deletion of their account
    private void delete_account(View v) {
        String user_UID = user.getUid();

        if(!SystemOperations.internetConnectionAvailability(getActivity())){
            Toast.makeText(getContext(), getString(R.string.no_internet_connection_settings), Toast.LENGTH_SHORT).show();
            Log.d("INTERNET", "No internet connection");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child("/" + user_UID);
        userRef.removeValue().addOnCompleteListener(task_delete_user -> {
            if (task_delete_user.isSuccessful()) {
                Log.d("DELETE_ACCOUNT", "User record deleted from database.");
                DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("Games").child("/" + user_UID);
                gameRef.removeValue().addOnCompleteListener(task_delete_game -> {
                    if (task_delete_game.isSuccessful()) {
                        if (user != null) {
                            user.delete().addOnCompleteListener(task_user -> {
                                if (task_user.isSuccessful()) {
                                    Log.d("DELETE_ACCOUNT", "User account deleted.");
                                    // Delete user record from Realtime Database

                                    // Sign out completed and redirect to MainActivity
                                    if (getActivity() != null) {

                                        Intent intent = new Intent(getActivity(), StartActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);

                                        getActivity().finish();
                                    }
                                } else {
                                    Log.e("DELETE_ACCOUNT", "Failed to delete user account.", task_user.getException());
                                }
                            });
                        }

                    } else {
                        Log.e("DELETE_ACCOUNT", "Failed to delete game record from database.", task_delete_game.getException());
                    }
                });

            } else {
                Log.e("DELETE_ACCOUNT", "Failed to delete user record from database.", task_delete_user.getException() );
            }
        });

    }




}