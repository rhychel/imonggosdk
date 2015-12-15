package net.nueca.concessioengine.activities;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 12/15/15.
 */
public class AddCustomerActivity extends ImonggoAppCompatActivity {

    private RecyclerView rvFields;
    private ArrayList<CustomerField> customerFieldArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_customer_activity);
        rvFields = (RecyclerView) findViewById(R.id.rvFields);

        customerFieldArrayList.add(new CustomerField("Name", FieldType.EDITTEXT));
        customerFieldArrayList.add(new CustomerField("Mobile", FieldType.EDITTEXT));
        customerFieldArrayList.add(new CustomerField("Work", FieldType.EDITTEXT));
        customerFieldArrayList.add(new CustomerField("Company", FieldType.EDITTEXT));
        customerFieldArrayList.add(new CustomerField("Address", FieldType.EDITTEXT));
        try {
            List<PaymentTerms> paymentTerms = getHelper().fetchObjectsList(PaymentTerms.class);
            customerFieldArrayList.add(new CustomerField<PaymentTerms>("Business", paymentTerms, FieldType.SPINNER));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        CustomerFieldsAdapter customerFieldsAdapter = new CustomerFieldsAdapter(this, customerFieldArrayList);
        customerFieldsAdapter.initializeRecyclerView(this, rvFields);
        rvFields.setAdapter(customerFieldsAdapter);
    }

    public enum FieldType {
        EDITTEXT,
        SPINNER
    }

    public class CustomerField<T> {
        private String label;
        private List<T> values;
        private FieldType fieldType;

        public CustomerField(String label, FieldType fieldType) {
            this.label = label;
            this.fieldType = fieldType;
        }

        public CustomerField(String label, List<T> values, FieldType fieldType) {
            this.label = label;
            this.values = values;
            this.fieldType = fieldType;
        }

        public CustomerField(String label, List<T> values) {
            this.label = label;
            this.values = values;
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
            ListItemView listItemView = new ListItemView(view, fieldType);
            return listItemView;
        }

        @Override
        public void onBindViewHolder(ListItemView holder, int position) {
            FieldType fieldType = FieldType.values()[getItemViewType(position)];
            if(fieldType == FieldType.EDITTEXT)
                holder.etField.setHint(getItem(position).getLabel());
            else {
                holder.tvLabel.setText(getItem(position).getLabel());
                ArrayAdapter<?> valuesAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, getItem(position).getValues());
                valuesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
                holder.spOptions.setAdapter(valuesAdapter);
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

        public class ListItemView extends BaseRecyclerAdapter.ViewHolder {

            ImageView ivIcon;
            EditText etField;
            TextView tvLabel;
            Spinner spOptions;

            FieldType fieldType = FieldType.EDITTEXT;

            public ListItemView(View itemView, FieldType fieldType) {
                super(itemView);
                this.fieldType = fieldType;
                ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);

                if(fieldType == FieldType.EDITTEXT)
                    etField = (EditText) itemView.findViewById(R.id.etField);
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

    }

}
