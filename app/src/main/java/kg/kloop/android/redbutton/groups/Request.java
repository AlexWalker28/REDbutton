package kg.kloop.android.redbutton.groups;

/**
 * Created by erlan on 28.04.2017.
 */

public class Request {
    private int agreeCount;
    private String userId;
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Request() {

    }

    public Request(String userId, String userName) {
        this(0, userId, userName);
    }

    public Request(int agreeCount, String userId, String userName) {
        this.agreeCount = agreeCount;
        this.userId = userId;
        this.userName = userName;
    }

    public int getAgreeCount() {
        return agreeCount;
    }

    public void setAgreeCount(int agreeCount) {
        this.agreeCount = agreeCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
