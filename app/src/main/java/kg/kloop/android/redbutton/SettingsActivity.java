package kg.kloop.android.redbutton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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

    static final int REQUEST_SELECT_FIRST_PHONE_NUMBER = 1;
    static final int REQUEST_SELECT_SECOND_PHONE_NUMBER = 2;
    private static final String TAG = "SettingsActivity";

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
    private String userPhoneNumber;
    private User user;
    private Button firstNumberButton;
    private Button secondNumberButton;

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
                    userPhoneNumber = firebaseUser.getPhoneNumber();
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
                    Toast.makeText(getApplicationContext(), R.string.dataSaved, Toast.LENGTH_LONG).show();
                } else if(preferences.getString(Constants.FIRST_NUMBER, "").length() == 0 ||
                          preferences.getString(Constants.SECOND_NUMBER, "").length() == 0 ){
                    Toast.makeText(getApplicationContext(), R.string.enterPhoneNumbers, Toast.LENGTH_LONG).show();
                }
                if (preferences.getString(Constants.MESSAGE, "").length() == 0){
                    Toast.makeText(getApplicationContext(), R.string.enterMessage, Toast.LENGTH_LONG).show();
                }

                if(firebaseUser != null){
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/userID", userID);
                    childUpdates.put("/userName", userName);
                    childUpdates.put("/userEmail", userEmail);
                    childUpdates.put("/firstNumber", firstNumber);
                    childUpdates.put("/secondNumber", secondNumber);
                    childUpdates.put("/message", message);
                    childUpdates.put("/userPhoneNumber", userPhoneNumber);
                    databaseReference.child(userID).updateChildren(childUpdates);
                } else {
                    databaseReference.push().setValue(getUser());
                }
                finish();
            }
        });

        firstNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectContact(REQUEST_SELECT_FIRST_PHONE_NUMBER);
            }
        });
        secondNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectContact(REQUEST_SELECT_SECOND_PHONE_NUMBER);
            }
        });



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contactUri, projection,
                    null, null, null);
            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberIndex);
                // Do something with the phone number
                switch (requestCode){
                    case REQUEST_SELECT_FIRST_PHONE_NUMBER:
                        firstNumberEditText.setText(number);
                        break;
                    case REQUEST_SELECT_SECOND_PHONE_NUMBER:
                        secondNumberEditText.setText(number);
                        break;
                }
            }

        }
    }

    public void selectContact(int requestCode) {
        // Start an activity for the user to pick a phone number from contacts
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        }
    }

    private void saveDataInPref(String firstNumber, String secondNumber, String message) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putString(Constants.FIRST_NUMBER, firstNumber);
        editor.putString(Constants.SECOND_NUMBER, secondNumber);
        editor.putString(Constants.MESSAGE, message);
        editor.apply();
        Log.v(TAG, "saved first number: " + firstNumber
                + "\nsaved second number: " + secondNumber);
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
        Log.v(TAG, "loaded first number: " + firstNumber
                + "\nloaded second number: " + secondNumber);

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
        firstNumberButton = (Button)findViewById(R.id.firstNumberButton);
        secondNumberButton = (Button)findViewById(R.id.secondNumberButton);
        messageEditText = (EditText)findViewById(R.id.messageEditText);
        saveSettingsButton = (Button)findViewById(R.id.saveSettingsButton);
        preferences = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");
        auth = FirebaseAuth.getInstance();
        user = new User();
    }
}
