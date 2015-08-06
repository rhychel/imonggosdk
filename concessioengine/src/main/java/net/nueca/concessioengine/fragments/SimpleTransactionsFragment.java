package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleTransactionListAdapter;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.swable.ImonggoSwable;

import java.util.List;

/**
 * Created by rhymart on 8/3/15.
 * imonggosdk2 (c)2015
 *
 * Do the recyclerview
 */
public class SimpleTransactionsFragment extends BaseTransactionsFragment implements ImonggoSwable.SwableStateListener {

    private boolean useRecyclerView = true;
    private Toolbar tbActionBar;
    private ListView lvTransactions;
    private RecyclerView rvTransactions;
    private Spinner spTransactionType;
    private Spinner spBranches;

    private SimpleTransactionListAdapter simpleTransactionListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(useRecyclerView ? R.layout.simple_transactions_fragment_rv : R.layout.simple_transactions_fragment_lv, container, false);

        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        spBranches = (Spinner) view.findViewById(R.id.spBranches);
        spTransactionType = (Spinner) view.findViewById(R.id.spTransactionType);

        if(hasFilterByBranch) {
            spBranches.setVisibility(View.VISIBLE);
            branchAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, branches);
            spBranches.setAdapter(branchAdapter);
        }
        if(hasFilterByTransactionType) {
            spTransactionType.setVisibility(View.VISIBLE);
            transactionTypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, transactionTypes);
            spTransactionType.setAdapter(transactionTypeAdapter);
        }

        if(useRecyclerView) {

        }
        else {
            lvTransactions = (ListView) view.findViewById(R.id.lvTransactions);
            simpleTransactionListAdapter = new SimpleTransactionListAdapter(getActivity(), getTransactions());
            lvTransactions.setAdapter(simpleTransactionListAdapter);
            lvTransactions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    if(transactionsListener != null)
                        transactionsListener.showTransactionDetails(simpleTransactionListAdapter.getItem(position).getId());
                }
            });
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }

    public void setUseRecyclerView(boolean useRecyclerView) {
        this.useRecyclerView = useRecyclerView;
    }

    // *** THESE ARE FOR SWABLE ***
    @Override
    public void onSwableStarted() {

    }

    @Override
    public void onQueued(OfflineData offlineData) {
        if(useRecyclerView) {

        }
        else {
            int position = simpleTransactionListAdapter.getPosition(offlineData);
            if(position > -1) {
                simpleTransactionListAdapter.getItem(position).setQueued(offlineData.isQueued());
                simpleTransactionListAdapter.notifyItemChanged(lvTransactions, position);
            }
        }
    }

    @Override
    public void onSyncing(OfflineData offlineData) {
        if(useRecyclerView) {

        }
        else {
            int position = simpleTransactionListAdapter.getPosition(offlineData);
            if(position > -1) {
                simpleTransactionListAdapter.getItem(position).setSyncing(offlineData.isSyncing());
                simpleTransactionListAdapter.notifyItemChanged(lvTransactions, position);
            }
        }
    }

    @Override
    public void onSynced(OfflineData offlineData) {
        if(useRecyclerView) {

        }
        else {
            int position = simpleTransactionListAdapter.getPosition(offlineData);
            if(position > -1) {
                simpleTransactionListAdapter.getItem(position).setQueued(offlineData.isQueued());
                simpleTransactionListAdapter.getItem(position).setSynced(offlineData.isSynced());
                simpleTransactionListAdapter.notifyItemChanged(lvTransactions, position);
            }
        }
    }

    @Override
    public void onSyncProblem(OfflineData offlineData, boolean hasInternet, Object response, int responseCode) {

    }

    @Override
    public void onUnauthorizedAccess(Object response, int responseCode) {

    }

    @Override
    public void onAlreadyCancelled(OfflineData offlineData) {

    }

    @Override
    public void onSwableStopping() {

    }
}
