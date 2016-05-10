package net.nueca.concessioengine.fragments;

import android.util.Log;
import android.widget.ArrayAdapter;

import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.adapters.TransactionTypesAdapter;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.AccountSettings;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.tools.ModuleSettingTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 7/31/15.
 * imonggosdk2 (c)2015
 */
public abstract class BaseTransactionsFragment extends ImonggoFragment {

    public interface TransactionsListener {
        void showTransactionDetails(OfflineData offlineData);
    }

    protected TransactionsListener transactionsListener;
    protected int transactionType = -1; // Default is all[-1]
    protected int branchId = -1; // Default is all branches
    protected ListingType listingType;

    protected boolean hasFilterByBranch = false, hasFilterByTransactionType = false;
    protected boolean isTypesInitialized = false,
            hasDocument = false, hasOrder = false, hasInvoice = false, hasCustomer = false,
            priorityDocument = true, priorityOrder = true, priorityInvoice = true, layawaysOnly = false;
    protected String searchKey = "";
    protected Customer customer = null;

    protected TransactionTypesAdapter transactionTypeAdapter;
    protected List<ConcessioModule> transactionTypes;

    protected ArrayAdapter<Branch> branchAdapter;
    protected List<Branch> branches;

    protected SetupActionBar setupActionBar;
    protected ConcessioModule concessioModule = ConcessioModule.ALL;
    protected ConcessioModule[] filterModules;

    protected abstract void toggleNoItems(String msg, boolean show);

    private void initializeTypes() throws SQLException {
        if(isTypesInitialized)
            return;
        List<ModuleSetting> moduleSettings = getHelper().fetchObjects(ModuleSetting.class).queryBuilder()
                .where().in("module_type", ModuleSettingTools.getModulesToString(ConcessioModule.STOCK_REQUEST,
                        ConcessioModule.RECEIVE_BRANCH, ConcessioModule.RECEIVE_BRANCH_PULLOUT, ConcessioModule.RELEASE_BRANCH,
                        ConcessioModule.RECEIVE_ADJUSTMENT, ConcessioModule.RELEASE_ADJUSTMENT,
                        ConcessioModule.RECEIVE_SUPPLIER, ConcessioModule.RELEASE_SUPPLIER,
                        ConcessioModule.INVOICE)).query();

        hasDocument = false;
        hasOrder = false;
        hasInvoice = false;
        for(ModuleSetting moduleSetting : moduleSettings) {
            switch (moduleSetting.getModuleType()) {
                case RECEIVE_BRANCH:
                case RECEIVE_BRANCH_PULLOUT:
                case RECEIVE_SUPPLIER:
                case RECEIVE_ADJUSTMENT:
                case RELEASE_BRANCH:
                case RELEASE_SUPPLIER:
                case RELEASE_ADJUSTMENT:
                    hasDocument = true && priorityDocument;
                    break;
                case INVOICE:
                    hasInvoice = true && priorityInvoice;
                    break;
                case STOCK_REQUEST:
                case PURCHASE_ORDERS:
                    hasOrder = true && priorityOrder;
                    break;
            }
        }
        isTypesInitialized = true;
    }

