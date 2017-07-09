package kg.kloop.android.redbutton;

/**
 * Created by alexwalker on 16.04.17.
 */

class User {
    private String userID;
    private String userName;
    private String userEmail;
    private String firstNumber;
    private String secondNumber;
    private String message;
    private String phoneNumber;

    public User() {
    }

    public User(String userID, String userName, String userEmail, String firstNumber, String secondNumber, String message, String phoneNumber) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
        this.message = message;
        this.phoneNumber = phoneNumber;
    }

    public User(String userID, String userName, String userEmail, String firstNumber, String secondNumber, String message) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
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

    public String getFirstNumber() {
        return firstNumber;
    }

    public void setFirstNumber(String firstNumber) {
        this.firstNumber = firstNumber;
    }

    public String getSecondNumber() {
        return secondNumber;
    }

    public void setSecondNumber(String secondNumber) {
        this.secondNumber = secondNumber;
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
