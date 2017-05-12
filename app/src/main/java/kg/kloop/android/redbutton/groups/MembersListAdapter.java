package kg.kloop.android.redbutton.groups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.alexwalker.sendsmsapp.R;

import java.util.ArrayList;

public class MembersListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Member> members;

    public MembersListAdapter(Context context, ArrayList<Member> members) {
        this.context = context;
        this.members = members;
    }


    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Object getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.request_list_item, parent, false);
        }
        TextView userNameTextView = (TextView) convertView.findViewById(R.id.request_item_userName);
        TextView infoTextView = (TextView) convertView.findViewById(R.id.request_item_info);

        Member member = members.get(position);
        userNameTextView.setText(member.getName());

        if (member.isModerator()){
            infoTextView.setText("модератор");
        }

        return convertView;
    }
}
