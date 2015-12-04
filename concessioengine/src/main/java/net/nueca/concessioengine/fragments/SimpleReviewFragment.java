package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.fragments.ImonggoFragment;

/**
 * Created by rhymart on 12/2/15.
 */
public class SimpleReviewFragment extends ImonggoFragment {

    private TabLayout tabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_review_activity, container, false);
        return view;
    }
}
