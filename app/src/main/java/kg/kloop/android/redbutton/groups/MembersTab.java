package kg.kloop.android.redbutton.groups;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class MembersTab extends Fragment {

    private static final String TAG = "MembersTab log";
    private ListView groupsList;
    private ArrayList<GroupMembership> groupMembershipList;
    GroupListAdapter adapter2;
    private String userId;
    View v;
    DatabaseReference mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_members_tab, container, false);

        init();

        return v;
    }

    private void init(){

    }

}
