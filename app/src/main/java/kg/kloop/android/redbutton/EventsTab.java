package kg.kloop.android.redbutton;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;

import kg.kloop.android.redbutton.groups.GroupRoom;

/**
 * Created by alexwalker on 01.08.17.
 */

public class EventsTab extends Fragment {
    View v;
    ListView eventsListView;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    ArrayList<Event> eventsArrayList;
    EventsListViewAdapter adapter;
    ViewPager viewPager;
    SharedPreferences preferences;
    DatabaseReference groupsDatabaseReference;
    ArrayList<String> groupNamesArrayList;
    GroupRoom groupRoom;
    ArrayList<GroupRoom> groupRoomArrayList;
    String currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_tab2_events, container, false);
        init();

        getGroupsDataFromFirebase();

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Event event = dataSnapshot.getValue(Event.class);
                String userID = event.getUser().getUserID();
                Log.v("EventsTab", "current user: " + currentUser + "\n" +
                                    "event user: " + event.getUser().getUserName() + "\n" +
                                    "event userID: " + event.getUser().getUserID());
                for (GroupRoom room : groupRoomArrayList) {
                    if (room.getMembers().containsKey(userID) && room.getMembers().containsKey(currentUser)) {
                        eventsArrayList.add(event);
                        adapter.notifyDataSetChanged();
                        Log.v("usersToShow", "users: " + event.getUser().getUserName());
                    }
                }

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


        adapter = new EventsListViewAdapter(getContext(), R.layout.events_listview_item, eventsArrayList);
        eventsListView.setAdapter(adapter);
        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(Constants.EVENT_INDEX, Math.abs(eventsListView.getCount() - position - 1));
                editor.apply();
                Log.v("EventsTab", "saved index: " + preferences.getInt(Constants.EVENT_INDEX, 0));
                viewPager.setCurrentItem(0, true);
            }
        });

        return v;
    }

    private void getGroupsDataFromFirebase() {
        groupsDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                groupNamesArrayList.add(dataSnapshot.getKey());
                groupRoom = dataSnapshot.getValue(GroupRoom.class);
                groupRoomArrayList.add(groupRoom);
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
    }

    private void init() {
        eventsListView = (ListView)v.findViewById(R.id.eventsListView);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Events");
        groupsDatabaseReference = firebaseDatabase.getReference("Groups");
        eventsArrayList = new ArrayList<Event>();
        viewPager = (ViewPager) getActivity().findViewById(R.id.mapsViewPager);
        preferences = getActivity().getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        groupNamesArrayList = new ArrayList<>();
        groupRoom = new GroupRoom();
        groupRoomArrayList = new ArrayList<>();
        currentUser = preferences.getString(Constants.CURRENT_USER_ID, "");
    }

}
