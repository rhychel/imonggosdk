package net.nueca.concessioengine.lists;

import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class ValuesList extends ArrayList<Values> {
    public ValuesList(int capacity) {
        super(capacity);
    }

    public ValuesList() { }

    public ValuesList(Collection<? extends Values> collection) {
        super(collection);
    }

    public String getQuantity() {
        String quantity = "0";
        BigDecimal totalQuantity = new BigDecimal(0);
        for(Values values : this) {
            String qty = values.getQuantity().replaceAll(",", "");
            if(qty.length() > 0)
                totalQuantity = totalQuantity.add(new BigDecimal(qty));
        }

        quantity = totalQuantity.toString();
        return quantity;
    }

    /**
     * Used to get the actual quantity from either computed from the unit or from base unit(1).
     * @return
     */
    public String getActualQuantity() {
        String quantity = "0";
        BigDecimal totalQuantity = new BigDecimal(0);
        for(Values values : this) {
            totalQuantity = totalQuantity.add(new BigDecimal(values.getActualQuantity().replaceAll(",", "")));
        }

        quantity = totalQuantity.toString();
        return quantity;
    }

    public String getDiscrepancy() {
        BigDecimal totalDsc = BigDecimal.ZERO;
        for(Values values : this) {
            if(values.getExtendedAttributes() != null && values.getExtendedAttributes().getDiscrepancy() != null)
                totalDsc = totalDsc.add(NumberTools.toBigDecimal(values.getExtendedAttributes().getDiscrepancy()));
        }

        return totalDsc.toString();
    }

    public String getOutrightReturn() {
        BigDecimal totalRet = BigDecimal.ZERO;
        for(Values values : this) {
            if(values.getExtendedAttributes() != null && values.getExtendedAttributes().getOutright_return() != null)
                totalRet = totalRet.add(NumberTools.toBigDecimal(values.getExtendedAttributes().getOutright_return()));
        }

        return totalRet.toString();
    }
}
