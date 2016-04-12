package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.tools.LoggingTools;
import net.nueca.imonggosdk.tools.LoginTools;

import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;

public class AddCustomersFragment extends BaseCustomersFragment {

    private static String TAG = "AddCustomerFragment";
    private String[] genderArray;
    private String[] civilStatusArray;
    private boolean isSending = false;
    private Customer current_customer;

    private EditText mFirstName, mLastName, mCompanyName, mTelephone, mMobile, mFax, mEmail,
            mCity, mTown, mZipCode, mCountry, mState, mTIN, mStreet;

    private Spinner mGender, mCivilStatus;
    private Switch mExcemptFromTax;

    private View addCustomerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        addCustomerView = inflater.inflate(R.layout.add_customer_fragment, container, false);
        ArrayAdapter<String> genderAdapter;
        ArrayAdapter<String> civilStatusAdapter;

        mFirstName = (EditText) addCustomerView.findViewById(R.id.etFirstName);
        mLastName = (EditText) addCustomerView.findViewById(R.id.etLastName);
        mCompanyName = (EditText) addCustomerView.findViewById(R.id.etCompanyName);

        mTelephone = (EditText) addCustomerView.findViewById(R.id.etTelephone);
        mMobile = (EditText) addCustomerView.findViewById(R.id.etMobile);
        mFax = (EditText) addCustomerView.findViewById(R.id.etFax);
        mEmail = (EditText) addCustomerView.findViewById(R.id.etEmail);

        mStreet = (EditText) addCustomerView.findViewById(R.id.etStreet);
        mCity = (EditText) addCustomerView.findViewById(R.id.etCity);
        mZipCode = (EditText) addCustomerView.findViewById(R.id.etZipcode);
        mCountry = (EditText) addCustomerView.findViewById(R.id.etCountry);
        mState = (EditText) addCustomerView.findViewById(R.id.etState);

        mTIN = (EditText) addCustomerView.findViewById(R.id.etTIN);
        mExcemptFromTax = (Switch) addCustomerView.findViewById(R.id.swTaxExempt);

        tbActionBar = (Toolbar) addCustomerView.findViewById(R.id.tbAddCustomer);

        genderArray = getResources().getStringArray(R.array.gender_array);
        civilStatusArray = getResources().getStringArray(R.array.civil_status_array);

        genderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, genderArray);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        civilStatusAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, civilStatusArray);
        civilStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mGender = (MaterialSpinner) addCustomerView.findViewById(R.id.spGender);
        mCivilStatus = (MaterialSpinner) addCustomerView.findViewById(R.id.spCivilStatus);

        mGender.setAdapter(genderAdapter);
        mCivilStatus.setAdapter(civilStatusAdapter);


        return addCustomerView;
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

    public Boolean validateCustomerInput() {
        Boolean cancelAddCustomer = false;

        mFirstName.setError(null);
        mEmail.setError(null);

        View focusView = null;

        String firstName = mFirstName.getText().toString();
        String email = mEmail.getText().toString();

        if (TextUtils.isEmpty(firstName)) {
            mFirstName.setError("Customer name is required.");
            focusView = mFirstName;
            cancelAddCustomer = true;
        } else if (!LoginTools.isValidEmail(email)) {
            mEmail.setError("Invalid Email");
            focusView = mEmail;
            cancelAddCustomer = true;
        } else if (TextUtils.isEmpty(email)) {
            mEmail.setError("Customer email is required.");
            focusView = mEmail;
            cancelAddCustomer = true;
        }

        if (cancelAddCustomer) {
            focusView.requestFocus();
            LoggingTools.showToast(addCustomerView.getContext(), "This field is required.");
            return false;
        } else {
            return true;
        }
    }

    public Customer getCustomerData() {
        String gender = mGender.getSelectedItem().toString();

        current_customer = new Customer(
                mFirstName.getText().toString(),
                mLastName.getText().toString(),
                mFirstName.getText().toString() + " " + mLastName.getText().toString(),
                mCompanyName.getText().toString(),
                mTelephone.getText().toString(),
                mMobile.getText().toString(),
                mFax.getText().toString(),
                mEmail.getText().toString(),
                mStreet.getText().toString(),
                mCity.getText().toString(),
                mTown.getText().toString(),
                mZipCode.getText().toString(),
                mCountry.getText().toString(),
                mState.getText().toString(),
                mTIN.getText().toString(),
                mGender.getSelectedItem().toString().equals("Gender") ? "" : gender);

        Log.e(TAG, "Add This Customer: " + current_customer.toString() + " with gender: " + (mGender.getSelectedItem().toString().equals("Gender") ? "no input" : gender));
        LoggingTools.showToast(addCustomerView.getContext(), "Adding Customer");
        return current_customer;
    }

    @Override
    protected void toggleNoItems(String msg, boolean show) {

    }

    @Override
    protected void whenListEndReached(List<Customer> customers) {

    }
}