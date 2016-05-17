package net.nueca.concessioengine.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.android.volley.toolbox.NetworkImageView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.RVArrayAdapter;
import net.nueca.concessioengine.adapters.SimpleMultiInputAdapter;
import net.nueca.concessioengine.adapters.interfaces.ImageLoaderListener;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.BaseQuantityDialog;
import net.nueca.concessioengine.dialogs.SimpleQuantityDialog;
import net.nueca.concessioengine.dialogs.SimpleSalesQuantityDialog;
import net.nueca.concessioengine.enums.DialogType;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.operations.ImonggoTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhymart on 8/11/15.
 * imonggosdk (c)2015
 */
public class MultiInputSelectedItemFragment extends BaseProductsFragment {

    public static final String PRODUCT_ID = "product_id";
    public static final String IS_MANUAL_RECEIVE = "is_manual_receive";
    public static String TAG = "MultiInputSelectedItemFragment";
    private int productId = 0;
    private NetworkImageView ivProductImage;
    private RecyclerView rvProducts;
    private Toolbar tbActionBar;
    private AutofitTextView tvTotalQuantity;
    private CollapsingToolbarLayout ctlActionBar;
    private SetupActionBar setupActionBar;
    private FloatingActionButton fabAddValue;
    private boolean hasUnits = false, hasBrand = true, hasDeliveryDate = true, hasBatchNo = false, isManualReceive = false;
    private ConcessioModule concessioModule;
    private Product product;
    private SelectedProductItem selectedProductItem;

    private DialogType mDialogType;

    private SimpleMultiInputAdapter simpleMultiInputAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_multiinput_layout, container, false);

        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        rvProducts = (RecyclerView) view.findViewById(R.id.rvProducts);
        ivProductImage = (NetworkImageView) view.findViewById(R.id.ivProductImage);
        fabAddValue = (FloatingActionButton) view.findViewById(R.id.fabAddValue);

        ctlActionBar = (CollapsingToolbarLayout) view.findViewById(R.id.ctlActionBar);

        try {
            product = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", productId).queryForFirst();

            hasBatchNo = product.getExtras() != null && product.getExtras().isBatch_maintained() != null ? product.getExtras().isBatch_maintained() : false;

            ctlActionBar.setTitle(product.getName());
            String imageUrl = ImonggoTools.buildProductImageUrl(getActivity(), getSession().getApiToken(),
                    getSession().getAcctUrlWithoutProtocol(), product.getId() + "", true, false);
            ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getActivity(), true));
            ivProductImage.setColorFilter(Color.rgb(200, 200, 200), android.graphics.PorterDuff.Mode.MULTIPLY);

            selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().getSelectedProductItem(product);
            if (selectedProductItem == null) {
                selectedProductItem = new SelectedProductItem();
                selectedProductItem.setProduct(product);
                selectedProductItem.setIsMultiline(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        simpleMultiInputAdapter = new SimpleMultiInputAdapter(getActivity());
        simpleMultiInputAdapter.setList(selectedProductItem.getValues());
        simpleMultiInputAdapter.initializeRecyclerView(getActivity(), rvProducts);
        simpleMultiInputAdapter.setHasDeliveryDate(hasDeliveryDate);
        simpleMultiInputAdapter.setHasBrand(hasBrand);
        simpleMultiInputAdapter.setHasBatchNo(hasBatchNo);
        simpleMultiInputAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                showQuantityDialog(selectedProductItem, position);

            }
        });
        if(concessioModule == ConcessioModule.PHYSICAL_COUNT || isManualReceive) {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(rvProducts);

            fabAddValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showQuantityDialog(selectedProductItem, -1);
                }
            });
        }
        else
            fabAddValue.setVisibility(View.GONE);

        rvProducts.setAdapter(simpleMultiInputAdapter);

        return view;
    }


    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if(target != null)
                Log.e("target", "Not null");

            if(viewHolder != null)
                Log.e("viewHolder", "Not null");
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            Log.e("onSwiped", "Index=" + viewHolder.getAdapterPosition());
            selectedProductItem.remove(viewHolder.getAdapterPosition());

