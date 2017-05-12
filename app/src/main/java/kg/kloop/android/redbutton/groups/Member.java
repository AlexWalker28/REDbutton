package kg.kloop.android.redbutton.groups;

public class Member {
    private String name;
    private boolean isModerator;

    public Member(String name, boolean isModerator){
        this.name = name;
        this.isModerator = isModerator;
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
