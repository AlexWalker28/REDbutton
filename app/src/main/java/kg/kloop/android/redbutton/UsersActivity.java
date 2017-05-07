package kg.kloop.android.redbutton;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity {

    ListView usersListView;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private ArrayAdapter adapter;
    private ArrayList<User> userArrayList;
    private ArrayList<String>userNameArrayList;
    private User user;
    private String currentUserID;
    private String currentUserName;
    private Approvement approvement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        init();
        Intent intent = getIntent();
        currentUserID = intent.getExtras().getString("currentUserID");
        currentUserName = intent.getExtras().getString("currentUserName");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                user = dataSnapshot.getValue(User.class);
                userArrayList.add(user);
                userNameArrayList.add(user.getUserName());
                adapter.notifyDataSetChanged();
                /*if(user.getApprovement().getCount() < 2) {
                    userArrayList.add(user);
                    for (User user : userArrayList){
                        userNameArrayList.add(user.getUserName());
                    }
                }*/
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        adapter = new ArrayAdapter<>(UsersActivity.this, R.layout.support_simple_spinner_dropdown_item, userNameArrayList);
        usersListView.setAdapter(adapter);

        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = new User();
                user.setUserID(currentUserID);
                user.setUserName(currentUserName);
                approvement.setUser(user);


                databaseReference.child(userArrayList.get(position).getUserID()).child("approvements").child(currentUserID).setValue(approvement);
            }
        });

    }

    private void  init(){
        usersListView = (ListView)findViewById(R.id.usersListView);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");
        userArrayList = new ArrayList<>();
        userNameArrayList = new ArrayList<>();
        user = new User();
        approvement = new Approvement();
    }
}
