package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.BaseProductsRecyclerAdapter;
import net.nueca.concessioengine.adapters.SimpleProductRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.tools.DividerItemDecoration;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.SimpleQuantityDialog;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.operations.ImonggoTools;

import java.sql.SQLException;

/**
 * Created by rhymart on 6/3/15.
 * imonggosdk (c)2015
 */
public class SimpleProductsFragment extends BaseProductsFragment {

    private SlidingUpPanelLayout suplProduct;
    private NetworkImageView ivProductImage;
    private TextView tvProductName, tvProductDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_products_fragment_rv, container, false);

        try {
            simpleProductRecyclerViewAdapter = new SimpleProductRecyclerViewAdapter(getActivity(), getHelper(), getHelper().getProducts().queryForAll());
            simpleProductRecyclerViewAdapter.setOnItemClickListener(new BaseProductsRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    try {
                        SelectedProductItem selectedProductItem = new SelectedProductItem();
                        selectedProductItem.setProduct(simpleProductRecyclerViewAdapter.getProductsList2().get(position));

                        SimpleQuantityDialog quantityDialog = new SimpleQuantityDialog(getActivity());
                        quantityDialog.setUnitList(getHelper().getUnits().queryForAll());
                        quantityDialog.setSelectedProductItem(selectedProductItem);
                        quantityDialog.show();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            simpleProductRecyclerViewAdapter.setOnItemLongClickListener(new BaseProductsRecyclerAdapter.OnItemLongClickListener() {
                @Override
                public void onItemLongClicked(View view, int position) {
                    Product product = simpleProductRecyclerViewAdapter.getProductsList2().get(position);

                    tvProductName.setText(product.getName());
                    tvProductDescription.setText(product.getDescription());
                    String imageUrl = ImonggoTools.buildProductImageUrl(getActivity(), ProductsAdapterHelper.getSession().getApiToken(), ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId() + "", true, false);
                    ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getActivity(), true));

                    suplProduct.setPanelState((suplProduct.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)
                            ? SlidingUpPanelLayout.PanelState.COLLAPSED : SlidingUpPanelLayout.PanelState.ANCHORED);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rvProducts = (RecyclerView) view.findViewById(R.id.rvProducts);
        suplProduct = (SlidingUpPanelLayout) view.findViewById(R.id.suplProduct);
        tvProductName = (TextView) view.findViewById(R.id.tvProductName);
        tvProductDescription = (TextView) view.findViewById(R.id.tvProductDescription);
        ivProductImage = (NetworkImageView) view.findViewById(R.id.ivProductImage);

        suplProduct.setAnchorPoint(0.5f);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        rvProducts.setLayoutManager(linearLayoutManager);
        rvProducts.setHasFixedSize(true);
        rvProducts.setAdapter(simpleProductRecyclerViewAdapter);
        rvProducts.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        return view;
    }

}
