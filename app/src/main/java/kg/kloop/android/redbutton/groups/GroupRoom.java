package kg.kloop.android.redbutton.groups;

import java.util.HashMap;
import java.util.Map;

public class GroupRoom {
    private String name;
    private int requiredAmountOfApprovals;
    Map <String, Boolean> moderators;
    Map <String, Boolean> members;
    private boolean onlyModeratorApprovingRequests;

    public boolean isOnlyModeratorApprovingRequests() {
        return onlyModeratorApprovingRequests;
    }

    public void setOnlyModeratorApprovingRequests(boolean onlyModeratorApprovingRequests) {
        this.onlyModeratorApprovingRequests = onlyModeratorApprovingRequests;
    }

    public int getRequiredAmountOfApprovals() {
        return requiredAmountOfApprovals;
    }

    public void setRequiredAmountOfApprovals(int requiredAmountOfApprovals) {
        this.requiredAmountOfApprovals = requiredAmountOfApprovals;
    }


    public GroupRoom(){
        this("");

    }

    public GroupRoom(String name) {
        this.name = name;
        members = new HashMap<>();
        moderators = new HashMap<>();
        requiredAmountOfApprovals = 2;
    }

    public GroupRoom(String name, String groupCreator) {
        this.name = name;
        this.members = new HashMap<>();
        this.moderators = new HashMap<>();
        this.members.put(groupCreator, true);
        this.moderators.put(groupCreator, true);
        requiredAmountOfApprovals = 2;
        this.onlyModeratorApprovingRequests = false;
    }

    public GroupRoom(String groupName, String groupCreator, int requiredAmountOfApprovals, boolean onlyModeratorApprovingRequests){
        this.name = groupName;
        this.members = new HashMap<>();
        this.moderators = new HashMap<>();
        this.members.put(groupCreator, true);
        this.moderators.put(groupCreator, true);
        this.requiredAmountOfApprovals = requiredAmountOfApprovals;
        this.onlyModeratorApprovingRequests = onlyModeratorApprovingRequests;
    }

    public String getName(){
        return this.name;
    }

    public Map<String, Boolean> getMembers() {
        return members;
    }

    public void setName(String name){
        this.name = name;
    }

}
