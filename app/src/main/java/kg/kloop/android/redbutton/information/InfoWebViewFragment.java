package kg.kloop.android.redbutton.information;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.alexwalker.sendsmsapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoWebViewFragment extends Fragment {


    public InfoWebViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_web_view, container, false);

        WebView webView = (WebView)view.findViewById(R.id.info_web_view);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        // Configure the client to use when opening URLs
        webView.setWebViewClient(new WebViewClient());
        // Load the initial URL
        webView.loadUrl("http://sozsende.info/pages");

        return view;
    }

}
