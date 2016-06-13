package net.nueca.concessio;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.j256.ormlite.stmt.QueryBuilder;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.printer.epson.tools.EpsonPrinterTools;
import net.nueca.concessioengine.printer.starmicronics.enums.StarIOPaperSize;
import net.nueca.concessioengine.printer.starmicronics.tools.StarIOPrinterTools;
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
import net.nueca.imonggosdk.objects.accountsettings.ProductSorting;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.operations.sync.ImonggoService;
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
 * Created by rhymartmanchus on 10/06/2016.
 */
public class PrintingService extends ImonggoService {

    private List<Branch> branches;
    private OfflineData offlineData;
    private boolean startPrint = false, isPrinting = false, killMe = false;
    private IBinder swableLocalBinder = new PrintingLocalBinder();
    private Dialogging dialogging;
    public interface Dialogging {
        void onStart();
        void onEnd();
    }

    public class PrintingLocalBinder extends Binder {
        public PrintingService getServerInstance() {
            return PrintingService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return swableLocalBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("PRINTING-onCreate", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread syncThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("PRINTING-onStartCommand", "!killMe");
                while (!killMe) {
                    try {
                        Thread.sleep(4000); // 4 seconds
                        Log.e("PRINTING-onStartCommand", "!isPrinting");
                        if(!isPrinting) {
                            Log.e("PRINTING-onStartCommand", "startPrint");
                            if(startPrint) {
                                isPrinting = true;
                                startPrint = true;
                                Log.e("PRINTING-onStartCommand", "before print | startPrint="+startPrint+" || isPrinting="+isPrinting);
                                if(dialogging != null)
                                    dialogging.onStart();
                                printTransactionStar(offlineData, "SAMPLER", "SAMPLER", "SAMPLER");
                                if(dialogging != null)
                                    dialogging.onEnd();
                                startPrint = false;
                                stopSelf();
                                Log.e("PRINTING-onStartCommand", "after print | startPrint="+startPrint+" || isPrinting="+isPrinting);
                            }
                            Log.e("PRINTING-onStartCommand", "startPrint="+startPrint+" || isPrinting="+isPrinting);
                        }
                        Log.e("PRINTING-onStartCommand", "startPrint="+startPrint+" || isPrinting="+isPrinting);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.e("PRINTING-onStartCommand", "startPrint="+startPrint+" || isPrinting="+isPrinting);
//                stopSelf();
            }
        });
        syncThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("PRINTING-onDestroy", "Service destroyed");
    }

    public OfflineData getOfflineData() {
        return offlineData;
    }

