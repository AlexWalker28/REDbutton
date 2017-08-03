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

import java.util.ArrayList;
import java.util.Comparator;

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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_tab2_events, container, false);
        init();

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                eventsArrayList.add(dataSnapshot.getValue(Event.class));
                adapter.notifyDataSetChanged();
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
                editor.putInt(Constants.EVENT_INDEX, position);
                editor.apply();
                Log.v("EventsTab", "saved index: " + preferences.getInt(Constants.EVENT_INDEX, 0));
                viewPager.setCurrentItem(0, true);
            }
        });

        return v;
    }

    private void init() {
        eventsListView = (ListView)v.findViewById(R.id.eventsListView);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Events");
        eventsArrayList = new ArrayList<Event>();
        viewPager = (ViewPager) getActivity().findViewById(R.id.mapsViewPager);
        preferences = getActivity().getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
    }

}
