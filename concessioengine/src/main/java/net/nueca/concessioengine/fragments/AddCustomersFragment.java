package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.fragments.ImonggoFragment;

public class AddCustomersFragment extends ImonggoFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_customers_fragment, container, false);

        return view;
    }


    public static AddCustomersFragment newInstance() {
        return new AddCustomersFragment();
    }

}
