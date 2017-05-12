package kg.kloop.android.redbutton.groups;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class AllGroupsTab extends Fragment implements View.OnClickListener {
    private static final String TAG = "AllGroupsTab log";
    private Button createGroup;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference groupsReference;
    private ListView groupsList;
    private ArrayList<GroupMembership> groupMembershipList;
    GroupListAdapter adapter2;
    private String userId;
    View v;
    DatabaseReference mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_tab1_all_groups, container, false);

        init();
        groupMembershipList = new ArrayList<>();

        adapter2 = new GroupListAdapter(v.getContext(), groupMembershipList, AllGroupsTab.this);
        groupsList.setAdapter(adapter2);

        return v;
    }

    private void init() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        createGroup = (Button) v.findViewById(R.id.buttonPush);
        createGroup.setOnClickListener(this);
        groupsList = (ListView) v.findViewById(R.id.groupsListView);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        groupsReference = firebaseDatabase.getReference(GroupDefaults.groupsBranch);

        groupsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                updateListOnChildAddedOrChildChanged(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "Child changed");
                updateListOnChildAddedOrChildChanged(dataSnapshot);
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


    }


    void sendRequest(String groupName) {
        String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Request request = new Request(Uid, userName);
        groupsReference.child(groupName).child(GroupDefaults.requestsChild).child(userId).setValue(request);
        Toast.makeText(v.getContext(), "Запрос отправлен", Toast.LENGTH_SHORT).show();
    }

    private boolean isInPending(DataSnapshot postSnapshot, String userId) {
        boolean isPending = false;
        Iterable requests = postSnapshot.child(GroupDefaults.requestsChild).getChildren();
        Iterator i = requests.iterator();
        while (i.hasNext()) {
            DataSnapshot value = (DataSnapshot) i.next();
            Request r = value.getValue(Request.class);
            if (userId.equals(r.getUserId())) {
                isPending = true;
                break;
            }
        }

        return isPending;
    }

    private boolean isInMembers(DataSnapshot postSnapshot, String userId) {
        boolean isMember = false;
        Iterable members = postSnapshot.child(GroupDefaults.membersChild).getChildren();
        Iterator memberIterator = members.iterator();
        while (memberIterator.hasNext()) {
            DataSnapshot member = (DataSnapshot) memberIterator.next();
            if (userId.equals(member.getKey())) {
                isMember = true;
                break;
            }
        }

        return isMember;
    }

    private void updateListOnChildAddedOrChildChanged(DataSnapshot dataSnapshot) {
        String groupName = dataSnapshot.getKey();
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setGroupName(groupName);
        groupMembership.setPending(isInPending(dataSnapshot, userId));
        groupMembership.setMember(isInMembers(dataSnapshot, userId));

        boolean flag = false;
        for (GroupMembership gMembership : groupMembershipList) {
            if (gMembership.getGroupName().equals(groupName)) {
                groupMembershipList.remove(gMembership);
                groupMembershipList.add(groupMembership);
                flag = true;
                break;
            }
        }

        if (!flag) {
            groupMembershipList.add(groupMembership);
        }
        adapter2.notifyDataSetChanged();
    }

    private void updateListOnChildRemoved(DataSnapshot dataSnapshot) {
        String groupName = dataSnapshot.getKey();
        for (GroupMembership groupMembership : groupMembershipList) {
            if (groupMembership.getGroupName().equals(groupName)) {
                groupMembershipList.remove(groupMembership);
                break;
            }
        }
        adapter2.notifyDataSetChanged();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.buttonPush:
                createNewGroupAlertDialog();
                break;
        }
    }

    private void createNewGroupAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.new_group_dialog, null);
        builder.setView(dialog);
        builder.setTitle("Настройки группы");
        final EditText newGroupName;
        final RadioButton radioButtonOnlyModerator;
        final RadioButton radioButtonModeratorAndUsers;
        final Spinner spinner;
        final TextView requiredCount;

        newGroupName = (EditText) dialog.findViewById(R.id.dialog_groupname);
        requiredCount = (TextView) dialog.findViewById(R.id.dialog_required_count);
        radioButtonOnlyModerator = (RadioButton) dialog.findViewById(R.id.dialog_radio_only_moderator);
        radioButtonModeratorAndUsers = (RadioButton) dialog.findViewById(R.id.dialog_radio_moderaotr_and_users);
        spinner = (Spinner) dialog.findViewById(R.id.dialog_spinner);
        spinner.setEnabled(false);
        requiredCount.setVisibility(View.INVISIBLE);
        spinner.setVisibility(View.INVISIBLE);
        //String[] requiredCounts = {"2", "3", "4", "5", "6", "7"};
        String[] requiredCounts = {"1", "2", "3", "4", "5", "6", "7"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_item, requiredCounts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        builder.setPositiveButton("Создать группу", null);
        builder.setNegativeButton("Отмена", null);
        radioButtonOnlyModerator.setChecked(true);
        radioButtonModeratorAndUsers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //only moderator can add new users
                    spinner.setEnabled(true);
                    requiredCount.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.VISIBLE);
                } else {
                    spinner.setEnabled(false);
                    requiredCount.setVisibility(View.INVISIBLE);
                    spinner.setVisibility(View.INVISIBLE);
                }
            }
        });

        radioButtonOnlyModerator.setChecked(true);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button buttonPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                Button buttonNegative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        final String groupName = newGroupName.getText().toString().trim();
                        if (groupName.isEmpty()) {
                            Toast.makeText(v.getContext(), "Название группы не может быть пустым", Toast.LENGTH_SHORT).show();
                        } else {
                            //check this groupName in firebase Database, create group if this name is available
                            groupsReference.child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        //other group with this groupname already exists in firebase database
                                        Toast.makeText(v.getContext(), "Такое название группы уже занято", Toast.LENGTH_SHORT).show();
                                    } else {
                                        //groupName is available, create new group
                                        boolean onlyModeratorCanApproveRequests = radioButtonOnlyModerator.isChecked();
                                        int requiredAmountOfApproves = Integer.parseInt(spinner.getSelectedItem().toString());
                                        GroupRoom group = new GroupRoom(groupName, userId, requiredAmountOfApproves, onlyModeratorCanApproveRequests);
                                        groupsReference.child(groupName).setValue(group);
                                        Toast.makeText(v.getContext(), "Группа создана", Toast.LENGTH_SHORT).show();
                                        alertDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(v.getContext(), "Ошибка доступа к базе данных", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

                buttonNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.show();
    }
}