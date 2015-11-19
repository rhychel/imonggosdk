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
import net.nueca.imonggosdk.objects.RoutePlan;
import net.nueca.imonggosdk.objects.associatives.CustomerCustomerGroupAssoc;
import net.nueca.imonggosdk.objects.associatives.ProductSalesPromotionAssoc;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.deprecated.Extras_2;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.Settings;
import net.nueca.imonggosdk.objects.TaxRate;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.associatives.ProductTaxRateAssoc;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.deprecated.DocumentLineExtras;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.document.DocumentType;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.objects.invoice.InvoiceTaxRate;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Created by rhymart on 5/12/15.
 * ImonggoLibrary (c)2015
 */
@Deprecated
public class ImonggoDBHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "imonggosdk.db";
    private static final int DATABASE_VERSION = 32;

    private Dao<Branch, Integer> branches = null;
    private Dao<BranchPrice, Integer> branchPrices = null;
    private Dao<BranchTag, Integer> branchTags = null;
    private Dao<Customer, Integer> customers = null;
    private Dao<Inventory, Integer> inventories = null;
    private Dao<Product, Integer> products = null;
    private Dao<ProductTag, Integer> productTags = null;
    private Dao<Extras_2, Integer> extras_2 = null;
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

    /**      added by gama      **/
    private Dao<OfflineData, Integer> offlineData = null;

    private Dao<Document, Integer> documents = null;
    private Dao<DocumentLine, Integer> documentLines = null;
    private Dao<DocumentLineExtras, Integer> documentLineExtras = null;
    //private Dao<DocumentLineExtras_Old, Integer> documentLineExtras = null;

    private Dao<Order, Integer> orders = null;
    private Dao<OrderLine, Integer> orderLines = null;

    private Dao<Invoice, Integer> invoices = null;
    private Dao<InvoiceLine, Integer> invoiceLines = null;
    private Dao<InvoicePayment, Integer> payments = null;
    private Dao<InvoiceTaxRate, Integer> invoiceTaxRates = null;
    /**           end           **/
    /**
     * added by jn
     **/
    private Dao<DailySales, Integer> dailySales = null;
    private Dao<Settings, Integer> settings = null;

    /**
     * end
     **/

    /** added by RHY for REBISCO **/
    private Dao<Extras, String> extras = null; // e.g. PRODUCT_<id>
    private Dao<CustomerCategory, Integer> customerCategories = null;
    private Dao<CustomerGroup, Integer> customerGroups = null;
    private Dao<InvoicePurpose, Integer> invoicePurposes = null;
    private Dao<PaymentTerms, Integer> paymentTerms = null;
    private Dao<PaymentType, Integer> paymentTypes = null;
    private Dao<SalesPromotion, Integer> salesPromotions = null;
    private Dao<Price, Integer> prices = null;
    private Dao<PriceList, Integer> priceLists = null;
    private Dao<RoutePlan, Integer> routePlans = null;

    private Dao<CustomerCustomerGroupAssoc, Integer> customerCustomerGroupAssocs = null;
    private Dao<ProductSalesPromotionAssoc, Integer> productSalesPromotionAssocs = null;
    /**
     * end
     **/


    public ImonggoDBHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

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
            TableUtils.createTable(connectionSource, Extras_2.class);
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
            TableUtils.createTable(connectionSource, DocumentLineExtras.class);
            //TableUtils.createTable(connectionSource, DocumentLineExtras_Old.class);
            TableUtils.createTable(connectionSource, DailySales.class);
            TableUtils.createTable(connectionSource, Settings.class);

            TableUtils.createTable(connectionSource, Order.class);
            TableUtils.createTable(connectionSource, OrderLine.class);

            TableUtils.createTable(connectionSource, Invoice.class);
            TableUtils.createTable(connectionSource, InvoiceLine.class);
            TableUtils.createTable(connectionSource, InvoicePayment.class);
            TableUtils.createTable(connectionSource, InvoiceTaxRate.class);

            // FOR REBISCO
            TableUtils.createTable(connectionSource, Extras.class);
            TableUtils.createTable(connectionSource, CustomerCategory.class);
            TableUtils.createTable(connectionSource, CustomerGroup.class);
            TableUtils.createTable(connectionSource, InvoicePurpose.class);
            TableUtils.createTable(connectionSource, PaymentTerms.class);
            TableUtils.createTable(connectionSource, PaymentType.class);
            TableUtils.createTable(connectionSource, SalesPromotion.class);
            TableUtils.createTable(connectionSource, Price.class);
            TableUtils.createTable(connectionSource, PriceList.class);
            TableUtils.createTable(connectionSource, RoutePlan.class);

            TableUtils.createTable(connectionSource, CustomerCustomerGroupAssoc.class);
            TableUtils.createTable(connectionSource, ProductSalesPromotionAssoc.class);
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
            TableUtils.dropTable(connectionSource, Extras_2.class, true);
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
            //TableUtils.dropTable(connectionSource, ExtendedAttributes.class, true);
            TableUtils.dropTable(connectionSource, DailySales.class, true);
            TableUtils.dropTable(connectionSource, Settings.class, true);
            TableUtils.dropTable(connectionSource, DocumentLineExtras.class, true);
            TableUtils.dropTable(connectionSource, DailySales.class, true);
            //TableUtils.dropTable(connectionSource, DocumentLineExtras_Old.class, true);

            TableUtils.dropTable(connectionSource, Order.class, true);
            TableUtils.dropTable(connectionSource, OrderLine.class, true);

            TableUtils.dropTable(connectionSource, Invoice.class, true);
            TableUtils.dropTable(connectionSource, InvoiceLine.class, true);
            TableUtils.dropTable(connectionSource, InvoicePayment.class, true);
            TableUtils.dropTable(connectionSource, InvoiceTaxRate.class, true);

            // FOR REBISCO
            TableUtils.dropTable(connectionSource, Extras.class, true);
            TableUtils.dropTable(connectionSource, CustomerCategory.class, true);
            TableUtils.dropTable(connectionSource, CustomerGroup.class, true);
            TableUtils.dropTable(connectionSource, InvoicePurpose.class, true);
            TableUtils.dropTable(connectionSource, PaymentTerms.class, true);
            TableUtils.dropTable(connectionSource, PaymentType.class, true);
            TableUtils.dropTable(connectionSource, SalesPromotion.class, true);
            TableUtils.dropTable(connectionSource, Price.class, true);
            TableUtils.dropTable(connectionSource, PriceList.class, true);
            TableUtils.dropTable(connectionSource, RoutePlan.class, true);

            TableUtils.dropTable(connectionSource, CustomerCustomerGroupAssoc.class, true);
            TableUtils.dropTable(connectionSource, ProductSalesPromotionAssoc.class, true);

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
        if(branchTags == null)
            branchTags = getDao(BranchTag.class);
        return branchTags;
    }

    public Dao<Customer, Integer> getCustomers() throws SQLException {
        if(customers == null)
            customers = getDao(Customer.class);
        return customers;
    }

    public Dao<Inventory, Integer> getInventories() throws SQLException {
        if(inventories == null)
            inventories = getDao(Inventory.class);
        return inventories;
    }

    public Dao<Product, Integer> getProducts() throws SQLException {
        if(products == null)
            products = getDao(Product.class);
        return products;
    }

    public Dao<ProductTag, Integer> getProductTags() throws SQLException {
        if(productTags == null)
            productTags = getDao(ProductTag.class);
        return productTags;
    }

    public Dao<Extras_2, Integer> getProductExtras() throws SQLException {
        if(extras_2 == null)
            extras_2 = getDao(Extras_2.class);
        return extras_2;
    }

    public Dao<Session, Integer> getSessions() throws SQLException {
        if(sessions == null)
            sessions = getDao(Session.class);
        return sessions;
    }

    public Dao<TaxRate, Integer> getTaxRates() throws SQLException {
        if(taxRates == null)
            taxRates = getDao(TaxRate.class);
        return taxRates;
    }

    public Dao<TaxSetting, Integer> getTaxSettings() throws SQLException {
        if(taxSettings == null)
            taxSettings = getDao(TaxSetting.class);
        return taxSettings;
    }

    public Dao<Unit, Integer> getUnits() throws SQLException {
        if(units == null)
            units = getDao(Unit.class);
        return units;
    }

    public Dao<User, Integer> getUsers() throws SQLException {
        if(users == null)
            users = getDao(User.class);
        return users;
    }

    public Dao<DocumentType, Integer> getDocumentTypes() throws SQLException {
        if(documentTypes == null)
            documentTypes = getDao(DocumentType.class);
        return documentTypes;
    }

    public Dao<DocumentPurpose, Integer> getDocumentPurposes() throws SQLException {
        if(documentPurposes == null)
            documentPurposes = getDao(DocumentPurpose.class);
        return documentPurposes;
    }

    public Dao<BranchUserAssoc, Integer> getBranchUserAssocs() throws SQLException {
        if(branchUserAssocs == null)
            branchUserAssocs = getDao(BranchUserAssoc.class);
        return branchUserAssocs;
    }

    public Dao<ProductTaxRateAssoc, Integer> getProductTaxRateAssocs() throws SQLException {
        if(productTaxRateAssocs == null)
            productTaxRateAssocs = getDao(ProductTaxRateAssoc.class);
        return productTaxRateAssocs;
    }

    public Dao<LastUpdatedAt, Integer> getLastUpdatedAts() throws SQLException {
        if(lastUpdatedAts == null)
            lastUpdatedAts = getDao(LastUpdatedAt.class);
        return lastUpdatedAts;
    }

    public Dao<OfflineData, Integer> getOfflineData() throws SQLException {
        if(offlineData == null)
            offlineData = getDao(OfflineData.class);
        return offlineData;
    }

    public Dao<Document, Integer> getDocuments() throws SQLException {
        if(documents == null)
            documents = getDao(Document.class);
        return documents;
    }
    public Dao<DocumentLine, Integer> getDocumentLines() throws SQLException {
        if(documentLines == null)
            documentLines = getDao(DocumentLine.class);
        return documentLines;
    }
    public Dao<DocumentLineExtras, Integer> getDocumentLineExtras() throws SQLException {
        if(documentLineExtras == null)
            documentLineExtras = getDao(DocumentLineExtras.class);
        return documentLineExtras;
    }
    /*public Dao<DocumentLineExtras_Old, Integer> getDocumentLineExtras() throws SQLException {
        if(documentLineExtras == null)
            documentLineExtras = getDao(DocumentLineExtras_Old.class);
        return documentLineExtras;
    }*/

    public Dao<DailySales, Integer> getDailySales() throws SQLException {
        if (dailySales == null) {
            dailySales = getDao(DailySales.class);
        }
        return dailySales;
    }

    public Dao<Order, Integer> getOrders() throws SQLException {
        if(orders == null)
            orders = getDao(Order.class);
        return orders;
    }
    public Dao<OrderLine, Integer> getOrderLines() throws SQLException {
        if(orderLines == null)
            orderLines = getDao(OrderLine.class);
        return orderLines;
    }

    public Dao<Invoice, Integer> getInvoices() throws SQLException {
        if(invoices == null)
            invoices = getDao(Invoice.class);
        return invoices;
    }
    public Dao<InvoiceLine, Integer> getInvoiceLines() throws SQLException {
        if(invoiceLines == null)
            invoiceLines = getDao(InvoiceLine.class);
        return invoiceLines;
    }
    public Dao<InvoicePayment, Integer> getPayments() throws SQLException {
        if(payments == null)
            payments = getDao(InvoicePayment.class);
        return payments;
    }
    public Dao<InvoiceTaxRate, Integer> getInvoiceTaxRates() throws SQLException {
        if(invoiceTaxRates == null)
            invoiceTaxRates = getDao(InvoiceTaxRate.class);
        return invoiceTaxRates;
    }

    public Dao<Settings, Integer> getSettings() throws SQLException {
        if (settings == null) {
            settings = getDao(Settings.class);
        }
        return settings;
    }

    // --------------------------------- FOR REBISCO
    public Dao<Extras, String> getExtras() throws SQLException {
        if(extras == null)
            extras = getDao(Extras.class);
        return extras;
    }
    public Dao<CustomerCategory, Integer> getCustomerCategory() throws SQLException {
        if(customerCategories == null)
            customerCategories = getDao(CustomerCategory.class);
        return customerCategories;
    }
    public Dao<CustomerGroup, Integer> getCustomerGroup() throws SQLException {
        if(customerGroups == null)
            customerGroups = getDao(CustomerGroup.class);
        return customerGroups;
    }

    public Dao<InvoicePurpose, Integer> getInvoicePurposes() throws SQLException {
        if(invoicePurposes == null)
            invoicePurposes = getDao(InvoicePurpose.class);
        return invoicePurposes;
    }
    public Dao<PaymentTerms, Integer> getPaymentTerms() throws SQLException {
        if(paymentTerms == null)
            paymentTerms = getDao(PaymentTerms.class);
        return paymentTerms;
    }
    public Dao<PaymentType, Integer> getPaymentTypes() throws SQLException {
        if(paymentTypes == null)
            paymentTypes = getDao(PaymentType.class);
        return paymentTypes;
    }
    public Dao<SalesPromotion, Integer> getSalesPromotions() throws SQLException {
        if(salesPromotions == null)
            salesPromotions = getDao(SalesPromotion.class);
        return salesPromotions;
    }

    public Dao<Price, Integer> getPrices() throws SQLException {
        if(prices == null)
            prices = getDao(Price.class);
        return prices;
    }
    public Dao<PriceList, Integer> getPriceLists() throws SQLException {
        if(priceLists == null)
            priceLists = getDao(PriceList.class);
        return priceLists;
    }

    public Dao<RoutePlan, Integer> getRoutePlans() throws SQLException {
        if(routePlans == null)
            routePlans = getDao(RoutePlan.class);
        return routePlans;
    }
    public Dao<CustomerCustomerGroupAssoc, Integer> getCustomerCustomerGroupAssocs() throws SQLException {
        if(customerCustomerGroupAssocs == null)
            customerCustomerGroupAssocs = getDao(CustomerCustomerGroupAssoc.class);
        return customerCustomerGroupAssocs;
    }
    public Dao<ProductSalesPromotionAssoc, Integer> getProductSalesPromotionAssocs() throws SQLException {
        if(productSalesPromotionAssocs == null)
            productSalesPromotionAssocs = getDao(ProductSalesPromotionAssoc.class);
        return productSalesPromotionAssocs;
    }

    /**
     * DROP Table
     */
    public void dropTable(Table table) throws SQLException {
        switch (table) {
            case BRANCHES: {
                TableUtils.dropTable(getConnectionSource(), Branch.class, true);
            } break;
            case BRANCH_PRICES: {
                TableUtils.dropTable(getConnectionSource(), BranchPrice.class, true);
            } break;
            case BRANCH_TAGS: {
                TableUtils.dropTable(getConnectionSource(), BranchTag.class, true);
            } break;
            case CUSTOMERS: {
                TableUtils.dropTable(getConnectionSource(), Customer.class, true);
            } break;
            case INVENTORIES: {
                TableUtils.dropTable(getConnectionSource(), Inventory.class, true);
            } break;
            case PRODUCTS: {
                TableUtils.dropTable(getConnectionSource(), Product.class, true);
            } break;
            case PRODUCT_TAGS: {
                TableUtils.dropTable(getConnectionSource(), ProductTag.class, true);
            } break;
            case SESSIONS: {
                TableUtils.dropTable(getConnectionSource(), Session.class, true);
            } break;
            case TAX_RATES: {
                TableUtils.dropTable(getConnectionSource(), TaxRate.class, true);
            } break;
            case TAX_SETTINGS: {
                TableUtils.dropTable(getConnectionSource(), TaxSetting.class, true);
            } break;
            case UNITS: {
                TableUtils.dropTable(getConnectionSource(), Unit.class, true);
            } break;
            case USERS: {
                TableUtils.dropTable(getConnectionSource(), User.class, true);
            } break;
            case LAST_UPDATED_AT: {
                TableUtils.dropTable(getConnectionSource(), LastUpdatedAt.class, true);
            } break;
            case DOCUMENT_TYPES:
                TableUtils.dropTable(getConnectionSource(), DocumentType.class, true);
                break;
            case DOCUMENT_PURPOSES:
                TableUtils.dropTable(getConnectionSource(), DocumentPurpose.class, true);
                break;

            case OFFLINEDATA: {
                TableUtils.dropTable(getConnectionSource(), OfflineData.class, true);
            } break;

            case DOCUMENTS: {
                TableUtils.dropTable(getConnectionSource(), Document.class, true);
            } break;
            case DOCUMENT_LINES: {
                TableUtils.dropTable(getConnectionSource(), DocumentLine.class, true);
            } break;
            /*case EXTENDED_ATTRIBUTES: {
                TableUtils.dropTable(getConnectionSource(), DocumentLineExtras.class, true);
            } break;*/
            case DOCUMENT_LINE_EXTRAS: {
                TableUtils.dropTable(getConnectionSource(), DocumentLineExtras.class, true);
            } break;

            case ORDERS: {
                TableUtils.dropTable(getConnectionSource(), Order.class, true);
            } break;
            case ORDER_LINES: {
                TableUtils.dropTable(getConnectionSource(), OrderLine.class, true);
            } break;

            case INVOICES: {
                TableUtils.dropTable(getConnectionSource(), Invoice.class, true);
            } break;
            case INVOICE_LINES: {
                TableUtils.dropTable(getConnectionSource(), InvoiceLine.class, true);
            } break;
            case PAYMENTS: {
                TableUtils.dropTable(getConnectionSource(), InvoicePayment.class, true);
            } break;
            case INVOICE_TAX_RATES: {
                TableUtils.dropTable(getConnectionSource(), InvoiceTaxRate.class, true);
            } break;

            // ASSOCIATIVES
            case BRANCH_USERS: {
                TableUtils.dropTable(getConnectionSource(), BranchUserAssoc.class, true);
            } break;
            case PRODUCT_TAX_RATES: {
                TableUtils.dropTable(getConnectionSource(), ProductTaxRateAssoc.class, true);
            } break;

            case DAILY_SALES: {
                TableUtils.dropTable(getConnectionSource(), DailySales.class, true);
            }
            break;

            case SETTINGS: {
                TableUtils.dropTable(getConnectionSource(), DailySales.class, true);
            }
            break;

            // FOR REBISCO
            case EXTRAS: {
                TableUtils.dropTable(getConnectionSource(), Extras.class, true);
            } break;
            case CUSTOMER_CATEGORIES: {
                TableUtils.dropTable(getConnectionSource(), CustomerCategory.class, true);
            } break;
            case CUSTOMER_GROUPS: {
                TableUtils.dropTable(getConnectionSource(), CustomerGroup.class, true);
            } break;
            case INVOICE_PURPOSES: {
                TableUtils.dropTable(getConnectionSource(), InvoicePurpose.class, true);
            } break;
            case PAYMENT_TERMS: {
                TableUtils.dropTable(getConnectionSource(), PaymentTerms.class, true);
            } break;
            case PAYMENT_TYPES: {
                TableUtils.dropTable(getConnectionSource(), PaymentType.class, true);
            } break;
            case SALES_PROMOTIONS: {
                TableUtils.dropTable(getConnectionSource(), SalesPromotion.class, true);
            } break;
            case PRICES: {
                TableUtils.dropTable(getConnectionSource(), Price.class, true);
            } break;
            case PRICE_LISTS: {
                TableUtils.dropTable(getConnectionSource(), PriceList.class, true);
            } break;
            case ROUTE_PLANS: {
                TableUtils.dropTable(getConnectionSource(), RoutePlan.class, true);
            } break;
            case CUSTOMER_CUSTOMER_GROUP: {
                TableUtils.dropTable(getConnectionSource(), CustomerCustomerGroupAssoc.class, true);
            } break;
            case PRODUCT_SALES_PROMOTION: {
                TableUtils.dropTable(getConnectionSource(), ProductSalesPromotionAssoc.class, true);
            } break;
        }
    }

    /**
     * CREATE, UPDATE, DELETE Operations
     */
    public void dbOperations(Object obj, Table table, DatabaseOperation databaseOperations) throws SQLException {
        switch (databaseOperations) {
            case INSERT: {
                insert(obj, table);
            } break;
            case DELETE: {
                delete(obj, table);
            } break;
            case DELETE_ALL: {
                deleteAll(table);
            } break;
            case UPDATE: {
                update(obj, table);
            } break;
            case UPDATE_WITH_PARAMETER: {

            } break;
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
            case PRODUCT_EXTRAS:
                getProductExtras().create((Extras_2) object);
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
            /*case EXTENDED_ATTRIBUTES:
                getExtendedAttributes().create((DocumentLineExtras) object);
                break;*/
            case DOCUMENT_LINE_EXTRAS:
                getDocumentLineExtras().create((DocumentLineExtras) object);
                break;

            case ORDERS:
                getOrders().create((Order) object);
                break;
            case ORDER_LINES:
                getOrderLines().create((OrderLine) object);
                break;

            case INVOICES:
                getInvoices().create((Invoice) object);
                break;
            case INVOICE_LINES:
                getInvoiceLines().create((InvoiceLine) object);
                break;
            case PAYMENTS:
                getPayments().create((InvoicePayment) object);
                break;
            case INVOICE_TAX_RATES:
                getInvoiceTaxRates().create((InvoiceTaxRate) object);
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
            case SETTINGS:
                getSettings().create((Settings) object);
                break;

            // FOR REBISCO
            case EXTRAS: {
                getExtras().create((Extras) object);
            } break;
            case CUSTOMER_CATEGORIES: {
                getCustomerCategory().create((CustomerCategory) object);
            } break;
            case CUSTOMER_GROUPS: {
                getCustomerGroup().create((CustomerGroup) object);
            } break;
            case INVOICE_PURPOSES: {
                getInvoicePurposes().create((InvoicePurpose) object);

            } break;
            case PAYMENT_TERMS: {
                getPaymentTerms().create((PaymentTerms) object);

            } break;
            case PAYMENT_TYPES: {
                getPaymentTypes().create((PaymentType) object);

            } break;
            case SALES_PROMOTIONS: {
                getSalesPromotions().create((SalesPromotion) object);

            } break;
            case PRICES: {
                getPrices().create((Price) object);

            } break;
            case PRICE_LISTS: {
                getPriceLists().create((PriceList) object);

            } break;
            case ROUTE_PLANS: {
                getRoutePlans().create((RoutePlan) object);
            } break;
            case CUSTOMER_CUSTOMER_GROUP: {
                getCustomerCustomerGroupAssocs().create((CustomerCustomerGroupAssoc) object);

            } break;
            case PRODUCT_SALES_PROMOTION: {
                getProductSalesPromotionAssocs().create((ProductSalesPromotionAssoc) object);
            } break;
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
            case PRODUCT_EXTRAS:
                getProductExtras().delete((Extras_2) object);
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
            /*case EXTENDED_ATTRIBUTES:
                getExtendedAttributes().delete((DocumentLineExtras) object);
                break;*/
            case DOCUMENT_LINE_EXTRAS:
                getDocumentLineExtras().delete((DocumentLineExtras) object);
                break;

            case ORDERS:
                getOrders().delete((Order) object);
                break;
            case ORDER_LINES:
                getOrderLines().delete((OrderLine) object);
                break;

            case INVOICES:
                getInvoices().delete((Invoice) object);
                break;
            case INVOICE_LINES:
                getInvoiceLines().delete((InvoiceLine) object);
                break;
            case PAYMENTS:
                getPayments().delete((InvoicePayment) object);
                break;
            case INVOICE_TAX_RATES:
                getInvoiceTaxRates().delete((InvoiceTaxRate) object);
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
                break;
            case SETTINGS:
                getSettings().delete((Settings) object);
                break;

            // FOR REBISCO
            case EXTRAS: {
                getExtras().delete((Extras) object);
            } break;
            case CUSTOMER_CATEGORIES: {
                getCustomerCategory().delete((CustomerCategory) object);
            } break;
            case CUSTOMER_GROUPS: {
                getCustomerGroup().delete((CustomerGroup) object);
            } break;
            case INVOICE_PURPOSES: {
                getInvoicePurposes().delete((InvoicePurpose) object);

            } break;
            case PAYMENT_TERMS: {
                getPaymentTerms().delete((PaymentTerms) object);

            } break;
            case PAYMENT_TYPES: {
                getPaymentTypes().delete((PaymentType) object);

            } break;
            case SALES_PROMOTIONS: {
                getSalesPromotions().delete((SalesPromotion) object);

            } break;
            case PRICES: {
                getPrices().delete((Price) object);

            } break;
            case PRICE_LISTS: {
                getPriceLists().delete((PriceList) object);

            } break;
            case ROUTE_PLANS: {
                getRoutePlans().delete((RoutePlan) object);
            } break;
            case CUSTOMER_CUSTOMER_GROUP: {
                getCustomerCustomerGroupAssocs().delete((CustomerCustomerGroupAssoc) object);

            } break;
            case PRODUCT_SALES_PROMOTION: {
                getProductSalesPromotionAssocs().delete((ProductSalesPromotionAssoc) object);
            } break;
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
            case PRODUCT_EXTRAS:
                getProductExtras().deleteBuilder().delete();
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
            /*case EXTENDED_ATTRIBUTES:
                getExtendedAttributes().deleteBuilder().delete();
                break;*/
            case DOCUMENT_LINE_EXTRAS:
                getDocumentLineExtras().deleteBuilder().delete();
                break;

            case ORDERS:
                getOrders().deleteBuilder().delete();
                break;
            case ORDER_LINES:
                getOrderLines().deleteBuilder().delete();
                break;

            case INVOICES:
                getInvoices().deleteBuilder().delete();
                break;
            case INVOICE_LINES:
                getInvoiceLines().deleteBuilder().delete();
                break;
            case PAYMENTS:
                getPayments().deleteBuilder().delete();
                break;
            case INVOICE_TAX_RATES:
                getInvoiceTaxRates().deleteBuilder().delete();
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
            case SETTINGS:
                getSettings().deleteBuilder().delete();

            // FOR REBISCO
            case EXTRAS: {
                getExtras().deleteBuilder().delete();
            } break;
            case CUSTOMER_CATEGORIES: {
                getCustomerCategory().deleteBuilder().delete();
            } break;
            case CUSTOMER_GROUPS: {
                getCustomerGroup().deleteBuilder().delete();
            } break;
            case INVOICE_PURPOSES: {
                getInvoicePurposes().deleteBuilder().delete();

            } break;
            case PAYMENT_TERMS: {
                getPaymentTerms().deleteBuilder().delete();

            } break;
            case PAYMENT_TYPES: {
                getPaymentTypes().deleteBuilder().delete();

            } break;
            case SALES_PROMOTIONS: {
                getSalesPromotions().deleteBuilder().delete();

            } break;
            case PRICES: {
                getPrices().deleteBuilder().delete();

            } break;
            case PRICE_LISTS: {
                getPriceLists().deleteBuilder().delete();

            } break;
            case ROUTE_PLANS: {
                getRoutePlans().deleteBuilder().delete();
            } break;
            case CUSTOMER_CUSTOMER_GROUP: {
                getCustomerCustomerGroupAssocs().deleteBuilder().delete();

            } break;
            case PRODUCT_SALES_PROMOTION: {
                getProductSalesPromotionAssocs().deleteBuilder().delete();
            } break;

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
            case PRODUCT_EXTRAS:
                getProductExtras().update((Extras_2) object);
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
            /*case EXTENDED_ATTRIBUTES:
                getExtendedAttributes().update((DocumentLineExtras) object);
                break;*/
            case DOCUMENT_LINE_EXTRAS:
                getDocumentLineExtras().update((DocumentLineExtras) object);
                break;

            case ORDERS:
                getOrders().update((Order) object);
                break;
            case ORDER_LINES:
                getOrderLines().update((OrderLine) object);
                break;

            case INVOICES:
                getInvoices().update((Invoice) object);
                break;
            case INVOICE_LINES:
                getInvoiceLines().update((InvoiceLine) object);
                break;
            case PAYMENTS:
                getPayments().update((InvoicePayment) object);
                break;
            case INVOICE_TAX_RATES:
                getInvoiceTaxRates().update((InvoiceTaxRate) object);
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
            case SETTINGS:
                getSettings().update((Settings) object);
                break;

            // FOR REBISCO
            case EXTRAS: {
                getExtras().update((Extras) object);
            } break;
            case CUSTOMER_CATEGORIES: {
                getCustomerCategory().update((CustomerCategory) object);
            } break;
            case CUSTOMER_GROUPS: {
                getCustomerGroup().update((CustomerGroup) object);
            } break;
            case INVOICE_PURPOSES: {
                getInvoicePurposes().update((InvoicePurpose) object);

            } break;
            case PAYMENT_TERMS: {
                getPaymentTerms().update((PaymentTerms) object);

            } break;
            case PAYMENT_TYPES: {
                getPaymentTypes().update((PaymentType) object);

            } break;
            case SALES_PROMOTIONS: {
                getSalesPromotions().update((SalesPromotion) object);

            } break;
            case PRICES: {
                getPrices().update((Price) object);

            } break;
            case PRICE_LISTS: {
                getPriceLists().update((PriceList) object);

            } break;
            case ROUTE_PLANS: {
                getRoutePlans().update((RoutePlan) object);
            } break;
            case CUSTOMER_CUSTOMER_GROUP: {
                getCustomerCustomerGroupAssocs().update((CustomerCustomerGroupAssoc) object);

            } break;
            case PRODUCT_SALES_PROMOTION: {
                getProductSalesPromotionAssocs().update((ProductSalesPromotionAssoc) object);
            } break;
        }
    }

