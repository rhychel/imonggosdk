package net.nueca.concessioengine.fragments;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.fragments.interfaces.ListScrollListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.lists.ReceivedProductItemList;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 9/3/15.
 */
public abstract class BaseReceiveFragment extends ImonggoFragment {
    protected static final long LIMIT = 15l;
    protected long offset = 0l;
    protected int prevLast = -1;

    private String searchKey = "", category = "";
    protected boolean hasCategories = true;

    protected ListScrollListener listScrollListener;
    protected boolean useRecyclerView = true;

    protected Toolbar tbActionBar;
    protected SetupActionBar setupActionBar;

    protected RecyclerView rvProducts;
    protected ListView lvProducts;

    private Integer parentDocumentId;
    protected String deliveryReceiptNo;

    protected ArrayAdapter<String> productCategoriesAdapter;
    protected List<String> productCategories = new ArrayList<>();

    protected FloatingActionButton fabContinue;

    protected abstract void whenListEndReached(List<DocumentLine> documentLines);
    protected abstract void toggleNoItems(String msg, boolean show);
    protected abstract boolean shouldContinue();
    protected abstract void clearSelectedItems();
    public abstract ReceivedProductItemList getReceivedProductItemList();
    public abstract ReceivedProductItemList getDisplayProductItemList();

    public User getUser() throws SQLException {
        if(getSession() == null)
            return null;
        return getSession().getUser();
    }

    public String getDeliveryReceiptNo() {
        return deliveryReceiptNo;
    }

    public void setDeliveryReceiptNo(String deliveryReceiptNo) {
        clearSelectedItems();
        this.deliveryReceiptNo = deliveryReceiptNo;
    }

    public void setUseRecyclerView(boolean useRecyclerView) {
        this.useRecyclerView = useRecyclerView;
    }

    public Integer getParentDocumentId() {
        return parentDocumentId;
    }

    public void setParentDocumentId(Integer parentDocumentId) {
        this.parentDocumentId = parentDocumentId;
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public String messageCategory() {
        return category.toLowerCase().equals("All") ? "" : " in \""+category+"\" category";
    }

    public void setProductCategories(List<String> productCategories) {
        this.productCategories = productCategories;
    }

    protected List<DocumentLine> getReceivedProducts() {
        List<DocumentLine> documentLines = new ArrayList<>();

        boolean includeSearchKey = !searchKey.equals("");
        boolean includeCategory = (!category.toLowerCase().equals("all") && hasCategories);
        try {
            Where<Product, Integer> whereProducts = getHelper().getProducts().queryBuilder().where();
            whereProducts.isNull("status");
            if(includeSearchKey) {
                whereProducts.and().like("searchKey", "%" + searchKey + "%");
            }
            if(includeCategory) {
                QueryBuilder<ProductTag, Integer> productWithTag = getHelper().getProductTags().queryBuilder();
                productWithTag.selectColumns("product_id").where().like("searchKey", "#"+category.toLowerCase()+"%");

                whereProducts.and().in("id", productWithTag);
            }

            QueryBuilder<Product, Integer> resultProducts = getHelper().getProducts().queryBuilder()
                    .orderByRaw("name " + "COLLATE NOCASE ASC").limit(LIMIT).offset(offset);
            resultProducts.setWhere(whereProducts);

            for(Product product : resultProducts.query()) {
                documentLines.add(
                        new DocumentLine.Builder()
                                .line_no((int) offset + documentLines.size() + 1)
                                .useProductDetails(product)
                                .build()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return documentLines;
    }

    protected List<DocumentLine> getDocumentLines() {
        if(parentDocumentId == null)
            return getReceivedProducts();

        List<DocumentLine> documentLines = new ArrayList<>();

        boolean includeSearchKey = !searchKey.equals("");
        boolean includeCategory = (!category.toLowerCase().equals("all") && hasCategories);
        Log.e("includeCategory", includeCategory + "");
        try {
            Where<DocumentLine, Integer> whereDocumentLines = getHelper().getDocumentLines().queryBuilder().where();

            Where<Product, Integer> whereProducts = getHelper().getProducts().queryBuilder().where();
            whereProducts.isNull("status");
            if(includeSearchKey) {
                whereProducts.and().like("searchKey", "%" + searchKey + "%");
            }
            if(includeCategory) {
                QueryBuilder<ProductTag, Integer> productWithTag = getHelper().getProductTags().queryBuilder();
                productWithTag.selectColumns("product_id").where().like("searchKey", "#"+category.toLowerCase()+"%");

                whereProducts.and().in("id", productWithTag);
            }

            QueryBuilder<Product, Integer> resultProducts = getHelper().getProducts().queryBuilder()
                    /*.orderByRaw("name " + "COLLATE NOCASE ASC").limit(LIMIT).offset(offset)*/;
            resultProducts.setWhere(whereProducts);

            whereDocumentLines.in("product_id", resultProducts.selectColumns("id"));

            QueryBuilder<DocumentLine, Integer> resultDocumentLines = getHelper().getDocumentLines().queryBuilder()
                    /*.orderBy("line_no",false)*/.limit(LIMIT).offset(offset);
            resultDocumentLines.setWhere(whereDocumentLines);

            QueryBuilder<Document, Integer> documentQb = getHelper().getDocuments().queryBuilder();
            documentQb.where().eq("reference", deliveryReceiptNo);
            resultDocumentLines.join(documentQb);

            Log.e("QUERY", resultDocumentLines.prepareStatementString());

            documentLines = resultDocumentLines.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return documentLines;
    }

    protected AbsListView.OnScrollListener lvScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                if (listScrollListener != null)
                    listScrollListener.onScrollStopped();

                if(fabContinue != null) {
                    ViewCompat.animate(fabContinue).translationY(0.0f).setDuration(400)
                            .setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }

            } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                if (listScrollListener != null)
                    listScrollListener.onScrolling();

                if(fabContinue != null) {
                    ViewCompat.animate(fabContinue).translationY(1000.0f).setDuration(400)
                            .setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int lastItem = firstVisibleItem + visibleItemCount;
            if (lastItem == totalItemCount) {
                if (prevLast != lastItem) {
                    offset += LIMIT;
                    whenListEndReached(getDocumentLines());
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

                if(fabContinue != null) {
                    ViewCompat.animate(fabContinue).translationY(0.0f).setDuration(400)
                            .setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }
            }
            else if(newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                if(listScrollListener != null)
                    listScrollListener.onScrolling();

                if(fabContinue != null) {
                    ViewCompat.animate(fabContinue).translationY(1000.0f).setDuration(400)
                            .setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int visibleItemCount = rvProducts.getChildCount();
            int totalItemCount = ((BaseRecyclerAdapter)rvProducts.getAdapter())
                    .getLinearLayoutManager().getItemCount();
            int firstVisibleItem = ((BaseRecyclerAdapter)rvProducts.getAdapter())
                    .getLinearLayoutManager().findFirstVisibleItemPosition();

            int lastItem = firstVisibleItem + visibleItemCount;

            if(lastItem == totalItemCount) {
                if(prevLast != lastItem) {
                    offset += LIMIT;
                    whenListEndReached(getDocumentLines());
                    prevLast = lastItem;
                }
            }
        }
    };
}