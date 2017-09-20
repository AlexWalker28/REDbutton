package kg.kloop.android.redbutton.information;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alexwalker.sendsmsapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class RSSFeedFragment extends Fragment {


    public RSSFeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rssfeed, container, false);
    }

}
