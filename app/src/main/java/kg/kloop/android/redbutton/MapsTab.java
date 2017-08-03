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

public class MapsTab extends Fragment implements OnMapReadyCallback{

    private GoogleMap mMap;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ArrayList<Event> eventArrayList;
    private LatLng eventLatLng;
    private String time;
    MapView mapView;
    View v;
    SharedPreferences preferences;
    ViewPager viewPager;


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

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Events");
        eventArrayList = new ArrayList<>();
        preferences = getActivity().getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        viewPager = (ViewPager) getActivity().findViewById(R.id.mapsViewPager);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                databaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Event event = dataSnapshot.getValue(Event.class);
                        eventArrayList.add(event);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss", Locale.getDefault());
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
                            LatLng almaty = new LatLng(43.250384, 76.911368);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(almaty, 13));
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
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int index = preferences.getInt(Constants.EVENT_INDEX, 0);
                Event pressedEvent = eventArrayList.get(index);
                if(position == 0) {
                    if(pressedEvent.getCoordinates().getLat() != 0 && pressedEvent.getCoordinates().getLng() != 0) {
                        eventLatLng = new LatLng(pressedEvent.getCoordinates().getLat(), pressedEvent.getCoordinates().getLng());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, 17));
                        Log.v("MapsTab", "loaded index: " + index);
                    } else {
                        Toast.makeText(getContext(), R.string.noCoordinates, Toast.LENGTH_LONG).show();
                    }

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return v;
    }

    private void resetPref() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.EVENT_INDEX, 0);
        editor.apply();
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