    public void setOfflineData(OfflineData offlineData) {
        this.offlineData = offlineData;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public boolean isStartPrint() {
        return startPrint;
    }

    public void setStartPrint(boolean startPrint) {
        this.startPrint = startPrint;
    }

    public boolean isPrinting() {
        return isPrinting;
    }

    public void setPrinting(boolean printing) {
        isPrinting = printing;
    }

    public boolean isKillMe() {
        return killMe;
    }

    public void setKillMe(boolean killMe) {
        this.killMe = killMe;
    }

    public Dialogging getDialogging() {
        return dialogging;
    }

    public void setDialogging(Dialogging dialogging) {
        this.dialogging = dialogging;
    }

    private void printTransactionStar(final OfflineData offlineData, final String... labels) {
        if(!BluetoothTools.isEnabled())
            return;
        Log.e("printTransactionStar", "BT Enabled");

        if(!StarIOPrinterTools.isPrinterOnline(this, StarIOPrinterTools.getTargetPrinter(this), "portable"))
            return;
        Log.e("printTransactionStar", "printer is online");

        Branch branch = branches.get(0);
        ArrayList<byte[]> data = new ArrayList<>();

        double numberOfPages = 1.0, items = 0;
        int page = 1;

        for(int i = 0;i < labels.length;i++) {
            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)0,
            data.add((branch.getName()+"\r\n").getBytes());
            data.add((branch.generateAddress()+"\r\n\r\n").getBytes());

            if(offlineData != null) {
                if(offlineData.getConcessioModule() == ConcessioModule.RELEASE_ADJUSTMENT)
                    data.add(("MISCELLANEOUS STOCK OUT SLIP\r\n\r\n").getBytes());
                else
                    data.add(("Receive SLIP\r\n\r\n").getBytes());
            }
            else
                data.add(("INVENTORY SLIP\r\n\r\n").getBytes());
//                data.add(("Salesman: "+getSession().getUser().getName()+"\r\n").getBytes());
            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
            data.add((EpsonPrinterTools.tabber("Salesman: ", getSession().getUser().getName(), 32)+"\r\n").getBytes());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
            if(offlineData != null) {
                data.add(("Ref #: "+offlineData.getReference_no()+"\r\n").getBytes());
                data.add(("Date: " + simpleDateFormat.format(offlineData.getDateCreated())+"\r\n").getBytes());
                if(offlineData.getConcessioModule() == ConcessioModule.RELEASE_ADJUSTMENT) {
                    data.add(("Company: " + offlineData.getCategory().toUpperCase()+"\r\n").getBytes()); // TODO,
                    data.add((EpsonPrinterTools.tabber("Reason: ", offlineData.getDocumentReason(), 32)+"\r\n").getBytes()); //ProductsAdapterHelper.getReason().getName()
                }
            }
            else
                data.add(("Date: " + simpleDateFormat.format(Calendar.getInstance().getTime())+"\r\n").getBytes());


            double totalQuantity = 0.0;
            double totalAmount = 0.0;
            data.add("================================".getBytes());
            data.add("Quantity                  Amount".getBytes());
            data.add("================================".getBytes());

            numberOfPages = Math.ceil((double)offlineData.getObjectFromData(Document.class).getDocument_lines().size()/ Configurations.MAX_ITEMS_FOR_PRINTING);
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
                    final BranchProduct branchProduct = getHelper().fetchForeignCollection(documentLine.getProduct().getBranchProducts().closeableIterator(), new ImonggoDBHelper2.Conditional<BranchProduct>() {
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
                    retail_price = PriceTools.identifyRetailPrice(getHelper(), documentLine.getProduct(), branch, null, null, unit);

                    if(retail_price == null)
                        retail_price = documentLine.getRetail_price();
                    Log.e("identified retail_price", retail_price.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                data.add((documentLine.getProduct().getName() + "\r\n").getBytes());
                Log.e("documentLine.unit_id", documentLine.getUnit_id()+" --- ");
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
                    if(!StarIOPrinterTools.print(this, StarIOPrinterTools.getTargetPrinter(this), "portable", StarIOPaperSize.p2INCH, data))
                        break;
                    data.clear();
                }
            }

            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
            data.add(("--------------------------------").getBytes());
            data.add(("Total Quantity: " + NumberTools.separateInCommas(totalQuantity) + "\r\n").getBytes());
//                if(concessioModule == ConcessioModule.RELEASE_ADJUSTMENT)
//                    data.add((EpsonPrinterTools.spacer("Total MSO Amount: ", NumberTools.separateInCommas(totalAmount), 32)+"\r\n\r\n").getBytes());
//                else
                data.add((EpsonPrinterTools.spacer("Total Order Amount: ", NumberTools.separateInCommas(totalAmount), 32)+"\r\n\r\n").getBytes());
            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Left

//                if(concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
//                    Document document = offlineData.getObjectFromData(Document.class);
//                    Customer customer = ProductsAdapterHelper.getSelectedCustomer();
//                    if(customer == null)
//                        customer = document.getCustomer();
//
//                    data.add(("\r\n\r\nCustomer Name: "+customer.generateFullName()+"\r\n").getBytes());
//                    data.add(("Customer Code: "+customer.getCode()+"\r\n").getBytes());
//                    data.add(("Address: "+customer.generateAddress()+"\r\n").getBytes());
//                    data.add("Signature:______________________\r\n".getBytes());
//                }

            data.add(labels[i].getBytes());
//                if(simpleTransactionDetailsFragment != null) {
//                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
//                    data.add("\r\n** This is a reprint **\r\n".getBytes());
//                }
            if(i < labels.length-1) {
                data.add(("\r\n\r\n\r\n").getBytes());
                data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
            }
            else
                data.add(("\r\n\r\n").getBytes());

            if(!StarIOPrinterTools.print(this, StarIOPrinterTools.getTargetPrinter(this), "portable", StarIOPaperSize.p2INCH, data))
                break;
            data.clear();
        }
    }

}
