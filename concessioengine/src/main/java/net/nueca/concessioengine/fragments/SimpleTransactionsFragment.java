package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleTransactionListAdapter;
import net.nueca.concessioengine.adapters.SimpleTransactionRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.TransactionTypesAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.swable.ImonggoSwable;
import net.nueca.imonggosdk.swable.SwableSendModule;
import net.nueca.imonggosdk.swable.SwableTools;

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
    private TextView tvNoTransactions;

    private SimpleTransactionListAdapter simpleTransactionListAdapter;
    private SimpleTransactionRecyclerViewAdapter simpleTransactionRecyclerViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(useRecyclerView ? R.layout.simple_transactions_fragment_rv : R.layout.simple_transactions_fragment_lv, container, false);

        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        spBranches = (Spinner) view.findViewById(R.id.spBranches);
        spTransactionType = (Spinner) view.findViewById(R.id.spTransactionType);
        tvNoTransactions = (TextView) view.findViewById(R.id.tvNoTransactions);

        if(hasFilterByBranch) {
            spBranches.setVisibility(View.VISIBLE);
            branchAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_dark, branches);
            branchAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
            spBranches.setAdapter(branchAdapter);
        }
        if(hasFilterByTransactionType) {
            spTransactionType.setVisibility(View.VISIBLE);
            transactionTypeAdapter = new TransactionTypesAdapter(getActivity(), R.layout.spinner_item_dark, transactionTypes);
            transactionTypeAdapter.setDropdownLayout(R.layout.spinner_dropdown_item_list_light);
//            transactionTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
            spTransactionType.setAdapter(transactionTypeAdapter);
            spTransactionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    concessioModule = transactionTypeAdapter.getItem(position);
                    simpleTransactionRecyclerViewAdapter.updateList(getTransactions());

                    toggleNoItems("No transactions"+(concessioModule == ConcessioModule.ALL ? "" : " for "+concessioModule.getLabel()),
                            (simpleTransactionRecyclerViewAdapter.getItemCount() > 0));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }

        if(useRecyclerView) {
            rvTransactions = (RecyclerView) view.findViewById(R.id.rvTransactions);
            simpleTransactionRecyclerViewAdapter = new SimpleTransactionRecyclerViewAdapter(getActivity(), getTransactions(), listingType);
            simpleTransactionRecyclerViewAdapter.setDbHelper(getHelper());
            simpleTransactionRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    if(transactionsListener != null)
                        transactionsListener.showTransactionDetails(simpleTransactionRecyclerViewAdapter.getItem(position));
                }
            });

            simpleTransactionRecyclerViewAdapter.initializeRecyclerView(getActivity(), rvTransactions);
            rvTransactions.setAdapter(simpleTransactionRecyclerViewAdapter);

            toggleNoItems("No transactions", (simpleTransactionRecyclerViewAdapter.getItemCount() > 0));
        }
        else {
            lvTransactions = (ListView) view.findViewById(R.id.lvTransactions);
            simpleTransactionListAdapter = new SimpleTransactionListAdapter(getActivity(), getTransactions());
            lvTransactions.setAdapter(simpleTransactionListAdapter);
            lvTransactions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    if(transactionsListener != null)
                        transactionsListener.showTransactionDetails(simpleTransactionListAdapter.getItem(position));
                }
            });

            toggleNoItems("No transactions", (simpleTransactionListAdapter.getCount() > 0));
        }
        return view;
    }

    /*
    * Used after the duplication
    * */
    public void addOfflineData(OfflineData offlineData) {
        if(useRecyclerView)
            simpleTransactionRecyclerViewAdapter.add(0, offlineData);
        else
            simpleTransactionListAdapter.add(offlineData);

    }

    public void updateOfflineData(OfflineData offlineData) {
        if(useRecyclerView) {
            int indexOf = simpleTransactionRecyclerViewAdapter.getList().indexOf(offlineData);
            simpleTransactionRecyclerViewAdapter.getItem(indexOf).setSynced(false);
            simpleTransactionRecyclerViewAdapter.notifyItemChanged(indexOf);
        }
        else {
            int indexOf = simpleTransactionListAdapter.getPosition(offlineData);
            simpleTransactionListAdapter.getItem(indexOf).setSynced(false);
            simpleTransactionListAdapter.notifyItemChanged(lvTransactions, indexOf);
        }
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

    @Override
    protected void toggleNoItems(String msg, boolean show) {
        if(useRecyclerView) {
            rvTransactions.setVisibility(show ? View.VISIBLE : View.GONE);
            tvNoTransactions.setVisibility(show ? View.GONE : View.VISIBLE);
            tvNoTransactions.setText(msg);
        }
        else {
            lvTransactions.setVisibility(show ? View.VISIBLE : View.GONE);
            tvNoTransactions.setVisibility(show ? View.GONE : View.VISIBLE);
            tvNoTransactions.setText(msg);
        }
    }

    // *** THESE ARE FOR SWABLE ***
    @Override
    public void onSwableStarted() {
        Log.e("onSwableStarted", "yeah");
    }

    @Override
    public void onQueued(OfflineData offlineData) {
        Log.e("onQueued", "yeah");
        if(useRecyclerView) {
            int position = simpleTransactionRecyclerViewAdapter.getPosition(offlineData);
            if(position > -1) {
                simpleTransactionRecyclerViewAdapter.getItem(position).setCancelled(offlineData.isCancelled());
                simpleTransactionRecyclerViewAdapter.getItem(position).setQueued(offlineData.isQueued());
                simpleTransactionRecyclerViewAdapter.getItem(position).setSynced(offlineData.isSynced());
                simpleTransactionRecyclerViewAdapter.getItem(position).setSyncing(offlineData.isSyncing());
                simpleTransactionRecyclerViewAdapter.notifyItemChanged(position);
            }
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
        Log.e("onSyncing", "yeah");
        if(useRecyclerView) {
            int position = simpleTransactionRecyclerViewAdapter.getPosition(offlineData);
            if(position > -1) {
                simpleTransactionRecyclerViewAdapter.getItem(position).setCancelled(offlineData.isCancelled());
                simpleTransactionRecyclerViewAdapter.getItem(position).setQueued(offlineData.isQueued());
                simpleTransactionRecyclerViewAdapter.getItem(position).setSynced(offlineData.isSynced());
                simpleTransactionRecyclerViewAdapter.getItem(position).setSyncing(offlineData.isSyncing());
                simpleTransactionRecyclerViewAdapter.notifyItemChanged(position);
            }
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
        Log.e("onSynced", "yeah");
        if(useRecyclerView) {
            int position = simpleTransactionRecyclerViewAdapter.getPosition(offlineData);
            if (position > -1) {
                simpleTransactionRecyclerViewAdapter.getItem(position).setCancelled(offlineData.isCancelled());
                simpleTransactionRecyclerViewAdapter.getItem(position).setQueued(offlineData.isQueued());
                simpleTransactionRecyclerViewAdapter.getItem(position).setSynced(offlineData.isSynced());
                simpleTransactionRecyclerViewAdapter.getItem(position).setSyncing(offlineData.isSyncing());
                simpleTransactionRecyclerViewAdapter.notifyItemChanged(position);
            }
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
        Log.e("onSyncProblem", "yeah");
        if(useRecyclerView) {
            int position = simpleTransactionRecyclerViewAdapter.getPosition(offlineData);
            if (position > -1) {
                simpleTransactionRecyclerViewAdapter.getItem(position).setCancelled(offlineData.isCancelled());
                simpleTransactionRecyclerViewAdapter.getItem(position).setQueued(offlineData.isQueued());
                simpleTransactionRecyclerViewAdapter.getItem(position).setSynced(offlineData.isSynced());
                simpleTransactionRecyclerViewAdapter.getItem(position).setSyncing(offlineData.isSyncing());

                simpleTransactionRecyclerViewAdapter.notifyItemChanged(position);
            }
        }
    }

    @Override
    public void onUnauthorizedAccess(Object response, int responseCode) {

    }

    @Override
    public void onAlreadyCancelled(OfflineData offlineData) {

    }

    @Override
    public void onSwableStopping() {
        Log.e("onSwableStopping", "yeah");
    }

    public void updateListWhenSearch(String searchKey) {
        setSearchKey(searchKey);

        if(simpleTransactionRecyclerViewAdapter != null)
            Log.e("productRecyclerViewAd", "is not null");
        if(useRecyclerView)
            toggleNoItems("No results for \"" + searchKey + "\".", simpleTransactionRecyclerViewAdapter.updateList(getTransactions()));
        else
            toggleNoItems("No results for \"" + searchKey + "\".", simpleTransactionListAdapter.updateList(getTransactions()));
    }

}
