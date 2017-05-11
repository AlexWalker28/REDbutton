package kg.kloop.android.redbutton;

/**
 * Created by alexwalker on 23.04.17.
 */

class Approvement {
    private User user;

    public Approvement() {
    }

    public Approvement(User user) {
        this.user = user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
