package net.nueca.concessioengine.receipts.rebisco;

import android.content.Context;

import com.j256.ormlite.stmt.QueryBuilder;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.printer.ReceiptTools;
import net.nueca.concessioengine.printer.starmicronics.enums.StarIOPaperSize;
import net.nueca.concessioengine.printer.starmicronics.tools.StarIOPrinterTools;
import net.nueca.concessioengine.receipts.BaseBuilder;
import net.nueca.concessioengine.receipts.BaseReceipt;
import net.nueca.concessioengine.tools.BluetoothTools;
import net.nueca.concessioengine.tools.PriceTools;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.objects.accountsettings.ProductSorting;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.tools.Configurations;
import net.nueca.imonggosdk.tools.NumberTools;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by rhymartmanchus on 17/05/2016.
 */
public class DocumentsReceipt extends BaseReceipt {

    public DocumentsReceipt() { }

    public DocumentsReceipt(Builder builder) {
        this.isReprint = builder.isReprint;
        this.offlineData = builder.offlineData;

        if(builder.context == null)
            throw new NullPointerException("DocumentsReceipt.context cannot be null");
        this.context = builder.context;

        if(builder.concessioModule == null)
            throw new NullPointerException("DocumentsReceipt.concessioModule cannot be null");
        this.concessioModule = builder.concessioModule;

        if(builder.agentName == null)
            throw new NullPointerException("DocumentsReceipt.agentName cannot be null");
        this.agentName = builder.agentName;

        if(builder.title == null)
            throw new NullPointerException("DocumentsReceipt.title cannot be null");
        this.title = builder.title;

        if(builder.branch == null)
            throw new NullPointerException("DocumentsReceipt.branch cannot be null");
        this.branch = builder.branch;

        if(builder.moduleSetting == null)
            throw new NullPointerException("DocumentsReceipt.moduleSetting cannot be null");
        this.moduleSetting = builder.moduleSetting;

    }

    public static class Builder extends BaseBuilder<DocumentsReceipt> {

        public Builder(Context context) {
            super(context);
        }

        @Override
        public DocumentsReceipt build() {
            return new DocumentsReceipt(this);
        }

        @Override
        public DocumentsReceipt print(String ...labels) {
            DocumentsReceipt documentsReceipt = new DocumentsReceipt(this);
            try {
                documentsReceipt.printViaStarPrinter(labels);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return documentsReceipt;
        }
    }

