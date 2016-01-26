package net.nueca.concessioengine.fragments;

import android.util.Log;
import android.widget.ArrayAdapter;

import com.j256.ormlite.stmt.QueryBuilder;
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
    protected boolean isTypesInitialized = false, hasDocument = false, hasOrder = false, hasInvoice = false;
    protected String searchKey = "";

    protected TransactionTypesAdapter transactionTypeAdapter;
    protected List<ConcessioModule> transactionTypes;

    protected ArrayAdapter<Branch> branchAdapter;
    protected List<Branch> branches;

    protected SetupActionBar setupActionBar;
    protected ConcessioModule concessioModule = ConcessioModule.ALL;

    protected abstract void toggleNoItems(String msg, boolean show);

    private void initializeTypes() throws SQLException {
        if(isTypesInitialized)
            return;
        List<ModuleSetting> moduleSettings = getHelper().fetchObjects(ModuleSetting.class).queryBuilder()
                .where().in("module_type", ModuleSettingTools.getModulesToString  (ConcessioModule.STOCK_REQUEST,
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
                    hasDocument = true;
                    break;
                case INVOICE:
                    hasInvoice = true;
                    break;
                case STOCK_REQUEST:
                case PURCHASE_ORDERS:
                    hasOrder = true;
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
            boolean hasOne = false;
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

                if(hasOne)
                    whereOfflineData.and().in("type", transactionTypes);
                else {
                    whereOfflineData.in("type", transactionTypes);//.and()
                    hasOne = true;
                }
                if(includeSearchKey)
                    whereOfflineData.and().like("reference_no", "%"+searchKey+"%");
                if(concessioModule != ConcessioModule.ALL)
                    whereOfflineData.and().eq("concessioModule", concessioModule);
            }
            if(branchId > 0) {
                if(hasOne)
                    whereOfflineData.and().eq("branch_id", branchId);//.and()
                else
                    whereOfflineData.eq("branch_id", branchId);
            }

            QueryBuilder<OfflineData, Integer> resultTransactions = getHelper().fetchIntId(OfflineData.class).queryBuilder();
            resultTransactions.orderBy("dateCreated", false);
            resultTransactions.setWhere(whereOfflineData);

            transactions = resultTransactions.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}
