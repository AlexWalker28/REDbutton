package kg.kloop.android.redbutton.groups;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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


public class Approve extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference requestsRef;
    private DatabaseReference groupModeratorsRef;
    private ListView listView;
    //private ArrayList<String> requests;
    //ArrayAdapter<String> adapter;
    RequestsListAdapter adapter;
    ArrayList<Request> requests;
    TextView info;
    String userIdForChange;
    String reqId;
    String groupName;
    String userId;
    boolean isModerator;
    private static final String TAG = "myLog";

    private HashMap<String, Request> requestsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve);
        init();

    }

    private void init(){
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        isModerator = false;
        groupName = getIntent().getStringExtra("groupName");

        info = (TextView) findViewById(R.id.infotext);
        listView  = (ListView) findViewById(R.id.listView);
        requests = new ArrayList<>();

        requestsList = new HashMap<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        requestsRef = firebaseDatabase.getReference().child(GroupDefaults.groupsBranch).child(groupName).child(GroupDefaults.requestsChild);
        groupModeratorsRef = firebaseDatabase.getReference().child(GroupDefaults.groupsBranch).child(groupName).child(GroupDefaults.moderatorsChild);

        //requests = new ArrayList<>();
        //adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, requests);
        adapter = new RequestsListAdapter(this, requests);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Request request = (Request) listView.getAdapter().getItem(position);

                if (isModerator){
                    createAlertDialogForModerator(request);
                } else {
                    createAlertDialogForMember(request);
                }

//                String userName = ((TextView)view).getText().toString();
//
//                for (HashMap.Entry<String, Request> entry: requestsList.entrySet()){
//                    Request req = entry.getValue();
//                    if (req.getUserName().equals(userName)){
//                        userIdForChange = req.getUserId();
//                        reqId = entry.getKey();
//                        requestsRef.child(reqId).child(GroupDefaults.approvedChild).child(userId).setValue(true);
//                        Toast.makeText(Approve.this, "Вы одобрили заявку", Toast.LENGTH_SHORT).show();
//                    }
//                }
            }
        });

        requestsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Request request = dataSnapshot.getValue(Request.class);

                if (!isRequestAlreadyApproved(dataSnapshot)) {
                    requests.add(request);
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "requestsList size: " + Integer.toString(requestsList.size()));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (!isRequestAlreadyApproved(dataSnapshot)){

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

        groupModeratorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    if (postSnapshot.getKey().equals(userId)){
                        isModerator = true;
                        info.append("\nВы модератор в этой группе\n");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private boolean isRequestAlreadyApproved(DataSnapshot dataSnapshot){
        boolean isAlreadyApproved = false;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for (DataSnapshot child: dataSnapshot.getChildren()){
            if (child.getKey().equals(GroupDefaults.approvedChild)){
                //Map of users who approved this request
                Map<String, Boolean> users = (HashMap<String, Boolean>) child.getValue();
                //Looking for current user's Id in users
                for (String userId: users.keySet()){
                    if (userId.equals(currentUserId)){
                        isAlreadyApproved = true;
                        break;
                    }
                }
            }
        }

        return isAlreadyApproved;
    }

    private void deleteRequestFromList(DataSnapshot dataSnapshot){

        String key = dataSnapshot.getKey();
        for (Request request: requests){
            if (key.equals(request.getUserId())){
                requests.remove(request);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void createAlertDialogForModerator(final Request request){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private void createAlertDialogForMember(final Request request){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
