package kg.kloop.android.redbutton.groups;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestsTab extends Fragment {

    private static final String TAG = "RequestsTab log";
    private View v;
    private DatabaseReference requestsRef;
    private DatabaseReference groupModeratorsRef;
    private ListView listView;
    RequestsListAdapter adapter;
    ArrayList<Request> requests;
    TextView info;
    String groupName;
    String userId;
    boolean isModerator;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_requests_tab, container, false);

        init();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Request request = (Request) listView.getAdapter().getItem(position);

                if (isModerator) {
                    createAlertDialogForModerator(request);
                } else {
                    createAlertDialogForMember(request);
                }
            }
        });

        requestsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Request request = dataSnapshot.getValue(Request.class);

                if (!isRequestAlreadyApproved(dataSnapshot)) {
                    requests.add(request);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (!isRequestAlreadyApproved(dataSnapshot)) {

                } else {
                    deleteRequestFromList(dataSnapshot);
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                deleteRequestFromList(dataSnapshot);
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
        groupName = ((GroupActivity) v.getContext()).groupName;
        requestsRef = FirebaseDatabase.getInstance().getReference().child(GroupDefaults.groupsBranch).child(groupName).child(GroupDefaults.requestsChild);
        groupModeratorsRef = FirebaseDatabase.getInstance().getReference().child(GroupDefaults.groupsBranch).child(groupName).child(GroupDefaults.moderatorsChild);
        info = (TextView) v.findViewById(R.id.requests_tab_info);
        info.setText("Ожидают подтверждения:");

        listView = (ListView) v.findViewById(R.id.requests_tab_listview);
        requests = new ArrayList<>();
        adapter = new RequestsListAdapter(v.getContext(), requests);
        listView.setAdapter(adapter);


        groupModeratorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot.getKey().equals(userId)) {
                        isModerator = true;
                        info.setText("\nВы модератор в этой группе\nОжидают подтверждения:");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private boolean isRequestAlreadyApproved(DataSnapshot dataSnapshot) {
        boolean isAlreadyApproved = false;
        String currentUserId = userId;
        for (DataSnapshot child : dataSnapshot.getChildren()) {
            if (child.getKey().equals(GroupDefaults.approvedChild)) {
                //Map of users who approved this request
                Map<String, Boolean> users = (HashMap<String, Boolean>) child.getValue();
                //Looking for current user's Id in users
                for (String userId : users.keySet()) {
                    if (userId.equals(currentUserId)) {
                        isAlreadyApproved = true;
                        break;
                    }
                }
            }
        }

        return isAlreadyApproved;
    }

    private void deleteRequestFromList(DataSnapshot dataSnapshot) {

        String key = dataSnapshot.getKey();
        for (Request request : requests) {
            if (key.equals(request.getUserId())) {
                requests.remove(request);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void createAlertDialogForModerator(final Request request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Рассмотрение заявки");
        builder.setMessage("Одобрить заявку от " + request.getUserName() + "?." +
                "\nВы модератор в этой группе, одобрив заявку, вы добавите пользователя в группу");
        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child(GroupDefaults.groupsBranch).child(groupName);
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/" + GroupDefaults.membersChild + "/" + request.getUserId(), true);
                childUpdates.put("/" + GroupDefaults.requestsChild + "/" + request.getUserId(), null);
                groupRef.updateChildren(childUpdates);
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createAlertDialogForMember(final Request request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Рассмотрение заявки");
        builder.setMessage("Одобрить заявку от " + request.getUserName() + "?." +
                "\nПользователь будет добавлен в группу когда наберет необходимое количество одобрений");
        builder.setPositiveButton("Одобрить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestsRef.child(request.getUserId()).child(GroupDefaults.approvedChild).child(userId).setValue(true);

            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
