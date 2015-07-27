package net.nueca.concessioengine.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

    private static final long LIMIT = 100l;
    protected long offset = 0l;
    private int prevLast = -1;
    private String searchKey = "", category = "";
    private List<Product> filterProductsBy = new ArrayList<>();

    protected ArrayAdapter<String> productCategoriesAdapter;
    protected List<String> productCategories = new ArrayList<>();

    public interface SetupActionBar {
        void setupActionBar(Toolbar toolbar);
    }

    protected SetupActionBar setupActionBar;
    protected LinearLayoutManager linearLayoutManager;
    protected RecyclerView rvProducts;
    protected ListView lvProducts;
    protected Toolbar tbActionBar;

    protected SimpleProductRecyclerViewAdapter simpleProductRecyclerViewAdapter;
    protected SimpleProductListAdapter simpleProductListAdapter;

    protected abstract void showQuantityDialog(int position, Product product, SelectedProductItem selectedProductItem);
    protected abstract void showProductDetails(Product product);

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

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setProductCategories(List<String> productCategories) {
        this.productCategories = productCategories;
    }

}
