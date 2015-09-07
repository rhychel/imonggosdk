package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleReceiveListAdapter;
import net.nueca.concessioengine.dialogs.SearchDRDialog;
import net.nueca.concessioengine.dialogs.SimpleReceiveDialog;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.views.SimpleReceiveToolbarExt;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 9/2/15.
 */
public class SimpleReceiveFragment extends BaseReceiveFragment {

    private TextView tvNoProducts;
    private Spinner spCategories;

    private boolean useRecyclerView = true;

    public FloatingActionButtonListener fabListener;

    private SearchDRDialog searchDRDialog;
    private SimpleReceiveToolbarExt simpleReceiveToolbarExt;

    private SimpleReceiveListAdapter simpleReceiveListAdapter;

    private TextView tvDRNo, tvQuantityLabel, tvDiscrepancyLabel;

    public User getUser() throws SQLException {
        if(getSession() == null)
            return null;
        return getSession().getUser();
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
            productCategoriesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, productCategories);
            spCategories.setAdapter(productCategoriesAdapter);
            spCategories.setOnItemSelectedListener(onCategorySelected);
            if(productCategories.size() > 0)
                setCategory(productCategories.get(0));
        }
        else
            spCategories.setVisibility(View.GONE);

        offset = 0l;

        if(useRecyclerView) {
            rvProducts = (RecyclerView) view.findViewById(R.id.rvProducts);
        }
        else {
            lvProducts = (ListView) view.findViewById(R.id.lvProducts);
            simpleReceiveListAdapter = new SimpleReceiveListAdapter(getActivity(), getHelper(),
                    new ArrayList<DocumentLine>());
            lvProducts.setAdapter(simpleReceiveListAdapter);

            lvProducts.setOnScrollListener(lvScrollListener);

            lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final SimpleReceiveDialog simpleReceiveDialog = new SimpleReceiveDialog(getActivity());

                    simpleReceiveDialog.setProductName(
                            simpleReceiveListAdapter.getProductItem(position).getName());
                    simpleReceiveDialog.setReceiveText(
                            simpleReceiveListAdapter.getProductItem(position).getRcv_quantity());
                    simpleReceiveDialog.setReturnText(
                            simpleReceiveListAdapter.getProductItem(position).getRet_quantity());

                    simpleReceiveDialog.setDialogListener(new SimpleReceiveDialog.SimpleReceiveDialogListener() {
                        @Override
                        public boolean onCancel() {
                            return true;
                        }

                        @Override
                        public void onSearch(String receivetxt, String returntxt) {
                            simpleReceiveListAdapter.getProductItem(position).setRcv_quantity(receivetxt);
                            simpleReceiveListAdapter.getProductItem(position).setRet_quantity(returntxt);

                            Product product = simpleReceiveListAdapter.getProductItem(position);
                            BigDecimal orig_qty = new BigDecimal(product.getOrig_quantity().replaceAll("[^0-9.]",""));
                            BigDecimal rcv_qty = new BigDecimal(product.getRcv_quantity().replaceAll("[^0-9.]", ""));
                            BigDecimal ret_qty = new BigDecimal(product.getRet_quantity().replaceAll("[^0-9.]", ""));

                            Log.e(">>>>", orig_qty + " - " + rcv_qty + " + " + ret_qty);

                            simpleReceiveListAdapter.getProductItem(position).setDsc_quantity(String.format("%,1.0f",
                                    orig_qty.subtract(rcv_qty.add(ret_qty))));

                            simpleReceiveListAdapter.notifyDataSetChanged();
                        }
                    });

                    simpleReceiveDialog.show();
                }
            });

            toggleNoItems("No items to display.", simpleReceiveListAdapter.getCount() > 0);
        }

        try {
            searchDRDialog = new SearchDRDialog(getActivity(), getHelper(), getUser());

            searchDRDialog.setDialogListener(new SearchDRDialog.SearchDRDialogListener() {
                @Override
                public boolean onCancel() {
                    Log.e("onCancel", "called");
                    return tvDRNo != null && tvDRNo.getText().length() > 0;
                }

                @Override
                public void onSearch(String deliveryReceiptNo, Branch target_branch, List<DocumentLine> documentLines) {
                    Log.e("Search " + deliveryReceiptNo,""+documentLines.size());
                    simpleReceiveListAdapter.setIsManual(false);
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
                    forceUpdateProductList();

                    if (tvDRNo == null && simpleReceiveToolbarExt != null)
                        tvDRNo = (TextView) simpleReceiveToolbarExt.getToolbarExtensionView()
                                .findViewById(R.id.tvDRNo);
                    if (tvDRNo != null)
                        tvDRNo.setText(deliveryReceiptNo);
                }

                @Override
                public void onManualReceive(String deliveryReceiptNo, Branch target_branch) {
                    simpleReceiveListAdapter.setIsManual(true);
                    showQtyDscLabel(false);

                    offset = 0l;
                    prevLast = 0;

                    setDeliveryReceiptNo(null);
                    forceUpdateProductList();

                    if (tvDRNo == null && simpleReceiveToolbarExt != null)
                        tvDRNo = (TextView) simpleReceiveToolbarExt.getToolbarExtensionView()
                                .findViewById(R.id.tvDRNo);
                    if (tvDRNo != null)
                        tvDRNo.setText(deliveryReceiptNo);
                }
            });
            searchDRDialog.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        fabContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fabListener != null)
                    fabListener.onClick();
            }
        });

        return view;
    }

    public void showQtyDscLabel(boolean show) {
        tvQuantityLabel.setVisibility(show? View.VISIBLE : View.GONE);
        tvDiscrepancyLabel.setVisibility(show? View.VISIBLE : View.GONE);
    }

    public void forceUpdateProductList() {
        if(useRecyclerView)
            ;//simpleProductRecyclerViewAdapter.updateList(getDocumentLines());
        else
            simpleReceiveListAdapter.updateList(getDocumentLines());

        toggleNoItems("No items to display.", simpleReceiveListAdapter.getCount() > 0);
    }

    @Override
    protected void whenListEndReached(List<DocumentLine> documentLines) {
        if(useRecyclerView)
            ;//simpleProductRecyclerViewAdapter.addAll(productList);
        else
            simpleReceiveListAdapter.addAll(documentLines);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        simpleReceiveToolbarExt = new SimpleReceiveToolbarExt();
        simpleReceiveToolbarExt.attachAfter(getActivity(), tbActionBar, false);
        simpleReceiveToolbarExt.setOnClickListener(new SimpleReceiveToolbarExt.OnToolbarClickedListener() {
            @Override
            public void onClick() {
                searchDRDialog.showWithText(((TextView) simpleReceiveToolbarExt.getToolbarExtensionView()
                        .findViewById(R.id.tvDRNo)).getText().toString());
            }
        });

        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }

    public void updateListWhenSearch(String searchKey) {
        setSearchKey(searchKey);
        offset = 0l;
        prevLast = 0;

        if(useRecyclerView)
            ;//toggleNoItems("No results for \""+searchKey+"\""+messageCategory()+".", simpleProductRecyclerViewAdapter
            //    .updateList(getProducts()));
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
            String category = productCategoriesAdapter.getItem(position).toLowerCase();
            setCategory(category);
            offset = 0l;
            prevLast = 0;

            if(useRecyclerView)
                ;//toggleNoItems("No results for \"" + category + "\".",
                //        simpleProductRecyclerViewAdapter.updateList(getProducts()));
            else
                toggleNoItems("No results for \"" + category + "\".",
                        simpleReceiveListAdapter.updateList(getDocumentLines()));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { }
    };

    public void setUseRecyclerView(boolean useRecyclerView) {
        this.useRecyclerView = useRecyclerView;
    }

    public void setFABListener(FloatingActionButtonListener fabListener) {
        this.fabListener = fabListener;
    }

    public interface FloatingActionButtonListener {
        void onClick();
    }
}
