package kg.kloop.android.redbutton.groups;

public class Member {
    private String name;
    private boolean isModerator;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Member(String name, boolean isModerator, String userId){
        this.name = name;
        this.isModerator = isModerator;
        this.userId = userId;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isModerator() {
        return isModerator;
    }

    public void setModerator(boolean moderator) {
        isModerator = moderator;
    }
}
