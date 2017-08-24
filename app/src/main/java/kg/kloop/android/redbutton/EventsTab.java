package kg.kloop.android.redbutton;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
    DatabaseReference messageDatabaseReference;
    ArrayList<String> groupNamesArrayList;
    GroupRoom groupRoom;
    ArrayList<GroupRoom> groupRoomArrayList;
    String currentUser;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_tab2_events, container, false);
        init();

        getGroupsDataFromFirebase();

        getEventsDataFromFirebase();


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

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetGroupsData();
                getGroupsDataFromFirebase();
                resetEventsData();
                getEventsDataFromFirebase();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return v;
    }


    private void getEventsDataFromFirebase() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Event event = dataSnapshot.getValue(Event.class);
                messageDatabaseReference = firebaseDatabase.getReference("Users").child(event.getUser().getUserID()).child("message");
                //since Events branch doesn't have user's messages in it, we need to add them
                getEventWithMessage(event);

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

        private Event getEventWithMessage(final Event event){
            messageDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String message = dataSnapshot.getValue(String.class);
                    event.getUser().setMessage(message);

                    String userID = event.getUser().getUserID();
                    //add only events from users from the same groups as user
                    for (GroupRoom room : groupRoomArrayList) {
                        if (room.getMembers().containsKey(userID) && room.getMembers().containsKey(currentUser)) {
                            eventsArrayList.add(event);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return event;
        }
    private void resetEventsData() {
        eventsArrayList.clear();
        adapter.notifyDataSetChanged();
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
    private void resetGroupsData() {
        groupNamesArrayList.clear();
        groupRoomArrayList.clear();
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
        swipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.swiperefresh);

    }

}
