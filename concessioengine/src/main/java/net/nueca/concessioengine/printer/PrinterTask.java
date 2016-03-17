package net.nueca.concessioengine.printer;

import android.content.Context;
import android.os.AsyncTask;

import net.nueca.concessioengine.printer.epson.listener.PrintListener;
import net.nueca.concessioengine.printer.epson.tools.EpsonPrinterTools;

/**
 * Created by rhymartmanchus on 22/02/2016.
 */
public class PrinterTask extends AsyncTask<Void, Void, Void> {

    private PrintListener printListener;
    private Context context;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        String targetPrinter = EpsonPrinterTools.targetPrinter(context);
        if(targetPrinter != null)
            EpsonPrinterTools.print(targetPrinter, printListener, context);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    public PrintListener getPrintListener() {
        return printListener;
    }

    public void setPrintListener(PrintListener printListener) {
        this.printListener = printListener;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}