//    /**
//     * BATCH INSERT or UPDATE
//     */
//    public void batchCreateOrUpdateBranches(final BatchList branches, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<Branch, Integer> daoBranches = getBranches();
//            daoBranches.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(Branch branch : ((BatchList<Branch>)branches))
//                        branch.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateBranchPrices(final BatchList branchPrices, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<BranchPrice, Integer> daoBranchPrices = getBranchPrices();
//            daoBranchPrices.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(BranchPrice branchPrice : ((BatchList<BranchPrice>)branchPrices))
//                        branchPrice.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateBranchTags(final BatchList branchTags, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<BranchTag, Integer> daoBranchTags = getBranchTags();
//            daoBranchTags.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(BranchTag branchTag : ((BatchList<BranchTag>)branchTags))
//                        branchTag.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void batchCreateOrUpdateBranchAssocs(final BatchList branchAssocs, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<BranchUserAssoc, Integer> daoBranchUserAssocs = getBranchUserAssocs();
//            daoBranchUserAssocs.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for (BranchUserAssoc branchUser : ((BatchList<BranchUserAssoc>) branchAssocs))
//                        branchUser.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateCustomers(final BatchList customers, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<Customer, Integer> daoCustomers = getCustomers();
//            daoCustomers.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(Customer customer : ((BatchList<Customer>)customers))
//                        customer.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateInventories(final BatchList inventories, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<Inventory, Integer> daoInventories = getInventories();
//            daoInventories.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(Inventory inventory : ((BatchList<Inventory>)inventories))
//                        inventory.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateProducts(final BatchList products, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<Product, Integer> daoProducts = getProducts();
//            daoProducts.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(Product product : ((BatchList<Product>)products))
//                        product.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateProductTags(final BatchList productTags, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<ProductTag, Integer> daoProductTags = getProductTags();
//            daoProductTags.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(ProductTag productTag : ((BatchList<ProductTag>)productTags))
//                        productTag.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateProductExtras(final BatchList productExtras, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<Extras, Integer> daoProductExtras = getProductExtras();
//            daoProductExtras.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(Extras extras : ((BatchList<Extras>)productExtras))
//                        extras.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateTaxRates(final BatchList taxRates, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<TaxRate, Integer> daoTaxRates = getTaxRates();
//            daoTaxRates.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(TaxRate taxRate : ((BatchList<TaxRate>)taxRates))
//                        taxRate.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateTaxSettings(final BatchList taxSettings, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<TaxSetting, Integer> daoTaxSettings = getTaxSettings();
//            daoTaxSettings.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(TaxSetting taxSetting : ((BatchList<TaxSetting>)taxSettings))
//                        taxSetting.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateUnits(final BatchList units, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<Unit, Integer> daoUnits = getUnits();
//            daoUnits.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(Unit unit : ((BatchList<Unit>)units))
//                        unit.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateUsers(final BatchList users, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<User, Integer> daoUsers = getUsers();
//            daoUsers.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(User user : ((BatchList<User>)users))
//                        user.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateDocumentTypes(final BatchList documentTypes, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<DocumentType, Integer> daoDocumentTypes = getDocumentTypes();
//            daoDocumentTypes.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(DocumentType documentType : ((BatchList<DocumentType>)documentTypes))
//                        documentType.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateDocumentPurposes(final BatchList documentPurposes, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<DocumentPurpose, Integer> daoDocumentPurposes = getDocumentPurposes();
//            daoDocumentPurposes.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for (DocumentPurpose documentPurpose : ((BatchList<DocumentPurpose>)documentPurposes))
//                        documentPurpose.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateBranchUsers(final BatchList branchUserAssocs, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<BranchUserAssoc, Integer> daoBranchUsers = getBranchUserAssocs();
//            daoBranchUsers.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(BranchUserAssoc branchUserAssoc : ((BatchList<BranchUserAssoc>)branchUserAssocs))
//                        branchUserAssoc.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateProductTaxRates(final BatchList productTaxRateAssocs, final DatabaseOperation databaseOperations) {
//        try {
//            Dao<ProductTaxRateAssoc, Integer> daoProductTaxRates = getProductTaxRateAssocs();
//            daoProductTaxRates.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(ProductTaxRateAssoc productTaxRateAssoc : ((BatchList<ProductTaxRateAssoc>)productTaxRateAssocs))
//                        productTaxRateAssoc.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void batchCreateOrUpdateDocuments(final BatchList documents, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<Document, Integer> daoDocuments = getDocuments();
//            daoDocuments.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(Document document : ((BatchList<Document>)documents))
//                        document.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    public void batchCreateOrUpdateDocumentLines(final BatchList documentLines, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<DocumentLine, Integer> daoDocumentLines = getDocumentLines();
//            daoDocumentLines.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(DocumentLine documentLine : ((BatchList<DocumentLine>)documentLines))
//                        documentLine.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /*public void batchCreateOrUpdateExtendedAttributes(final BatchList extendedAttributes, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<DocumentLineExtras, Integer> daoExtendedAttributes = getExtendedAttributes();
//            daoExtendedAttributes.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(DocumentLineExtras extendedAttribute : ((BatchList<DocumentLineExtras>)extendedAttributes))
//                        extendedAttribute.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }*/
//
//    public void batchCreateOrUpdateDocumentLineExtras(final BatchList documentLineExtras, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<DocumentLineExtras, Integer> daoDocumentLineExtras = getDocumentLineExtras();
//            daoDocumentLineExtras.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(DocumentLineExtras documentLineExtra : ((BatchList<DocumentLineExtras>)documentLineExtras))
//                        documentLineExtra.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateOrders(final BatchList orders, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<Order, Integer> daoOrders = getOrders();
//            daoOrders.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(Order order : ((BatchList<Order>)orders))
//                        order.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateOrderLines(final BatchList orderLines, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<OrderLine, Integer> daoOrderLines = getOrderLines();
//            daoOrderLines.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(Order orderLine : ((BatchList<Order>)orderLines))
//                        orderLine.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void batchCreateOrUpdateInvoices(final BatchList invoices, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<Invoice, Integer> daoInvoices = getInvoices();
//            daoInvoices.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(Invoice invoice : ((BatchList<Invoice>)invoices))
//                        invoice.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    public void batchCreateOrUpdateInvoiceLines(final BatchList invoiceLines, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<InvoiceLine, Integer> daoInvoiceLines = getInvoiceLines();
//            daoInvoiceLines.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(InvoiceLine invoiceLine : ((BatchList<InvoiceLine>)invoiceLines))
//                        invoiceLine.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    public void batchCreateOrUpdateInvoicePayments(final BatchList payments, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<InvoicePayment, Integer> daoInvoicePayments = getPayments();
//            daoInvoicePayments.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(InvoicePayment invoiceTaxRate : ((BatchList<InvoicePayment>)payments))
//                        invoiceTaxRate.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    public void batchCreateOrUpdateInvoiceTaxRates(final BatchList invoiceTaxRates, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<InvoiceTaxRate, Integer> daoInvoiceTaxRates = getInvoiceTaxRates();
//            daoInvoiceTaxRates.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(InvoiceTaxRate invoiceTaxRate : ((BatchList<InvoiceTaxRate>)invoiceTaxRates))
//                        invoiceTaxRate.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    public void batchCreateOrUpdatePaymentTypes(final BatchList paymentTypes, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<PaymentType, Integer> daoPaymentTypes = getPaymentTypes();
//            daoPaymentTypes.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(PaymentType paymentType : ((BatchList<PaymentType>)paymentTypes))
//                        paymentType.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // FOR REBISCO ---------------
//    public void batchCreateOrUpdatefExtras(final BatchList fExtras, final DatabaseOperation
//            databaseOperations) {
//        try {
//            Dao<net.nueca.imonggosdk.objects.base.Extras, String> daoExtras = getfExtras();
//            daoExtras.callBatchTasks(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    for(net.nueca.imonggosdk.objects.base.Extras extras : ((BatchList<net.nueca.imonggosdk.objects.base.Extras>)fExtras))
//                        extras.dbOperation(ImonggoDBHelper.this, databaseOperations);
//                    return null;
//                }
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void deleteAllDatabaseValues() {
        try {
            getBranches().deleteBuilder().delete();
            getBranchPrices().deleteBuilder().delete();
            getBranchTags().deleteBuilder().delete();
            getCustomers().deleteBuilder().delete();
            getInventories().deleteBuilder().delete();
            getProducts().deleteBuilder().delete();
            getProductTags().deleteBuilder().delete();
            getProductExtras().deleteBuilder().delete();
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
            //getExtendedAttributes().deleteBuilder().delete();
            getDocumentLineExtras().deleteBuilder().delete();

            getOrders().deleteBuilder().delete();
            getOrderLines().deleteBuilder().delete();

            getInvoices().deleteBuilder().delete();
            getInvoiceLines().deleteBuilder().delete();
            getPayments().deleteBuilder().delete();
            getInvoiceTaxRates().deleteBuilder().delete();

            getPaymentTypes().deleteBuilder().delete();

            getBranchUserAssocs().deleteBuilder().delete();
            getProductTaxRateAssocs().deleteBuilder().delete();
            getDailySales().deleteBuilder().delete();
            getSettings().deleteBuilder().delete();

            // FOR REBISCO
            getExtras().deleteBuilder().delete();
            getCustomerCategory().deleteBuilder().delete();
            getCustomerGroup().deleteBuilder().delete();
            getInvoicePurposes().deleteBuilder().delete();
            getPaymentTerms().deleteBuilder().delete();
            getPaymentTypes().deleteBuilder().delete();
            getSalesPromotions().deleteBuilder().delete();
            getPrices().deleteBuilder().delete();
            getPriceLists().deleteBuilder().delete();
            getRoutePlans().deleteBuilder().delete();
            getCustomerCustomerGroupAssocs().deleteBuilder().delete();
            getProductSalesPromotionAssocs().deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}