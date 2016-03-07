package net.nueca.concessioengine.printer.epson.enums;

import android.util.Log;

import com.epson.epos2.printer.Printer;

/**
 * Created by Jn on 31/01/16.
 */
public enum EPSONPrinterSeries {
    t20("TM-T20", Printer.TM_T20),
    m10("TM-m10", Printer.TM_M10),
    m30("TM-m30", Printer.TM_M30),
    p20("TM-P20", Printer.TM_P20),
    p60("TM-P60 ", Printer.TM_P60),
    p60ii("TM-P60II", Printer.TM_P60II),
    p80("TM-P80", Printer.TM_P80),
    t70("TM-T70", Printer.TM_T70),
    t81("TM-T81", Printer.TM_T81),
    t82("TM-T82", Printer.TM_T82),
    t83("TM-T83", Printer.TM_T83),
    t88("TM-T88", Printer.TM_T88),
    t90("TM-T90", Printer.TM_T90),
    t90kp("TM-T90KP", Printer.TM_T90KP),
    u220("TM-U220", Printer.TM_U220),
    u330("TM-U330", Printer.TM_U330),
    l90("TM-L90", Printer.TM_L90),
    h6000("TM-H600", Printer.TM_H6000),
    none("None", -1);

    private static String TAG = "EPSONPrinterSeries";
    private String name;
    private int series;

    EPSONPrinterSeries(String name, int series) {
        this.series = series;
        this.name = name;
    }

    public static EPSONPrinterSeries getPrinterSeriesByName(String name) {
        for (EPSONPrinterSeries p : EPSONPrinterSeries.values()) {
            if (p.getName().equals(name)) {
                Log.e(TAG, "Found! " + p.getName());
                return p;
            }
        }
        return none;
    }

    public int getSeries() {
        return series;
    }

    public String getName() {
        return this.name + " Series";
    }

}
