package com.camnter.newlife.ui.fragment.tabalphaindicatorfragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Description：TabAlphaFragment
 * Created by：CaMnter
 */

public class TabAlphaFragment extends Fragment {

    private static final String BUNDLE_CONTENT = "bundle_content";

    private String content;


    public static TabAlphaFragment newInstance(@NonNull final String title) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_CONTENT, title);
        TabAlphaFragment fragment = new TabAlphaFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable @Override public View onCreateView(LayoutInflater inflater,
                                                 @Nullable ViewGroup container,
                                                 @Nullable Bundle savedInstanceState) {
        final Bundle arguments = this.getArguments();
        if (arguments != null) this.content = arguments.getString(BUNDLE_CONTENT);

        TextView textView = new TextView(this.getActivity());
        textView.setText(this.content);
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER);

        return textView;
    }

}
