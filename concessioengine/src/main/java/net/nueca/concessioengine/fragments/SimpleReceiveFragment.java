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

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleReceiveListAdapter;
import net.nueca.concessioengine.adapters.SimpleReceiveRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.interfaces.OnItemLongClickListener;
import net.nueca.concessioengine.dialogs.SearchDRDialog;
import net.nueca.concessioengine.dialogs.SimpleReceiveDialog;
import net.nueca.concessioengine.views.SimpleReceiveToolbarExt;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.RemarkBuilder;
import net.nueca.imonggosdk.tools.NumberTools;

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

    private SimpleReceiveDialog simpleReceiveDialog;

    private TextView tvDRNo, tvQuantityLabel, tvDiscrepancyLabel;

    private Branch targetBranch;

    public User getUser() throws SQLException {
        if(getSession() == null)
            return null;
        return getSession().getUser();
    }

    @Override
    protected boolean shouldContinue() {
        if(!isManual)
            return true;

        List<DocumentLine> documentLines = new ArrayList<>();
        if(useRecyclerView)
            documentLines = simpleReceiveRecyclerViewAdapter.generateDocumentLines();
        else
            documentLines = simpleReceiveListAdapter.generateDocumentLines();

        return documentLines.size() > 0;
    }

    @Override
    protected Document generateReceiveDocument() {
        List<DocumentLine> documentLines = new ArrayList<>();
        if(useRecyclerView)
            documentLines = simpleReceiveRecyclerViewAdapter.generateDocumentLines();
        else
            documentLines = simpleReceiveListAdapter.generateDocumentLines();

        Document document = null;
        try {
            document = new Document.Builder()
                    .generateReference(getActivity(), getSession().getDevice_id())
                    .target_branch_id(targetBranch.getId())
                    .document_type_code(DocumentTypeCode.RECEIVE_BRANCH)
                    .document_lines(documentLines)
                    .remark(
                            new RemarkBuilder()
                                    .isManual(isManual)
                                    .page(1,1)
                                    .delivery_reference_no(isManual? getDeliveryReceiptNo() : null)
                                    .build()
                    )
                    .build();
            if (!isManual)
                document.setParent_document_id(getParentDocumentId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return document;
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
            simpleReceiveRecyclerViewAdapter = new SimpleReceiveRecyclerViewAdapter(getActivity(), getHelper(),
                    new ArrayList<DocumentLine>());
            simpleReceiveRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    onProductClick(position);
                }
            });
            simpleReceiveRecyclerViewAdapter.setOnItemLongClickListener(null);
            simpleReceiveRecyclerViewAdapter.initializeRecyclerView(getActivity(), rvProducts);

            rvProducts.setAdapter(simpleReceiveRecyclerViewAdapter);
            rvProducts.addOnScrollListener(rvScrollListener);

            toggleNoItems("No items to display.", simpleReceiveRecyclerViewAdapter.getItemCount() > 0);
        }
        else {
            lvProducts = (ListView) view.findViewById(R.id.lvProducts);
            simpleReceiveListAdapter = new SimpleReceiveListAdapter(getActivity(), getHelper(),
                    new ArrayList<DocumentLine>());
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

        try {
            searchDRDialog = new SearchDRDialog(getActivity(), getHelper(), getUser());

            searchDRDialog.setDialogListener(new SearchDRDialog.SearchDRDialogListener() {
                @Override
                public boolean onCancel() {
                    Log.e("onCancel", "called");
                    return tvDRNo != null && tvDRNo.getText().length() > 0;
                }

                @Override
                public void onSearch(String deliveryReceiptNo, Branch target_branch, Document document) {
                    targetBranch = target_branch;

                    Log.e("Search " + deliveryReceiptNo, "" + document.getDocument_lines().size());

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

                    if (tvDRNo == null && simpleReceiveToolbarExt != null)
                        tvDRNo = (TextView) simpleReceiveToolbarExt.getToolbarExtensionView()
                                .findViewById(R.id.tvDRNo);
                    if (tvDRNo != null)
                        tvDRNo.setText(deliveryReceiptNo);
                }

                @Override
                public void onManualReceive(String deliveryReceiptNo, Branch target_branch) {
                    targetBranch = target_branch;

                    setIsManual(true);
                    showQtyDscLabel(false);

                    offset = 0l;
                    prevLast = 0;

                    setDeliveryReceiptNo(deliveryReceiptNo);
                    setParentDocumentId(null);
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
                if(shouldContinue())
                    if(fabListener != null)
                        fabListener.onClick(generateReceiveDocument());
            }
        });

        return view;
    }

    public void showQtyDscLabel(boolean show) {
        tvQuantityLabel.setVisibility(show? View.VISIBLE : View.GONE);
        tvDiscrepancyLabel.setVisibility(show? View.VISIBLE : View.GONE);
    }

    private boolean isManual = false;

    public boolean isManual() {
        return isManual;
    }

    public void setIsManual(boolean isManual) {
        this.isManual = isManual;
    }

    public void forceUpdateProductList() {
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
            String category = productCategoriesAdapter.getItem(position).toLowerCase();
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

    public void setUseRecyclerView(boolean useRecyclerView) {
        this.useRecyclerView = useRecyclerView;
    }

    public void setFABListener(FloatingActionButtonListener fabListener) {
        this.fabListener = fabListener;
    }

    public interface FloatingActionButtonListener {
        void onClick(Document document);
    }

    private void onProductClick(final int position) {
        if(simpleReceiveDialog == null)
            simpleReceiveDialog = new SimpleReceiveDialog(getActivity());

        simpleReceiveDialog.setProductName(useRecyclerView ?
                simpleReceiveRecyclerViewAdapter.getProductItem(position).getName() :
                simpleReceiveListAdapter.getProductItem(position).getName());

        simpleReceiveDialog.setQuantity(useRecyclerView ?
                simpleReceiveRecyclerViewAdapter.getProductItem(position).getOrig_quantity() :
                simpleReceiveListAdapter.getProductItem(position).getOrig_quantity());

        simpleReceiveDialog.setReceiveText(useRecyclerView ?
                simpleReceiveRecyclerViewAdapter.getProductItem(position).getRcv_quantity() :
                simpleReceiveListAdapter.getProductItem(position).getRcv_quantity());

        simpleReceiveDialog.setReturnText(useRecyclerView ?
                simpleReceiveRecyclerViewAdapter.getProductItem(position).getRet_quantity() :
                simpleReceiveListAdapter.getProductItem(position).getRet_quantity());

        simpleReceiveDialog.setDiscrepancy(useRecyclerView ?
                simpleReceiveRecyclerViewAdapter.getProductItem(position).getDsc_quantity() :
                simpleReceiveListAdapter.getProductItem(position).getDsc_quantity());

        simpleReceiveDialog.setIsManual(isManual);

        simpleReceiveDialog.setDialogListener(new SimpleReceiveDialog.SimpleReceiveDialogListener() {
            @Override
            public boolean onCancel() {
                fabContinue.setVisibility(shouldContinue()? View.VISIBLE : View.INVISIBLE);
                return true;
            }

            @Override
            public void onSearch(String receivetxt, String returntxt) {
                Product product = null;
                if (useRecyclerView) {
                    simpleReceiveRecyclerViewAdapter.getProductItem(position).setRcv_quantity(receivetxt);
                    simpleReceiveRecyclerViewAdapter.getProductItem(position).setRet_quantity(returntxt);

                    product = simpleReceiveRecyclerViewAdapter.getProductItem(position);
                } else {
                    simpleReceiveListAdapter.getProductItem(position).setRcv_quantity(receivetxt);
                    simpleReceiveListAdapter.getProductItem(position).setRet_quantity(returntxt);

                    product = simpleReceiveListAdapter.getProductItem(position);
                }

                BigDecimal orig_qty = NumberTools.toBigDecimal(product.getOrig_quantity());
                BigDecimal rcv_qty = NumberTools.toBigDecimal(product.getRcv_quantity());
                BigDecimal ret_qty = NumberTools.toBigDecimal(product.getRet_quantity());

                if (useRecyclerView) {
                    simpleReceiveRecyclerViewAdapter.getProductItem(position).setDsc_quantity(
                            NumberTools.separateInCommas(orig_qty.subtract(rcv_qty.add(ret_qty))));
                    simpleReceiveRecyclerViewAdapter.notifyDataSetChanged();
                } else {
                    simpleReceiveListAdapter.getProductItem(position).setDsc_quantity(
                            NumberTools.separateInCommas(orig_qty.subtract(rcv_qty.add(ret_qty))));
                    simpleReceiveListAdapter.notifyDataSetChanged();
                }

                fabContinue.setVisibility(shouldContinue()? View.VISIBLE : View.INVISIBLE);
            }
        });

        simpleReceiveDialog.show();
    }
}
