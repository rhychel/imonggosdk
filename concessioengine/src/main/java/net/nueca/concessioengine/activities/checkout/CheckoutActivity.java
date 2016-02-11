package net.nueca.concessioengine.activities.checkout;

import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.BaseCheckoutFragment;
import net.nueca.concessioengine.fragments.SimpleCheckoutFragment;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.concessioengine.tools.LocationTools;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.ReferenceNumberTool;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 5/14/15.
 * imonggosdk (c)2015
 */
public abstract class CheckoutActivity extends ModuleActivity {
    protected BaseCheckoutFragment checkoutFragment;

    public static final String REFERENCE = "reference";
    public static final String IS_LAYAWAY = "is_layaway";

    protected String reference;
    protected boolean isLayaway = false;

    protected OfflineData offlineData = null;
    protected Invoice invoice = null;

    protected abstract void initializeFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().hasExtra(REFERENCE))
            reference = getIntent().getStringExtra(REFERENCE);
        if(getIntent().hasExtra(IS_LAYAWAY))
            isLayaway = getIntent().getBooleanExtra(IS_LAYAWAY, false);

        Log.e("CheckoutActivity", "onCreate : reference - " + reference);

        initializeFragment();

        if(reference == null || reference.length() == 0) {
            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.addAll(InvoiceTools.generateInvoiceLines(ProductsAdapterHelper
                    .getSelectedProductItems()));
            invoiceLines.addAll(InvoiceTools.generateInvoiceLines(ProductsAdapterHelper
                    .getSelectedReturnProductItems(), invoiceLines.size()));
            Invoice.Builder invoiceBuilder = new Invoice.Builder()
                    .invoice_lines(invoiceLines);
            invoice = invoiceBuilder.build();

            checkoutFragment.setInvoice(invoice);
        } else {
            try {
                offlineData = getHelper().fetchObjects(OfflineData.class).queryBuilder()
                        .where().eq("reference_no", reference).queryForFirst();
                invoice = offlineData.getObjectFromData(Invoice.class);
                ProductsAdapterHelper.setSelectedCustomer(invoice.getCustomer());

                List<CustomerGroup> customerGroups = invoice.getCustomer().getCustomerGroups(getHelper());
                if (customerGroups.size() > 0)
                    ProductsAdapterHelper.setSelectedCustomerGroup(customerGroups.get(0));

                checkoutFragment.setInvoice(invoice);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        Log.e("CheckoutActivity", "onCreate : invoice - " + invoice.toJSONString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationTools.startLocationSearch(this);
    }

    @Override
    protected void onStop() {
        LocationTools.stopLocationSearch(this);
        super.onStop();
    }

    public Invoice generateInvoice() {
        Invoice invoice = checkoutFragment.getCheckoutInvoice();
        Extras extras = invoice.getExtras() == null? new Extras() : invoice.getExtras();

        if(!isLayaway) {
            try {
                invoice.setReference(ReferenceNumberTool.generateRefNo(this, getSession().getDevice_id()));
                invoice.setSalesman_id(getUser().getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        invoice.setInvoice_date(DateTimeTools.convertDateForUrl(DateTimeTools.getCurrentDateTimeUTCFormat().replaceAll("-","/")));

        /** Location **/
        Location location = LocationTools.getCurrentLocation();
        if(location != null) {
            extras.setLongitude("" + location.getLongitude());
            extras.setLatitude("" + location.getLatitude());
        }
        invoice.setExtras(extras);

        invoice.createNewPaymentBatch();

        return invoice;
    }
}