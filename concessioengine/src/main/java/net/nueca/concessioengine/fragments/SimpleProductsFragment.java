package net.nueca.concessioengine.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.j256.ormlite.stmt.Where;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleProductListAdapter;
import net.nueca.concessioengine.adapters.SimpleProductRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.SimpleSalesProductRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseProductsRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseSalesProductRecyclerAdapter;
import net.nueca.concessioengine.dialogs.SimplePulloutRequestDialog;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.interfaces.OnItemLongClickListener;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.BaseQuantityDialog;
import net.nueca.concessioengine.dialogs.SimpleQuantityDialog;
import net.nueca.concessioengine.dialogs.SimpleSalesQuantityDialog;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.AccountSettings;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.TimerTools;

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

    private LinearLayout llReason;
    private TextView tvReason;
    private ImageView ivEdit;

    private boolean useRecyclerView = true;
    private int prevLast = -1, prevSelectedCategory = 0;

    private boolean isCustomAdapter = false;

    public static SimpleProductsFragment newInstance() {
        return new SimpleProductsFragment();
    }

    public void showReasonDialog(boolean emptyReason) {
        showReasonDialog(emptyReason, true);
    }

    public void showReasonDialog(boolean emptyReason, boolean autoShow) {
        SimplePulloutRequestDialog simplePulloutRequestDialog = new SimplePulloutRequestDialog(getActivity(), getHelper(), R.style.AppCompatDialogStyle_Light_NoTitle);
        simplePulloutRequestDialog.setDTitle(getModuleSetting(concessioModule).getLabel());
        simplePulloutRequestDialog.setShouldShowBranchSelection(false);
        simplePulloutRequestDialog.setCurrentReason(ProductsAdapterHelper.getReason());
        simplePulloutRequestDialog.setListener(new SimplePulloutRequestDialog.PulloutRequestDialogListener() {
            @Override
            public void onSave(DocumentPurpose reason, Branch source, Branch destination) {
                ProductsAdapterHelper.setReason(reason);
                ProductsAdapterHelper.setSource(source);
                ProductsAdapterHelper.setDestination(destination);

                if(reason != null)
                    tvReason.setText(reason.getName());
            }

            @Override
            public void onCancel() {
                if(getModuleSetting(concessioModule).isRequire_document_reason())
                   getActivity().finish();
            }
        });

        if(!isFinalize && autoShow)
            simplePulloutRequestDialog.show();

        if(emptyReason)
            tvReason.setText("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(useRecyclerView ? R.layout.simple_products_fragment_rv : R.layout.simple_products_fragment_lv, container,
                false);

        if(hasPromotionalProducts)
            getPromotionalProducts();

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
            if(productCategories.size() > 0)
                setCategory(productCategories.get(0));
        }
        else
            spCategories.setVisibility(View.GONE);

        suplProduct.setAnchorPoint(0.5f);
        offset = 0l;

        Log.e("useRecyclerView", useRecyclerView+"");
        if(useRecyclerView) {
            llReason = (LinearLayout) view.findViewById(R.id.llReason);
            if(concessioModule == ConcessioModule.RELEASE_ADJUSTMENT || concessioModule == ConcessioModule.RELEASE_BRANCH) {
                llReason.setVisibility(View.VISIBLE);
                tvReason = (TextView) view.findViewById(R.id.tvReason);
                ivEdit = (ImageView) view.findViewById(R.id.ivEdit);

                if(isFinalize)
                    ivEdit.setVisibility(View.GONE);
                else
                    ivEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showReasonDialog(false);
//                        SimplePulloutRequestDialog simplePulloutRequestDialog = new SimplePulloutRequestDialog(getActivity(), getHelper(), R.style.AppCompatDialogStyle_Light_NoTitle);
//                        simplePulloutRequestDialog.setDTitle("MSO");
//                        simplePulloutRequestDialog.setShouldShowBranchSelection(false);
//                        simplePulloutRequestDialog.setCurrentReason(ProductsAdapterHelper.getReason());
//                        simplePulloutRequestDialog.setListener(new SimplePulloutRequestDialog.PulloutRequestDialogListener() {
//                            @Override
//                            public void onSave(DocumentPurpose reason, Branch source, Branch destination) {
//                                ProductsAdapterHelper.setReason(reason);
//                                tvReason.setText(reason.getName());
//                            }
//
//                            @Override
//                            public void onCancel() {
//
//                            }
//                        });
//                        simplePulloutRequestDialog.show();
                        }
                    });

                if(ProductsAdapterHelper.getReason() != null)
                    tvReason.setText(ProductsAdapterHelper.getReason().getName());
            }
            rvProducts = (RecyclerView) view.findViewById(R.id.rvProducts);
            if(!isCustomAdapter) {
                if (useSalesProductAdapter) {
                    productRecyclerViewAdapter = new SimpleSalesProductRecyclerAdapter(getActivity(), getHelper(), getProducts());//, customer, customerGroup, branch

                    ((SimpleSalesProductRecyclerAdapter)productRecyclerViewAdapter).setBranch(ProductsAdapterHelper.getSelectedBranch());
                    ((SimpleSalesProductRecyclerAdapter)productRecyclerViewAdapter).setCustomer(ProductsAdapterHelper.getSelectedCustomer());
                    ((SimpleSalesProductRecyclerAdapter)productRecyclerViewAdapter).setCustomerGroup(ProductsAdapterHelper.getSelectedCustomerGroup());
                    ((SimpleSalesProductRecyclerAdapter)productRecyclerViewAdapter).setPromotionalProducts(promotionalProducts);
                }
                else {
                    productRecyclerViewAdapter = new SimpleProductRecyclerViewAdapter(getActivity(), getHelper(), getProducts());
                    productRecyclerViewAdapter.setBranch(ProductsAdapterHelper.getSelectedBranch());
                }
            }
            else {
                productRecyclerViewAdapter.setDbHelper(getHelper());
                productRecyclerViewAdapter.setList(getProducts());
            }
            productRecyclerViewAdapter.setReturnItems(isReturnItems);
            productRecyclerViewAdapter.setHasSubtotal(hasSubtotal);
            productRecyclerViewAdapter.setListingType(listingType);
            if(!displayOnly)
                productRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClicked(View view, int position) {
                        Product product = productRecyclerViewAdapter.getItem(position);
                        if (multipleInput) {
                            if (multiInputListener != null)
                                multiInputListener.showInputScreen(product);
                        } else {
                            SelectedProductItem selectedProductItem = productRecyclerViewAdapter.getSelectedProductItems().getSelectedProductItem(product);
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

            if(isFinalize && !displayOnly) {
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
                itemTouchHelper.attachToRecyclerView(rvProducts);
            }

            toggleNoItems("No products available.", (productRecyclerViewAdapter.getItemCount() > 0));
        }
        else {
            lvProducts = (ListView) view.findViewById(R.id.lvProducts);
            productListAdapter = new SimpleProductListAdapter(getActivity(), getHelper(), getProducts());
            lvProducts.setAdapter(productListAdapter);
            if(!displayOnly)
                lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Product product = productListAdapter.getItem(position);
                        if (multipleInput) {
                            if (multiInputListener != null)
                                multiInputListener.showInputScreen(product);
                        } else {
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

        if(productsFragmentListener != null)
            productsFragmentListener.whenItemsSelectedUpdated();

        return view;
    }

    public void setProductsRecyclerAdapter(BaseProductsRecyclerAdapter adapter) {
        productRecyclerViewAdapter = adapter;
        isCustomAdapter = productRecyclerViewAdapter != null;
    }

    public BaseProductsRecyclerAdapter getProductsRecyclerAdapter() {
        return productRecyclerViewAdapter;
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
        try {
            if(listingType == ListingType.SALES || listingType == ListingType.ADVANCED_SALES) {
                TimerTools.start("showQuantityDialog");
                SimpleSalesQuantityDialog simpleSalesQuantityDialog = new SimpleSalesQuantityDialog(getActivity(), R.style.AppCompatDialogStyle_Light_NoTitle);
                simpleSalesQuantityDialog.setListPosition(position);
                simpleSalesQuantityDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                simpleSalesQuantityDialog.setSelectedProductItem(selectedProductItem);
                simpleSalesQuantityDialog.setHelper(getHelper());

                if(productRecyclerViewAdapter instanceof BaseSalesProductRecyclerAdapter) {
                    BaseSalesProductRecyclerAdapter salesAdapter = (BaseSalesProductRecyclerAdapter) productRecyclerViewAdapter;
                    simpleSalesQuantityDialog.setHelper(salesAdapter.getHelper());
                    simpleSalesQuantityDialog.setSalesCustomer(salesAdapter.getCustomer());
                    simpleSalesQuantityDialog.setSalesCustomerGroup(salesAdapter.getCustomerGroup());
                }
                else
                    simpleSalesQuantityDialog.setForceSellableUnit(true);
                simpleSalesQuantityDialog.setSalesBranch(productRecyclerViewAdapter.getBranch());

                simpleSalesQuantityDialog.setHasSubtotal(hasSubtotal);
                simpleSalesQuantityDialog.setHasUnits(hasUnits);
                simpleSalesQuantityDialog.setHasInvoicePurpose(isReturnItems);
                simpleSalesQuantityDialog.setHasExpiryDate(isReturnItems);
                simpleSalesQuantityDialog.setHasBadStock(isReturnItems);

                if(concessioModule == ConcessioModule.RECEIVE_BRANCH_PULLOUT) {
                    simpleSalesQuantityDialog.setHasStock(false);
                    simpleSalesQuantityDialog.setHasPrice(false);
                    simpleSalesQuantityDialog.setUnitDisplay(true);
                    simpleSalesQuantityDialog.setHasExpectedQty(true);
                }

                simpleSalesQuantityDialog.setInvoicePurposeList(InvoicePurpose.fetchAll(getHelper(), InvoicePurpose.class));

                double subtotal = product.getRetail_price() * Double.valueOf(productRecyclerViewAdapter.getSelectedProductItems().getQuantity
                        (product));
                simpleSalesQuantityDialog.setRetailPrice(String.format("P%.2f", product.getRetail_price()));
                simpleSalesQuantityDialog.setSubtotal(String.format("P%.2f", subtotal));

                List<Unit> unitList = new ArrayList<>();
//                boolean addBaseProduct = true;
                //concessioModule == ConcessioModule.INVOICE && ---- ALL MODULES should inherit branch pricing for units
                if(getHelper().fetchObjects(BranchProduct.class).countOf() > 0l) { // TODO CHECK IF ACCOUNT HAS BRANCH PRODUCTS
                    // Improve!
//                    addBaseProduct = !getHelper().fetchForeignCollection(product.getBranchProducts().closeableIterator(), new ImonggoDBHelper2.Conditional<BranchProduct>() {
//                        @Override
//                        public boolean validate(BranchProduct obj) {
//                            return obj.getUnit() == null;
//                        }
//                    }).isEmpty();

                    List<BranchProduct> branchProducts = getHelper().fetchForeignCollection(product.getBranchProducts().closeableIterator());

                    for(BranchProduct branchProduct : branchProducts) {
                        Unit unit = branchProduct.getUnit();
                        if(unit == null) {
                            unit = new Unit();
                            unit.setId(-1);
                            unit.setName(product.getBase_unit_name());
                            unit.setRetail_price(branchProduct.getUnit_retail_price());
                            if(unitList.size() > 0 && (product.getExtras().getDefault_selling_unit() == null || product.getExtras().getDefault_selling_unit().equals("")))
                                unitList.add(0, unit);
                            else
                                unitList.add(unit);
                            Log.e("Unit", "is null --> " + product.getExtras().getDefault_selling_unit());
                        }
                        else {
                            unit.setRetail_price(branchProduct.getUnit_retail_price());
                            if(product.getExtras().getDefault_selling_unit().equals(unit.getId()+"") && unitList.size() > 0)
                                unitList.add(0, unit);
                            else
                                unitList.add(unit);
                        }

                        Log.e("Unit", "default selling unit id --> "+product.getExtras().getDefault_selling_unit());
                    }
                }
                else
                    unitList = getHelper().fetchForeignCollection(product.getUnits().closeableIterator());

                if(hasUnits)
                    simpleSalesQuantityDialog.setUnitList(unitList); //, addBaseProduct
                if (hasBrand) {
                    List<ProductTag> tags = getHelper().fetchForeignCollection(product.getTags().closeableIterator());
                    List<String> brands = new ArrayList<>();

                    for (ProductTag productTag : tags)
                        if (productTag.getTag().matches("^##[A-Za-z0-9_ ]*$"))
                            brands.add(productTag.getTag().replaceAll("##", ""));
                    brands.add("Sample 1");
                    brands.add("Sample 2");
                    simpleSalesQuantityDialog.setBrandList(brands, true);
                    simpleSalesQuantityDialog.setHasBrand(true);
                }
                simpleSalesQuantityDialog.setFragmentManager(getActivity().getFragmentManager());
                simpleSalesQuantityDialog.setQuantityDialogListener(quantityDialogListener);
                simpleSalesQuantityDialog.show();
                TimerTools.duration("showQuantityDialog");
            }
            else {
                SimpleQuantityDialog quantityDialog = new SimpleQuantityDialog(getActivity(), R.style.AppCompatDialogStyle);
                quantityDialog.setListPosition(position);
                quantityDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
                quantityDialog.setQuantityDialogListener(quantityDialogListener);
                quantityDialog.show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private BaseQuantityDialog.QuantityDialogListener quantityDialogListener = new BaseQuantityDialog.QuantityDialogListener() {
        @Override
        public void onSave(SelectedProductItem selectedProductItem, int position) {
            if (useRecyclerView) {
                boolean isRemoved = productRecyclerViewAdapter.getSelectedProductItems().add(selectedProductItem);
                if(isRemoved && isFinalize)
                    productRecyclerViewAdapter.notifyDataSetChanged();
                else
                    productRecyclerViewAdapter.notifyItemChanged(position);
            } else {
                productListAdapter.getSelectedProductItems().add(selectedProductItem);
                productListAdapter.notifyItemChanged(lvProducts, position);
            }
            Log.e("ProductsFragListener", productsFragmentListener+"");
            if (productsFragmentListener != null)
                productsFragmentListener.whenItemsSelectedUpdated();
        }

        @Override
        public void onDismiss() {

        }
    };

    @Override
    protected void showProductDetails(Product product) {
        tvProductName.setText(product.getName());
        tvProductDescription.setText(product.getDescription());
        String imageUrl = ImonggoTools.buildProductImageUrl(getActivity(), ProductsAdapterHelper.getSession().getApiToken(),
                ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId() + "", true, false);
        ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getActivity(), true));

        suplProduct.setPanelState((suplProduct.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)
                ? SlidingUpPanelLayout.PanelState.COLLAPSED : SlidingUpPanelLayout.PanelState.ANCHORED);
    }

    @Override
    protected void whenListEndReached(List<Product> productList) {
        if(useRecyclerView) {
            productRecyclerViewAdapter.addAll(productList);
            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    productRecyclerViewAdapter.notifyDataSetChanged();
                }
            };
            handler.sendEmptyMessageDelayed(0, 200);
        }
        else
            productListAdapter.addAll(productList);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
        if(ProductsAdapterHelper.getReason() != null)
            tvReason.setText(ProductsAdapterHelper.getReason().getName());
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
            if(lockCategory) {
                if(ProductsAdapterHelper.hasSelectedProductItems() && prevSelectedCategory != position)
                    DialogTools.showConfirmationDialog(getActivity(), "Ooopps!", "Selected items will be deleted. Would you like to switch to " + category + "?",
                            "Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ProductsAdapterHelper.clearSelectedProductItemList(false, false);
                                    changeCategory(category, position);
                                    productsFragmentListener.whenItemsSelectedUpdated();
                                }
                            },
                            "No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    spCategories.setSelection(prevSelectedCategory);
                                }
                            }, R.style.AppCompatDialogStyle_Light);
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

        if(productRecyclerViewAdapter != null)
            Log.e("productRecyclerViewAd", "is not null");
        if(useRecyclerView)
            toggleNoItems("No results for \"" + searchKey + "\"" + messageCategory() + ".", productRecyclerViewAdapter.updateList(getProducts()));
        else
            toggleNoItems("No results for \"" + searchKey + "\"" + messageCategory() + ".", productListAdapter.updateList(getProducts()));
    }

    public void forceUpdateProductList() {
        forceUpdateProductList(getProducts());
    }

    public void forceUpdateProductList(List<Product> productList) {
        if(useRecyclerView)
            productRecyclerViewAdapter.updateList(productList);
        else
            productListAdapter.updateList(productList);
        toggleNoItems("No products available.", (productRecyclerViewAdapter.getItemCount() > 0));
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

    // ---------------------- Return
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
            if(isReturnItems) {
                SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedReturnProductItems().getSelectedProductItem(productRecyclerViewAdapter.getItem(viewHolder.getAdapterPosition()));
                Log.e("onSwiped="+isReturnItems, ProductsAdapterHelper.getSelectedReturnProductItems().size() + "-before");
                ProductsAdapterHelper.getSelectedReturnProductItems().remove(selectedProductItem);
                Log.e("onSwiped="+isReturnItems, ProductsAdapterHelper.getSelectedReturnProductItems().size() + "-after");
            }
            else {
                SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().getSelectedProductItem(productRecyclerViewAdapter.getItem(viewHolder.getAdapterPosition()));
                Log.e("onSwiped="+isReturnItems, ProductsAdapterHelper.getSelectedProductItems().size() + "-before");
                ProductsAdapterHelper.getSelectedProductItems().remove(selectedProductItem);
                Log.e("onSwiped="+isReturnItems, ProductsAdapterHelper.getSelectedProductItems().size() + "-after");
            }

            productRecyclerViewAdapter.remove(viewHolder.getAdapterPosition());
            productRecyclerViewAdapter.notifyItemChanged(0);
            productRecyclerViewAdapter.notifyDataSetChanged();

            if (productsFragmentListener != null)
                productsFragmentListener.whenItemsSelectedUpdated();
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            Log.e("getSwipeDirs", "Yeah");
            return super.getSwipeDirs(recyclerView, viewHolder);
        }
    };

}
