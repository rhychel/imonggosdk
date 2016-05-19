package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.customer.CustomerField;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rhymartmanchus on 18/05/2016.
 */
public class CustomerFieldsAdapter extends BaseRecyclerAdapter<CustomerFieldsAdapter.ListItemView, CustomerField> {

    public CustomerFieldsAdapter(Context context, List<CustomerField> list) {
        super(context, list);
    }

    @Override
    public ListItemView onCreateViewHolder(ViewGroup parent, int viewType) {
        CustomerField.FieldType fieldType = CustomerField.FieldType.values()[viewType];
        View view;

        if(fieldType == CustomerField.FieldType.EDITTEXT) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.add_customer_fielditem, parent, false);
        }
        else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.add_customer_spinneritem, parent, false);
        }
        ListItemView listItemView = new ListItemView(view, new EditTextWatcher(), fieldType);
        return listItemView;
    }

    @Override
    public void onBindViewHolder(ListItemView holder, final int position) {
        CustomerField.FieldType fieldType = CustomerField.FieldType.values()[getItemViewType(position)];
        holder.ivIcon.setVisibility(View.INVISIBLE);

        if(getItem(position).getIconField() != -1) {
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(getItem(position).getIconField());
        }

        if(fieldType == CustomerField.FieldType.EDITTEXT) {
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

    public class ListItemView extends BaseRecyclerAdapter.ViewHolder {

        ImageView ivIcon;
        EditText etField;
        TextView tvLabel;
        Spinner spOptions;
        TextInputLayout tilEt;

        CustomerField.FieldType fieldType = CustomerField.FieldType.EDITTEXT;
        EditTextWatcher editTextWatcher;

        public ListItemView(View itemView, EditTextWatcher editTextWatcher, CustomerField.FieldType fieldType) {
            super(itemView);
            this.editTextWatcher = editTextWatcher;
            this.fieldType = fieldType;

            ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);

            if(fieldType == CustomerField.FieldType.EDITTEXT) {
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
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            getItem(position).setEditTextValue(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) { }

        public void setPosition(int position) {
            this.position = position;
        }
    }

}
