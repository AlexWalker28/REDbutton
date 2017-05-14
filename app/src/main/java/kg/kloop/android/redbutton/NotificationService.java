package kg.kloop.android.redbutton;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kg.kloop.android.redbutton.groups.GroupRoom;

public class NotificationService extends Service {
    FirebaseDatabase firebaseDatabase;
    DatabaseReference eventDatabaseReference;
    DatabaseReference groupsDatabaseReference;
    ArrayList<String> groupNamesArrayList;
    User user;
    Event event;
    String time;
    GroupRoom groupRoom;
    ArrayList<GroupRoom> groupRoomArrayList;
    String currentUser;
    SharedPreferences preferences;

    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventDatabaseReference = firebaseDatabase.getReference().child("Events");
        groupsDatabaseReference = firebaseDatabase.getReference().child("Groups");
        groupNamesArrayList = new ArrayList<>();
        user = new User();
        groupRoom = new GroupRoom();
        groupRoomArrayList = new ArrayList<>();
        preferences = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);

        groupsDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                groupNamesArrayList.add(dataSnapshot.getKey());
                groupRoom = dataSnapshot.getValue(GroupRoom.class);
                groupRoomArrayList.add(groupRoom);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                groupNamesArrayList.add(dataSnapshot.getKey());
                groupRoom = dataSnapshot.getValue(GroupRoom.class);
                for(int i = 0; i < groupRoomArrayList.size(); i ++){
                    if (groupRoomArrayList.get(i).getName() == groupRoom.getName()){
                        groupRoomArrayList.set(i, groupRoom);
                    }
                }
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

        eventDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                event = dataSnapshot.getValue(Event.class);
                try {
                    currentUser = preferences.getString(Constants.CURRENT_USER_ID, "");
                } catch (Exception e){
                    e.printStackTrace();
                }
                user = event.getUser();
                Log.v("user", "eventUser: " + user.getUserID() + "\ncurrentUser: " + currentUser);
                if(currentUser == user.getUserID()){
                    showNotification("");
                }
                for(GroupRoom room : groupRoomArrayList){
                    if(room.getMembers().containsKey(user.getUserID()) && room.getMembers().containsKey(currentUser)){
                        showNotification(room.getName());
                        Log.v("containsKey", "contains key: " + true);
                    }
                    Log.v("target", "group: " + room.getName() + "\nmembers: " + room.getMembers());

                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                time = dateFormat.format(event.getTimeInMillis());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

        return START_STICKY;
    }

    private void showNotification(String name) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(user.getUserName()+ " from " + name + " pressed Red Button")
                .setContentText(time);
        int notificationID = 001;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, notificationBuilder.build());
    }
}
