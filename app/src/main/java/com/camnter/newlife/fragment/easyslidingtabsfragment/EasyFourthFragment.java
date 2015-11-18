package com.camnter.newlife.fragment.easyslidingtabsfragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.camnter.newlife.R;

/**
 * Description：EasyFourthFragment
 * Created by：CaMnter
 * Time：2015-10-17 12:15
 */
public class EasyFourthFragment extends Fragment {

    private View self;

    private static EasyFourthFragment instance;

    private EasyFourthFragment() {
    }

    public static EasyFourthFragment getInstance() {
        if (instance == null) instance = new EasyFourthFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.self == null) {
            this.self = inflater.inflate(R.layout.easy_fourth_fragment, null);
        }
        if (this.self.getParent() != null) {
            ViewGroup parent = (ViewGroup) this.self.getParent();
            parent.removeView(this.self);
        }
        return this.self;
    }
}
