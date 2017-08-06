package kg.kloop.android.redbutton;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by ThirtySeven on 13.05.2017.
 */

public class SampleSlide extends Fragment
        //implements View.OnClickListener
        {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private int layoutResId;
    Button goToSettingsButton;
    Button regButton;

    public static SampleSlide newInstance(int layoutResId) {
        SampleSlide sampleSlide = new SampleSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
        }

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutResId, container, false);
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.regButton:
//                Toast.makeText(getContext(), "rtrt", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.goToSettingsButton:
//
//                break;
//        }
//    }
}
