package net.nueca.concessioengine.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleProductListAdapter;
import net.nueca.concessioengine.adapters.SimpleProductRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.base.BaseProductsRecyclerAdapter;
import net.nueca.concessioengine.adapters.enums.ListingType;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.interfaces.OnItemLongClickListener;
import net.nueca.concessioengine.adapters.tools.DividerItemDecoration;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.BaseQuantityDialog;
import net.nueca.concessioengine.dialogs.SimpleQuantityDialog;
import net.nueca.concessioengine.dialogs.SimpleSalesQuantityDialog;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.objects.AccountSettings;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.tools.DialogTools;

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
    private TextView tvProductName, tvProductDescription, tvNoProducts;
    private Spinner spCategories;

    private boolean useRecyclerView = true;
    private int prevLast = -1, prevSelectedCategory = 0;

    private boolean isCustomAdapter = false;

    public static SimpleProductsFragment newInstance() {
        return new SimpleProductsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(useRecyclerView ? R.layout.simple_products_fragment_rv : R.layout.simple_products_fragment_lv, container, false);

        suplProduct = (SlidingUpPanelLayout) view.findViewById(R.id.suplProduct);
        tvProductName = (TextView) view.findViewById(R.id.tvProductName);
        tvProductDescription = (TextView) view.findViewById(R.id.tvProductDescription);
        ivProductImage = (NetworkImageView) view.findViewById(R.id.ivProductImage);
        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        spCategories = (Spinner) view.findViewById(R.id.spCategories);
        tvNoProducts = (TextView) view.findViewById(R.id.tvNoProducts);

        if(hasCategories) {
            productCategoriesAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_dark, productCategories);
            productCategoriesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
            spCategories.setAdapter(productCategoriesAdapter);
            spCategories.setOnItemSelectedListener(onCategorySelected);
            setCategory(productCategories.get(0));
        }
        else
            spCategories.setVisibility(View.GONE);

        suplProduct.setAnchorPoint(0.5f);
        offset = 0l;

        if(useRecyclerView) {
            rvProducts = (RecyclerView) view.findViewById(R.id.rvProducts);
            if(!isCustomAdapter)
                productRecyclerViewAdapter = new SimpleProductRecyclerViewAdapter(getActivity(), getHelper(), getProducts());
            else {
                productRecyclerViewAdapter.setDbHelper(getHelper());
                productRecyclerViewAdapter.setList(getProducts());
            }
            productRecyclerViewAdapter.setListingType(listingType);
            productRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    Product product = productRecyclerViewAdapter.getItem(position);
                    if (multipleInput) {
                        if (multiInputListener != null)
                            multiInputListener.showInputScreen(product);
                    } else {
                        SelectedProductItem selectedProductItem = productRecyclerViewAdapter.getSelectedProductItems()
                                .getSelectedProductItem(product);
                        if (selectedProductItem == null) {
                            selectedProductItem = new SelectedProductItem();
                            selectedProductItem.setProduct(product);
                            selectedProductItem.setInventory(product.getInventory()); // add the inventory object
                        }
                        showQuantityDialog(position, product, selectedProductItem);
                    }
                }
            });
            productRecyclerViewAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public void onItemLongClicked(View view, int position) {
                    Product product = productRecyclerViewAdapter.getItem(position);
                    showProductDetails(product);
                }
            });

            productRecyclerViewAdapter.initializeRecyclerView(getActivity(), rvProducts);

            rvProducts.setAdapter(productRecyclerViewAdapter);
            rvProducts.addOnScrollListener(rvScrollListener);

            toggleNoItems("No products available.", (productRecyclerViewAdapter.getItemCount() > 0));
        }
        else {
            lvProducts = (ListView) view.findViewById(R.id.lvProducts);
            productListAdapter = new SimpleProductListAdapter(getActivity(), getHelper(), getProducts());
            lvProducts.setAdapter(productListAdapter);
            lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Product product = productListAdapter.getItem(position);
                    if(multipleInput) {
                        if(multiInputListener != null)
                            multiInputListener.showInputScreen(product);
                    }
                    else {
                        SelectedProductItem selectedProductItem = productListAdapter.getSelectedProductItems().getSelectedProductItem(product);

                        if (selectedProductItem == null) {
                            selectedProductItem = new SelectedProductItem();
                            selectedProductItem.setProduct(product);
                            selectedProductItem.setInventory(product.getInventory()); // add the inventory object
                        }

                        showQuantityDialog(position, product, selectedProductItem);
                    }
                }
            });
            lvProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Product product = productListAdapter.getItem(position);
                    showProductDetails(product);

                    return true;
                }
            });
            lvProducts.setOnScrollListener(lvScrollListener);

            toggleNoItems("No products available.", (productListAdapter.getCount() > 0));
        }

        if(!hasToolBar)
            tbActionBar.setVisibility(View.GONE);

        return view;
    }

    public void setProductsRecyclerAdapter(BaseProductsRecyclerAdapter adapter) {
        productRecyclerViewAdapter = adapter;
        isCustomAdapter = productRecyclerViewAdapter != null;
    }


    public void refreshList() {
        if(useRecyclerView)
            productRecyclerViewAdapter.notifyDataSetChanged();
        else
            productListAdapter.notifyDataSetChanged();
        if(productsFragmentListener != null)
            productsFragmentListener.whenItemsSelectedUpdated();
    }

    public void setUseRecyclerView(boolean useRecyclerView) {
        this.useRecyclerView = useRecyclerView;
    }

    @Override
    protected void showQuantityDialog(final int position, Product product, SelectedProductItem selectedProductItem) {
        Log.e("SimpleProductFragment", selectedProductItem.toString());
        Log.e("SimpleProductFragment", "RetailPrice: " + selectedProductItem.getRetailPriceWithTax());

        try {
            if(listingType == ListingType.SALES) {
                SimpleSalesQuantityDialog simpleSalesQuantityDialog = new SimpleSalesQuantityDialog(getActivity(), R.style.AppCompatDialogStyle_Light_NoTitle);
                simpleSalesQuantityDialog.setSelectedProductItem(selectedProductItem);
                List<BranchPrice> branchPrices = getHelper().fetchForeignCollection(product.getBranchPrices().closeableIterator());
                double subtotal = product.getRetail_price()*Double.valueOf(ProductsAdapterHelper.getSelectedProductItems().getQuantity(product));
                if(branchPrices.size() > 0) {
                    simpleSalesQuantityDialog.setRetailPrice(String.format("P%.2f", branchPrices.get(0).getRetail_price()));
                    subtotal = branchPrices.get(0).getRetail_price()*Double.valueOf(ProductsAdapterHelper.getSelectedProductItems().getQuantity(product));
                }
                else
                    simpleSalesQuantityDialog.setRetailPrice(String.format("P%.2f", product.getRetail_price()));
                simpleSalesQuantityDialog.setSubtotal(String.format("P%.2f", subtotal));
                simpleSalesQuantityDialog.setUnitList(getHelper().fetchForeignCollection(product.getUnits().closeableIterator()), true);
                simpleSalesQuantityDialog.setFragmentManager(getActivity().getFragmentManager());
                simpleSalesQuantityDialog.setQuantityDialogListener(new BaseQuantityDialog.QuantityDialogListener() {
                    @Override
                    public void onSave(SelectedProductItem selectedProductItem) {
                        if (useRecyclerView) {
                            productRecyclerViewAdapter.getSelectedProductItems().add(selectedProductItem);
                            productRecyclerViewAdapter.notifyItemChanged(position);
                        } else {
                            productListAdapter.getSelectedProductItems().add(selectedProductItem);
                            productListAdapter.notifyItemChanged(lvProducts, position);
                        }
                        if (productsFragmentListener != null)
                            productsFragmentListener.whenItemsSelectedUpdated();
                    }

                    @Override
                    public void onDismiss() {
                    }
                });
                simpleSalesQuantityDialog.show();
            }
            else {
                SimpleQuantityDialog quantityDialog = new SimpleQuantityDialog(getActivity(), R.style.AppCompatDialogStyle);
                quantityDialog.setSelectedProductItem(selectedProductItem);
                if (hasUnits) {
                    quantityDialog.setHasUnits(true);
//                product.getUnits()
                    // getHelper().fetchObjects(Unit.class).queryBuilder().where().eq("product_id", product).query()
                    quantityDialog.setUnitList(getHelper().fetchForeignCollection(product.getUnits().closeableIterator()), true);
                }
                if (hasBrand) {
//                List<ProductTag> tags = getHelper().fetchObjects(ProductTag.class).queryBuilder().where().eq("product_id", product).query();
                    List<ProductTag> tags = getHelper().fetchForeignCollection(product.getTags().closeableIterator());
                    List<String> brands = new ArrayList<>();

                    for (ProductTag productTag : tags)
                        if (productTag.getTag().matches("^##[A-Za-z0-9_ ]*$"))
                            brands.add(productTag.getTag().replaceAll("##", ""));
                    quantityDialog.setBrandList(brands, true);
                    quantityDialog.setHasBrand(true);
                }
                quantityDialog.setHasDeliveryDate(hasDeliveryDate);
                quantityDialog.setFragmentManager(getActivity().getFragmentManager());
                quantityDialog.setQuantityDialogListener(new BaseQuantityDialog.QuantityDialogListener() {
                    @Override
                    public void onSave(SelectedProductItem selectedProductItem) {
                        if (useRecyclerView) {
                            productRecyclerViewAdapter.getSelectedProductItems().add(selectedProductItem);
                            productRecyclerViewAdapter.notifyItemChanged(position);
                        } else {
                            productListAdapter.getSelectedProductItems().add(selectedProductItem);
                            productListAdapter.notifyItemChanged(lvProducts, position);
                        }
                        if (productsFragmentListener != null)
                            productsFragmentListener.whenItemsSelectedUpdated();
                    }

                    @Override
                    public void onDismiss() {
                    }
                });
                quantityDialog.show();
            }
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
    protected void whenListEndReached(List<Product> productList) {
        if(useRecyclerView)
            productRecyclerViewAdapter.addAll(productList);
        else
            productListAdapter.addAll(productList);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
        if(showCategoryOnStart)
            new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    if(spCategories != null) {
                        spCategories.performClick();
                    }
                    return false;
                }
            }).sendEmptyMessageDelayed(0, 200);

    }

    private AdapterView.OnItemSelectedListener onCategorySelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, final int position, long id) {
            final String category = productCategoriesAdapter.getItem(position).toLowerCase();
            if(AccountSettings.allowLimitOrdersToOneCategory(getActivity())) {
                if(ProductsAdapterHelper.hasSelectedProductItems() && prevSelectedCategory != position)
                    DialogTools.showConfirmationDialog(getActivity(), "Ooopps!", "Selected items will be deleted. Would you like to switch to " + category + "?",
                            "Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ProductsAdapterHelper.clearSelectedProductItemList();
                                    changeCategory(category, position);
                                }
                            },
                            "No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    spCategories.setSelection(prevSelectedCategory);
                                }
                            });
                else
                    changeCategory(category, position);
            }
            else
                changeCategory(category, position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { }
    };

    private void changeCategory(String category, int position) {
        prevSelectedCategory = position;
        setCategory(category);
        offset = 0l;
        prevLast = 0;

        if(useRecyclerView)
            toggleNoItems("No results for \"" + category + "\".", productRecyclerViewAdapter.updateList(getProducts()));
        else
            toggleNoItems("No results for \"" + category + "\".", productListAdapter.updateList(getProducts()));
    }

    public void updateListWhenSearch(String searchKey) {
        setSearchKey(searchKey);
        offset = 0l;
        prevLast = 0;

        if(useRecyclerView)
            toggleNoItems("No results for \""+searchKey+"\""+messageCategory()+".", productRecyclerViewAdapter.updateList(getProducts()));
        else
            toggleNoItems("No results for \"" + searchKey + "\"" + messageCategory() + ".", productListAdapter.updateList(getProducts()));
    }

    public void forceUpdateProductList(List<Product> productList) {
        if(useRecyclerView)
            productRecyclerViewAdapter.updateList(productList);
        else
            productListAdapter.updateList(productList);
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
    }

    public void clearSelectedItems() {
        if(useRecyclerView) {
            productRecyclerViewAdapter.clearSelectedItems();
            productRecyclerViewAdapter.notifyDataSetChanged();
            toggleNoItems("No products available.", productRecyclerViewAdapter.updateList(new ArrayList<Product>()));
        } else {
            productListAdapter.clearSelectedItems();
            productListAdapter.notifyDataSetChanged();
            toggleNoItems("No products available.", productListAdapter.updateList(new ArrayList<Product>()));
        }
    }
}
