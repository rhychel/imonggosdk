package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.BaseProductsRecyclerAdapter;
import net.nueca.concessioengine.adapters.SimpleProductListAdapter;
import net.nueca.concessioengine.adapters.SimpleProductRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.tools.DividerItemDecoration;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.BaseQuantityDialog;
import net.nueca.concessioengine.dialogs.SimpleQuantityDialog;
import net.nueca.concessioengine.exceptions.ProductsFragmentException;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.operations.ImonggoTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 6/3/15. <br/>
 * imonggosdk (c)2015 <br/> <br/>
 *
 * To use the actionbar(Toolbar), implement BaseProductsFragment.SetupActionBar. It should return the actionbar(toolbar) to you via <code>setupActionBar<code/> method
 */
public class SimpleProductsFragment extends BaseProductsFragment {

    /**
     * For the product description
     */
    private SlidingUpPanelLayout suplProduct;
    private NetworkImageView ivProductImage;
    private TextView tvProductName, tvProductDescription;

    private boolean useRecyclerView = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(useRecyclerView ? R.layout.simple_products_fragment_rv : R.layout.simple_products_fragment_lv, container, false);

        suplProduct = (SlidingUpPanelLayout) view.findViewById(R.id.suplProduct);
        tvProductName = (TextView) view.findViewById(R.id.tvProductName);
        tvProductDescription = (TextView) view.findViewById(R.id.tvProductDescription);
        ivProductImage = (NetworkImageView) view.findViewById(R.id.ivProductImage);
        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);

        suplProduct.setAnchorPoint(0.5f);

        if(useRecyclerView) {
            rvProducts = (RecyclerView) view.findViewById(R.id.rvProducts);
            try {
                simpleProductRecyclerViewAdapter = new SimpleProductRecyclerViewAdapter(getActivity(), getHelper(), getHelper().getProducts().queryForAll());
                simpleProductRecyclerViewAdapter.setOnItemClickListener(new BaseProductsRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClicked(View view, int position) {
                        Product product = simpleProductRecyclerViewAdapter.getProductsList().get(position);
                        SelectedProductItem selectedProductItem = simpleProductRecyclerViewAdapter.getSelectedProductItems().getSelectedProductItem(product);
                        if (selectedProductItem == null) {
                            selectedProductItem = new SelectedProductItem();
                            selectedProductItem.setProduct(product);
                        }
                        showQuantityDialog(position, product, selectedProductItem);
                    }
                });
                simpleProductRecyclerViewAdapter.setOnItemLongClickListener(new BaseProductsRecyclerAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClicked(View view, int position) {
                        Product product = simpleProductRecyclerViewAdapter.getProductsList().get(position);
                        showProductDetails(product);
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }

            initializeRecyclerView(rvProducts);
            rvProducts.setAdapter(simpleProductRecyclerViewAdapter);
        }
        else {
            lvProducts = (ListView) view.findViewById(R.id.lvProducts);
            try {
                simpleProductListAdapter = new SimpleProductListAdapter(getActivity(), getHelper(), getHelper().getProducts().queryForAll());
                lvProducts.setAdapter(simpleProductListAdapter);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Product product = simpleProductListAdapter.getItem(position);
                    SelectedProductItem selectedProductItem = simpleProductListAdapter.getSelectedProductItems().getSelectedProductItem(product);
                    if(selectedProductItem == null) {
                        selectedProductItem = new SelectedProductItem();
                        selectedProductItem.setProduct(product);
                    }

                    showQuantityDialog(position, product, selectedProductItem);
                }
            });
            lvProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Product product = simpleProductListAdapter.getItem(position);
                    showProductDetails(product);
                    return true;
                }
            });
        }

        return view;
    }

    public void refreshList() {
        if(useRecyclerView)
            simpleProductRecyclerViewAdapter.notifyDataSetChanged();
        else
            simpleProductListAdapter.notifyDataSetChanged();
    }

    public void setUseRecyclerView(boolean useRecyclerView) {
        this.useRecyclerView = useRecyclerView;
    }

    @Override
    protected void showQuantityDialog(final int position, Product product, SelectedProductItem selectedProductItem) {
        try {
            List<ProductTag> tags = getHelper().getProductTags().queryBuilder().where().eq("product_id", product).query();
            List<String> brands = new ArrayList<>();

            for(ProductTag productTag : tags)
                if(productTag.getTag().matches("^##[A-Za-z0-9_ ]*$"))
                    brands.add(productTag.getTag().replaceAll("##", ""));

            SimpleQuantityDialog quantityDialog = new SimpleQuantityDialog(getActivity(), R.style.AppCompatDialogStyle);
            quantityDialog.setSelectedProductItem(selectedProductItem);
            quantityDialog.setUnitList(getHelper().getUnits().queryForAll(), true);
            //quantityDialog.setBrandList(brands);
            quantityDialog.setFragmentManager(getActivity().getFragmentManager());
            quantityDialog.setQuantityDialogListener(new BaseQuantityDialog.QuantityDialogListener() {
                @Override
                public void onSave(SelectedProductItem selectedProductItem) {
                    if(useRecyclerView) {
                        simpleProductRecyclerViewAdapter.getSelectedProductItems().add(selectedProductItem);
                        simpleProductRecyclerViewAdapter.notifyItemChanged(position);
                    }
                    else {
                        simpleProductListAdapter.getSelectedProductItems().add(selectedProductItem);
                        simpleProductListAdapter.notifyItemChanged(lvProducts, position);
                    }
                }

                @Override
                public void onDismiss() { }
            });
            quantityDialog.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void showProductDetails(Product product) {
        tvProductName.setText(product.getName());
        tvProductDescription.setText(product.getDescription());
        String imageUrl = ImonggoTools.buildProductImageUrl(getActivity(), ProductsAdapterHelper.getSession().getApiToken(), ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId() + "", true, false);
        ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getActivity(), true));

        suplProduct.setPanelState((suplProduct.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)
                ? SlidingUpPanelLayout.PanelState.COLLAPSED : SlidingUpPanelLayout.PanelState.ANCHORED);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }
}
