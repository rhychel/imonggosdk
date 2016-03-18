package net.nueca.concessioengine.fragments;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.adapters.base.BaseProductsAdapter;
import net.nueca.concessioengine.adapters.base.BaseProductsRecyclerAdapter;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.exceptions.ProductsFragmentException;
import net.nueca.concessioengine.fragments.interfaces.ListScrollListener;
import net.nueca.concessioengine.fragments.interfaces.MultiInputListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.accountsettings.ProductSorting;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.salespromotion.Discount;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;
import net.nueca.imonggosdk.tools.DateTimeTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rhymart on 7/15/15.
 * imonggosdk (c)2015
 */
public abstract class BaseProductsFragment extends ImonggoFragment {

    // For special sales pricing and discounting
    protected Customer customer;
    protected CustomerGroup customerGroup;
    protected Branch branch;
    // For special sales pricing and discounting

    protected static final long LIMIT = 100l;
    protected long offset = 0l;
    protected boolean hasUnits = true,
            hasBrand = false,
            hasDeliveryDate = false,
            hasCategories = true,
            multipleInput = false,
            showCategoryOnStart = false,
            lockCategory = false,
            hasToolBar = true,
            hasSubtotal = false,
            isFinalize = false,
            displayOnly = false,
            useSalesProductAdapter = false,
            hasPromotionalProducts = false,
            isReturnItems = false;
    private int prevLast = -1;
    private String searchKey = "", category = "";
    protected DocumentPurpose reason = null;
    private List<Product> filterProductsBy = new ArrayList<>();
    protected List<Product> promotionalProducts = new ArrayList<>();
    protected ListingType listingType = ListingType.BASIC;

    protected ArrayAdapter<String> productCategoriesAdapter;
    protected List<String> productCategories = new ArrayList<>();

    public interface ProductsFragmentListener {
        void whenItemsSelectedUpdated();
    }

    protected ProductsFragmentListener productsFragmentListener;
    protected ListScrollListener listScrollListener;
    protected SetupActionBar setupActionBar;
    protected MultiInputListener multiInputListener;
    protected RecyclerView rvProducts;
    protected ListView lvProducts;
    protected Toolbar tbActionBar;

    protected BaseProductsRecyclerAdapter productRecyclerViewAdapter;
    protected BaseProductsAdapter productListAdapter;

    protected ConcessioModule concessioModule = ConcessioModule.NONE;

    protected abstract void showQuantityDialog(int position, Product product, SelectedProductItem selectedProductItem);
    protected abstract void showProductDetails(Product product);
    protected abstract void whenListEndReached(List<Product> productList);
    protected abstract void toggleNoItems(String msg, boolean show);

