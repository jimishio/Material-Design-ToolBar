package com.example.jimish.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import justbe.mobile.R;


public class PageFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    public static PageFragment newInstance(int position) {
        PageFragment f = new PageFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int position = getArguments().getInt(ARG_POSITION);
        final View rootView = inflater.inflate(R.layout.page, container, false);

        switch (position) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
        }

        return rootView;
    }
}