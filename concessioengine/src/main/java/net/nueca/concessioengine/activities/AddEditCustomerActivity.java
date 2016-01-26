package net.nueca.concessioengine.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.FieldValidatorMessage;
import net.nueca.imonggosdk.tools.TempIdGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 12/15/15.
 */
public class AddEditCustomerActivity extends ImonggoAppCompatActivity {

    private Customer updateCustomer;

    private Toolbar tbAddCustomer;
    private RecyclerView rvFields;

    private ArrayList<CustomerField> customerFieldArrayList = new ArrayList<>();
    private CustomerFieldsAdapter customerFieldsAdapter;

    /**
     * Fix the UI
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_customer_activity);
        rvFields = (RecyclerView) findViewById(R.id.rvFields);
        tbAddCustomer = (Toolbar) findViewById(R.id.tbAddCustomer);
        if(getIntent().hasExtra(CUSTOMER_ID)) {
            try {
                updateCustomer = getHelper().fetchIntId(Customer.class).queryForId(getIntent().getIntExtra(CUSTOMER_ID, -1));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        setSupportActionBar(tbAddCustomer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("New Customer");
        tbAddCustomer.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if(updateCustomer != null) {
            customerFieldArrayList.add(new CustomerField("Last Name", FieldType.EDITTEXT, Customer.CustomerFields.LAST_NAME, updateCustomer.getLast_name()));
            customerFieldArrayList.add(new CustomerField("First Name", FieldType.EDITTEXT, Customer.CustomerFields.FIRST_NAME, updateCustomer.getFirst_name()));
            customerFieldArrayList.add(new CustomerField("Middle Name", FieldType.EDITTEXT, Customer.CustomerFields.MIDDLE_NAME, updateCustomer.getMiddle_name()));
            customerFieldArrayList.add(new CustomerField("Mobile", FieldType.EDITTEXT, R.drawable.ic_phone_orange, Customer.CustomerFields.MOBILE, updateCustomer.getMobile()));
            customerFieldArrayList.add(new CustomerField("Work", FieldType.EDITTEXT, Customer.CustomerFields.TELEPHONE, updateCustomer.getTelephone()));
            customerFieldArrayList.add(new CustomerField("Company", FieldType.EDITTEXT, R.drawable.ic_branch_orange, Customer.CustomerFields.COMPANY_NAME, updateCustomer.getCompany_name()));
            customerFieldArrayList.add(new CustomerField("Address", FieldType.EDITTEXT, Customer.CustomerFields.STREET, updateCustomer.getStreet()));
            try {
                initSpinnerValues(true, true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            customerFieldArrayList.add(new CustomerField("Last Name", FieldType.EDITTEXT, Customer.CustomerFields.LAST_NAME));
            customerFieldArrayList.add(new CustomerField("First Name", FieldType.EDITTEXT, Customer.CustomerFields.FIRST_NAME));
            customerFieldArrayList.add(new CustomerField("Middle Name", FieldType.EDITTEXT, Customer.CustomerFields.MIDDLE_NAME));
            customerFieldArrayList.add(new CustomerField("Mobile", FieldType.EDITTEXT, R.drawable.ic_phone_orange, Customer.CustomerFields.MOBILE));
            customerFieldArrayList.add(new CustomerField("Work", FieldType.EDITTEXT, Customer.CustomerFields.TELEPHONE));
            customerFieldArrayList.add(new CustomerField("Company", FieldType.EDITTEXT, R.drawable.ic_branch_orange, Customer.CustomerFields.COMPANY_NAME));
            customerFieldArrayList.add(new CustomerField("Address", FieldType.EDITTEXT, Customer.CustomerFields.STREET));
            try {
                initSpinnerValues(true, false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        customerFieldsAdapter = new CustomerFieldsAdapter(this, customerFieldArrayList);
        customerFieldsAdapter.initializeRecyclerView(this, rvFields);
        rvFields.setAdapter(customerFieldsAdapter);
    }

    public void initSpinnerValues(boolean includeBlank, boolean getCurrentValue) throws SQLException {
        List<PaymentTerms> paymentTerms = getHelper().fetchObjectsList(PaymentTerms.class);
        List<CustomerCategory> customerCategories = getHelper().fetchObjectsList(CustomerCategory.class);
        if(includeBlank) {
            PaymentTerms paymentTerm = new PaymentTerms();
            paymentTerm.setId(-1);
            paymentTerm.setName("--");

            CustomerCategory customerCategory = new CustomerCategory();
            customerCategory.setId(-1);
            customerCategory.setName("--");

            paymentTerms.add(0, paymentTerm);
            customerCategories.add(0, customerCategory);
        }
        if(getCurrentValue) {
            int ptIndex = paymentTerms.indexOf(updateCustomer.getPaymentTerms());
            int ccIndex = customerCategories.indexOf(updateCustomer.getCustomerCategory());

            customerFieldArrayList.add(new CustomerField<CustomerCategory>("Outlet Type", customerCategories, FieldType.SPINNER, Customer.CustomerFields.EXTRAS_CATEGORY_ID, ccIndex));
            customerFieldArrayList.add(new CustomerField<PaymentTerms>("Payment Terms", paymentTerms, FieldType.SPINNER, Customer.CustomerFields.PAYMENT_TERMS_ID, ptIndex));
        }
        else {
            customerFieldArrayList.add(new CustomerField<CustomerCategory>("Outlet Type", customerCategories, FieldType.SPINNER, Customer.CustomerFields.EXTRAS_CATEGORY_ID));
            customerFieldArrayList.add(new CustomerField<PaymentTerms>("Payment Terms", paymentTerms, FieldType.SPINNER, Customer.CustomerFields.PAYMENT_TERMS_ID));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simple_add_customers_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mSaveCustomer) {
            final Customer customer = customerFieldsAdapter.generateCustomer();
            FieldValidatorMessage fieldValidatorMessage = customer.doesRequiredSatisfied(Customer.CustomerFields.FIRST_NAME, Customer.CustomerFields.LAST_NAME);
            if(fieldValidatorMessage.isPassed()) {
                DialogTools.showConfirmationDialog(this, "Save Customer",
                        updateCustomer != null ? "Update customer details?" : "Create this customer?",
                        "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("customer Name", customer.getFirst_name()+"");
                        if(updateCustomer == null) {
                            OfflineData offlineData = new SwableTools.Transaction(getHelper())
                                    .toSend()
                                    .object(customer)
                                    .queue();
                        }
                        else {
                            OfflineData offlineData = new SwableTools.Transaction(getHelper())
                                    .toUpdate()
                                    .object(customer)
                                    .queue();
                        }

                        Intent intent = new Intent();
                        intent.putExtra(CUSTOMER_ID, customer.getId());
                        setResult(SUCCESS, intent);
                        finish();
                    }
                }, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                }, R.style.AppCompatDialogStyle_Light);
            }
            else {
                DialogTools.showDialog(this, "Ooops!", fieldValidatorMessage.getMessage(), "Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                }, R.style.AppCompatDialogStyle_Light);
            }
                Log.e("Satisfied", "nope!");
        }
        return super.onOptionsItemSelected(item);
    }

    public enum FieldType {
        EDITTEXT,
        SPINNER
    }

    public class CustomerField<T> {
        private String label;
        private Customer.CustomerFields fieldName;
        private List<T> values;
        private FieldType fieldType;
        private int iconField = -1;
        private int selectedIndex = 0;
        private String editTextValue = "";
        private boolean hasTextChangedListener = false;

        public CustomerField(String label, FieldType fieldType, Customer.CustomerFields fieldName) {
            this.label = label;
            this.fieldType = fieldType;
            this.fieldName = fieldName;
        }

        public CustomerField(String label, FieldType fieldType, Customer.CustomerFields fieldName, String editTextValue) {
            this.label = label;
            this.fieldType = fieldType;
            this.fieldName = fieldName;
            this.editTextValue = editTextValue;
        }

        public CustomerField(String label, List<T> values, FieldType fieldType, Customer.CustomerFields fieldName) {
            this.label = label;
            this.values = values;
            this.fieldType = fieldType;
            this.fieldName = fieldName;
        }

        public CustomerField(String label, List<T> values, FieldType fieldType, Customer.CustomerFields fieldName, int selectedIndex) {
            this.label = label;
            this.values = values;
            this.fieldType = fieldType;
            this.fieldName = fieldName;
            this.selectedIndex = selectedIndex;
        }

        public CustomerField(String label, FieldType fieldType, int iconField, Customer.CustomerFields fieldName) {
            this.label = label;
            this.fieldType = fieldType;
            this.iconField = iconField;
            this.fieldName = fieldName;
        }

        public CustomerField(String label, FieldType fieldType, int iconField, Customer.CustomerFields fieldName, String editTextValue) {
            this.label = label;
            this.fieldType = fieldType;
            this.iconField = iconField;
            this.fieldName = fieldName;
            this.editTextValue = editTextValue;
        }

        public CustomerField(String label, List<T> values, FieldType fieldType, int iconField, Customer.CustomerFields fieldName) {
            this.label = label;
            this.values = values;
            this.fieldType = fieldType;
            this.iconField = iconField;
            this.fieldName = fieldName;
        }

        public CustomerField(String label, List<T> values, Customer.CustomerFields fieldName) {
            this.label = label;
            this.values = values;
            this.fieldName = fieldName;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<T> getValues() {
            return values;
        }

        public void setValues(List<T> values) {
            this.values = values;
        }

        public FieldType getFieldType() {
            return fieldType;
        }

        public void setFieldType(FieldType fieldType) {
            this.fieldType = fieldType;
        }

        public int getIconField() {
            return iconField;
        }

        public void setIconField(int iconField) {
            this.iconField = iconField;
        }

        public Customer.CustomerFields getFieldName() {
            return fieldName;
        }

        public void setFieldName(Customer.CustomerFields fieldName) {
            this.fieldName = fieldName;
        }

        public int getSelectedIndex() {
            return selectedIndex;
        }

        public void setSelectedIndex(int selectedIndex) {
            this.selectedIndex = selectedIndex;
        }

        public String getEditTextValue() {
            return editTextValue;
        }

        public void setEditTextValue(String editTextValue) {
            this.editTextValue = editTextValue;
        }

        public boolean isHasTextChangedListener() {
            return hasTextChangedListener;
        }

        public void setHasTextChangedListener(boolean hasTextChangedListener) {
            this.hasTextChangedListener = hasTextChangedListener;
        }
    }

    public class CustomerFieldsAdapter extends BaseRecyclerAdapter<CustomerFieldsAdapter.ListItemView, CustomerField> {

        public CustomerFieldsAdapter(Context context, List<CustomerField> list) {
            super(context, list);
        }

        @Override
        public ListItemView onCreateViewHolder(ViewGroup parent, int viewType) {
            FieldType fieldType = FieldType.values()[viewType];
            View view;

            if(fieldType == FieldType.EDITTEXT) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.add_customer_fielditem, parent, false);
            }
            else { // SPINNER
                view = LayoutInflater.from(getContext()).inflate(R.layout.add_customer_spinneritem, parent, false);
            }
            ListItemView listItemView = new ListItemView(view, new EditTextWatcher(), fieldType);
            return listItemView;
        }

        @Override
        public void onBindViewHolder(ListItemView holder, final int position) {
            FieldType fieldType = FieldType.values()[getItemViewType(position)];
            holder.ivIcon.setVisibility(View.INVISIBLE);

            if(getItem(position).getIconField() != -1) {
                holder.ivIcon.setVisibility(View.VISIBLE);
                holder.ivIcon.setImageResource(getItem(position).getIconField());
            }

            if(fieldType == FieldType.EDITTEXT) {
                holder.tilEt.setHint(getItem(position).getLabel());
                holder.editTextWatcher.setPosition(position);
                holder.etField.setInputType(getItem(position).getFieldName().getInputType());
                holder.etField.setText(getItem(position).getEditTextValue());
            }
            else {
                holder.tvLabel.setText(getItem(position).getLabel());
                ArrayAdapter<?> valuesAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, getItem(position).getValues());
                valuesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
                holder.spOptions.setAdapter(valuesAdapter);
                holder.spOptions.setSelection(getItem(position).getSelectedIndex());
                holder.spOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int selectedPosition, long id) {
                        getItem(position).setSelectedIndex(selectedPosition);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) { }
                });
            }
        }

        @Override
        public int getItemCount() {
            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getFieldType().ordinal();
        }

        public Customer generateCustomer() {
            Customer customer = null;
            Gson gson = new GsonBuilder().serializeNulls().create();
            JSONObject jsonObject = new JSONObject();
            try {
                Extras extras = new Extras();
                extras.setSalesman_id(getSession().getUser_id());
                PaymentTerms paymentTerm = null;
                CustomerCategory customerCategory = null;
                for(CustomerField customerField : getList()) {
                    if(customerField.getFieldName() == Customer.CustomerFields.EXTRAS_CATEGORY_ID) {
                        CustomerField<CustomerCategory> category = (CustomerField<CustomerCategory>)customerField;
                        if(category.getSelectedIndex() == -1)
                            continue;
                        customerCategory = category.getValues().get(category.getSelectedIndex());
                        if(customerCategory.getId() == -1)
                            continue;
                        extras.setCustomer_category_id(String.valueOf(customerCategory.getId()));
                        continue;
                    }
                    if(customerField.getFieldName().equals(Customer.CustomerFields.PAYMENT_TERMS_ID)) {
                        CustomerField<PaymentTerms> paymentTerms = (CustomerField<PaymentTerms>)customerField;
                        if(paymentTerms.getSelectedIndex() == -1)
                            continue;
                        paymentTerm = paymentTerms.getValues().get(paymentTerms.getSelectedIndex());
                        if(paymentTerm.getId() == -1)
                            continue;
                        jsonObject.put(Customer.CustomerFields.PAYMENT_TERMS_ID.getLabel(), paymentTerm.getId());
                        continue;
                    }

                    jsonObject.put(customerField.getFieldName().getLabel(), customerField.getEditTextValue());
                }
                customer = gson.fromJson(jsonObject.toString(), Customer.class);
                if(updateCustomer == null)
                    customer.setId(TempIdGenerator.generateTempId(getContext(), Customer.class));
                else
                    customer.setId(updateCustomer.getId());
                customer.setExtras(extras);
                if(paymentTerm == null)
                    Log.e("paymentTerm", "null");
                customer.setPaymentTerms(paymentTerm);
                if(customerCategory == null)
                    Log.e("customerCategory", "null");
                customer.setCustomerCategory(customerCategory);
                customer.generateFullName();
            } catch (SQLException | JSONException e) {
                e.printStackTrace();
            }

            return customer;
        }

        private Extras getExtras(Extras extras) {
            if(extras == null)
                extras = new Extras();
            return extras;
        }

        public class ListItemView extends BaseRecyclerAdapter.ViewHolder {

            ImageView ivIcon;
            EditText etField;
            TextView tvLabel;
            Spinner spOptions;
            TextInputLayout tilEt;

            FieldType fieldType = FieldType.EDITTEXT;
            EditTextWatcher editTextWatcher;

            public ListItemView(View itemView, EditTextWatcher editTextWatcher, FieldType fieldType) {
                super(itemView);
                this.editTextWatcher = editTextWatcher;
                this.fieldType = fieldType;

                ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);

                if(fieldType == FieldType.EDITTEXT) {
                    etField = (EditText) itemView.findViewById(R.id.etField);
                    etField.addTextChangedListener(editTextWatcher);
                    tilEt = (TextInputLayout) itemView.findViewById(R.id.tilEt);
                }
                else {
                    tvLabel = (TextView) itemView.findViewById(R.id.tvLabel);
                    spOptions = (Spinner) itemView.findViewById(R.id.spOptions);
                }
            }

            @Override
            public void onClick(View v) {

            }

            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        }


        public class EditTextWatcher implements TextWatcher {

            private int position = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("Position", position+"");
                getItem(position).setEditTextValue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            public void setPosition(int position) {
                this.position = position;
            }
        }



    }

}
