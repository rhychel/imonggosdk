package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
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
import net.nueca.concessioengine.adapters.SimpleReceiveListAdapter;
import net.nueca.concessioengine.adapters.SimpleReceiveRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.dialogs.SearchDRDialog;
import net.nueca.concessioengine.dialogs.SimpleReceiveDialog;
import net.nueca.concessioengine.lists.ReceivedProductItemList;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.concessioengine.views.SimpleReceiveToolbarExt;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.tools.NumberTools;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by gama on 9/2/15.
 */
public class SimpleReceiveFragment extends BaseReceiveFragment {

    private TextView tvNoProducts;
    private Spinner spCategories;

    private boolean isManual = false;

    public FloatingActionButtonListener fabListener;

    private SearchDRDialog searchDRDialog;
    private SimpleReceiveToolbarExt simpleReceiveToolbarExt;

    private SimpleReceiveDialog simpleReceiveDialog;

    private TextView tvDRNo, tvQuantityLabel, tvDiscrepancyLabel;

    private Branch targetBranch;

    protected SimpleReceiveListAdapter simpleReceiveListAdapter;
    protected SimpleReceiveRecyclerViewAdapter simpleReceiveRecyclerViewAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            for(OfflineData offlineData : getHelper().getOfflineData().queryForAll()) {
                Log.e("OfflineData " + offlineData.getId(),
                        offlineData.getObjectFromData().getReference() + " " +
                        offlineData.getObjectFromData().getId() + " -- " + offlineData.getReturnId() + " >> isSynced?" +
                        offlineData.isSynced() + " isSyncing?" + offlineData.isSyncing() + " isQueued?" + offlineData
                        .isQueued() + " isCancelled?" + offlineData.isCancelled() + " isPastCutoff?" + offlineData
                        .isPastCutoff());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(hasCategories)
            productCategoriesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line,
                    productCategories);

