package kg.kloop.android.redbutton;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.alexwalker.sendsmsapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by alexwalker on 01.08.17.
 */

class EventsListViewAdapter extends ArrayAdapter<Event> {

    private Event currentEvent;

    public EventsListViewAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Event> events) {
        super(context, resource, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.events_listview_item, parent, false);
        }

        currentEvent = getItem(super.getCount() - position - 1);


        TextView userNameTextView = (TextView) listItemView.findViewById(R.id.userNameTextView);
        userNameTextView.setText(currentEvent.getUser().getUserName());

        TextView messageTextView = (TextView)listItemView.findViewById(R.id.messageTextView);
        messageTextView.setText(currentEvent.getUser().getMessage());

        TextView timeTextView = (TextView)listItemView.findViewById(R.id.timeTextView);
        /*String time;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        time = dateFormat.format(currentEvent.getTimeInMillis());*/
        timeTextView.setText(DateUtils.getRelativeTimeSpanString(currentEvent.getTimeInMillis(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS));

        return listItemView;
    }


}
