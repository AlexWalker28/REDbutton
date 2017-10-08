package kg.kloop.android.redbutton.information;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.alexwalker.sendsmsapp.R;

import java.util.List;

/**
 * Created by alexwalker on 20.09.17.
 */

public class RssFeedListAdapter
        extends RecyclerView.Adapter<RssFeedListAdapter.FeedModelViewHolder> {

    private List<RSSFeedFragment.RssFeedModel> mRssFeedModels;
    private FragmentManager fragmentManager;

    public class FeedModelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView titleTextView;
        private TextView descriptionTextView;
        private TextView linkTextView;

        public FeedModelViewHolder(View v) {
            super(v);
            titleTextView = (TextView)v.findViewById(R.id.titleText);
            descriptionTextView = (TextView)v.findViewById(R.id.descriptionText);
            linkTextView = (TextView)v.findViewById(R.id.linkText);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String link = mRssFeedModels.get(getAdapterPosition()).link;
            InfoWebViewFragment infoWebViewFragment = new InfoWebViewFragment();
            infoWebViewFragment.setUrl(link);

            FragmentManager manager = fragmentManager;
            FragmentTransaction transaction;
            transaction = manager.beginTransaction();
            transaction.add(R.id.main_frame_layout, infoWebViewFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public RssFeedListAdapter(List<RSSFeedFragment.RssFeedModel> rssFeedModels, FragmentManager fragmentManager) {
        mRssFeedModels = rssFeedModels;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public FeedModelViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rss_feed, parent, false);
        FeedModelViewHolder holder = new FeedModelViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(FeedModelViewHolder holder, int position) {
        RSSFeedFragment.RssFeedModel rssFeedModel = mRssFeedModels.get(position);
        holder.titleTextView.setText(rssFeedModel.title);
        holder.descriptionTextView.setText(rssFeedModel.description);
        holder.linkTextView.setText(rssFeedModel.link);
    }

    @Override
    public int getItemCount() {
        return mRssFeedModels.size();
    }
}