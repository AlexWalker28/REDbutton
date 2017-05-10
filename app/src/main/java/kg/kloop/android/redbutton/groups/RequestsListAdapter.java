package kg.kloop.android.redbutton.groups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.alexwalker.sendsmsapp.R;

import java.util.ArrayList;

public class RequestsListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Request> requestsList;

    public RequestsListAdapter(Context context, ArrayList<Request> requestsList) {
        this.context = context;
        this.requestsList = requestsList;
    }

    @Override
    public int getCount() {
        return requestsList.size();
    }

    @Override
    public Object getItem(int position) {
        return requestsList.get(position);
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

        Request request = requestsList.get(position);
        userNameTextView.setText(request.getUserName());

        return convertView;
    }
}
