package net.nueca.concessioengine.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.adapters.SimpleProductListAdapter;
import net.nueca.concessioengine.adapters.SimpleProductRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.tools.DividerItemDecoration;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.exceptions.ProductsFragmentException;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 7/15/15.
 * imonggosdk (c)2015
 */
public abstract class BaseProductsFragment extends ImonggoFragment {

    protected static final long LIMIT = 100l;
    protected long offset = 0l;
    private int prevLast = -1;
    private String searchKey = "", category = "";
    private List<Product> filterProductsBy = new ArrayList<>();

    protected ArrayAdapter<String> productCategoriesAdapter;
    protected List<String> productCategories = new ArrayList<>();

    public interface SetupActionBar {
        void setupActionBar(Toolbar toolbar);
    }

    public interface ListScrollListener {
        void onScrolling();
        void onScrollStopped();
    }

    public interface ProductsFragmentListener {
        void whenItemsSelectedUpdated();
    }

    protected ProductsFragmentListener productsFragmentListener;
    protected ListScrollListener listScrollListener;
    protected SetupActionBar setupActionBar;
    protected LinearLayoutManager linearLayoutManager;
    protected RecyclerView rvProducts;
    protected ListView lvProducts;
    protected Toolbar tbActionBar;

    protected SimpleProductRecyclerViewAdapter simpleProductRecyclerViewAdapter;
    protected SimpleProductListAdapter simpleProductListAdapter;

    protected abstract void showQuantityDialog(int position, Product product, SelectedProductItem selectedProductItem);
    protected abstract void showProductDetails(Product product);
    protected abstract void whenListEndReached(List<Product> productList);

    protected void initializeRecyclerView(RecyclerView rvProducts) {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        rvProducts.setLayoutManager(linearLayoutManager);
        rvProducts.setHasFixedSize(true);
        rvProducts.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
    }

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

    @Override
    public ImonggoDBHelper getHelper() {
        if(super.getHelper() == null)
            throw new ProductsFragmentException("dbHelper is null. Use "+this.getClass().getSimpleName()+".setHelper().");
        return super.getHelper();
    }

    protected List<Product> getProducts() {
        List<Product> products = new ArrayList<>();

        boolean includeSearchKey = !searchKey.equals("");
        boolean includeCategory = !category.toLowerCase().equals("all");

        try {
            Where<Product, Integer> whereProducts = getHelper().getProducts().queryBuilder().where();
            whereProducts.isNull("status");
            if(includeSearchKey)
                whereProducts.and().like("searchKey", "%"+searchKey+"%");
            if(filterProductsBy.size() > 0)
                whereProducts.and().in("id", filterProductsBy);
            if(includeCategory) {
                QueryBuilder<ProductTag, Integer> productWithTag = getHelper().getProductTags().queryBuilder();
                productWithTag.selectColumns("product_id").where().like("searchKey", "#"+category.toLowerCase()+"%");

                whereProducts.and().in("id", productWithTag);
            }

            QueryBuilder<Product, Integer> resultProducts = getHelper().getProducts().queryBuilder().orderByRaw("name COLLATE NOCASE ASC").limit(LIMIT).offset(offset);
            resultProducts.setWhere(whereProducts);

            products = resultProducts.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
            int totalItemCount = linearLayoutManager.getItemCount();
            int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

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

    public String messageCategory() {
        return category.toLowerCase().equals("All") ? "" : " in \""+category+"\" category";
    }

    public void setProductCategories(List<String> productCategories) {
        this.productCategories = productCategories;
    }

}
