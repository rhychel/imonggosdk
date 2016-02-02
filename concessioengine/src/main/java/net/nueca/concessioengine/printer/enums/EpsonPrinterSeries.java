package net.nueca.concessioengine.printer.enums;

import android.util.Log;

/**
 * Created by Jn on 31/01/16.
 */
public enum EpsonPrinterSeries {
    m10("TM-m10"),
    m30("TM-m30"),
    p20("TM-P20"),
    p60("TM-P60 "),
    p60ii("TM-P60II"),
    p80("TM-P80"),
    t20("TM-T20"),
    t70("TM-T70"),
    t81("TM-T81"),
    t82("TM-T82"),
    t83("TM-T83"),
    t88("TM-T88"),
    t90("TM-T90"),
    t90kp("TM-T90KP"),
    u220("TM-U220"),
    u330("TM-U330"),
    l90("TM-L90"),
    h6000("TM-H600"),
    none("none");

    private static String TAG = "EpsonPrinterSeries";
    private String name;
    EpsonPrinterSeries(String name) {
        this.name = name;
    }

    public String getName(){
       return this.name + " Series";
    }

    public static EpsonPrinterSeries getPrinterSeriesByName(String name) {
        for (EpsonPrinterSeries p : EpsonPrinterSeries.values()) {
            if(p.getName().equals(name)) {
                Log.e(TAG, "Found! " + p.getName());
                return p;
            }
        }

        return none;
    }

}
