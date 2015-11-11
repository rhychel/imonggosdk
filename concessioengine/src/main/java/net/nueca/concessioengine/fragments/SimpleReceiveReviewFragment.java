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
import android.widget.ListView;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleReceiveListAdapter;
import net.nueca.concessioengine.adapters.SimpleReceiveRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.dialogs.SimpleReceiveDialog;
import net.nueca.concessioengine.lists.ReceivedProductItemList;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.concessioengine.views.SimpleReceiveToolbarExt;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.deprecated.DocumentLineExtras;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.RemarkBuilder;
import net.nueca.imonggosdk.tools.NumberTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 9/10/15.
 */
public class SimpleReceiveReviewFragment extends BaseReviewFragment {

    private TextView tvNoProducts;

    protected SimpleReceiveListAdapter simpleReceiveListAdapter;
    protected SimpleReceiveRecyclerViewAdapter simpleReceiveRecyclerViewAdapter;

    private SimpleReceiveDialog simpleReceiveDialog;

    private Branch targetBranch;

    private FloatingActionButtonListener fabListener;

    private SimpleReceiveToolbarExt simpleReceiveToolbarExt;

    private String DRNo = "";

    private Integer parentID;

    private int fragmentContainer;

    public int getFragmentContainer() {
        return fragmentContainer;
    }

    public void setFragmentContainer(int fragmentContainer) {
        this.fragmentContainer = fragmentContainer;
    }

    public void setTargetBranch(Branch targetBranch) {
        this.targetBranch = targetBranch;
    }

    public void setDRNo(String DRNo) {
        this.DRNo = DRNo;
    }

    public void setParentID(Integer parentID) {
        this.parentID = parentID;
    }

    protected Document generateReceiveDocument() {
        List<DocumentLine> documentLines = generateDocumentLines();
        Document document = null;
        try {
            document = new Document.Builder()
                    .generateReference(getActivity(), getSession().getDevice_id())
                    .target_branch_id(targetBranch.getId())
                    .document_type_code(DocumentTypeCode.RECEIVE_BRANCH)
                    .document_lines(documentLines)
                    //.intransit_status("Received")
                    .remark(
                            new RemarkBuilder()
                                    .isManual(isManual)
                                    .page(1,1)
                                    .delivery_reference_no(isManual? DRNo : null)
                                    .build()
                    )
                    .build();
            if (!isManual)
                document.setParent_document_id(parentID);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return document;
    }

    private List<DocumentLine> generateDocumentLines() {
        List<DocumentLine> documentLines = new ArrayList<>();
        for(SelectedProductItem selectedProductItem : receivedProductItemList) {
            Product product = selectedProductItem.getProduct();
            for(Values values : selectedProductItem.getValues()) {
                ExtendedAttributes extendedAttributes = values.getExtendedAttributes();
                Double outright_return = 0d;
                Double discrepancy = 0d;

                if(extendedAttributes != null) {
                    outright_return = NumberTools.toDouble(extendedAttributes.getOutright_return());
                    discrepancy = isManual ? 0d : NumberTools.toDouble(extendedAttributes.getDiscrepancy());
                }

                Double receive_qty = NumberTools.toDouble(values.getQuantity());

                int line_no = values.getLine_no();
                if (isManual) {
                    if (outright_return == 0d && receive_qty == 0d)
                        continue;
                    line_no = documentLines.size()+1;
                }

                documentLines.add(
                        new DocumentLine.Builder()
                                .line_no(line_no)
                                .product_id(product.getId())
                                .useProductDetails(product)
                                .quantity(receive_qty)
                                .extras(
                                        new DocumentLineExtras.Builder()
                                                .outright_return(outright_return != 0d ? "" + outright_return : null)
                                                .discrepancy(discrepancy != 0d ? "" + discrepancy : null)
                                                .buildIfNotEmpty()
                                )
                                .price(1)
                                .discount_text("0.0%")
                                .build()
                );
            }
        }
        return documentLines;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        offset = 0l;

        if(useRecyclerView) {
            simpleReceiveRecyclerViewAdapter = new SimpleReceiveRecyclerViewAdapter(getActivity(), getHelper());
            simpleReceiveRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    onProductClick(position);
                }
            });
            simpleReceiveRecyclerViewAdapter.setOnItemLongClickListener(null);