        if(useRecyclerView) {
            simpleReceiveRecyclerViewAdapter = new SimpleReceiveRecyclerViewAdapter(getActivity(), getHelper());
            simpleReceiveRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    onProductClick(position);
                }
            });
            simpleReceiveRecyclerViewAdapter.setOnItemLongClickListener(null);
        }
        else {
            simpleReceiveListAdapter = new SimpleReceiveListAdapter(getActivity(), getHelper());
            //simpleReceiveListAdapter.setIsMultiline(isMultiline);
        }

        try {
            searchDRDialog = new SearchDRDialog(getActivity(), getHelper(), getUser());

            searchDRDialog.setDialogListener(new SearchDRDialog.SearchDRDialogListener() {
                @Override
                public boolean onCancel() {
                    Log.e("onCancel", "called");
                    if(tvDRNo == null || tvDRNo.getText().length() == 0)
                        getActivity().onBackPressed();
                    return tvDRNo != null && tvDRNo.getText().length() > 0;
                }

                @Override
                public void onSearch(String deliveryReceiptNo, Branch target_branch, Document document) {
                    getReceivedProductItemList().clear();
                    targetBranch = target_branch;

                    Log.e("Search " + deliveryReceiptNo, "" + document.getDocument_lines().size());
                    Log.e("Document", document.toString());

                    setIsManual(false);
                    showQtyDscLabel(true);
                    /*for(DocumentLine documentLine : documentLines) {
                        try {
                            Product product = getHelper().getProducts().queryBuilder()
                                    .where().eq("id", documentLine.getProduct_id())
                                    .queryForFirst();
                            Log.e("DocumentLine " + documentLine.getLine_no(),
                                    product.getName() + " status-isNull? " + (product.getStatus() == null));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }*/
                    offset = 0l;
                    prevLast = 0;

                    setDeliveryReceiptNo(deliveryReceiptNo);
                    setParentDocumentId(document.getId());
                    forceUpdateProductList();

                    if (tvDRNo != null)
                        tvDRNo.setText(deliveryReceiptNo);

                    for(DocumentLine documentLine : document.getDocument_lines())
                        addProductOf(documentLine);
                }

                @Override
                public void onManualReceive(String deliveryReceiptNo, Branch target_branch) {
                    getReceivedProductItemList().clear();
                    targetBranch = target_branch;

                    setIsManual(true);
                    showQtyDscLabel(false);

                    offset = 0l;
                    prevLast = 0;

                    setDeliveryReceiptNo(deliveryReceiptNo);
                    setParentDocumentId(null);
                    forceUpdateProductList();

                    if (tvDRNo != null)
                        tvDRNo.setText(deliveryReceiptNo);
                }
            });
            searchDRDialog.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(useRecyclerView ?
                R.layout.simple_receive_fragment_rv : R.layout.simple_receive_fragment_lv, container, false);

        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        spCategories = (Spinner) view.findViewById(R.id.spCategories);
        tvNoProducts = (TextView) view.findViewById(R.id.tvNoProducts);
        fabContinue = (FloatingActionButton) view.findViewById(R.id.fabContinue);

        tvQuantityLabel = (TextView) view.findViewById(R.id.tvQuantity);
        tvDiscrepancyLabel = (TextView) view.findViewById(R.id.tvDiscrepancy);

        if(hasCategories) {
            spCategories.setAdapter(productCategoriesAdapter);
            spCategories.setOnItemSelectedListener(onCategorySelected);
            //if(productCategories.size() > 0)
            //    setCategory(productCategories.get(0));
        }
        else
            spCategories.setVisibility(View.GONE);

        if(useRecyclerView) {
            rvProducts = (RecyclerView) view.findViewById(R.id.rvProducts);
            /*simpleReceiveRecyclerViewAdapter = new SimpleReceiveRecyclerViewAdapter(getActivity(), getHelper());
            simpleReceiveRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    onProductClick(position);
                }
            });
            simpleReceiveRecyclerViewAdapter.setOnItemLongClickListener(null);*/
            simpleReceiveRecyclerViewAdapter.initializeRecyclerView(getActivity(), rvProducts);

            rvProducts.setAdapter(simpleReceiveRecyclerViewAdapter);
            rvProducts.addOnScrollListener(rvScrollListener);

            toggleNoItems("No items to display.", simpleReceiveRecyclerViewAdapter.getItemCount() > 0);
        }
        else {
            lvProducts = (ListView) view.findViewById(R.id.lvProducts);
            //simpleReceiveListAdapter = new SimpleReceiveListAdapter(getActivity(), getHelper());
            lvProducts.setAdapter(simpleReceiveListAdapter);

            lvProducts.setOnScrollListener(lvScrollListener);

            lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onProductClick(position);
                }
            });

            toggleNoItems("No items to display.", simpleReceiveListAdapter.getCount() > 0);
        }

        fabContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shouldContinue())
                    if(fabListener != null)
                        fabListener.onClick(getReceivedProductItemList(), targetBranch, deliveryReceiptNo,
                                getParentDocumentId());
            }
        });

        return view;
    }

    public interface FloatingActionButtonListener {
        void onClick(ReceivedProductItemList receivedProductItemList, Branch targetBranch, String reference,
                     Integer parentDocumentID);
    }

    public void setFABListener(FloatingActionButtonListener fabListener) {
        this.fabListener = fabListener;
    }

    @Override
    protected boolean shouldContinue() {
        return !isManual || getReceivedProductItemList().size() > 0;
    }

    protected void addProductOf(DocumentLine documentLine) {
        Product product = documentLine.getProduct();
        SelectedProductItem selectedProductItem = getDisplayProductItemList()
                .getSelectedProductItem(product);
        if(selectedProductItem == null) {
            selectedProductItem = new SelectedProductItem();
            selectedProductItem.setProduct(product);
            if(product.getExtras() != null) {
                selectedProductItem.setIsMultiline(product.getExtras().isBatch_maintained());
            }
        }
        try {
            Unit unit = null;
            Integer unit_id = documentLine.getUnit_id();
            if(unit_id != null)
                unit = getHelper().getUnits().queryBuilder().where().eq("id", unit_id).queryForFirst();

            Values values = new Values();
            ExtendedAttributes extendedAttributes = new ExtendedAttributes(0d, documentLine.getQuantity());
            if(documentLine.getExtras() != null) {
                extendedAttributes.setBatch_no(documentLine.getExtras().getBatch_no());
            }
            if(documentLine.getExtended_attributes() != null) {
                extendedAttributes.setDelivery_date(documentLine.getExtended_attributes().getDelivery_date());
                extendedAttributes.setBrand(documentLine.getExtended_attributes().getBrand());
            }
            if(unit == null) {
                if(documentLine.getUnit_name() != null)
                    values.setUnit_name(documentLine.getUnit_name());
                if(documentLine.getUnit_content_quantity() != null)
                    values.setUnit_content_quantity(documentLine.getUnit_content_quantity());
                if(documentLine.getUnit_retail_price() != null)
                    values.setUnit_retail_price(documentLine.getUnit_retail_price());
                if(documentLine.getUnit_quantity() != null)
                    values.setUnit_quantity(""+documentLine.getUnit_quantity());
            }

            values.setValue("0", unit, extendedAttributes);

            values.setLine_no(documentLine.getLine_no());

            selectedProductItem.addValues(values);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getReceivedProductItemList().add(selectedProductItem);
    }

    @Override
    public ReceivedProductItemList getReceivedProductItemList() {
        return useRecyclerView? simpleReceiveRecyclerViewAdapter.getReceivedProductListItem() :
                simpleReceiveListAdapter.getReceivedProductListItem();
    }
    @Override
    public ReceivedProductItemList getDisplayProductItemList() {
        return useRecyclerView? simpleReceiveRecyclerViewAdapter.getDisplayProductListItem() :
                simpleReceiveListAdapter.getDisplayProductListItem();
    }

    @Override
    protected void clearSelectedItems() {
        getReceivedProductItemList().clear();
    }

    public void showQtyDscLabel(boolean show) {
        tvQuantityLabel.setVisibility(show? View.VISIBLE : View.GONE);
        tvDiscrepancyLabel.setVisibility(show? View.VISIBLE : View.GONE);
    }

    public boolean isManual() {
        return isManual;
    }

    public void setIsManual(boolean isManual) {
        this.isManual = isManual;
    }

    public void forceUpdateProductList() {
        Log.e("SimpleReceive", "forceUpdateProductList");
        if(useRecyclerView) {
            simpleReceiveRecyclerViewAdapter.setIsManual(isManual);
            simpleReceiveRecyclerViewAdapter.updateList(getDocumentLines());
            toggleNoItems("No items to display.", simpleReceiveRecyclerViewAdapter.getCount() > 0);
        }
        else {
            simpleReceiveListAdapter.setIsManual(isManual);
            simpleReceiveListAdapter.updateList(getDocumentLines());
            toggleNoItems("No items to display.", simpleReceiveListAdapter.getCount() > 0);
        }

        if(fabContinue != null)
            fabContinue.setVisibility(shouldContinue()? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void whenListEndReached(List<DocumentLine> documentLines) {
        Log.e("SimpleReceive", "whenListEndReached");
        if(useRecyclerView) {
            simpleReceiveRecyclerViewAdapter.addAll(documentLines);
            simpleReceiveRecyclerViewAdapter.notifyDataSetChanged();
        }
        else {
            simpleReceiveListAdapter.addAll(documentLines);
            simpleReceiveListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(simpleReceiveToolbarExt == null)
            simpleReceiveToolbarExt = new SimpleReceiveToolbarExt();
        simpleReceiveToolbarExt.setOnClickListener(new SimpleReceiveToolbarExt.OnToolbarClickedListener() {
            @Override
            public void onClick() {
                searchDRDialog.showWithText(((TextView) simpleReceiveToolbarExt.getToolbarExtensionView()
                        .findViewById(R.id.tvDRNo)).getText().toString());
            }
        });
        simpleReceiveToolbarExt.attachAfter(getActivity(), tbActionBar, false);

        tvDRNo = (TextView) simpleReceiveToolbarExt.getToolbarExtensionView()
                .findViewById(R.id.tvDRNo);

        if(tvDRNo != null && deliveryReceiptNo != null)
            tvDRNo.setText(deliveryReceiptNo);

        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }

    public void updateListWhenSearch(String searchKey) {
        Log.e("SimpleReceive", "updateListWhenSearch");
        setSearchKey(searchKey);
        offset = 0l;
        prevLast = 0;

        if(useRecyclerView)
            toggleNoItems("No results for \""+searchKey+"\""+messageCategory()+".", simpleReceiveRecyclerViewAdapter
                .updateList(getDocumentLines()));
        else
            toggleNoItems("No results for \"" + searchKey + "\"" + messageCategory() + ".",
                    simpleReceiveListAdapter.updateList(getDocumentLines()));
    }

    @Override
    protected void toggleNoItems(String msg, boolean show) {
        if(useRecyclerView) {
            rvProducts.setVisibility(show ? View.VISIBLE : View.GONE);
            tvNoProducts.setVisibility(show ? View.GONE : View.VISIBLE);
            tvNoProducts.setText(msg);

            if(show)
                rvProducts.scrollToPosition(0);
        }
        else {
            lvProducts.setVisibility(show ? View.VISIBLE : View.GONE);
            tvNoProducts.setVisibility(show ? View.GONE : View.VISIBLE);
            tvNoProducts.setText(msg);

            if(show)
                lvProducts.smoothScrollToPosition(0);
        }
    }

    private AdapterView.OnItemSelectedListener onCategorySelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            Log.e("SimpleReceive", "onCategorySelected : onItemSelected : " + getCategory());
            String category = productCategoriesAdapter.getItem(position).toLowerCase();

            if(getCategory().equals(category))
                return;

            setCategory(category);

            offset = 0l;
            prevLast = 0;

            if(useRecyclerView)
                toggleNoItems("No results for \"" + category + "\".",
                        simpleReceiveRecyclerViewAdapter.updateList(getDocumentLines()));
            else
                toggleNoItems("No results for \"" + category + "\".",
                        simpleReceiveListAdapter.updateList(getDocumentLines()));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { }
    };

    private void onProductClick(final int position) {
        if(simpleReceiveDialog == null)
            simpleReceiveDialog = new SimpleReceiveDialog(getActivity());

        final SelectedProductItem selectedProductItem = useRecyclerView?
                simpleReceiveRecyclerViewAdapter.getDisplayProductListItem().get(position) :
                simpleReceiveListAdapter.getDisplayProductListItem().get(position);

        Double original_qty;
        if(useRecyclerView)
            original_qty = simpleReceiveRecyclerViewAdapter.getItem(position).getQuantity();
        else
            original_qty = simpleReceiveListAdapter.getItem(position).getQuantity();

        if(selectedProductItem.isMultiline()) {
            ReceiveMultiInputFragment receiveMultiInputFragment = new
                    ReceiveMultiInputFragment();
            receiveMultiInputFragment.setHelper(getHelper());
            receiveMultiInputFragment.setSelectedProductItem(selectedProductItem);
            receiveMultiInputFragment.setIsManual(isManual);
            receiveMultiInputFragment.setOnValuesUpdatedListener(new ReceiveMultiInputFragment.OnValuesUpdatedListener() {
                @Override
                public void onUpdate(Values values) {
                    selectedProductItem.addValues(values);
                    if(isManual)
                        getReceivedProductItemList().add(selectedProductItem);
                }
            });
            //receiveMultiInputFragment.setOriginalQuantity(original_qty);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(getFragmentContainer(), receiveMultiInputFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("multiinput_fragment")
                    .commit();
            return;
        }

        simpleReceiveDialog.setProductName(selectedProductItem.getProduct().getName());

        simpleReceiveDialog.setQuantity(NumberTools.separateInCommas(original_qty));

        simpleReceiveDialog.setReceiveText(selectedProductItem.getQuantity());

        simpleReceiveDialog.setReturnText(selectedProductItem.getReturn());

        simpleReceiveDialog.setDiscrepancy(selectedProductItem.getDiscrepancy());

        simpleReceiveDialog.setIsManual(isManual);

        simpleReceiveDialog.setDialogListener(new SimpleReceiveDialog.SimpleReceiveDialogListener() {
            @Override
            public boolean onCancel() {
                fabContinue.setVisibility(shouldContinue() ? View.VISIBLE : View.INVISIBLE);
                return true;
            }

            @Override
            public void onSave(String receivetxt, String returntxt, String discrepancytxt) {
                SelectedProductItem selectedProductItem = getDisplayProductItemList().get(position);

                Log.e(selectedProductItem.getProduct().getName(), selectedProductItem.isMultiline() + "");

                if (!selectedProductItem.isMultiline()/** || selectedProductItem.getValues().size() <= 1*/) {
                    Values values;
                    if (selectedProductItem.getValues() != null && selectedProductItem.getValues().size() > 0)
                        values = selectedProductItem.getValues().get(0);
                    else
                        values = new Values();

                    /**
                     * TODO: replace UNIT
                     **/
//                    values.setValue(receivetxt, null, new ExtendedAttributes(
//                            NumberTools.toNullableDouble(returntxt), NumberTools.toNullableDouble(discrepancytxt)));
                    values.setQuantity(receivetxt);
                    ExtendedAttributes extendedAttributes = values.getExtendedAttributes();
                    if (extendedAttributes == null)
                        extendedAttributes = new ExtendedAttributes();
                    extendedAttributes.setOutright_return(returntxt);
                    extendedAttributes.setDiscrepancy(discrepancytxt);

                    values.setExtendedAttributes(extendedAttributes);

                    selectedProductItem.addValues(values);
                    getReceivedProductItemList().add(selectedProductItem);
                }

                if (useRecyclerView)
                    simpleReceiveRecyclerViewAdapter.notifyItemChanged(position);
                else
                    simpleReceiveListAdapter.notifyItemChanged(lvProducts, position);

                fabContinue.setVisibility(shouldContinue() ? View.VISIBLE : View.INVISIBLE);
            }
        });

        simpleReceiveDialog.show();
    }
}