    protected List<OfflineData> getTransactions() { // TODO BUGGED!
        List<OfflineData> transactions = new ArrayList<>();
        try {
            boolean includeSearchKey = !searchKey.trim().isEmpty();
            Where<OfflineData, Integer> whereOfflineData = getHelper().fetchIntId(OfflineData.class).queryBuilder().where();
//            whereOfflineData.eq("user_id", getSession().getUser().getId());
            if(transactionType > 0)
                whereOfflineData.eq("type", transactionType); //.and()
            else {
                List<Integer> transactionTypes = new ArrayList<>();

                initializeTypes();

                if(hasDocument)
                    transactionTypes.add(OfflineData.DOCUMENT);
                if(hasOrder)
                    transactionTypes.add(OfflineData.ORDER);
                if(hasInvoice)
                    transactionTypes.add(OfflineData.INVOICE);

                if(!layawaysOnly)
                    transactionTypes.add(OfflineData.CUSTOMER);


                whereOfflineData.in("type", transactionTypes);//.and()
                if(includeSearchKey)
                    whereOfflineData.and().like("reference_no", "%"+searchKey+"%");
                if(concessioModule != ConcessioModule.ALL)
                    whereOfflineData.and().eq("concessioModule", concessioModule);
                else if(filterModules != null)
                    whereOfflineData.and().in("concessioModule", filterModules);
            }
            if(branchId > 0) {
                whereOfflineData.and().eq("branch_id", branchId);//.and()
            }

            QueryBuilder<OfflineData, Integer> resultTransactions = getHelper().fetchIntId(OfflineData.class).queryBuilder();
            if(customer != null) {
                QueryBuilder<Invoice, Integer> custInvoices = getHelper().fetchIntId(Invoice.class).queryBuilder();
                custInvoices.selectColumns("id");
                custInvoices.where().eq("customer_id", customer);
                List<Invoice> invoices = custInvoices.query();

                QueryBuilder<Document, Integer> custDocuments = getHelper().fetchIntId(Document.class).queryBuilder();
                custDocuments.selectColumns("id");
                custDocuments.where().eq("customer_id", customer);
                List<Document> documents = custDocuments.query();

                SelectArg[] selectArgs = new SelectArg[invoices.size()+documents.size()];
                String invQuery = "invoice_id IN (";
                String docQuery = "document_id IN (";
                int index = 0;
                for(int i = 0;i < invoices.size();i++) {
                    if(i > 0)
                        invQuery+=",";
                    selectArgs[index] = new SelectArg("invoice_id", invoices.get(i));
                    invQuery += "?";
                    index++;
                }
                invQuery += ")";
                for(int i = 0;i < documents.size();i++) {
                    if(i > 0)
                        docQuery+=",";
                    selectArgs[index] = new SelectArg("document_id", documents.get(i));
                    docQuery += "?";
                    index++;
                }
                docQuery += ")";
                whereOfflineData.and().raw("("+invQuery+" OR "+docQuery+")", selectArgs);
            }
            resultTransactions.orderBy("dateCreated", false);
            resultTransactions.setWhere(whereOfflineData);
            Log.e("Query", resultTransactions.prepareStatementString());

            transactions = resultTransactions.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(layawaysOnly) {
            List<OfflineData> finalTransactions = new ArrayList<>();
            for(OfflineData offlineData : transactions) {
                if(!offlineData.isVoided() && offlineData.getObjectFromData(Invoice.class).getStatus() != null && offlineData.getObjectFromData(Invoice.class).getStatus().equals("L"))
                    finalTransactions.add(offlineData);
            }
            return finalTransactions;
        }
//        if(customer != null) {
//            List<OfflineData> finalTransactions = new ArrayList<>();
//            for(OfflineData offlineData : transactions) {
////                if(filterModules != null && filterModules.size() > 0)
////                    if(!filterModules.contains(offlineData.getConcessioModule()))
////                        continue;
//
//                if(offlineData.getObjectFromData() instanceof Document)
//                    if(offlineData.getObjectFromData(Document.class).getCustomer() != null && offlineData.getObjectFromData(Document.class).getCustomer().equals(customer))
//                        finalTransactions.add(offlineData);
//                if(offlineData.getObjectFromData() instanceof Invoice && offlineData.getObjectFromData(Invoice.class).getCustomer().equals(customer))
//                    finalTransactions.add(offlineData);
//            }
//            return finalTransactions;
//        }
//        if(filterModules != null && filterModules.size() > 0) {
//            List<OfflineData> finalTransactions = new ArrayList<>();
//            for(OfflineData offlineData : transactions) {
//                if(filterModules != null && filterModules.size() > 0)
//                    if(!filterModules.contains(offlineData.getConcessioModule()))
//                        continue;
//            }
//            return finalTransactions;
//        }
        return transactions;
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public void setTransactionsListener(TransactionsListener transactionsListener) {
        this.transactionsListener = transactionsListener;
    }

    public void setHasFilterByBranch(boolean hasFilterByBranch) {
        this.hasFilterByBranch = hasFilterByBranch;
    }

    public void setHasFilterByTransactionType(boolean hasFilterByTransactionType) {
        this.hasFilterByTransactionType = hasFilterByTransactionType;
    }

    public void setTransactionTypes(List<ConcessioModule> transactionTypes) {
        this.transactionTypes = transactionTypes;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public void setListingType(ListingType listingType) {
        this.listingType = listingType;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void onlyInvoices(boolean layawaysOnly) {
        priorityOrder = false;
        priorityDocument = false;
        this.layawaysOnly = layawaysOnly;
    }

    public void onlyDocuments() {
        priorityInvoice = false;
        priorityOrder = false;
    }

    public void onlyOrders() {
        priorityInvoice = false;
        priorityDocument = false;
    }

    public void setFilterModules(ConcessioModule... filterModules) {
        this.filterModules = filterModules;
    }
}