    public void printViaStarPrinter(final String... labels) throws SQLException {
        if(!BluetoothTools.isEnabled())
            return;

        if(!StarIOPrinterTools.isPrinterOnline(context, StarIOPrinterTools.getTargetPrinter(context), "portable"))
            return;

        ArrayList<byte[]> data = new ArrayList<>();

        double numberOfPages = 1.0, items = 0;
        int page = 1;

        try {
            for(int i = 0;i < labels.length;i++) {
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)0,
                data.add((branch.getName()+"\r\n").getBytes());
                data.add((branch.generateAddress()+"\r\n\r\n").getBytes());

                data.add((title+"\r\n\r\n").getBytes());
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                data.add((ReceiptTools.tabber("Salesman: ", agentName, 32)+"\r\n").getBytes());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                if(offlineData != null) {
                    data.add(("Ref #: "+offlineData.getReference_no()+"\r\n").getBytes());
                    data.add(("Date: " + simpleDateFormat.format(offlineData.getDateCreated())+"\r\n").getBytes());
                    if(offlineData.getConcessioModule() == ConcessioModule.RELEASE_ADJUSTMENT) {
                        data.add(("Company: " + offlineData.getCategory().toUpperCase()+"\r\n").getBytes()); // TODO,
                        data.add((ReceiptTools.tabber("Reason: ", offlineData.getDocumentReason(), 32)+"\r\n").getBytes()); //ProductsAdapterHelper.getReason().getName()
                    }
                }
                else
                    data.add(("Date: " + simpleDateFormat.format(Calendar.getInstance().getTime())+"\r\n").getBytes());


                double totalQuantity = 0.0;
                double totalAmount = 0.0;
                data.add("================================".getBytes());
                data.add("Quantity                  Amount".getBytes());
                data.add("================================".getBytes());

                if (offlineData != null && offlineData.getType() == OfflineData.DOCUMENT &&
                        (concessioModule == ConcessioModule.RECEIVE_SUPPLIER
                                || concessioModule == ConcessioModule.RELEASE_SUPPLIER
                                || concessioModule == ConcessioModule.RELEASE_ADJUSTMENT
                                || concessioModule == ConcessioModule.HISTORY)) {

                    numberOfPages = Math.ceil((double)offlineData.getObjectFromData(Document.class).getDocument_lines().size()/Configurations.MAX_ITEMS_FOR_PRINTING);
                    page = 1;
                    items = 0;

                    List<DocumentLine> documentLines = offlineData.getObjectFromData(Document.class).getDocument_lines();
                    Collections.sort(documentLines, new Comparator<DocumentLine>() {
                        @Override
                        public int compare(DocumentLine lhs, DocumentLine rhs) {
                            return 0;
                        }
                    });

                    for (final DocumentLine documentLine : documentLines) {
                        Double retail_price = 0.0;
                        try {
                            final BranchProduct branchProduct = ProductsAdapterHelper.getDbHelper().fetchForeignCollection(documentLine.getProduct().getBranchProducts().closeableIterator(), new ImonggoDBHelper2.Conditional<BranchProduct>() {
                                @Override
                                public boolean validate(BranchProduct obj) {
                                    if(documentLine.getUnit_id() == null) {
                                        if(obj.getUnit() == null)
                                            return true;
                                    }
                                    else if(obj.getUnit() != null && documentLine.getUnit_id() == obj.getUnit().getId())
                                        return true;
                                    return false;
                                }
                            }, 0);

                            Unit unit = null;
                            if(branchProduct != null)
                                unit = branchProduct.getUnit();
                            retail_price = PriceTools.identifyRetailPrice(ProductsAdapterHelper.getDbHelper(), documentLine.getProduct(), branch, null, null, unit);

                            if(retail_price == null)
                                retail_price = documentLine.getRetail_price();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                        data.add((documentLine.getProduct().getName() + "\r\n").getBytes());
                        if (documentLine.getUnit_id() != null) {
                            totalQuantity += documentLine.getUnit_quantity();
                            data.add(("  " + documentLine.getUnit_quantity() + "   " + documentLine.getUnit_name() + " x " + NumberTools.separateInCommas(retail_price)+"\r\n").getBytes());
                            Double subtotal = documentLine.getUnit_quantity() * retail_price;
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                            data.add((NumberTools.separateInCommas(subtotal)+"\r\n").getBytes());
                            totalAmount += subtotal;
                        } else {
                            totalQuantity += documentLine.getQuantity();
                            data.add(("  " + documentLine.getQuantity() + "   " + documentLine.getProduct().getBase_unit_name() + " x " + NumberTools.separateInCommas(retail_price) + "\r\n").getBytes());
                            Double subtotal = documentLine.getQuantity() * retail_price;
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                            data.add((NumberTools.separateInCommas(subtotal)+"\r\n").getBytes());
                            totalAmount += subtotal;
                        }

                        items++;

                        if(numberOfPages > 1.0 && page < (int)numberOfPages && items == Configurations.MAX_ITEMS_FOR_PRINTING) {
                            data.add(("\r\n\r\n\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                            data.add(("*Page "+page+"*\r\n\r\n").getBytes());
                            data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                            page++;
                            items = 0;
                            // print
                            if(!StarIOPrinterTools.print(context, StarIOPrinterTools.getTargetPrinter(context), "portable", StarIOPaperSize.p2INCH, data))
                                break;
                            data.clear();
                        }
                    }
                }
                else {
                    QueryBuilder<Inventory, Integer> currentInventories = ProductsAdapterHelper.getDbHelper().fetchObjectsInt(Inventory.class).queryBuilder();
                    currentInventories.selectColumns("id");
                    currentInventories.where().gt("quantity", 0.0);

                    ProductSorting productSorting = ProductsAdapterHelper.getDbHelper().fetchForeignCollection(moduleSetting.getProductSortings().closeableIterator(), new ImonggoDBHelper2.Conditional<ProductSorting>() {
                        @Override
                        public boolean validate(ProductSorting obj) {
                            if(obj.is_default())
                                return true;
                            return false;
                        }
                    }, 0);
                    List<Product> products = ProductsAdapterHelper.getDbHelper().fetchObjects(Product.class).queryBuilder()
                            .orderBy(productSorting.getColumn(), true)
                            .where()
                            .isNotNull("inventory_id").and()
                            .in("inventory_id", currentInventories)
                            .query();

                    numberOfPages = Math.ceil((double)products.size()/Configurations.MAX_ITEMS_FOR_PRINTING);
                    page = 1;
                    items = 0;

                    for(Product product : products) {
                        Double retail_price = 0.0;
                        final Unit unit = Unit.fetchById(ProductsAdapterHelper.getDbHelper(), Unit.class, product.getExtras().getDefault_selling_unit());
                        try {
                            final BranchProduct branchProduct = ProductsAdapterHelper.getDbHelper().fetchForeignCollection(product.getBranchProducts().closeableIterator(), new ImonggoDBHelper2.Conditional<BranchProduct>() {
                                @Override
                                public boolean validate(BranchProduct obj) {
                                    if(unit == null) {
                                        if(obj.getUnit() == null)
                                            return true;
                                    }
                                    else if(obj.getUnit() != null && unit.getId() == obj.getUnit().getId())
                                        return true;
                                    return false;
                                }
                            }, 0);

                            retail_price = PriceTools.identifyRetailPrice(ProductsAdapterHelper.getDbHelper(), product, branch, null, null, unit);

                            if(retail_price == null)
                                retail_price = branchProduct.getUnit_retail_price();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                        data.add((product.getName() + "\r\n").getBytes());

                        double invQuantity = Double.valueOf(product.getInStock());;
                        String unitName = product.getBase_unit_name();
                        if(product.getExtras() != null && product.getExtras().getDefault_selling_unit() != null && !product.getExtras().getDefault_selling_unit().isEmpty()) {
                            if(unit != null) {
                                invQuantity = Double.valueOf(product.getInStock(unit.getQuantity(), ProductsAdapterHelper.getDecimalPlace()));
                                unitName = unit.getName();
                            }
                        }

                        totalQuantity += invQuantity; //product.getInventory().getQuantity();

                        data.add(("  " + invQuantity + "   " //product.getInventory().getQuantity()
                                + unitName + " x " //(unit == null ? product.getBase_unit_name() : unit.getName())
                                + NumberTools.separateInCommas(retail_price)+"\r\n").getBytes());
                        Double subtotal = invQuantity * retail_price; //product.getInventory().getQuantity()

                        totalAmount += subtotal;

                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Left
                        data.add((NumberTools.separateInCommas(subtotal)+"\r\n").getBytes());

                        items++;

                        if(numberOfPages > 1.0 && page < (int)numberOfPages && items == Configurations.MAX_ITEMS_FOR_PRINTING) {
                            data.add(("\r\n\r\n\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                            data.add(("*Page "+page+"*\r\n\r\n").getBytes());
                            data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                            page++;
                            items = 0;

                            if(!StarIOPrinterTools.print(context, StarIOPrinterTools.getTargetPrinter(context), "portable", StarIOPaperSize.p2INCH, data))
                                break;
                            data.clear();
                        }
                    }
                }

                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                data.add(("--------------------------------").getBytes());
                data.add(("Total Quantity: " + NumberTools.separateInCommas(totalQuantity) + "\r\n").getBytes());
                if(concessioModule == ConcessioModule.RELEASE_ADJUSTMENT)
                    data.add((ReceiptTools.spacer("Total MSO Amount: ", NumberTools.separateInCommas(totalAmount), 32)+"\r\n\r\n").getBytes());
                else
                    data.add((ReceiptTools.spacer("Total Order Amount: ", NumberTools.separateInCommas(totalAmount), 32)+"\r\n\r\n").getBytes());
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Left

                if(concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                    Document document = offlineData.getObjectFromData(Document.class);
                    Customer customer = ProductsAdapterHelper.getSelectedCustomer();
                    if(customer == null)
                        customer = document.getCustomer();

                    data.add(("\r\n\r\nCustomer Name: "+customer.generateFullName()+"\r\n").getBytes());
                    data.add(("Customer Code: "+customer.getCode()+"\r\n").getBytes());
                    data.add(("Address: "+customer.generateAddress()+"\r\n").getBytes());
                    data.add("Signature:______________________\r\n".getBytes());
                }

                data.add(labels[i].getBytes());
                if(isReprint) {
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                    data.add("\r\n** This is a reprint **\r\n".getBytes());
                }
                if(i < labels.length-1) {
                    data.add(("\r\n\r\n\r\n").getBytes());
                    data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                }
                else
                    data.add(("\r\n\r\n").getBytes());

                if(!StarIOPrinterTools.print(context, StarIOPrinterTools.getTargetPrinter(context), "portable", StarIOPaperSize.p2INCH, data))
                    break;
                data.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
