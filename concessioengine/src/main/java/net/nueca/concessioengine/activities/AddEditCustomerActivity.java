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
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.CustomerFieldsAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.customer.CustomerField;
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
            Log.e("CUSTOMER_ID", getIntent().getIntExtra(CUSTOMER_ID, -1)+"<----");
            try {
                updateCustomer = getHelper().fetchIntId(Customer.class).queryForId(getIntent().getIntExtra(CUSTOMER_ID, -1));

                OfflineData offlineData = getHelper().fetchForeignCollection(updateCustomer.getOfflineData().closeableIterator()).get(0);
                offlineData.setBeingModified(true);
                offlineData.updateTo(getHelper());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Log.e("Customer Return ID", "" + updateCustomer.getReturnId());
        }

        setSupportActionBar(tbAddCustomer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        boolean isUpdate = updateCustomer != null;
        tbAddCustomer.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        customerFieldArrayList.add(new CustomerField("Last Name", CustomerField.FieldType.EDITTEXT, Customer.CustomerFields.LAST_NAME, updateCustomer));
        customerFieldArrayList.add(new CustomerField("First Name", CustomerField.FieldType.EDITTEXT, Customer.CustomerFields.FIRST_NAME, updateCustomer));
        customerFieldArrayList.add(new CustomerField("Middle Name", CustomerField.FieldType.EDITTEXT, Customer.CustomerFields.MIDDLE_NAME, updateCustomer));
        customerFieldArrayList.add(new CustomerField("Mobile", CustomerField.FieldType.EDITTEXT, R.drawable.ic_phone_orange, Customer.CustomerFields.MOBILE, updateCustomer));
        customerFieldArrayList.add(new CustomerField("Work", CustomerField.FieldType.EDITTEXT, Customer.CustomerFields.TELEPHONE, updateCustomer));
        customerFieldArrayList.add(new CustomerField("Company", CustomerField.FieldType.EDITTEXT, R.drawable.ic_branch_orange, Customer.CustomerFields.COMPANY_NAME, updateCustomer));
        customerFieldArrayList.add(new CustomerField("Address", CustomerField.FieldType.EDITTEXT, Customer.CustomerFields.STREET, updateCustomer));
        getSupportActionBar().setTitle(isUpdate ? "Update Customer" : "New Customer");
        try {
            initSpinnerValues(true, isUpdate);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        customerFieldsAdapter = new CustomerFieldsAdapter(this, customerFieldArrayList);
        customerFieldsAdapter.initializeRecyclerView(this, rvFields);
        rvFields.setAdapter(customerFieldsAdapter);
    }

    public void initSpinnerValues(boolean includeBlank, boolean getCurrentValue) throws SQLException {
        List<PaymentTerms> paymentTerms = PaymentTerms.fetchWithConditionInt(getHelper(), PaymentTerms.class, new DBTable.ConditionsWindow<PaymentTerms, Integer>() {
            @Override
            public Where<PaymentTerms, Integer> renderConditions(Where<PaymentTerms, Integer> where) throws SQLException {
                return where.eq("status", "A");
            }
        });
        List<CustomerCategory> customerCategories = CustomerCategory.fetchWithConditionInt(getHelper(), CustomerCategory.class, new DBTable.ConditionsWindow<CustomerCategory, Integer>() {
            @Override
            public Where<CustomerCategory, Integer> renderConditions(Where<CustomerCategory, Integer> where) throws SQLException {
                return where.eq("status", "A");
            }
        });
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
            if(updateCustomer.getExtras() != null)
                Log.e("Update Customer", updateCustomer.getExtras()+" <-- ");
            else
                Log.e("Update Customer", "extras is null");

            if(updateCustomer.getExtras().getCustomerCategory() != null)
                Log.e("Update Customer", updateCustomer.getExtras().getCustomerCategory()+" <-- ");
            else
                Log.e("Update Customer", "extras.customer_category is null");

            int ccIndex = customerCategories.indexOf(updateCustomer.getExtras().getCustomerCategory());

            customerFieldArrayList.add(new CustomerField<>("Outlet Type", customerCategories, CustomerField.FieldType.SPINNER, Customer.CustomerFields.EXTRAS_CATEGORY_ID, ccIndex));
            customerFieldArrayList.add(new CustomerField<>("Payment Terms", paymentTerms, CustomerField.FieldType.SPINNER, Customer.CustomerFields.PAYMENT_TERMS_ID, ptIndex));
        }
        else {
            customerFieldArrayList.add(new CustomerField<>("Outlet Type", customerCategories, CustomerField.FieldType.SPINNER, Customer.CustomerFields.EXTRAS_CATEGORY_ID));
            customerFieldArrayList.add(new CustomerField<>("Payment Terms", paymentTerms, CustomerField.FieldType.SPINNER, Customer.CustomerFields.PAYMENT_TERMS_ID));
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
            try {
                final Customer customer = Customer.generateCustomer(updateCustomer, getSession().getUser_id(), customerFieldsAdapter.getList());
                Gson gson = new Gson();
                Log.e("Customer", gson.toJson(customer));
                FieldValidatorMessage fieldValidatorMessage = customer.doesRequiredSatisfied(Customer.CustomerFields.FIRST_NAME, Customer.CustomerFields.LAST_NAME,
                        Customer.CustomerFields.MOBILE, Customer.CustomerFields.TELEPHONE, Customer.CustomerFields.EXTRAS_CATEGORY_ID, Customer.CustomerFields.PAYMENT_TERMS_ID);

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
//                            customer.set
                                        OfflineData offlineData = new SwableTools.Transaction(getHelper())
                                                .toUpdate()
                                                .object(customer)
                                                .queue();

                                        offlineData.setBeingModified(false);
                                        offlineData.updateTo(getHelper());
                                    }

                                    Intent intent = new Intent();
                                    intent.putExtra(CUSTOMER_ID, updateCustomer != null ? updateCustomer.getId() : customer.getId());
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Log.e("Satisfied", "nope!");
        }
        return super.onOptionsItemSelected(item);
    }

}
