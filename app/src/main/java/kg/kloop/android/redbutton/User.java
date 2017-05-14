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

    public User() {
    }

    public User(String userID, String userName, String userEmail, String firstNumber, String secondNumber, String message) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
        this.message = message;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setMessageData(String firstNumber, String secondNumber, String message) {
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
        this.message = message;
    }
    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getFirstNumber() {
        return firstNumber;
    }

    public String getSecondNumber() {
        return secondNumber;
    }

    public String getMessage() {
        return message;
    }

}
