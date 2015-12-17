package net.nueca.concessioengine.activities;

import android.content.Context;
import android.media.Image;
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
import net.nueca.concessioengine.adapters.base.BaseAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 12/15/15.
 */
public class AddCustomerActivity extends ImonggoAppCompatActivity {

    private Toolbar tbAddCustomer;
    private RecyclerView rvFields;

    private ArrayList<CustomerField> customerFieldArrayList = new ArrayList<>();
    private CustomerFieldsAdapter customerFieldsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_customer_activity);
        rvFields = (RecyclerView) findViewById(R.id.rvFields);
        tbAddCustomer = (Toolbar) findViewById(R.id.tbAddCustomer);

        setSupportActionBar(tbAddCustomer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tbAddCustomer.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        customerFieldArrayList.add(new CustomerField("Last Name", FieldType.EDITTEXT, Customer.LAST_NAME));
        customerFieldArrayList.add(new CustomerField("First Name", FieldType.EDITTEXT, Customer.FIRST_NAME));
        customerFieldArrayList.add(new CustomerField("Middle Name", FieldType.EDITTEXT, Customer.MIDDLE_NAME));
        customerFieldArrayList.add(new CustomerField("Mobile", FieldType.EDITTEXT, R.drawable.ic_phone_orange, Customer.MOBILE));
        customerFieldArrayList.add(new CustomerField("Work", FieldType.EDITTEXT, Customer.TELEPHONE));
        customerFieldArrayList.add(new CustomerField("Company", FieldType.EDITTEXT, R.drawable.ic_branch_orange, Customer.COMPANY_NAME));
        customerFieldArrayList.add(new CustomerField("Address", FieldType.EDITTEXT, Customer.STREET));
        try {
            List<PaymentTerms> paymentTerms = getHelper().fetchObjectsList(PaymentTerms.class);
            List<CustomerCategory> customerCategories = getHelper().fetchObjectsList(CustomerCategory.class);

            customerFieldArrayList.add(new CustomerField<CustomerCategory>("Outlet Type", customerCategories, FieldType.SPINNER, Customer.EXTRAS_CATEGORY_ID));
            customerFieldArrayList.add(new CustomerField<PaymentTerms>("Payment Terms", paymentTerms, FieldType.SPINNER, Customer.PAYMENT_TERMS_ID));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        customerFieldsAdapter = new CustomerFieldsAdapter(this, customerFieldArrayList);
        customerFieldsAdapter.initializeRecyclerView(this, rvFields);
        rvFields.setAdapter(customerFieldsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simple_add_customers_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mSaveCustomer) {
            Log.e("JSON", customerFieldsAdapter.toJSONObject());
        }
        return super.onOptionsItemSelected(item);
    }

    public enum FieldType {
        EDITTEXT,
        SPINNER
    }

    public class CustomerField<T> {
        private String label;
        private String fieldName;
        private List<T> values;
        private FieldType fieldType;
        private int iconField = -1;
        private int selectedIndex = 0;
        private String editTextValue = "";
        private boolean hasTextChangedListener = false;

        public CustomerField(String label, FieldType fieldType, String fieldName) {
            this.label = label;
            this.fieldType = fieldType;
            this.fieldName = fieldName;
        }

        public CustomerField(String label, List<T> values, FieldType fieldType, String fieldName) {
            this.label = label;
            this.values = values;
            this.fieldType = fieldType;
            this.fieldName = fieldName;
        }

        public CustomerField(String label, FieldType fieldType, int iconField, String fieldName) {
            this.label = label;
            this.fieldType = fieldType;
            this.iconField = iconField;
            this.fieldName = fieldName;
        }

        public CustomerField(String label, List<T> values, FieldType fieldType, int iconField, String fieldName) {
            this.label = label;
            this.values = values;
            this.fieldType = fieldType;
            this.iconField = iconField;
            this.fieldName = fieldName;
        }

        public CustomerField(String label, List<T> values, String fieldName) {
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

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
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
            if(fieldType == FieldType.EDITTEXT) {
                holder.tilEt.setHint(getItem(position).getLabel());
                holder.editTextWatcher.setPosition(position);
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

        public String toJSONObject() {
            Customer customer = null;
            Gson gson = new GsonBuilder().serializeNulls().create();
            JSONObject jsonObject = new JSONObject();
            try {
                Extras extras = new Extras();
                extras.setSalesman_id(getSession().getUser_id());
                PaymentTerms paymentTerm = null;
                CustomerCategory customerCategory = null;
                for(CustomerField customerField : getList()) {
                    if(customerField.getFieldName().equals(Customer.EXTRAS_CATEGORY_ID)) {
                        CustomerField<CustomerCategory> category = (CustomerField<CustomerCategory>)customerField;
                        customerCategory = category.getValues().get(category.getSelectedIndex());
                        extras.setCategory_id(String.valueOf(customerCategory.getId()));
                        continue;
                    }
                    if(customerField.getFieldName().equals(Customer.PAYMENT_TERMS_ID)) {
                        CustomerField<PaymentTerms> paymentTerms = (CustomerField<PaymentTerms>)customerField;
                        paymentTerm = paymentTerms.getValues().get(paymentTerms.getSelectedIndex());
                        jsonObject.put(Customer.PAYMENT_TERMS_ID, paymentTerm.getId());
                        continue;
                    }

                    jsonObject.put(customerField.getFieldName(), customerField.getEditTextValue());
                }

                customer = gson.fromJson(jsonObject.toString(), Customer.class);
                customer.setExtras(extras);
                customer.setPaymentTerms(paymentTerm);
            } catch (SQLException | JSONException e) {
                e.printStackTrace();
            }

            return customer.toJSONString();
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
