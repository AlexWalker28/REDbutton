package kg.kloop.android.redbutton.groups;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class MyGroupsTab extends Fragment {
    private ListView mygroupsListview;
    private GroupListAdapter adapter;
    private ArrayList<GroupMembership> myGroupsList;
    private DatabaseReference userGroupsReference;
    private DatabaseReference groupsReference;
    private String userId;
    private static final String TAG = "MyGroupsTab log";
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_tab2_my_groups, container, false);
        init();

        userGroupsReference.child(GroupDefaults.usersGroupsChild).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                updateListOnChildAdded(dataSnapshot, true);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                updateListOnChildRemoved(dataSnapshot);

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userGroupsReference.child(GroupDefaults.usersPendingChild).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                updateListOnChildAdded(dataSnapshot, false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                updateListOnChildRemoved(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return v;
    }

    private void init() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userGroupsReference = FirebaseDatabase.getInstance().getReference(GroupDefaults.usersBranch).child(userId);
        groupsReference = FirebaseDatabase.getInstance().getReference(GroupDefaults.groupsBranch);
        myGroupsList = new ArrayList<>();
        mygroupsListview = (ListView) v.findViewById(R.id.mygroupsListview);
        adapter = new GroupListAdapter(v.getContext(), myGroupsList, MyGroupsTab.this);
        mygroupsListview.setAdapter(adapter);
    }

    private void updateListOnChildAdded(DataSnapshot dataSnapshot, boolean isMember) {
        String groupname = dataSnapshot.getKey();
        GroupMembership groupMembership = new GroupMembership(groupname, isMember, !isMember);
        myGroupsList.add(groupMembership);
        adapter.notifyDataSetChanged();

    }

    private void updateListOnChildRemoved(DataSnapshot dataSnapshot) {
        String groupName = dataSnapshot.getKey();
        for (GroupMembership groupMembership : myGroupsList) {
            if (groupMembership.getGroupName().equals(groupName)) {
                myGroupsList.remove(groupMembership);
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void goToGroupActivity(final String groupName) {
        Intent intent = new Intent(getActivity(), GroupActivity.class);
        intent.putExtra("groupName", groupName);
        startActivity(intent);

    }
}