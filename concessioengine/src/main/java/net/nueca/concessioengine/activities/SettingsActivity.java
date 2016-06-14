package net.nueca.concessioengine.activities;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.j256.ormlite.stmt.Where;
import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.SettingsAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.printer.epson.listener.DiscoverySettingsListener;
import net.nueca.concessioengine.printer.epson.listener.PrintListener;
import net.nueca.concessioengine.printer.epson.tools.EpsonPrinterTools;
//import net.nueca.concessioengine.printer.tools.PrinterTools;
import net.nueca.concessioengine.printer.starmicronics.enums.StarIOPaperSize;
import net.nueca.concessioengine.printer.starmicronics.tools.StarIOPrinterTools;
import net.nueca.concessioengine.tools.appsettings.AppSettings;
import net.nueca.concessioengine.tools.appsettings.AppTools;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.accountsettings.DebugMode;
import net.nueca.imonggosdk.objects.accountsettings.ProductSorting;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.tools.DialogTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rhymartmanchus on 12/01/2016.
 */
public class SettingsActivity extends ModuleActivity {

    private RecyclerView rvCustomers;
    private Toolbar tbActionBar;
    private SettingsAdapter settingsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_customers_fragment_rv);
        rvCustomers = (RecyclerView) findViewById(R.id.rvCustomers);
        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);

        settingsAdapter = new SettingsAdapter(this, generateSettings());
        settingsAdapter.initializeRecyclerView(this, rvCustomers);
        rvCustomers.setAdapter(settingsAdapter);

        rvCustomers.setBackgroundResource(android.R.color.white);

        assert getSupportActionBar() != null;
        setSupportActionBar(tbActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Settings");
        tbActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private List<AppSettings> generateSettings() {
        int sectionFirstPosition = 0;
        ArrayList<AppSettings> appSettings = new ArrayList<>();

        // header
        AppSettings headerApp = new AppSettings();
        headerApp.setHeader(true);
        headerApp.setSectionFirstPosition(sectionFirstPosition);
        headerApp.setConcessioModule(ConcessioModule.APPLICATION);
        // header

        AppSettings version = new AppSettings();
        version.setHeader(false);
        version.setSectionFirstPosition(sectionFirstPosition);
        version.setConcessioModule(ConcessioModule.APPLICATION);
        version.setAppSettingEntry(AppSettings.AppSettingEntry.VERSION);
        version.setValue(AppTools.getAppVersionName(this));

        appSettings.add(headerApp);
        appSettings.add(version);

        DebugMode debugMode = getModuleSetting(ConcessioModule.APP).getDebugMode();
        if(debugMode.is_enabled()) {
            AppSettings debug = new AppSettings();
            debug.setHeader(false);
            debug.setSectionFirstPosition(sectionFirstPosition);
            debug.setConcessioModule(ConcessioModule.APPLICATION);
            debug.setAppSettingEntry(AppSettings.AppSettingEntry.CLEAR_TRANSACTIONS);
            debug.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    Log.e("Offline Datas", OfflineData.fetchAll(getHelper(), OfflineData.class).size()+"---");
                    Log.e("Offline Datas", Document.fetchAll(getHelper(), Document.class).size()+"---");
                    DialogTools.showConfirmationDialog(SettingsActivity.this, "Clear Transactions", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                getHelper().deleteAll(DocumentLine.class, InvoiceLine.class, OrderLine.class);
                                getHelper().deleteAll(Order.class, new DBTable.ConditionsWindow<Order, Integer>() {
                                    @Override
                                    public Where<Order, Integer> renderConditions(Where<Order, Integer> where) throws SQLException {
                                        return where.isNotNull("offlinedata_id");
                                    }
                                });
                                getHelper().deleteAll(Document.class, new DBTable.ConditionsWindow<Document, Integer>() {
                                    @Override
                                    public Where<Document, Integer> renderConditions(Where<Document, Integer> where) throws SQLException {
                                        return where.isNotNull("offlinedata_id");
                                    }
                                });
                                getHelper().deleteAll(Invoice.class, new DBTable.ConditionsWindow<Invoice, Integer>() {
                                    @Override
                                    public Where<Invoice, Integer> renderConditions(Where<Invoice, Integer> where) throws SQLException {
                                        return where.isNotNull("offlinedata_id");
                                    }
                                });
                                getHelper().deleteAll(OfflineData.class, new DBTable.ConditionsWindow<OfflineData, Integer>() {
                                    @Override
                                    public Where<OfflineData, Integer> renderConditions(Where<OfflineData, Integer> where) throws SQLException {
                                        return where.in("type", OfflineData.DOCUMENT, OfflineData.INVOICE, OfflineData.ORDER, OfflineData.CUSTOMER);
                                    }
                                });
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(SettingsActivity.this, "Transactions deleted!", Toast.LENGTH_LONG).show();
                        }
                    }, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    }, R.style.AppCompatDialogStyle_Light);
                }
            });
            appSettings.add(debug);
        }

        AppSettings autoUpdate = new AppSettings();
        autoUpdate.setHeader(false);
        autoUpdate.setSectionFirstPosition(sectionFirstPosition);
        autoUpdate.setConcessioModule(ConcessioModule.APPLICATION);
        autoUpdate.setAppSettingEntry(AppSettings.AppSettingEntry.AUTO_UPDATE_APP);
        autoUpdate.setValueType(AppSettings.ValueType.SWITCH);
        appSettings.add(autoUpdate);

        try {
            final List<ProductSorting> productSortings = getHelper().fetchForeignCollection(getModuleSetting(ConcessioModule.APP).getProductSortings().closeableIterator());
            ProductSorting defaultSorting = new ProductSorting();
            defaultSorting.setIs_default(true);
            int selected = productSortings.indexOf(defaultSorting);

            ArrayAdapter<ProductSorting> psAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_light, productSortings);
            psAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
            AppSettings multi = new AppSettings();
            multi.setSelectedItem(selected);
            multi.setHeader(false);
            multi.setSectionFirstPosition(sectionFirstPosition);
            multi.setConcessioModule(ConcessioModule.APPLICATION);
            multi.setAppSettingEntry(AppSettings.AppSettingEntry.PRODUCT_SORTING);
            multi.setValueType(AppSettings.ValueType.DROPDOWN);
            multi.setAdapter(psAdapter);
            multi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ProductSorting prevSelected = new ProductSorting();
                    prevSelected.setIs_default(true);
                    int selected = productSortings.indexOf(prevSelected);
                    prevSelected = productSortings.get(selected);
                    prevSelected.setIs_default(false);
                    prevSelected.updateTo(getHelper());

                    ProductSorting newSelected = productSortings.get(position);
                    newSelected.setIs_default(true);
                    newSelected.updateTo(getHelper());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
            appSettings.add(multi);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // --------- PRINTER
        sectionFirstPosition = appSettings.size()-1;

        AppSettings printerHeader = new AppSettings();
        printerHeader.setHeader(true);
        printerHeader.setSectionFirstPosition(sectionFirstPosition);
        printerHeader.setConcessioModule(ConcessioModule.PRINTER);
        appSettings.add(printerHeader);

