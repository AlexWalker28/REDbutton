package kg.kloop.android.redbutton;

import java.util.List;
import java.util.Set;

/**
 * Created by alexwalker on 16.04.17.
 */

class User {
    private String userID;
    private String userName;
    private String userEmail;
    private List<String> phoneNumbers;
    private String message;
    private String phoneNumber;

    public User() {
    }

    public User(String userID, String userName, String userEmail, List<String> phoneNumbers, String message, String phoneNumber) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.phoneNumbers = phoneNumbers;
        this.message = message;
        this.phoneNumber = phoneNumber;
    }

    public User(String userID, String userName, String userEmail, List<String> phoneNumbers, String message) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.phoneNumbers = phoneNumbers;
        this.message = message;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
