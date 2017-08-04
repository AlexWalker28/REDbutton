package kg.kloop.android.redbutton;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.alexwalker.sendsmsapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import kg.kloop.android.redbutton.groups.GroupRoom;

public class MapsTab extends Fragment implements OnMapReadyCallback{

    private GoogleMap mMap;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ArrayList<Event> eventArrayList;
    private LatLng eventLatLng;
    private String time;
    private MapView mapView;
    private View v;
    private SharedPreferences preferences;
    private ViewPager viewPager;
    private ProgressBar progressBar;
    private LatLng almaty;
    private DatabaseReference groupsDatabaseReference;
    private ArrayList<String> groupNamesArrayList;
    private GroupRoom groupRoom;
    private ArrayList<GroupRoom> groupRoomArrayList;
    String currentUser;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_tab1_maps, container, false);
        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        init();

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                getGroupsDataFromFirebase();

                databaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Event event = dataSnapshot.getValue(Event.class);
                        String userID = event.getUser().getUserID();
                        for (GroupRoom room : groupRoomArrayList) {
                            if (room.getMembers().containsKey(userID) && room.getMembers().containsKey(currentUser)) {
                                eventArrayList.add(event);
                            }
                        }

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        time = dateFormat.format(event.getTimeInMillis());
                        for(Event singleEvent : eventArrayList){
                            if(singleEvent.getCoordinates() != null) {
                                if(singleEvent.getCoordinates().getLat() != 0 && singleEvent.getCoordinates().getLng() != 0) {
                                    eventLatLng = new LatLng(singleEvent.getCoordinates().getLat(), singleEvent.getCoordinates().getLng());
                                    mMap.addMarker(new MarkerOptions().position(eventLatLng).title(time));
                                }
                            }
                        }
                        if(eventLatLng != null){
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, 17));
                        } else {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(almaty, 13));
                        }
                        progressBar.setVisibility(View.INVISIBLE);
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
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int index = preferences.getInt(Constants.EVENT_INDEX, 0);
                try {
                    Event pressedEvent = eventArrayList.get(index);
                    if(index == 0){
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(almaty, 17));
                    }
                    if (position == 0) {
                        if (pressedEvent.getCoordinates().getLat() != 0 && pressedEvent.getCoordinates().getLng() != 0) {
                            eventLatLng = new LatLng(pressedEvent.getCoordinates().getLat(), pressedEvent.getCoordinates().getLng());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, 17));
                            Log.v("MapsTab", "loaded index: " + index);
                        } else {
                            Toast.makeText(getContext(), R.string.noCoordinates, Toast.LENGTH_LONG).show();
                        }

                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

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

    private void resetPref() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.EVENT_INDEX, 0);
        editor.apply();
    }

    private void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Events");
        groupsDatabaseReference = firebaseDatabase.getReference().child("Groups");
        eventArrayList = new ArrayList<>();
        groupNamesArrayList = new ArrayList<>();
        groupRoom = new GroupRoom();
        groupRoomArrayList = new ArrayList<>();
        preferences = getActivity().getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        viewPager = (ViewPager) getActivity().findViewById(R.id.mapsViewPager);
        progressBar = (ProgressBar)v.findViewById(R.id.mapsProgressBar);
        almaty = new LatLng(43.250384, 76.911368);
        currentUser = preferences.getString(Constants.CURRENT_USER_ID, "");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        resetPref();
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

}