            simpleReceiveRecyclerViewAdapter.setReceivedProductListItem(receivedProductItemList);
            simpleReceiveRecyclerViewAdapter.setIsReview(true);

        }
        else {
            simpleReceiveListAdapter = new SimpleReceiveListAdapter(getActivity(), getHelper());

            simpleReceiveListAdapter.setReceivedProductListItem(receivedProductItemList);
            simpleReceiveListAdapter.setIsReview(true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(useRecyclerView ?
                R.layout.simple_review_fragment_rv : R.layout.simple_review_fragment_lv, container, false);

        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        tbActionBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        tbActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        tvNoProducts = (TextView) view.findViewById(R.id.tvNoProducts);
        fabContinue = (FloatingActionButton) view.findViewById(R.id.fabContinue);

        if(useRecyclerView) {
            rvProducts = (RecyclerView) view.findViewById(R.id.rvProducts);

            simpleReceiveRecyclerViewAdapter.initializeRecyclerView(getActivity(), rvProducts);

            rvProducts.setAdapter(simpleReceiveRecyclerViewAdapter);
            rvProducts.addOnScrollListener(rvScrollListener);

            toggleNoItems("No items to display.", simpleReceiveRecyclerViewAdapter.getItemCount() > 0);
            //forceUpdateProductList();
        }
        else {
            lvProducts = (ListView) view.findViewById(R.id.lvProducts);
            lvProducts.setAdapter(simpleReceiveListAdapter);

            lvProducts.setOnScrollListener(lvScrollListener);

            lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onProductClick(position);
                }
            });

            toggleNoItems("No items to display.", simpleReceiveListAdapter.getCount() > 0);
            //forceUpdateProductList();
        }

        fabContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Document document = generateReceiveDocument();
                if(fabListener != null && shouldContinue())
                    fabListener.onClick(document);
                Log.e("Document", document.toString());
            }
        });

        return view;
    }

    public interface FloatingActionButtonListener {
        void onClick(Document document);
    }

    public void setFABListener(FloatingActionButtonListener fabListener) {
        this.fabListener = fabListener;
    }

    protected boolean shouldContinue() {
        return !isManual || getReceivedProductItemList().size() > 0;
    }

    public List<SelectedProductItem> getReceivedProducts() {
        Log.e("SIZE", receivedProductItemList.size() + " " + offset + " " + LIMIT);
        if((int)offset > receivedProductItemList.size())
            return new ArrayList<>();

        if(receivedProductItemList.size() < LIMIT)
            return receivedProductItemList;

        if(receivedProductItemList.size() < (int)(offset+LIMIT))
            return receivedProductItemList.subList((int)offset, receivedProductItemList.size());

        return receivedProductItemList.subList((int) offset, (int) (offset+LIMIT));
    }

    public void forceUpdateProductList() {
        if(useRecyclerView) {
            simpleReceiveRecyclerViewAdapter.setIsManual(isManual);
            //simpleReceiveRecyclerViewAdapter.updateList(getObjects());
            toggleNoItems("No items to display.", simpleReceiveRecyclerViewAdapter.updateReceivedList(getReceivedProducts()));
        } else {
            simpleReceiveListAdapter.setIsManual(isManual);
            //simpleReceiveListAdapter.updateList(getObjects());
            toggleNoItems("No items to display.", simpleReceiveListAdapter.updateReceivedList(getReceivedProducts()));
        }
    }

    private View tvQuantityLabel, tvDiscrepancyLabel;
    public void showQtyDscLabel(boolean show) {
        if(tvQuantityLabel != null && tvDiscrepancyLabel != null) {
            tvQuantityLabel.setVisibility(show ? View.VISIBLE : View.GONE);
            tvDiscrepancyLabel.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(simpleReceiveToolbarExt == null) {
            simpleReceiveToolbarExt = new SimpleReceiveToolbarExt(R.layout.simple_receive_columns);
            forceUpdateProductList();
        }
        simpleReceiveToolbarExt.attachAfter(getActivity(), tbActionBar, false);

        tvQuantityLabel = simpleReceiveToolbarExt.getToolbarExtensionView().findViewById(R.id.tvQuantity);
        tvDiscrepancyLabel = simpleReceiveToolbarExt.getToolbarExtensionView().findViewById(R.id.tvDiscrepancy);

        showQtyDscLabel(!isManual);

        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }

    public ReceivedProductItemList getReceivedProductItemList() {
        return useRecyclerView? simpleReceiveRecyclerViewAdapter.getReceivedProductListItem() :
                simpleReceiveListAdapter.getReceivedProductListItem();
    }

    public ReceivedProductItemList getDisplayProductItemList() {
        return useRecyclerView? simpleReceiveRecyclerViewAdapter.getDisplayProductListItem() :
                simpleReceiveListAdapter.getDisplayProductListItem();
    }

    @Override
    protected void whenListEndReached(List objects) {
        if(useRecyclerView) {
            simpleReceiveRecyclerViewAdapter.addAllReceived(getReceivedProducts());
            simpleReceiveRecyclerViewAdapter.notifyDataSetChanged();
        }
        else {
            simpleReceiveListAdapter.addAllReceived(getReceivedProducts());
            simpleReceiveListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void toggleNoItems(String msg, boolean show) {
        if(useRecyclerView) {
            rvProducts.setVisibility(show ? View.VISIBLE : View.GONE);
            tvNoProducts.setVisibility(show ? View.GONE : View.VISIBLE);
            tvNoProducts.setText(msg);
        }
        else {
            lvProducts.setVisibility(show ? View.VISIBLE : View.GONE);
            tvNoProducts.setVisibility(show ? View.GONE : View.VISIBLE);
            tvNoProducts.setText(msg);
        }

        fabContinue.setVisibility(show? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected List<DocumentLine> getObjects() {
        return null;
    }

    @Override
    protected int getRecyclerAdapterItemCount() {
        return simpleReceiveRecyclerViewAdapter.getLinearLayoutManager().getItemCount();
    }

    @Override
    protected int getRecyclerAdapterFirstVisibleItemPosition() {
        return simpleReceiveRecyclerViewAdapter.getLinearLayoutManager().findFirstVisibleItemPosition();
    }

    private boolean isManual = false;

    public boolean isManual() {
        return isManual;
    }

    public void setIsManual(boolean isManual) {
        this.isManual = isManual;
    }

    private void onProductClick(final int position) {
        if(simpleReceiveDialog == null)
            simpleReceiveDialog = new SimpleReceiveDialog(getActivity());

        final SelectedProductItem selectedProductItem = useRecyclerView?
                simpleReceiveRecyclerViewAdapter.getDisplayProductListItem().get(position) :
                simpleReceiveListAdapter.getDisplayProductListItem().get(position);

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
                }
            });
            //receiveMultiInputFragment.setOriginalQuantity(original_qty);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(getFragmentContainer(), receiveMultiInputFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("review_multiinput_fragment")
                    .commitAllowingStateLoss();
            return;
        }

        simpleReceiveDialog.setProductName(selectedProductItem.getProduct().getName());

        simpleReceiveDialog.setQuantity(selectedProductItem.getOriginalQuantity());

        simpleReceiveDialog.setReceiveText(selectedProductItem.getQuantity());

        simpleReceiveDialog.setReturnText(selectedProductItem.getReturn());

        simpleReceiveDialog.setDiscrepancy(selectedProductItem.getDiscrepancy());

        simpleReceiveDialog.setIsManual(isManual);

        simpleReceiveDialog.setDialogListener(new SimpleReceiveDialog.SimpleReceiveDialogListener() {
            @Override
            public boolean onCancel() {
                return true;
            }

            @Override
            public void onSave(String receivetxt, String returntxt, String discrepancytxt) {

                SelectedProductItem selectedProductItem = getDisplayProductItemList().get(position);

                if (!selectedProductItem.isMultiline() || selectedProductItem.getValues().size() <= 1) {
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

                    if (isManual && NumberTools.toDouble(receivetxt) == 0d && NumberTools.toDouble(returntxt) == 0d) {
                        getReceivedProductItemList().remove(selectedProductItem);
                    } else {
                        getReceivedProductItemList().add(selectedProductItem);
                    }
                }

                if (useRecyclerView)
                    simpleReceiveRecyclerViewAdapter.notifyItemChanged(position);
                else
                    simpleReceiveListAdapter.notifyItemChanged(lvProducts, position);

                toggleNoItems("No items to display.", !getReceivedProductItemList().isEmpty());
            }
        });

        simpleReceiveDialog.show();
    }
}
