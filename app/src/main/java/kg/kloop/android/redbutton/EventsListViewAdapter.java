package kg.kloop.android.redbutton;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by alexwalker on 01.08.17.
 */

class EventsListViewAdapter extends ArrayAdapter<Event> {

    ArrayList eventsArrayList;
    public EventsListViewAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Event> events) {
        super(context, resource, events);
        //eventsArrayList = events;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.events_listview_item, parent, false);
        }

        Event currentEvent = getItem(position);

        TextView userNameTextView = (TextView) listItemView.findViewById(R.id.userNameTextView);
        userNameTextView.setText(currentEvent.getUser().getUserName());

        TextView timeTextView = (TextView)listItemView.findViewById(R.id.timeTextView);
        String time;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        time = dateFormat.format(currentEvent.getTimeInMillis());
        timeTextView.setText(time);



        return listItemView;
    }


}
