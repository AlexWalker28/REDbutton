package kg.kloop.android.redbutton;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LocationService extends Service {
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
        event = new Event();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Events");
        childKey = intent.getStringExtra(Constants.DATABASE_CHILD_ID);
        firstPhoneNumber = intent.getStringExtra(Constants.FIRST_NUMBER);
        secondPhoneNumber = intent.getStringExtra(Constants.SECOND_NUMBER);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setLocationListener();
        requestLocationUpdates();
        sendDataBack();
        stopSelf();

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
                    Log.v("service", "Location: " + event.getCoordinates().getLat() + " " + event.getCoordinates().getLng());
                }
                databaseReference.child(childKey).child("coordinates").setValue(coordinates);
                sendSMS(firstPhoneNumber);
                sendSMS(secondPhoneNumber);
                sendNotification();
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


    private void sendNotification() {
        if(event.getCoordinates() != null) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Coordinates")
                    .setContentText("lat: " + event.getCoordinates().getLat() + "\n" + "lng: " + event.getCoordinates().getLng());
            int notificationID = 001;
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(notificationID, notificationBuilder.build());
        }
    }

}
