package kg.kloop.android.redbutton.groups;

public class GroupMembership {
    private String groupName;
    private boolean isMember;
    private boolean isPending;
    private boolean isOnlyModeratorApprovingRequests;
    private boolean isModerator;

    public boolean isModerator() {
        return isModerator;
    }

    public void setModerator(boolean moderator) {
        isModerator = moderator;
    }

    public GroupMembership() {

    }

    public GroupMembership(String groupName, boolean isMember, boolean isPending, boolean isOnlyModeratorApprovingRequests) {
        this.groupName = groupName;
        this.isMember = isMember;
        this.isPending = isPending;
        this.isOnlyModeratorApprovingRequests = isOnlyModeratorApprovingRequests;

    }

    public GroupMembership(String groupName, boolean isMember, boolean isPending) {
        this.groupName = groupName;
        this.isMember = isMember;
        this.isPending = isPending;

    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isOnlyModeratorApprovingRequests() {
        return isOnlyModeratorApprovingRequests;
    }

    public void setOnlyModeratorApprovingRequests(boolean onlyModeratorApprovingRequests) {
        isOnlyModeratorApprovingRequests = onlyModeratorApprovingRequests;
    }

    public boolean isMember() {
        return isMember;

    }

    public void setMember(boolean member) {
        isMember = member;
    }

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }
}