    public String renderProducts() {
        String jsonSelected = "{}";
        ProductsAdapterHelper.getSelectedProductItems().renderToJson();
        return jsonSelected;
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    public void setListScrollListener(ListScrollListener listScrollListener) {
        this.listScrollListener = listScrollListener;
    }

    public void setProductsFragmentListener(ProductsFragmentListener productsFragmentListener) {
        this.productsFragmentListener = productsFragmentListener;
    }

    public void setMultiInputListener(MultiInputListener multiInputListener) {
        this.multiInputListener = multiInputListener;
    }

    @Override
    public ImonggoDBHelper2 getHelper() {
        if(super.getHelper() == null)
            throw new ProductsFragmentException("dbHelper is null. Use "+this.getClass().getSimpleName()+".setHelper().");
        return super.getHelper();
    }

    protected List<Product> getPromotionalProducts() {
        promotionalProducts = new ArrayList<>();
        try {
            Log.e("Sales Promotion", "getPromotaionalProducts");

            Date now = DateTimeTools.getCurrentDateTimeUTC0();

            List<SalesPromotion> salesPromotions = getHelper().fetchObjectsList(SalesPromotion.class);
            for(SalesPromotion salesPromotion : salesPromotions) {
                Log.e("Sales Promotion", salesPromotion.getName());
                Log.e("Sales Promotion", "from="+salesPromotion.getFromDate()
                        +" - to="+salesPromotion.getToDate()
                        +" - now="+now
                        +" - status="+salesPromotion.getStatus()
                        +" - promotion_type_name="+salesPromotion.getPromotion_type_name());
            }

            Where<SalesPromotion, Integer> whereCondition = getHelper().fetchIntId(SalesPromotion.class).queryBuilder().where();
            whereCondition.lt("toDate", now);

            UpdateBuilder<SalesPromotion, Integer> updatePromotion = getHelper().fetchIntId(SalesPromotion.class).updateBuilder();
            updatePromotion.updateColumnValue("status", "D");
            updatePromotion.setWhere(whereCondition);

            Log.e(">>", updatePromotion.prepareStatementString());

            updatePromotion.update();

            SalesPromotion salesPromotion = getHelper().fetchObjects(SalesPromotion.class).queryBuilder().orderBy("id", true)//.query();
                    .where().le("fromDate", now)
                            .and().ge("toDate", now).and().eq("status", "A")
                            .and().eq("promotion_type_name", SalesPromotion.DISCOUNT)
                    .queryForFirst();
            if(salesPromotion != null) {
                Log.e("Sales Promotion["+salesPromotion.getId()+"]", salesPromotion.getName()+" || "+salesPromotion.getStatus()
                        + " || fromDate "+salesPromotion.getFromDate()+" -- toDate "+salesPromotion.getToDate() + " || "+now);
                List<Discount> discounts = getHelper().fetchForeignCollection(salesPromotion.getDiscounts_fc().closeableIterator());
                Log.e("Discounts", discounts.size()+"");
                for(Discount discount : discounts) {
                    Log.e("Discounts", "iterating---");
                    if(discount.getProduct() != null) {
                        promotionalProducts.add(discount.getProduct());
                        Log.e("Discounts", discount.getProduct().getName()+"--");
                    }
                    else {
                        Log.e("Discounts", "no product is tagged = "+discount.getProduct_id());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return promotionalProducts;
    }

    protected List<Product> getProducts() {
        Log.e(getClass().getSimpleName(), "getProducts");
        List<Product> products = new ArrayList<>();

        boolean includeSearchKey = !searchKey.trim().isEmpty();
        boolean includeCategory = (!category.toLowerCase().equals("all") && hasCategories);
        boolean hasProductFilter = filterProductsBy.size() > 0;
        if(isFinalize && !hasProductFilter)
            return products;
        try {
            Where<Product, Integer> whereProducts = getHelper().fetchIntId(Product.class).queryBuilder().where();
            whereProducts.isNull("status");
//            whereProducts.eq("status", "A     ");
            Log.e("includeSearchKey", includeSearchKey + "");
            Log.e("includeCategory", includeCategory+"");
            Log.e("hasProductFilter", hasProductFilter+"");

            if(includeSearchKey)
                whereProducts.and().like("searchKey", "%"+searchKey+"%");
            if(hasProductFilter) {
                List<Integer> ids = new ArrayList<>();
                for(Product product : filterProductsBy) {
                    ids.add(product.getId());
                    //Log.e("FILTER", product.getId() + "");
                }
                whereProducts.and().in("id", ids);
            }
            if(includeCategory) {
                QueryBuilder<ProductTag, Integer> productWithTag = getHelper().fetchIntId(ProductTag.class).queryBuilder();
                productWithTag.selectColumns("product_id").where().like("searchKey", "#"+category.toLowerCase()+"%");

                whereProducts.and().in("id", productWithTag);
            }

            String orderBy = "";
            if(promotionalProducts.size() > 0) {
                orderBy = "CASE id";
                int order = 0;
                for(Product product : promotionalProducts) {
                    orderBy += " WHEN "+product.getId()+" THEN "+order;
                    order++;
                }
                orderBy += " ELSE 1000000 END, ";
            }
            ProductSorting productSorting = getHelper().fetchForeignCollection(getAppSetting().getProductSortings().closeableIterator(), new ImonggoDBHelper2.Conditional<ProductSorting>() {
                @Override
                public boolean validate(ProductSorting obj) {
                    if(obj.is_default())
                        return true;
                    return false;
                }
            }, 0);
            orderBy += (productSorting == null ? "name" : productSorting.getColumn()) + " COLLATE NOCASE ASC";

            QueryBuilder<Product, Integer> resultProducts = getHelper().fetchIntId(Product.class).queryBuilder().orderByRaw(orderBy)
                    .limit(LIMIT).offset(offset);
            resultProducts.setWhere(whereProducts);

            Log.e(">>", resultProducts.prepareStatementString());

            products = resultProducts.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Log.e(getClass().getSimpleName(), "getProducts = "+products.size());

        return products;
    }

    protected AbsListView.OnScrollListener lvScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                if (listScrollListener != null)
                    listScrollListener.onScrollStopped();
            } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                if (listScrollListener != null)
                    listScrollListener.onScrolling();
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int lastItem = firstVisibleItem + visibleItemCount;
            if (lastItem == totalItemCount) {
                if (prevLast != lastItem) {
                    offset += LIMIT;
                    whenListEndReached(getProducts());
                    prevLast = lastItem;
                }
            }
        }
    };

    protected RecyclerView.OnScrollListener rvScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (listScrollListener != null)
                    listScrollListener.onScrollStopped();
            }
            else if(newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                if(listScrollListener != null)
                    listScrollListener.onScrolling();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int visibleItemCount = rvProducts.getChildCount();
            int totalItemCount = productRecyclerViewAdapter.getLinearLayoutManager().getItemCount();
            int firstVisibleItem = productRecyclerViewAdapter.getLinearLayoutManager().findFirstVisibleItemPosition();

            int lastItem = firstVisibleItem + visibleItemCount;

            if(lastItem == totalItemCount) {
                if(prevLast != lastItem) {
                    offset += LIMIT;
                    whenListEndReached(getProducts());
                    prevLast = lastItem;
                }
            }
        }
    };

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setListingType(ListingType listingType) {
        this.listingType = listingType;
    }

    public String messageCategory() {
        return category.toLowerCase().equals("All") ? "" : " in \""+category+"\" category";
    }

    public void setProductCategories(List<String> productCategories) {
        this.productCategories = productCategories;
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

    public void setMultipleInput(boolean multipleInput) {
        this.multipleInput = multipleInput;
    }

    public void setHasCategories(boolean hasCategories) {
        this.hasCategories = hasCategories;
    }

    public void setShowCategoryOnStart(boolean showCategoryOnStart) {
        this.showCategoryOnStart = showCategoryOnStart;
    }

    public void setLockCategory(boolean lockCategory) {
        this.lockCategory = lockCategory;
    }

    public void setFilterProductsBy(List<Product> filterProductsBy) {
        this.filterProductsBy = filterProductsBy;
    }

    public void setHasToolBar(boolean hasToolBar) {
        this.hasToolBar = hasToolBar;
    }

    public void setHasSubtotal(boolean hasSubtotal) {
        this.hasSubtotal = hasSubtotal;
    }

    public void setIsFinalize(boolean isFinalize) {
        this.isFinalize = isFinalize;
    }

    public void setReason(DocumentPurpose reason) {
        this.reason = reason;
    }

    public void setDisplayOnly(boolean displayOnly) {
        this.displayOnly = displayOnly;
    }

    public void setUseSalesProductAdapter(boolean useSalesProductAdapter) {
        this.useSalesProductAdapter = useSalesProductAdapter;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
        ProductsAdapterHelper.setSelectedBranch(branch);
    }

    public void setCustomerGroup(CustomerGroup customerGroup) {
        this.customerGroup = customerGroup;
        ProductsAdapterHelper.setSelectedCustomerGroup(customerGroup);
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        ProductsAdapterHelper.setSelectedCustomer(customer);
    }

    public void setHasPromotionalProducts(boolean hasPromotionalProducts) {
        this.hasPromotionalProducts = hasPromotionalProducts;
    }

    public void setReturnItems(boolean returnItems) {
        isReturnItems = returnItems;
    }

    public ConcessioModule getConcessioModule() {
        return concessioModule;
    }

    public void setConcessioModule(ConcessioModule concessioModule) {
        this.concessioModule = concessioModule;
    }
}