//            simpleMultiInputAdapter.remove(viewHolder.getAdapterPosition());
            simpleMultiInputAdapter.notifyItemChanged(0);

            tvTotalQuantity.setText(selectedProductItem.getActualQuantity()+" "+product.getBase_unit_name());
            simpleMultiInputAdapter.notifyDataSetChanged();
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            Log.e("getSwipeDirs", "Yeah");
            return super.getSwipeDirs(recyclerView, viewHolder);
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.simple_multiinput_menu, menu);

        tvTotalQuantity = (AutofitTextView) menu.findItem(R.id.mQuantity).getActionView().findViewById(R.id.tvQuantity);
        tvTotalQuantity.setText(selectedProductItem.getActualQuantity()+" "+product.getBase_unit_name());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }

    public void setDialogType(DialogType dialogType) {
        if (dialogType != null) {
            mDialogType = dialogType;
        }
    }


    private void showQuantityDialog(final SelectedProductItem selectedProductItem, final int position) {
        SimpleSalesQuantityDialog simpleSalesQuantityDialog = new SimpleSalesQuantityDialog(getActivity(), R.style.AppCompatDialogStyle_Light_NoTitle);
        simpleSalesQuantityDialog.setValuePosition(position);
        simpleSalesQuantityDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        simpleSalesQuantityDialog.setSelectedProductItem(selectedProductItem);
        simpleSalesQuantityDialog.setHelper(getHelper());
        simpleSalesQuantityDialog.setIsMultiValue(true);
        simpleSalesQuantityDialog.setHasStock(false);
        if(concessioModule == ConcessioModule.RECEIVE_BRANCH)
           simpleSalesQuantityDialog.setUnitDisplay(!isManualReceive);

//        if(productRecyclerViewAdapter instanceof BaseSalesProductRecyclerAdapter) {
//            BaseSalesProductRecyclerAdapter salesAdapter = (BaseSalesProductRecyclerAdapter) productRecyclerViewAdapter;
//            simpleSalesQuantityDialog.setHelper(salesAdapter.getHelper());
//            simpleSalesQuantityDialog.setSalesCustomer(salesAdapter.getCustomer());
//            simpleSalesQuantityDialog.setSalesCustomerGroup(salesAdapter.getCustomerGroup());
//        }
//        else
//            simpleSalesQuantityDialog.setForceSellableUnit(true);
//        simpleSalesQuantityDialog.setSalesBranch(productRecyclerViewAdapter.getBranch());

//        simpleSalesQuantityDialog.setHasSubtotal(hasSubtotal);
        simpleSalesQuantityDialog.setHasUnits(true);
//        simpleSalesQuantityDialog.setHasInvoicePurpose(isReturnItems);
//        simpleSalesQuantityDialog.setHasExpiryDate(isReturnItems);
//        simpleSalesQuantityDialog.setHasBadStock(isReturnItems);
//        simpleSalesQuantityDialog.setInvoicePurposeList(InvoicePurpose.fetchAll(getHelper(), InvoicePurpose.class));

//        double subtotal = product.getRetail_price() * Double.valueOf(productRecyclerViewAdapter.getSelectedProductItems().getQuantity
//                (product));
//        simpleSalesQuantityDialog.setRetailPrice(String.format("P%.2f", product.getRetail_price()));
//        simpleSalesQuantityDialog.setSubtotal(String.format("P%.2f", subtotal));

//        boolean addBaseProduct = true;
//        if(concessioModule == ConcessioModule.INVOICE) {
//            // Improve!
//            addBaseProduct = !getHelper().fetchForeignCollection(product.getBranchProducts().closeableIterator(), new ImonggoDBHelper2.Conditional<BranchProduct>() {
//                @Override
//                public boolean validate(BranchProduct obj) {
//                    return obj.getUnit() == null;
//                }
//            }).isEmpty();
//        }

        try {
            simpleSalesQuantityDialog.setUnitList(getUnits(product));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(isManualReceive)
            simpleSalesQuantityDialog.setHasOutright(true);
        else {
            if(concessioModule == ConcessioModule.PHYSICAL_COUNT) {
                simpleSalesQuantityDialog.setHasBrand(true);

                try {
                    List<ProductTag> tags = getHelper().fetchForeignCollection(product.getTags().closeableIterator());
                    List<String> brands = new ArrayList<>();

                    for (ProductTag productTag : tags)
                        if (productTag.getTag().matches("^##[A-Za-z0-9_ ]*$"))
                            brands.add(productTag.getTag().replaceAll("##", ""));
    //                brands.add("Sample 1"); // TODO Remove this
    //                brands.add("Sample 2");
                    simpleSalesQuantityDialog.setBrandList(brands, true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else {
                simpleSalesQuantityDialog.setHasOutright(true);
                simpleSalesQuantityDialog.setHasDiscrepancy(true);
                simpleSalesQuantityDialog.setHasExpectedQty(true);
            }
        }

        simpleSalesQuantityDialog.setFragmentManager(getActivity().getFragmentManager());
        simpleSalesQuantityDialog.setMultiQuantityDialogListener(new BaseQuantityDialog.MultiQuantityDialogListener() {
            @Override
            public void onSave(Values values) {
                if (values == null)
                    selectedProductItem.remove(position);
                else {
                    values.setAllow_decimal(product.isAllow_decimal_quantities());
                    int index = selectedProductItem.addValues(values);
                    if (index > -1)
                        simpleMultiInputAdapter.set(position, values);
//                        simpleMultiInputAdapter.getItem(index).setValue(values.getQuantity(), values.getUnit(), values.getExtendedAttributes());
                }

                tvTotalQuantity.setText(selectedProductItem.getActualQuantity()+" "+product.getBase_unit_name());
                simpleMultiInputAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDismiss() {

            }
        });
        simpleSalesQuantityDialog.show();

//        if (mDialogType == DialogType.SALES) {
//            SimpleQuantityDialog quantityDialog = new SimpleQuantityDialog(getActivity(), R.style.AppCompatDialogStyle);
//            quantityDialog.setListPosition(position);
//            quantityDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//            quantityDialog.setSelectedProductItem(selectedProductItem);
//
//            if (hasUnits) {
//                quantityDialog.setHasUnits(true);
//                try {
//                    quantityDialog.setUnitList(getHelper().fetchForeignCollection(product.getUnits().closeableIterator()), true);
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if (hasBrand) {
//                List<ProductTag> tags = null;
//                try {
//                    tags = getHelper().fetchForeignCollection(product.getTags().closeableIterator());
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//                List<String> brands = new ArrayList<>();
//
//                for (ProductTag productTag : tags)
//                    if (productTag.getTag().matches("^##[A-Za-z0-9_ ]*$"))
//                        brands.add(productTag.getTag().replaceAll("##", ""));
//                quantityDialog.setBrandList(brands, true);
//                quantityDialog.setHasBrand(true);
//            }
//            quantityDialog.setHasDeliveryDate(hasDeliveryDate);
//            quantityDialog.setFragmentManager(getActivity().getFragmentManager());
//            //quantityDialog.setQuantityDialogListener(quantityDialogListener);
//            quantityDialog.show();
//        } else {
//            try {
//                SimpleQuantityDialog quantityDialog = new SimpleQuantityDialog(getActivity(), R.style.AppCompatDialogStyle);
//                quantityDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//                quantityDialog.setSelectedProductItem(selectedProductItem);
//                if (hasUnits) {
//                    quantityDialog.setHasUnits(true);
//                    quantityDialog.setUnitList(getHelper().fetchObjects(Unit.class).queryBuilder().where().eq("product_id", product).query(), true);
//                }
//                if (hasBrand) {
//                    List<ProductTag> tags = getHelper().fetchObjects(ProductTag.class).queryBuilder().where().eq("product_id", product).query();
//                    List<String> brands = new ArrayList<>();
//
//                    for (ProductTag productTag : tags)
//                        if (productTag.getTag().matches("^##[A-Za-z0-9_ ]*$"))
//                            brands.add(productTag.getTag().replaceAll("##", ""));
//                    quantityDialog.setBrandList(brands, true);
//                    quantityDialog.setHasBrand(true);
//                }
//                quantityDialog.setHasBrand(hasBrand);
//                quantityDialog.setHasDeliveryDate(hasDeliveryDate);
//                quantityDialog.setHasBatchNo(hasBatchNo);
//                quantityDialog.setIsMultiValue(true);
//                quantityDialog.setValuePosition(position);
//                quantityDialog.setFragmentManager(getActivity().getFragmentManager());
//                quantityDialog.setMultiQuantityDialogListener(new BaseQuantityDialog.MultiQuantityDialogListener() {
//                    @Override
//                    public void onSave(Values values) {
//                        if (values == null)
//                            selectedProductItem.remove(position);
//                        else {
//                            int index = selectedProductItem.addValues(values);
//                            if (index > -1)
//                                simpleMultiInputAdapter.getItem(index).setValue(values.getQuantity(), values.getUnit(), values.getExtendedAttributes());
//                        }
//
//                        tvTotalQuantity.setText(selectedProductItem.getQuantity());
//                        simpleMultiInputAdapter.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onDismiss() {
//                    }
//                });
//                quantityDialog.show();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void updateSelectedProductItem() {
        ProductsAdapterHelper.getSelectedProductItems().add(selectedProductItem);
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setHasUnits(boolean hasUnits) {
        this.hasUnits = hasUnits;
    }

    public void setHasBrand(boolean hasBrand) {
        this.hasBrand = hasBrand;
    }

    public void setHasDeliveryDate(boolean hasDeliveryDate) {
        this.hasDeliveryDate = hasDeliveryDate;
    }

    public void setHasBatchNo(boolean hasBatchNo) {
        this.hasBatchNo = hasBatchNo;
    }

    public void setManualReceive(boolean manualReceive) {
        isManualReceive = manualReceive;
    }

    public void setConcessioModule(ConcessioModule concessioModule) {
        this.concessioModule = concessioModule;
    }

    // -------------------------- IGNORE
    @Override
    protected void showQuantityDialog(int position, Product product, SelectedProductItem selectedProductItem) {

    }

    @Override
    protected void showProductDetails(Product product) {

    }

    @Override
    protected void whenListEndReached(List<Product> productList) {

    }

    @Override
    protected void toggleNoItems(String msg, boolean show) {

    }

}
