package net.nueca.imonggosdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.Customer;
import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.TaxRate;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.associatives.ProductTaxRateAssoc;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.document.DocumentType;
import net.nueca.imonggosdk.objects.document.ExtendedAttributes;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Created by rhymart on 5/12/15.
 * ImonggoLibrary (c)2015
 */
public class ImonggoDBHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "imonggosdk.db";

    private static final int DATABASE_VERSION = 20;

    private Dao<Branch, Integer> branches = null;
    private Dao<BranchPrice, Integer> branchPrices = null;
    private Dao<BranchTag, Integer> branchTags = null;
    private Dao<Customer, Integer> customers = null;
    private Dao<Inventory, Integer> inventories = null;
    private Dao<Product, Integer> products = null;
    private Dao<ProductTag, Integer> productTags = null;
    private Dao<Session, Integer> sessions = null;
    private Dao<TaxRate, Integer> taxRates = null;
    private Dao<TaxSetting, Integer> taxSettings = null;
    private Dao<Unit, Integer> units = null;
    private Dao<User, Integer> users = null;

    private Dao<DocumentType, Integer> documentTypes = null;
    private Dao<DocumentPurpose, Integer> documentPurposes = null;

    private Dao<BranchUserAssoc, Integer> branchUserAssocs = null;
    private Dao<ProductTaxRateAssoc, Integer> productTaxRateAssocs = null;

    private Dao<LastUpdatedAt, Integer> lastUpdatedAts = null;

    /**
     * added by gama
     **/
    private Dao<OfflineData, Integer> offlineData = null;

    private Dao<Document, Integer> documents = null;
    private Dao<DocumentLine, Integer> documentLines = null;
    private Dao<ExtendedAttributes, Integer> extendedAttributes = null;
    /**           end           **/
    /**- - - - - - - - - - - - -**/
    /**
     * added by jn
     **/
    private Dao<DailySales, Integer> dailySales = null;

    /**
     * end
     **/

    public ImonggoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Branch.class);
            TableUtils.createTable(connectionSource, BranchPrice.class);
            TableUtils.createTable(connectionSource, BranchTag.class);
            TableUtils.createTable(connectionSource, Customer.class);
            TableUtils.createTable(connectionSource, Inventory.class);
            TableUtils.createTable(connectionSource, Product.class);
            TableUtils.createTable(connectionSource, ProductTag.class);
            TableUtils.createTable(connectionSource, Session.class);
            TableUtils.createTable(connectionSource, TaxRate.class);
            TableUtils.createTable(connectionSource, TaxSetting.class);
            TableUtils.createTable(connectionSource, Unit.class);
            TableUtils.createTable(connectionSource, User.class);

            TableUtils.createTable(connectionSource, DocumentType.class);
            TableUtils.createTable(connectionSource, DocumentPurpose.class);

            TableUtils.createTable(connectionSource, BranchUserAssoc.class);
            TableUtils.createTable(connectionSource, ProductTaxRateAssoc.class);

            TableUtils.createTable(connectionSource, LastUpdatedAt.class);

            TableUtils.createTable(connectionSource, OfflineData.class);

            TableUtils.createTable(connectionSource, Document.class);
            TableUtils.createTable(connectionSource, DocumentLine.class);
            TableUtils.createTable(connectionSource, ExtendedAttributes.class);
            TableUtils.createTable(connectionSource, DailySales.class);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Branch.class, true);
            TableUtils.dropTable(connectionSource, BranchPrice.class, true);
            TableUtils.dropTable(connectionSource, BranchTag.class, true);
            TableUtils.dropTable(connectionSource, Customer.class, true);
            TableUtils.dropTable(connectionSource, Inventory.class, true);
            TableUtils.dropTable(connectionSource, Product.class, true);
            TableUtils.dropTable(connectionSource, ProductTag.class, true);
            TableUtils.dropTable(connectionSource, Session.class, true);
            TableUtils.dropTable(connectionSource, TaxRate.class, true);
            TableUtils.dropTable(connectionSource, TaxSetting.class, true);
            TableUtils.dropTable(connectionSource, Unit.class, true);
            TableUtils.dropTable(connectionSource, User.class, true);

            TableUtils.dropTable(connectionSource, DocumentType.class, true);
            TableUtils.dropTable(connectionSource, DocumentPurpose.class, true);

            TableUtils.dropTable(connectionSource, BranchUserAssoc.class, true);
            TableUtils.dropTable(connectionSource, ProductTaxRateAssoc.class, true);

            TableUtils.dropTable(connectionSource, LastUpdatedAt.class, true);

            TableUtils.dropTable(connectionSource, OfflineData.class, true);

            TableUtils.dropTable(connectionSource, Document.class, true);
            TableUtils.dropTable(connectionSource, DocumentLine.class, true);
            TableUtils.dropTable(connectionSource, ExtendedAttributes.class, true);
            TableUtils.dropTable(connectionSource, DailySales.class, true);

            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * FETCH query builders
     */
    public Dao<Branch, Integer> getBranches() throws SQLException {
        if (branches == null)
            branches = getDao(Branch.class);
        return branches;
    }

    public Dao<BranchPrice, Integer> getBranchPrices() throws SQLException {
        if (branchPrices == null)
            branchPrices = getDao(BranchPrice.class);
        return branchPrices;
    }

    public Dao<BranchTag, Integer> getBranchTags() throws SQLException {
        if (branchTags == null)
            branchTags = getDao(BranchTag.class);
        return branchTags;
    }

    public Dao<Customer, Integer> getCustomers() throws SQLException {
        if (customers == null)
            customers = getDao(Customer.class);
        return customers;
    }

    public Dao<Inventory, Integer> getInventories() throws SQLException {
        if (inventories == null)
            inventories = getDao(Inventory.class);
        return inventories;
    }

    public Dao<Product, Integer> getProducts() throws SQLException {
        if (products == null)
            products = getDao(Product.class);
        return products;
    }

    public Dao<ProductTag, Integer> getProductTags() throws SQLException {
        if (productTags == null)
            productTags = getDao(ProductTag.class);
        return productTags;
    }

    public Dao<Session, Integer> getSessions() throws SQLException {
        if (sessions == null)
            sessions = getDao(Session.class);
        return sessions;
    }

    public Dao<TaxRate, Integer> getTaxRates() throws SQLException {
        if (taxRates == null)
            taxRates = getDao(TaxRate.class);
        return taxRates;
    }

    public Dao<TaxSetting, Integer> getTaxSettings() throws SQLException {
        if (taxSettings == null)
            taxSettings = getDao(TaxSetting.class);
        return taxSettings;
    }

    public Dao<Unit, Integer> getUnits() throws SQLException {
        if (units == null)
            units = getDao(Unit.class);
        return units;
    }

    public Dao<User, Integer> getUsers() throws SQLException {
        if (users == null)
            users = getDao(User.class);
        return users;
    }

    public Dao<DocumentType, Integer> getDocumentTypes() throws SQLException {
        if (documentTypes == null)
            documentTypes = getDao(DocumentType.class);
        return documentTypes;
    }

    public Dao<DocumentPurpose, Integer> getDocumentPurposes() throws SQLException {
        if (documentPurposes == null)
            documentPurposes = getDao(DocumentPurpose.class);
        return documentPurposes;
    }

    public Dao<BranchUserAssoc, Integer> getBranchUserAssocs() throws SQLException {
        if (branchUserAssocs == null)
            branchUserAssocs = getDao(BranchUserAssoc.class);
        return branchUserAssocs;
    }

    public Dao<ProductTaxRateAssoc, Integer> getProductTaxRateAssocs() throws SQLException {
        if (productTaxRateAssocs == null)
            productTaxRateAssocs = getDao(ProductTaxRateAssoc.class);
        return productTaxRateAssocs;
    }

    public Dao<LastUpdatedAt, Integer> getLastUpdatedAts() throws SQLException {
        if (lastUpdatedAts == null)
            lastUpdatedAts = getDao(LastUpdatedAt.class);
        return lastUpdatedAts;
    }

    public Dao<OfflineData, Integer> getOfflineData() throws SQLException {
        if (offlineData == null)
            offlineData = getDao(OfflineData.class);
        return offlineData;
    }

    public Dao<Document, Integer> getDocuments() throws SQLException {
        if (documents == null)
            documents = getDao(Document.class);
        return documents;
    }

    public Dao<DocumentLine, Integer> getDocumentLines() throws SQLException {
        if (documentLines == null)
            documentLines = getDao(DocumentLine.class);
        return documentLines;
    }

    public Dao<ExtendedAttributes, Integer> getExtendedAttributes() throws SQLException {
        if (extendedAttributes == null)
            extendedAttributes = getDao(ExtendedAttributes.class);
        return extendedAttributes;
    }

    public Dao<DailySales, Integer> getDailySales() throws SQLException {
        if (dailySales == null) {
            dailySales = getDao(DailySales.class);
        }
        return dailySales;
    }

    /**
     * DROP Table
     */
    public void dropTable(Table table) throws SQLException {
        switch (table) {
            case BRANCHES: {
                TableUtils.dropTable(getConnectionSource(), Branch.class, true);
            }
            break;
            case BRANCH_PRICES: {
                TableUtils.dropTable(getConnectionSource(), BranchPrice.class, true);
            }
            break;
            case BRANCH_TAGS: {
                TableUtils.dropTable(getConnectionSource(), BranchTag.class, true);
            }
            break;
            case CUSTOMERS: {
                TableUtils.dropTable(getConnectionSource(), Customer.class, true);
            }
            break;
            case INVENTORIES: {
                TableUtils.dropTable(getConnectionSource(), Inventory.class, true);
            }
            break;
            case PRODUCTS: {
                TableUtils.dropTable(getConnectionSource(), Product.class, true);
            }
            break;
            case PRODUCT_TAGS: {
                TableUtils.dropTable(getConnectionSource(), ProductTag.class, true);
            }
            break;
            case SESSIONS: {
                TableUtils.dropTable(getConnectionSource(), Session.class, true);
            }
            break;
            case TAX_RATES: {
                TableUtils.dropTable(getConnectionSource(), TaxRate.class, true);
            }
            break;
            case TAX_SETTINGS: {
                TableUtils.dropTable(getConnectionSource(), TaxSetting.class, true);
            }
            break;
            case UNITS: {
                TableUtils.dropTable(getConnectionSource(), Unit.class, true);
            }
            break;
            case USERS: {
                TableUtils.dropTable(getConnectionSource(), User.class, true);
            }
            break;
            case LAST_UPDATED_AT: {
                TableUtils.dropTable(getConnectionSource(), LastUpdatedAt.class, true);
            }
            break;
            case DOCUMENT_TYPES:
                TableUtils.dropTable(getConnectionSource(), DocumentType.class, true);
                break;
            case DOCUMENT_PURPOSES:
                TableUtils.dropTable(getConnectionSource(), DocumentPurpose.class, true);
                break;

            case OFFLINEDATA: {
                TableUtils.dropTable(getConnectionSource(), OfflineData.class, true);
            }
            break;

            case DOCUMENTS: {
                TableUtils.dropTable(getConnectionSource(), Document.class, true);
            }
            break;
            case DOCUMENT_LINES: {
                TableUtils.dropTable(getConnectionSource(), DocumentLine.class, true);
            }
            break;
            case EXTENDED_ATTRIBUTES: {
                TableUtils.dropTable(getConnectionSource(), ExtendedAttributes.class, true);
            }
            break;

            // ASSOCIATIVES
            case BRANCH_USERS: {
                TableUtils.dropTable(getConnectionSource(), BranchUserAssoc.class, true);
            }
            break;
            case PRODUCT_TAX_RATES: {
                TableUtils.dropTable(getConnectionSource(), ProductTaxRateAssoc.class, true);
            }
            break;

            case DAILY_SALES: {
                TableUtils.dropTable(getConnectionSource(), DailySales.class, true);
            }
            break;
        }
    }

    /**
     * CREATE, UPDATE, DELETE Operations
     */
    public void dbOperations(Object obj, Table table, DatabaseOperation databaseOperations) throws SQLException {
        switch (databaseOperations) {
            case INSERT: {
                insert(obj, table);
            }
            break;
            case DELETE: {
                delete(obj, table);
            }
            break;
            case DELETE_ALL: {
                deleteAll(table);
            }
            break;
            case UPDATE: {
                update(obj, table);
            }
            break;
            case UPDATE_WITH_PARAMETER: {

            }
            break;
        }
    }

    // INSERT
    private void insert(Object object, Table tables) throws SQLException {
        switch (tables) {
            case BRANCHES:
                getBranches().create((Branch) object);
                break;
            case BRANCH_PRICES:
                getBranchPrices().create((BranchPrice) object);
                break;
            case BRANCH_TAGS:
                getBranchTags().create((BranchTag) object);
                break;
            case CUSTOMERS:
                getCustomers().create((Customer) object);
                break;
            case INVENTORIES:
                getInventories().create((Inventory) object);
                break;
            case PRODUCTS:
                getProducts().create((Product) object);
                break;
            case PRODUCT_TAGS:
                getProductTags().create((ProductTag) object);
                break;
            case SESSIONS:
                getSessions().create((Session) object);
                break;
            case TAX_RATES:
                getTaxRates().create((TaxRate) object);
                break;
            case TAX_SETTINGS:
                getTaxSettings().create((TaxSetting) object);
                break;
            case UNITS:
                getUnits().create((Unit) object);
                break;
            case USERS:
                getUsers().create((User) object);
                break;
            case LAST_UPDATED_AT:
                getLastUpdatedAts().create((LastUpdatedAt) object);
                break;
            case DOCUMENT_TYPES:
                getDocumentTypes().create((DocumentType) object);
                break;
            case DOCUMENT_PURPOSES:
                getDocumentPurposes().create((DocumentPurpose) object);
                break;

            case OFFLINEDATA:
                getOfflineData().create((OfflineData) object);
                break;

            case DOCUMENTS:
                getDocuments().create((Document) object);
                break;
            case DOCUMENT_LINES:
                getDocumentLines().create((DocumentLine) object);
                break;
            case EXTENDED_ATTRIBUTES:
                getExtendedAttributes().create((ExtendedAttributes) object);
                break;

            // ASSOCIATIVES
            case BRANCH_USERS:
                getBranchUserAssocs().create((BranchUserAssoc) object);
                break;
            case PRODUCT_TAX_RATES:
                getProductTaxRateAssocs().create((ProductTaxRateAssoc) object);
                break;

            case DAILY_SALES:
                getDailySales().create((DailySales) object);
                break;
        }
    }

    // DELETE
    private void delete(Object object, Table tables) throws SQLException {
        switch (tables) {
            case BRANCHES:
                getBranches().delete((Branch) object);
                break;
            case BRANCH_PRICES:
                getBranchPrices().delete((BranchPrice) object);
                break;
            case BRANCH_TAGS:
                getBranchTags().delete((BranchTag) object);
                break;
            case CUSTOMERS:
                getCustomers().delete((Customer) object);
                break;
            case INVENTORIES:
                getInventories().delete((Inventory) object);
                break;
            case PRODUCTS:
                getProducts().delete((Product) object);
                break;
            case PRODUCT_TAGS:
                getProductTags().delete((ProductTag) object);
                break;
            case SESSIONS:
                getSessions().delete((Session) object);
                break;
            case TAX_RATES:
                getTaxRates().delete((TaxRate) object);
                break;
            case TAX_SETTINGS:
                getTaxSettings().delete((TaxSetting) object);
                break;
            case UNITS:
                getUnits().delete((Unit) object);
                break;
            case USERS:
                getUsers().delete((User) object);
                break;
            case LAST_UPDATED_AT:
                getLastUpdatedAts().delete((LastUpdatedAt) object);
                break;
            case DOCUMENT_TYPES:
                getDocumentTypes().delete((DocumentType) object);
                break;
            case DOCUMENT_PURPOSES:
                getDocumentPurposes().delete((DocumentPurpose) object);
                break;

            case OFFLINEDATA:
                getOfflineData().delete((OfflineData) object);
                break;

            case DOCUMENTS:
                getDocuments().delete((Document) object);
                break;
            case DOCUMENT_LINES:
                getDocumentLines().delete((DocumentLine) object);
                break;
            case EXTENDED_ATTRIBUTES:
                getExtendedAttributes().delete((ExtendedAttributes) object);
                break;

            // ASSOCIATIVES
            case BRANCH_USERS:
                getBranchUserAssocs().delete((BranchUserAssoc) object);
                break;
            case PRODUCT_TAX_RATES:
                getProductTaxRateAssocs().delete((ProductTaxRateAssoc) object);
                break;

            case DAILY_SALES:
                getDailySales().delete((DailySales) object);
        }
    }

    // INSERT
    private void deleteAll(Table tables) throws SQLException {
        switch (tables) {
            case BRANCHES:
                getBranches().deleteBuilder().delete();
                break;
            case BRANCH_PRICES:
                getBranchPrices().deleteBuilder().delete();
                break;
            case BRANCH_TAGS:
                getBranchTags().deleteBuilder().delete();
                break;
            case CUSTOMERS:
                getCustomers().deleteBuilder().delete();
                break;
            case INVENTORIES:
                getInventories().deleteBuilder().delete();
                break;
            case PRODUCTS:
                getProducts().deleteBuilder().delete();
                break;
            case PRODUCT_TAGS:
                getProductTags().deleteBuilder().delete();
                break;
            case SESSIONS:
                getSessions().deleteBuilder().delete();
                break;
            case TAX_RATES:
                getTaxRates().deleteBuilder().delete();
                break;
            case TAX_SETTINGS:
                getTaxSettings().deleteBuilder().delete();
                break;
            case UNITS:
                getUnits().deleteBuilder().delete();
                break;
            case USERS:
                getUsers().deleteBuilder().delete();
                break;
            case LAST_UPDATED_AT:
                getLastUpdatedAts().deleteBuilder().delete();
                break;
            case DOCUMENT_TYPES:
                getDocumentTypes().deleteBuilder().delete();
                break;
            case DOCUMENT_PURPOSES:
                getDocumentPurposes().deleteBuilder().delete();
                break;

            case OFFLINEDATA:
                getOfflineData().deleteBuilder().delete();
                break;

            case DOCUMENTS:
                getDocuments().deleteBuilder().delete();
                break;
            case DOCUMENT_LINES:
                getDocumentLines().deleteBuilder().delete();
                break;
            case EXTENDED_ATTRIBUTES:
                getExtendedAttributes().deleteBuilder().delete();
                break;

            // ASSOCIATIVES
            case BRANCH_USERS:
                getBranchUserAssocs().deleteBuilder().delete();
                break;
            case PRODUCT_TAX_RATES:
                getProductTaxRateAssocs().deleteBuilder().delete();
                break;

            case DAILY_SALES:
                getDailySales().deleteBuilder().delete();
                break;
        }
    }

    // UPDATE
    private void update(Object object, Table tables) throws SQLException {
        switch (tables) {
            case BRANCHES:
                getBranches().update((Branch) object);
                break;
            case BRANCH_PRICES:
                getBranchPrices().update((BranchPrice) object);
                break;
            case BRANCH_TAGS:
                getBranchTags().update((BranchTag) object);
                break;
            case CUSTOMERS:
                getCustomers().update((Customer) object);
                break;
            case INVENTORIES:
                getInventories().update((Inventory) object);
                break;
            case PRODUCTS:
                getProducts().update((Product) object);
                break;
            case PRODUCT_TAGS:
                getProductTags().update((ProductTag) object);
                break;
            case SESSIONS:
                getSessions().update((Session) object);
                break;
            case TAX_RATES:
                getTaxRates().update((TaxRate) object);
                break;
            case TAX_SETTINGS:
                getTaxSettings().update((TaxSetting) object);
                break;
            case UNITS:
                getUnits().update((Unit) object);
                break;
            case USERS:
                getUsers().update((User) object);
                break;
            case LAST_UPDATED_AT:
                getLastUpdatedAts().update((LastUpdatedAt) object);
                break;
            case DOCUMENT_TYPES:
                getDocumentTypes().update((DocumentType) object);
                break;
            case DOCUMENT_PURPOSES:
                getDocumentPurposes().update((DocumentPurpose) object);
                break;

            case OFFLINEDATA:
                getOfflineData().update((OfflineData) object);
                break;

            case DOCUMENTS:
                getDocuments().update((Document) object);
                break;
            case DOCUMENT_LINES:
                getDocumentLines().update((DocumentLine) object);
                break;
            case EXTENDED_ATTRIBUTES:
                getExtendedAttributes().update((ExtendedAttributes) object);
                break;

            // ASSOCIATIVES
            case BRANCH_USERS:
                getBranchUserAssocs().update((BranchUserAssoc) object);
                break;
            case PRODUCT_TAX_RATES:
                getProductTaxRateAssocs().update((ProductTaxRateAssoc) object);
                break;

            case DAILY_SALES:
                getDailySales().update((DailySales) object);
                break;
        }
    }

    /**
     * BATCH INSERT or UPDATE
     */
    public void batchCreateOrUpdateBranches(final BatchList branches, final DatabaseOperation databaseOperations) {
        try {
            Dao<Branch, Integer> daoBranches = getBranches();
            daoBranches.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (Branch branch : ((BatchList<Branch>) branches))
                        branch.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateBranchPrices(final BatchList branchPrices, final DatabaseOperation databaseOperations) {
        try {
            Dao<BranchPrice, Integer> daoBranchPrices = getBranchPrices();
            daoBranchPrices.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (BranchPrice branchPrice : ((BatchList<BranchPrice>) branchPrices))
                        branchPrice.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateBranchTags(final BatchList branchTags, final DatabaseOperation databaseOperations) {
        try {
            Dao<BranchTag, Integer> daoBranchTags = getBranchTags();
            daoBranchTags.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (BranchTag branchTag : ((BatchList<BranchTag>) branchTags))
                        branchTag.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateCustomers(final BatchList customers, final DatabaseOperation databaseOperations) {
        try {
            Dao<Customer, Integer> daoCustomers = getCustomers();
            daoCustomers.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (Customer customer : ((BatchList<Customer>) customers))
                        customer.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateInventories(final BatchList inventories, final DatabaseOperation databaseOperations) {
        try {
            Dao<Inventory, Integer> daoInventories = getInventories();
            daoInventories.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (Inventory inventory : ((BatchList<Inventory>) inventories))
                        inventory.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateProducts(final BatchList products, final DatabaseOperation databaseOperations) {
        try {
            Dao<Product, Integer> daoProducts = getProducts();
            daoProducts.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (Product product : ((BatchList<Product>) products))
                        product.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateProductTags(final BatchList productTags, final DatabaseOperation databaseOperations) {
        try {
            Dao<ProductTag, Integer> daoProductTags = getProductTags();
            daoProductTags.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (ProductTag productTag : ((BatchList<ProductTag>) productTags))
                        productTag.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateTaxRates(final BatchList taxRates, final DatabaseOperation databaseOperations) {
        try {
            Dao<TaxRate, Integer> daoTaxRates = getTaxRates();
            daoTaxRates.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (TaxRate taxRate : ((BatchList<TaxRate>) taxRates))
                        taxRate.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateTaxSettings(final BatchList taxSettings, final DatabaseOperation databaseOperations) {
        try {
            Dao<TaxSetting, Integer> daoTaxSettings = getTaxSettings();
            daoTaxSettings.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (TaxSetting taxSetting : ((BatchList<TaxSetting>) taxSettings))
                        taxSetting.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateUnits(final BatchList units, final DatabaseOperation databaseOperations) {
        try {
            Dao<Unit, Integer> daoUnits = getUnits();
            daoUnits.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (Unit unit : ((BatchList<Unit>) units))
                        unit.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateUsers(final BatchList users, final DatabaseOperation databaseOperations) {
        try {
            Dao<User, Integer> daoUsers = getUsers();
            daoUsers.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (User user : ((BatchList<User>) users))
                        user.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateDocumentTypes(final BatchList documentTypes, final DatabaseOperation databaseOperations) {
        try {
            Dao<DocumentType, Integer> daoDocumentTypes = getDocumentTypes();
            daoDocumentTypes.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (DocumentType documentType : ((BatchList<DocumentType>) documentTypes))
                        documentType.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateDocumentPurposes(final BatchList documentPurposes, final DatabaseOperation databaseOperations) {
        try {
            Dao<DocumentPurpose, Integer> daoDocumentPurposes = getDocumentPurposes();
            daoDocumentPurposes.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (DocumentPurpose documentPurpose : ((BatchList<DocumentPurpose>) documentPurposes))
                        documentPurpose.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateBranchUsers(final BatchList branchUserAssocs, final DatabaseOperation databaseOperations) {
        try {
            Dao<BranchUserAssoc, Integer> daoBranchUsers = getBranchUserAssocs();
            daoBranchUsers.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (BranchUserAssoc branchUserAssoc : ((BatchList<BranchUserAssoc>) branchUserAssocs))
                        branchUserAssoc.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateProductTaxRates(final BatchList productTaxRateAssocs, final DatabaseOperation databaseOperations) {
        try {
            Dao<ProductTaxRateAssoc, Integer> daoProductTaxRates = getProductTaxRateAssocs();
            daoProductTaxRates.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (ProductTaxRateAssoc productTaxRateAssoc : ((BatchList<ProductTaxRateAssoc>) productTaxRateAssocs))
                        productTaxRateAssoc.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void batchCreateOrUpdateDocuments(final BatchList documents, final DatabaseOperation
            databaseOperations) {
        try {
            Dao<Document, Integer> daoDocuments = getDocuments();
            daoDocuments.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (Document document : ((BatchList<Document>) documents))
                        document.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateDocumentLines(final BatchList documentLines, final DatabaseOperation
            databaseOperations) {
        try {
            Dao<DocumentLine, Integer> daoDocumentLines = getDocumentLines();
            daoDocumentLines.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (DocumentLine documentLine : ((BatchList<DocumentLine>) documentLines))
                        documentLine.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchCreateOrUpdateExtendedAttributes(final BatchList extendedAttributes, final DatabaseOperation
            databaseOperations) {
        try {
            Dao<ExtendedAttributes, Integer> daoExtendedAttributes = getExtendedAttributes();
            daoExtendedAttributes.callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (ExtendedAttributes extendedAttribute : ((BatchList<ExtendedAttributes>) extendedAttributes))
                        extendedAttribute.dbOperation(ImonggoDBHelper.this, databaseOperations);
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllDatabaseValues() {
        try {
            getBranches().deleteBuilder().delete();
            getBranchPrices().deleteBuilder().delete();
            getBranchTags().deleteBuilder().delete();
            getCustomers().deleteBuilder().delete();
            getInventories().deleteBuilder().delete();
            getProducts().deleteBuilder().delete();
            getProductTags().deleteBuilder().delete();
            getSessions().deleteBuilder().delete();
            getTaxRates().deleteBuilder().delete();
            getTaxSettings().deleteBuilder().delete();
            getUnits().deleteBuilder().delete();
            getUsers().deleteBuilder().delete();
            getLastUpdatedAts().deleteBuilder().delete();
            getDocumentTypes().deleteBuilder().delete();
            getDocumentPurposes().deleteBuilder().delete();

            getOfflineData().deleteBuilder().delete();

            getDocuments().deleteBuilder().delete();
            getDocumentLines().deleteBuilder().delete();
            getExtendedAttributes().deleteBuilder().delete();

            getBranchUserAssocs().deleteBuilder().delete();
            getProductTaxRateAssocs().deleteBuilder().delete();
            getDailySales().deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}