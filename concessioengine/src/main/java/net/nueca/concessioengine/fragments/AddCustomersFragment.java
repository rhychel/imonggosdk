package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.objects.customer.Customer;

import org.w3c.dom.Text;

import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;

public class AddCustomersFragment extends BaseCustomersFragment {

    private String[] gender;
    private String[] civil_status;
    private boolean isSending = false;
    private Customer current_customer;

    // NAME
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mCompanyName;


    // CONTACT
    private EditText mTelephone;
    private EditText mMobile;
    private EditText mFax;
    private EditText mEmail;


    // EMAIL
    private EditText mStreet;
    private EditText mCity;
    private EditText mZipCode;
    private EditText mCountry;
    private EditText mState;

    // PERSONAL
    private Spinner mGender;
    private Spinner mCivilStatus;

    // OTHERS
    private EditText mTIN;
    private Switch mExcemptFromTax;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_customers_fragment, container, false);
        ArrayAdapter<String> genderAdapter;
        ArrayAdapter<String> civilStatusAdapter;

        mFirstName = (EditText) view.findViewById(R.id.etFirstName);
        mLastName = (EditText) view.findViewById(R.id.etLastName);
        mCompanyName = (EditText) view.findViewById(R.id.etCompanyName);

        mTelephone = (EditText) view.findViewById(R.id.etCompanyName);
        mMobile = (EditText) view.findViewById(R.id.etMobile);
        mFax =(EditText) view.findViewById(R.id.etFax);
        mEmail = (EditText) view.findViewById(R.id.etEmail);

        mStreet = (EditText) view.findViewById(R.id.etStreet);
        mCity = (EditText) view.findViewById(R.id.etCity);
        mZipCode = (EditText) view.findViewById(R.id.etZipcode);
        mCountry = (EditText) view.findViewById(R.id.etCountry);
        mState = (EditText) view.findViewById(R.id.etStreet);


        mTIN = (EditText) view.findViewById(R.id.etTIN);
        mExcemptFromTax = (Switch) view.findViewById(R.id.swTaxExempt);


        tbActionBar = (Toolbar) view.findViewById(R.id.tbAddCustomer);

        gender = getResources().getStringArray(R.array.gender_array);
        civil_status = getResources().getStringArray(R.array.civil_status_array);

        genderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, gender);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        civilStatusAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, civil_status);
        civilStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mGender = (MaterialSpinner) view.findViewById(R.id.spGender);
        mCivilStatus = (MaterialSpinner) view.findViewById(R.id.spCivilStatus);

        mGender.setAdapter(genderAdapter);
        mCivilStatus.setAdapter(civilStatusAdapter);

        return view;
    }

    public static AddCustomersFragment newInstance() {
        return new AddCustomersFragment();
    }

    public boolean isSending() {
        return isSending;
    }

    public void setIsSending(boolean isSending) {
        this.isSending = isSending;
    }

    public Customer getCustomer() {
        return current_customer != null ? current_customer : null;
    }

    private void validateCustomerInput() {


    }

    @Override
    protected void toggleNoItems(String msg, boolean show) {

    }

    @Override
    protected void whenListEndReached(List<Customer> customers) {

    }
}