//        // Epson Printer ----- TURNED off for Rebisco
//        appSettings.add(generateEpsonPrinter(sectionFirstPosition));

        // Star Printer
        appSettings.add(generateStarPrinter(sectionFirstPosition));

        ArrayAdapter<StarIOPaperSize> paperSizeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_light, StarIOPaperSize.values());
        paperSizeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
        AppSettings starPrinter = new AppSettings();
        starPrinter.setHeader(false);
        starPrinter.setSectionFirstPosition(sectionFirstPosition);
        starPrinter.setConcessioModule(ConcessioModule.PRINTER);
        starPrinter.setAppSettingEntry(AppSettings.AppSettingEntry.CONFIGURE_STAR_PRINTER_SIZE);
        starPrinter.setValueType(AppSettings.ValueType.DROPDOWN);
        starPrinter.setAdapter(paperSizeAdapter);
        starPrinter.setSelectedItem(StarIOPrinterTools.getPaperType(this).ordinal());
        starPrinter.setEnabled(!StarIOPrinterTools.getTargetPrinter(this).equals(""));

        appSettings.add(starPrinter);

        return appSettings;
    }

    private AppSettings generateStarPrinter(int sectionFirstPosition) {
        final AppSettings starPrinter = new AppSettings();
        starPrinter.setHeader(false);
        starPrinter.setSectionFirstPosition(sectionFirstPosition);
        starPrinter.setConcessioModule(ConcessioModule.PRINTER);
        starPrinter.setAppSettingEntry(AppSettings.AppSettingEntry.CONFIGURE_STAR_PRINTER);

        if(StarIOPrinterTools.getTargetPrinter(this).equals(""))
            starPrinter.setValue("Not Connected!");
        else {
            starPrinter.setValue(StarIOPrinterTools.getTargetPrinter(this));
        }

        starPrinter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                StarIOPrinterTools.showDiscoverDialog(SettingsActivity.this, new StarIOPrinterTools.StarIOPrinterListener() {
                    @Override
                    public void onPrinterSelected(PortInfo printer) {
                        if(StarIOPrinterTools.getTargetPrinter(SettingsActivity.this).equals(""))
                            starPrinter.setValue("Not Connected!");
                        else {
                            AppSettings temp = new AppSettings();
                            temp.setAppSettingEntry(AppSettings.AppSettingEntry.CONFIGURE_STAR_PRINTER);
                            int index = settingsAdapter.getList().indexOf(temp);
                            settingsAdapter.getItem(index).setValue(StarIOPrinterTools.getTargetPrinter(SettingsActivity.this));
                            settingsAdapter.getItem(index+1).setEnabled(true);
                            settingsAdapter.notifyItemChanged(index);
                            settingsAdapter.notifyItemChanged(index+1);

                            starPrinter.setValue(StarIOPrinterTools.getTargetPrinter(SettingsActivity.this));
                            String printData = "TEST PRINT\n";
                            printData += Build.MODEL+" is connected!\n";

                            StarIOPrinterTools.print(SettingsActivity.this,StarIOPrinterTools.getTargetPrinter(SettingsActivity.this), "portable", StarIOPaperSize.p2INCH, starIOPrinterErrorListener, printData);
                        }
                    }
                });
            }
        });

        return starPrinter;
    }

    private AppSettings generateEpsonPrinter(int sectionFirstPosition) {
        AppSettings epsonPrinter = new AppSettings();
        epsonPrinter.setHeader(false);
        epsonPrinter.setSectionFirstPosition(sectionFirstPosition);
        epsonPrinter.setConcessioModule(ConcessioModule.PRINTER);
        epsonPrinter.setAppSettingEntry(AppSettings.AppSettingEntry.CONFIGURE_EPSON_PRINTER);

        EpsonPrinterTools.updateTargetPrinter(this, new HashMap<String, String>(){{
            put(EpsonPrinterTools.PRINTER_NAME, "");
            put(EpsonPrinterTools.TARGET_PRINTER, "");
        }});

        if(EpsonPrinterTools.targetPrinterName(this).equals(""))
            epsonPrinter.setValue("Not Connected!");
        else
            epsonPrinter.setValue(EpsonPrinterTools.targetPrinterName(this));
        epsonPrinter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                EpsonPrinterTools.showDiscoveryDialog(SettingsActivity.this, new DiscoverySettingsListener() {
                    @Override
                    public void onPortTypeSelected(int portType) { }

                    @Override
                    public void onModelTypeSelected(int model) { }

                    @Override
                    public void onDeviceTypeSelected(int deviceType) { }

                    @Override
                    public void onLanguageSelected(int language) { }

                    @Override
                    public void onPrinterSeriesSelected(int series) { }

                    @Override
                    public void onFilterTypeSelected(int filterType) { }

                    @Override
                    public void onPrinterDiscovered(HashMap<String, String> printer) { }

                    @Override
                    public void onDiscoveryError(Exception e) { }

                    @Override
                    public void onTargetPrinterSelected(String targetPrinter) {
                        AppSettings printer = new AppSettings();
                        printer.setAppSettingEntry(AppSettings.AppSettingEntry.CONFIGURE_EPSON_PRINTER);
                        int index = settingsAdapter.getList().indexOf(printer);
                        settingsAdapter.getItem(index).setValue(EpsonPrinterTools.targetPrinterName(SettingsActivity.this));
                        settingsAdapter.notifyDataSetChanged();

                        EpsonPrinterTools.print(targetPrinter, new PrintListener() {
                            @Override
                            public Printer initializePrinter() {
                                try {
                                    return new Printer(EpsonPrinterTools.getPrinterProperties(SettingsActivity.this, EpsonPrinterTools.PRINTER_SERIES),
                                            EpsonPrinterTools.getPrinterProperties(SettingsActivity.this, EpsonPrinterTools.PRINTER_LANGUAGE), SettingsActivity.this);
                                } catch (Epos2Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            public Printer onBuildPrintData(Printer printer) {
                                try {
                                    printer.addTextFont(Printer.FONT_A);
                                    printer.addFeedLine(5);
                                    printer.addTextAlign(Printer.ALIGN_CENTER);
                                    printer.addText("TEST PRINT\n");
                                    printer.addText(Build.MODEL+" is connected!\n");
                                    printer.addFeedLine(5);
                                } catch (Epos2Exception e) {
                                    e.printStackTrace();
                                }
                                return printer;
                            }

                            @Override
                            public void onPrinterWarning(String message) { }

                            @Override
                            public void onPrinterReceive(Printer printerObj, int code, PrinterStatusInfo status, String printJobId) { }

                            @Override
                            public void onPrintError(String message) { }

                            @Override
                            public void onPrintSuccess() { }
                        }, SettingsActivity.this);
                    }
                });
            }
        });

        return epsonPrinter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            EpsonPrinterTools.stopDiscovery();
        } catch (Epos2Exception e) {
            e.printStackTrace();
        }
    }
}
