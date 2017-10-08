package kg.kloop.android.redbutton.information;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.alexwalker.sendsmsapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoWebViewFragment extends Fragment {
    String url = "";
    SwipeRefreshLayout swipeRefreshLayout;
    WebView webView;

    public InfoWebViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_web_view, container, false);

        webView = (WebView)view.findViewById(R.id.info_web_view);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.rss_feed_item_swipe_refresh_layout);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        // Configure the client to use when opening URLs
        //webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress == 100){
                    swipeRefreshLayout.setRefreshing(false);
                } else swipeRefreshLayout.setRefreshing(true);
            }
        });
        // Load the initial URL
        if(url.length() != 0){
            webView.loadUrl(url);
        } else webView.loadUrl("http://sozsende.info/pages");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });

        return view;
    }

    public void setUrl(String url){
        this.url = url;
    }

}
