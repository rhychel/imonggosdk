package net.nueca.concessioengine.fragments;

import android.util.Log;
import android.widget.ArrayAdapter;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.AccountSettings;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 7/31/15.
 * imonggosdk2 (c)2015
 */
public abstract class BaseTransactionsFragment extends ImonggoFragment {

    public interface TransactionsListener {
        void showTransactionDetails(int transactionId);
    }

    protected TransactionsListener transactionsListener;
    protected int transactionType = -1; // Default is all[-1]
    protected int branchId = -1; // Default is all branches

    protected boolean hasFilterByBranch = false, hasFilterByTransactionType = false;

    protected ArrayAdapter<String> transactionTypeAdapter;
    protected List<String> transactionTypes;

    protected ArrayAdapter<Branch> branchAdapter;
    protected List<Branch> branches;

    protected SetupActionBar setupActionBar;

    protected abstract void toggleNoItems(String msg, boolean show);

    protected List<OfflineData> getTransactions() { // TODO BUGGED!
        List<OfflineData> transactions = new ArrayList<>();
        try {
            Where<OfflineData, Integer> whereOfflineData = getHelper().fetchIntId(OfflineData.class).queryBuilder().where();
            boolean hasOne = false;
//            whereOfflineData.eq("user_id", getSession().getUser().getId());
            if(transactionType > 0)
                whereOfflineData.eq("type", transactionType); //.and()
            else {
                List<Integer> transactionTypes = new ArrayList<>();
                if(AccountSettings.hasCount(getActivity()) || AccountSettings.hasPullout(getActivity()) || AccountSettings.hasReceive(getActivity()))
                    transactionTypes.add(OfflineData.DOCUMENT);
                Log.e("hasOrder", AccountSettings.hasOrder(getActivity()) + "");
                if(AccountSettings.hasOrder(getActivity()))
                    transactionTypes.add(OfflineData.ORDER);
                if (AccountSettings.hasSales(getActivity()))
                    transactionTypes.add(OfflineData.INVOICE);
                if(hasOne)
                    whereOfflineData.and().in("type", transactionTypes);
                else {
                    whereOfflineData.in("type", transactionTypes);//.and()
                    hasOne = true;
                }
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

    public void setTransactionTypes(List<String> transactionTypes) {
        this.transactionTypes = transactionTypes;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

}
