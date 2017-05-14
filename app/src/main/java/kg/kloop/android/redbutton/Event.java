package kg.kloop.android.redbutton;


/**
 * Created by alexwalker on 15.04.17.
 */

public class Event {
    private User user;
    private CustomLatLng coordinates;
    private long timeInMillis;

    public Event() {
    }

    public Event(CustomLatLng coordinates, User user, long timestamp) {
        this.coordinates = coordinates;
        this.user = user;
        this.timeInMillis = timestamp;
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

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }
}
