package net.nueca.concessioengine.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.NetworkImageView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.RVArrayAdapter;
import net.nueca.concessioengine.adapters.SimpleMultiInputAdapter;
import net.nueca.concessioengine.adapters.interfaces.ImageLoaderListener;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.BaseQuantityDialog;
import net.nueca.concessioengine.dialogs.SimpleQuantityDialog;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.operations.ImonggoTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhymart on 8/11/15.
 * imonggosdk (c)2015
 */
public class MultiInputSelectedItemFragment extends ImonggoFragment {

    public static final String PRODUCT_ID = "product_id";

    private int productId = 0;
    private NetworkImageView ivProductImage;
    private RecyclerView rvProducts;
    private Toolbar tbActionBar;
    private AutofitTextView tvTotalQuantity;
    private CollapsingToolbarLayout ctlActionBar;
    private SetupActionBar setupActionBar;
    private FloatingActionButton fabAddValue;

    private boolean hasUnits = false, hasBrand = true, hasDeliveryDate = true, hasBatchNo = false;
    private Product product;
    private SelectedProductItem selectedProductItem;

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
            product = getHelper().getProducts().queryBuilder().where().eq("id", productId).queryForFirst();
            hasBatchNo = product.getExtras().isBatch_maintained();

            ctlActionBar.setTitle(product.getName());
            String imageUrl = ImonggoTools.buildProductImageUrl(getActivity(), getSession().getApiToken(),
                    getSession().getAcctUrlWithoutProtocol(), product.getId() + "", true, false);
            ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getActivity(), true));
            ivProductImage.setColorFilter(Color.rgb(200, 200, 200), android.graphics.PorterDuff.Mode.MULTIPLY);

            selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().getSelectedProductItem(product);
            if(selectedProductItem == null) {
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
        simpleMultiInputAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                showQuantityDialog(selectedProductItem, position);
            }
        });

        rvProducts.setAdapter(simpleMultiInputAdapter);

        fabAddValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuantityDialog(selectedProductItem, -1);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.simple_multiinput_menu, menu);

        tvTotalQuantity = (AutofitTextView) menu.findItem(R.id.mQuantity).getActionView().findViewById(R.id.tvQuantity);
        tvTotalQuantity.setText(selectedProductItem.getQuantity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }

    private void showQuantityDialog(final SelectedProductItem selectedProductItem, final int valuePosition) {
        try {
            SimpleQuantityDialog quantityDialog = new SimpleQuantityDialog(getActivity(), R.style.AppCompatDialogStyle);
            quantityDialog.setSelectedProductItem(selectedProductItem);
            if(hasUnits) {
                quantityDialog.setHasUnits(true);
                quantityDialog.setUnitList(getHelper().getUnits().queryBuilder().where().eq("product_id", product).query(), true);
            }
            if(hasBrand) {
                List<ProductTag> tags = getHelper().getProductTags().queryBuilder().where().eq("product_id", product).query();
                List<String> brands = new ArrayList<>();

                for(ProductTag productTag : tags)
                    if(productTag.getTag().matches("^##[A-Za-z0-9_ ]*$"))
                        brands.add(productTag.getTag().replaceAll("##", ""));
                quantityDialog.setBrandList(brands, true);
                quantityDialog.setHasBrand(true);
            }
            quantityDialog.setHasBrand(hasBrand);
            quantityDialog.setHasDeliveryDate(hasDeliveryDate);
            quantityDialog.setHasBatchNo(hasBatchNo);
            quantityDialog.setIsMultiValue(true);
            quantityDialog.setValuePosition(valuePosition);
            quantityDialog.setFragmentManager(getActivity().getFragmentManager());
            quantityDialog.setMultiQuantityDialogListener(new BaseQuantityDialog.MultiQuantityDialogListener() {
                @Override
                public void onSave(Values values) {
                    if(values == null)
                        selectedProductItem.remove(valuePosition);
                    else {
                        int index = selectedProductItem.addValues(values);
                        if(index > -1)
                            simpleMultiInputAdapter.getItem(index).setValue(values.getQuantity(), values.getUnit(), values.getExtendedAttributes());
                    }

                    tvTotalQuantity.setText(selectedProductItem.getQuantity());
                    simpleMultiInputAdapter.notifyDataSetChanged();
                }

                @Override
                public void onDismiss() { }
            });
            quantityDialog.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}
