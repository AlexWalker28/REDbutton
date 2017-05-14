package kg.kloop.android.redbutton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {


    private EditText firstNumberEditText;
    private EditText secondNumberEditText;
    private EditText messageEditText;
    private Button saveSettingsButton;
    private String firstNumber;
    private String secondNumber;
    private String message;
    private SharedPreferences preferences;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private String userID;
    private String userName;
    private String userEmail;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //getSupportActionBar().hide();

        init();
        loadDataFromPref();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null) {
                    userID = firebaseUser.getUid();
                    userName = firebaseUser.getDisplayName();
                    userEmail = firebaseUser.getEmail();
                }
            }
        };
        auth.addAuthStateListener(authStateListener);

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstNumber = firstNumberEditText.getText().toString();
                secondNumber = secondNumberEditText.getText().toString();
                message = messageEditText.getText().toString();

                saveDataInPref(firstNumber, secondNumber, message);
                if(isPrefSaved()){
                    Toast.makeText(getApplicationContext(), "Data saved", Toast.LENGTH_LONG).show();
                } else Toast.makeText(getApplicationContext(), "Save failed. Try again", Toast.LENGTH_LONG).show();

                if(firebaseUser != null){
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/userID", userID);
                    childUpdates.put("/userName", userName);
                    childUpdates.put("/userEmail", userEmail);
                    childUpdates.put("/firstNumber", firstNumber);
                    childUpdates.put("/secondNumber", secondNumber);
                    childUpdates.put("/message", message);
                    databaseReference.child(userID).updateChildren(childUpdates);
                } else {
                    databaseReference.push().setValue(getUser());
                }
            }
        });

    }

    private void saveDataInPref(String firstNumber, String secondNumber, String message) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.FIRST_NUMBER, firstNumber);
        editor.putString(Constants.SECOND_NUMBER, secondNumber);
        editor.putString(Constants.MESSAGE, message);
        editor.apply();
    }


    private boolean isPrefSaved() {
        if(preferences.getString(Constants.FIRST_NUMBER, "").length() != 0 &&
                preferences.getString(Constants.SECOND_NUMBER, "").length() != 0 &&
                preferences.getString(Constants.MESSAGE, "").length() != 0){
            return true;
        } else return false;
    }

    private void loadDataFromPref() {
        firstNumber = preferences.getString(Constants.FIRST_NUMBER, "");
        secondNumber = preferences.getString(Constants.SECOND_NUMBER, "");
        message = preferences.getString(Constants.MESSAGE, "");

        firstNumberEditText.setText(firstNumber);
        secondNumberEditText.setText(secondNumber);
        messageEditText.setText(message);

    }

    public User getUser() {
        user = new User(userID, userName, userEmail,
                firstNumber, secondNumber, message);
        Log.v("User", "userDataFromSettingActivity: " + userID + "\n" + userName + "\n" + userEmail);
        return user;
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    private void init() {
        firstNumberEditText = (EditText)findViewById(R.id.firstNumberEditText);
        secondNumberEditText = (EditText)findViewById(R.id.secondNumberEditText);
        messageEditText = (EditText)findViewById(R.id.messageEditText);
        saveSettingsButton = (Button)findViewById(R.id.saveSettingsButton);
        preferences = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");
        auth = FirebaseAuth.getInstance();
        user = new User();
    }
}
