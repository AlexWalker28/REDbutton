package kg.kloop.android.redbutton;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by alexwalker on 15.04.17.
 */

public class Event {
    private User user;
    private CustomLatLng coordinates;

    public Event() {
    }

    public Event(CustomLatLng coordinates, User user) {
        this.coordinates = coordinates;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CustomLatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(CustomLatLng coordinates) {
        this.coordinates = coordinates;
    }
}
