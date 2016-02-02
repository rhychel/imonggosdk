package net.nueca.imonggosdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.SalesPushSettings;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.Settings;
import net.nueca.imonggosdk.objects.TaxRate;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.accountsettings.Cutoff;
import net.nueca.imonggosdk.objects.accountsettings.DebugMode;
import net.nueca.imonggosdk.objects.accountsettings.Sequence;
import net.nueca.imonggosdk.objects.accountsettings.Manual;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.objects.accountsettings.ProductListing;
import net.nueca.imonggosdk.objects.accountsettings.ProductSorting;
import net.nueca.imonggosdk.objects.accountsettings.QuantityInput;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.associatives.CustomerCustomerGroupAssoc;
import net.nueca.imonggosdk.objects.associatives.ProductSalesPromotionAssoc;
import net.nueca.imonggosdk.objects.associatives.ProductTaxRateAssoc;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.branchentities.BranchProduct;
import net.nueca.imonggosdk.objects.branchentities.BranchUnit;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.document.DocumentType;
import net.nueca.imonggosdk.objects.invoice.Discount;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.objects.invoice.InvoiceTaxRate;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.objects.routeplan.RoutePlan;
import net.nueca.imonggosdk.objects.routeplan.RoutePlanDetail;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by rhymart on 11/16/15.
 */
public class ImonggoDBHelper2 extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "imonggosdk2.db";
    private static final int DATABASE_VERSION = 59;

    private static final Class<?> tables[] = {
            Branch.class, BranchTag.class, Customer.class,
            Inventory.class, Product.class, ProductTag.class, Session.class,
            Discount.class, TaxRate.class, TaxSetting.class, Unit.class, User.class,
            BranchProduct.class, BranchUnit.class, DocumentType.class, DocumentPurpose.class,
            BranchUserAssoc.class, ProductTaxRateAssoc.class,
            LastUpdatedAt.class, OfflineData.class, Document.class, DocumentLine.class,
            DailySales.class, Settings.class, Order.class, OrderLine.class,
            Invoice.class, InvoiceLine.class, InvoicePayment.class, InvoiceTaxRate.class,
            Extras.class, CustomerCategory.class, CustomerGroup.class, InvoicePurpose.class,
            PaymentTerms.class, PaymentType.class, SalesPromotion.class, net.nueca.imonggosdk.objects.salespromotion.Discount.class, Price.class,
            PriceList.class, RoutePlan.class, RoutePlanDetail.class, CustomerCustomerGroupAssoc.class,
            ProductSalesPromotionAssoc.class, ModuleSetting.class, Sequence.class, DebugMode.class, ProductSorting.class,
            Cutoff.class, ProductListing.class, QuantityInput.class, Manual.class, SalesPushSettings.class};

    public ImonggoDBHelper2(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            for(Class<?> table : tables) {
                Log.e("Table", table.getSimpleName());
                TableUtils.createTable(connectionSource, table);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            for(Class<?> table : tables) {
                TableUtils.dropTable(connectionSource, table, true);
            }

            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public <D> Dao<D, Integer> fetchIntId(Class<D> objClass) throws SQLException {
        return getDao(objClass);
    }

    public <D> Dao<D, String> fetchStrId(Class<D> objClass) throws SQLException {
        return getDao(objClass);
    }

    public <D> Dao<D, ?> fetchObjects(Class<D> objClass) throws SQLException {
        return getDao(objClass);
    }

    public <D> Dao<D, Integer> fetchObjectsInt(Class<D> objClass) throws SQLException {
        return getDao(objClass);
    }

    public <D> Dao<D, String> fetchObjectsStr(Class<D> objClass) throws SQLException {
        return getDao(objClass);
    }

    public <D> List<D> fetchObjectsList(Class<D> objClass) throws SQLException {
        return getDao(objClass).queryForAll();
    }

    public <D> int insert(Class<D> objClass, D obj) throws SQLException {
        return getDao(objClass).create(obj);
    }

    public <D> int update(Class<D> objClass, D obj) throws SQLException {
        return getDao(objClass).update(obj);
    }

    public <D> int delete(Class<D> objClass, D obj) throws SQLException {
        return getDao(objClass).delete(obj);
    }

    public <D> int deleteAll(Class<D> objClass) throws SQLException {
        return getDao(objClass).deleteBuilder().delete();
    }

    public void deleteAllDatabaseValues() throws SQLException {
        for(Class<?> table : tables) {
            deleteAll(table);
        }
    }

    public <D> void dropTable(Class<D> objClass) throws SQLException {
        TableUtils.dropTable(getConnectionSource(), objClass, true);
    }

    public <D extends BaseTable> void batchCreateOrUpdateBT(Class<D> objClass, final BatchList batchList, final DatabaseOperation databaseOperations) {
        try {
            Dao<D, ?> daoBatchLists = getDao(objClass);
            daoBatchLists.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for(D item : ((BatchList<D>)batchList))
                        item.dbOperation(ImonggoDBHelper2.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <D extends BaseTable2> void batchCreateOrUpdateBT2(Class<D> objClass, final BatchList batchList, final DatabaseOperation databaseOperations) {
        try {
            Dao<D, ?> daoBatchLists = getDao(objClass);
            daoBatchLists.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for(D item : ((BatchList<D>)batchList))
                        item.dbOperation(ImonggoDBHelper2.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <D extends DBTable> void batchCreateOrUpdate(Class<D> objClass, final BatchList batchList, final DatabaseOperation databaseOperations) {
        try {
            Dao<D, ?> daoBatchLists = getDao(objClass);
            daoBatchLists.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for(D item : ((BatchList<D>)batchList))
                        item.dbOperation(ImonggoDBHelper2.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve a List<D> the foreign collection for a specific table
     * @param <D>
     * @return
     * @throws SQLException
     */
    public <D> List<D> fetchForeignCollection(CloseableIterator<D> iterator) throws SQLException {
        return fetchForeignCollection(iterator, null);
    }

    public <D> List<D> fetchForeignCollection(CloseableIterator<D> iterator, Conditional<D> conditional) throws SQLException {
        List<D> theList = new ArrayList<>();
        try {
            while (iterator.hasNext()) {
                D obj = iterator.next();
                if(conditional != null)
                    if(!conditional.validate(obj))
                        continue;
                theList.add(obj);
            }
        } finally {
            iterator.close();
        }

        return theList;
    }

    public interface Conditional<D> {
        boolean validate(D obj);
    }

}
