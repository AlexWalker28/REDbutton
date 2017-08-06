package kg.kloop.android.redbutton;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LocationService extends Service {
    private static final String TAG = "LocationService";
    Event event;
    LocationListener locationListener;
    LocationManager locationManager;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    CustomLatLng coordinates;
    String childKey;
    String firstPhoneNumber;
    String secondPhoneNumber;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Location service is running");
        event = new Event();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Events");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        /*try {
            childKey = intent.getStringExtra(Constants.DATABASE_CHILD_ID);
            firstPhoneNumber = intent.getStringExtra(Constants.FIRST_NUMBER);
            secondPhoneNumber = intent.getStringExtra(Constants.SECOND_NUMBER);
        } catch (Exception e){
            e.printStackTrace();
        }*/
        getDataFromPref();
        setLocationListener();
        requestLocationUpdates();
        sendDataBack();

        return super.onStartCommand(intent, flags, startId);
    }


    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, locationListener);
        }
    }

    private void sendDataBack() {
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        if(event.getCoordinates() != null) {
            localIntent.putExtra(Constants.EVENT_LAT, event.getCoordinates().getLat());
            localIntent.putExtra(Constants.EVENT_LNG, event.getCoordinates().getLng());
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
    }

    void setLocationListener(){
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                coordinates = new CustomLatLng(location.getLatitude(), location.getLongitude());
                if(event.getCoordinates() != null) {
                    Log.v(TAG, "Location: " + event.getCoordinates().getLat() + " " + event.getCoordinates().getLng());
                }
                databaseReference.child(childKey).child("coordinates").setValue(coordinates);
                sendSMS(firstPhoneNumber);
                sendSMS(secondPhoneNumber);
                stopSelf();
                Log.v(TAG, "Location service should stop (onLocationChanged)");
                locationManager.removeUpdates(locationListener);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

    }

    private void sendSMS(String phoneNumber) {
        SmsManager smsManager = SmsManager.getDefault();
        String message = "My coordinates: ";
        if (coordinates != null) { //send SMS with coordinates
            smsManager.sendTextMessage(phoneNumber, null, message
                    + "\nhttp://maps.google.com/maps?q="
                    + coordinates.getLat()
                    + "," + coordinates.getLng(), null, null);
        }
    }

    private void getDataFromPref(){
        SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
        childKey = preferences.getString(Constants.DATABASE_CHILD_ID, null);
        firstPhoneNumber = preferences.getString(Constants.FIRST_NUMBER, null);
        secondPhoneNumber = preferences.getString(Constants.SECOND_NUMBER, null);
        Log.v(TAG, "childKey: " + childKey + "\nfirst number: " + firstPhoneNumber
                                + "\nsecond number: " + secondPhoneNumber);
    }

}
