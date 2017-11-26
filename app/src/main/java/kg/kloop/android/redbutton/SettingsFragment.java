package kg.kloop.android.redbutton;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import kg.kloop.android.redbutton.helpers.NavigationHelper;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    static final int REQUEST_SELECT_FIRST_PHONE_NUMBER = 1;
    static final int REQUEST_SELECT_SECOND_PHONE_NUMBER = 2;
    private static final String TAG = "SettingsFragment";
    private static final String PHONE_NUMBERS = "phoneNumbers";

    private View view;
    private EditText phoneNumberEditText;
    private EditText messageEditText;
    private Button saveSettingsButton;
    private ImageButton addPhoneNumberImageButton;
    private LinearLayout settingsLinearLayout;
    private ArrayList<String> phoneNumbersArrayList;
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
    private String number;
    private int requestCode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

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

        addPhoneNumberImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsLinearLayout.addView(addPhoneNumberView(getActivity()));
            }
        });

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < settingsLinearLayout.getChildCount(); i++) {
                    if (settingsLinearLayout.getChildAt(i) instanceof EditText) {
                        String phoneNumber = ((EditText) settingsLinearLayout.getChildAt(i)).getText().toString(); //saves message instead of phone
                        Log.v(TAG, "phone number to save: " + phoneNumber);
                        phoneNumbersArrayList.add(phoneNumber);

                        Log.v(TAG, "saved phone numbers: " + phoneNumbersArrayList.toString());
                    }
                }
                message = messageEditText.getText().toString();

                saveDataInPref(phoneNumbersArrayList, message);
                if(isPrefSaved()){
                    Toast.makeText(getActivity(), R.string.dataSaved, Toast.LENGTH_LONG).show();
                } else if(preferences.getString(Constants.FIRST_NUMBER, "").length() == 0 ||
                        preferences.getString(Constants.SECOND_NUMBER, "").length() == 0 ){
                    Toast.makeText(getActivity(), R.string.enterPhoneNumbers, Toast.LENGTH_LONG).show();
                }
                if (preferences.getString(Constants.MESSAGE, "").length() == 0){
                    Toast.makeText(getActivity(), R.string.enterMessage, Toast.LENGTH_LONG).show();
                }

                if(firebaseUser != null){
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/userID", userID);
                    childUpdates.put("/userName", userName);
                    childUpdates.put("/userEmail", userEmail);
                    /*childUpdates.put("/firstNumber", firstNumber);
                    childUpdates.put("/secondNumber", secondNumber);*/
                    childUpdates.put("/phoneNumbers", phoneNumbersArrayList);
                    childUpdates.put("/message", message);
                    childUpdates.put("/userPhoneNumber", userPhoneNumber);
                    databaseReference.child(userID).updateChildren(childUpdates);
                } else {
                    databaseReference.push().setValue(getUser());
                }
                //getFragmentManager().popBackStack();

                //go back to IntroActivity
                if(getActivity().getCallingActivity() != null) {
                    getActivity().setResult(RESULT_OK);
                    getActivity().finish();
                }
            }
        });

        addPhoneNumberImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectContact(REQUEST_SELECT_FIRST_PHONE_NUMBER);
            }
        });
        return view;
    }

    private View addPhoneNumberView(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.add_phone_number_item, null);
        ImageButton addContactImageButton = (ImageButton) view.findViewById(R.id.item_add_contact_image_button);
        addContactImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectContact(REQUEST_SELECT_FIRST_PHONE_NUMBER);
            }
        });


        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.v(TAG, "result ok");
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getActivity().getContentResolver().query(contactUri, projection,
                    null, null, null);
            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                number = cursor.getString(numberIndex);
                // Do something with the phone number
                this.requestCode = requestCode;
                switch (requestCode){
                    case REQUEST_SELECT_FIRST_PHONE_NUMBER:
                        phoneNumbersArrayList.add(number);
                        saveDataInPref(phoneNumbersArrayList, message);
                        phoneNumberEditText.setText(number);
                        Log.v(TAG, "first number: " + number);
                        break;
                    /*case REQUEST_SELECT_SECOND_PHONE_NUMBER:
                        secondNumberEditText.setText(number);
                        saveDataInPref(firstNumber, number, message);
                        Log.v(TAG, "second number: " + number);
                        break;*/
                }
                cursor.close();
            }

        }
    }

    public void selectContact(int requestCode) {
        // Start an activity for the user to pick a phone number from contacts
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
            NavigationHelper.setSelectedItemId(R.id.profile_item);
        }
    }

    private void saveDataInPref(ArrayList<String> phoneNumbersArrayList, String message) {
        Set<String> phonesSet = new LinkedHashSet<>();
        phonesSet.addAll(phoneNumbersArrayList);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putStringSet(PHONE_NUMBERS, phonesSet);
        editor.putString(Constants.MESSAGE, message);
        editor.apply();
        Log.v(TAG, "phone numbers saved in prefs: " + phoneNumbersArrayList.toString());
    }


    private boolean isPrefSaved() {
        if(preferences.getString(Constants.FIRST_NUMBER, "").length() != 0 &&
                preferences.getString(Constants.SECOND_NUMBER, "").length() != 0 &&
                preferences.getString(Constants.MESSAGE, "").length() != 0){
            return true;
        } else return false;
    }

    private void loadDataFromPref() {
        Set<String> phonesSet = preferences.getStringSet(PHONE_NUMBERS, null);
        phoneNumbersArrayList.addAll(phonesSet);
        message = preferences.getString(Constants.MESSAGE, "");
        for (int i = 0; i < settingsLinearLayout.getChildCount(); i++) {
            if (settingsLinearLayout.getChildAt(i) instanceof EditText) {
                ((EditText) settingsLinearLayout.getChildAt(i)).setText(phoneNumbersArrayList.get(i));
            }
        }
        Log.v(TAG, "loaded from pref: " + phoneNumbersArrayList.toString());

        messageEditText.setText(message);

    }

    public User getUser() {
        user = new User(userID, userName, userEmail, phoneNumbersArrayList, message);
        Log.v("User", "userDataFromSettingActivity: " + userID + "\n" + userName + "\n" + userEmail);
        return user;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    private void init() {
        phoneNumberEditText = (EditText)view.findViewById(R.id.item_phone_number_edit_text);
        addPhoneNumberImageButton = (ImageButton)view.findViewById(R.id.add_phone_number_image_button);
        settingsLinearLayout = (LinearLayout)view.findViewById(R.id.settings_linear_layout);
        messageEditText = (EditText)view.findViewById(R.id.messageEditText);
        saveSettingsButton = (Button)view.findViewById(R.id.saveSettingsButton);
        phoneNumbersArrayList = new ArrayList<>();
        preferences = getActivity().getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");
        auth = FirebaseAuth.getInstance();
        user = new User();
    }
}
