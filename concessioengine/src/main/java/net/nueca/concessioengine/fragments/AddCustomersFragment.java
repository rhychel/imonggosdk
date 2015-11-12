package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.objects.customer.Customer;

import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;

public class AddCustomersFragment extends BaseCustomersFragment {

    String[] gender;
    String[] civil_status;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_customers_fragment, container, false);
        ArrayAdapter<String> genderAdapter;
        ArrayAdapter<String> civilStatusAdapter;

        tbActionBar = (Toolbar) view.findViewById(R.id.tbAddCustomer);

        gender = getResources().getStringArray(R.array.gender_array);
        civil_status = getResources().getStringArray(R.array.civil_status_array);

        genderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, gender);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        civilStatusAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, civil_status);
        civilStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner genderSpinner = (MaterialSpinner) view.findViewById(R.id.spGender);
        Spinner civilStatusSpinner = (MaterialSpinner) view.findViewById(R.id.spCivilStatus);

        genderSpinner.setAdapter(genderAdapter);
        civilStatusSpinner.setAdapter(civilStatusAdapter);

        return view;
    }

    public static AddCustomersFragment newInstance() {
        return new AddCustomersFragment();
    }

    @Override
    protected void toggleNoItems(String msg, boolean show) {

    }

    @Override
    protected void whenListEndReached(List<Customer> customers) {

    }
}
