package net.nueca.concessioengine.printer.starmicronics.tools;

import android.app.AlertDialog;
import android.content.Context;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

import net.nueca.concessioengine.printer.starmicronics.enums.StarIOPaperSize;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jn on 16/02/16.
 */
public class StarIOPrinterTools {
    /**
     * This function shows how to read the MSR data(credit card) of a portable(ESC/POS) printer. The function first puts the printer into MSR read mode, then asks the user to swipe a credit card The function waits for a response from the user. The user can cancel MSR mode or have the printer read the card.
     *  @param context
     *     Activity for displaying messages to the user
     * @param portName
     *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
     * @param portSettings
     *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
     */
    public static boolean print(Context context, String portName, String portSettings, StarIOPaperSize mPaperSize, String toPrintData) {
        ArrayList<byte[]> list = new ArrayList<>();
        if(mPaperSize == StarIOPaperSize.p2INCH) {
            //list.add(new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }); // Page Area Setting <GS> <W> nL nH (nL = 128, nH = 1)
            list.add(toPrintData.getBytes());

            return sendCommand(context, portName, portSettings, list);
        } else if(mPaperSize == StarIOPaperSize.p3INCH) {
            list.add(new byte[] { 0x1d, 0x57, 0x40, 0x32 }); // Page Area Setting <GS> <W> nL nH (nL = 64, nH = 2)

        } else if(mPaperSize == StarIOPaperSize.p4INCH) {
            list.add(new byte[] { 0x1d, 0x57, 0x40, 0x32 }); // Page Area Setting <GS> <W> nL nH (nL = 64, nH = 2)
            list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)
        }

        list.add(toPrintData.getBytes());
        list.add(new byte[] { 0x1b, 0x64, 0x02 }); // Cut
        return sendCommand(context, portName, portSettings, list);
    }

    private static byte[] convertFromListByteArrayTobyteArray(List<byte[]> ByteArray) {
        int dataLength = 0;
        for (int i = 0; i < ByteArray.size(); i++) {
            dataLength += ByteArray.get(i).length;
        }

        int distPosition = 0;
        byte[] byteArray = new byte[dataLength];
        for (int i = 0; i < ByteArray.size(); i++) {
            System.arraycopy(ByteArray.get(i), 0, byteArray, distPosition, ByteArray.get(i).length);
            distPosition += ByteArray.get(i).length;
        }

        return byteArray;
    }

    private static boolean sendCommand(Context context, String portName, String portSettings, ArrayList<byte[]> byteList) {
        boolean result = true;
        StarIOPort port = null;
        try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
            port = StarIOPort.getPort(portName, portSettings, 20000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

			/*
			 * portable(ESC/POS) Printer Firmware Version 2.4 later, SM-S220i(Firmware Version 2.0 later)
			 * Using Begin / End Checked Block method for preventing "data detective".
			 * When sending large amounts of raster data, use Begin / End Checked Block method and adjust the value in the timeout in the "StarIOPort.getPort" in order to prevent "timeout" of the "endCheckedBlock method" while a printing.
			 * If receipt print is success but timeout error occurs(Show message which is "There was no response of the printer within the timeout period."), need to change value of timeout more longer in "StarIOPort.getPort" method. (e.g.) 10000 -> 30000When use "Begin / End Checked Block Sample Code", do comment out "query commands Sample code".
			 */

			/* Start of Begin / End Checked Block Sample code */
            StarPrinterStatus status = port.beginCheckedBlock();

            if (true == status.offline) {
                throw new StarIOPortException("A printer is offline");
            }

            byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
            port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);

            port.setEndCheckedBlockTimeoutMillis(30000);// Change the timeout time of endCheckedBlock method.
            status = port.endCheckedBlock();

            if (true == status.coverOpen) {
                throw new StarIOPortException("Printer cover is open");
            } else if (true == status.receiptPaperEmpty) {
                throw new StarIOPortException("Receipt paper is empty");
            } else if (true == status.offline) {
                throw new StarIOPortException("Printer is offline");
            }
			/* End of Begin / End Checked Block Sample code */

			/*
			 * portable(ESC/POS) Printer Firmware Version 2.3 earlier
			 * Using query commands for preventing "data detective".
			 * When sending large amounts of raster data, send query commands after writePort data for confirming the end of printing and adjust the value in the timeout in the "checkPrinterSendToComplete" method in order to prevent "timeout" of the "sending query commands" while a printing.
			 * If receipt print is success but timeout error occurs(Show message which is "There was no response of the printer within the timeout period."), need to change value of timeout more longer in "checkPrinterSendToComplete" method. (e.g.) 10000 -> 30000When use "query commands Sample code", do comment out "Begin / End Checked Block Sample Code".
			 */

			/* Start of query commands Sample code */
            // byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
            // port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);
            //
            // checkPrinterSendToComplete(port);
			/* End of query commands Sample code */
        } catch (StarIOPortException e) {
            result = false;
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setNegativeButton("Ok", null);
            AlertDialog alert = dialog.create();
            alert.setTitle("Failure");
            alert.setMessage(e.getMessage());
            alert.setCancelable(false);
            alert.show();
        } finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {
                }
            }
        }

        return result;
    }
}
