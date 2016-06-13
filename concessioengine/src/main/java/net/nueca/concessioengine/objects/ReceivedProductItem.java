package net.nueca.concessioengine.objects;

import net.nueca.imonggosdk.objects.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 31/05/2016.
 */
public class ReceivedProductItem {
    private Product product;
    private List<ReceivedProductItemLine> productItemLines = new ArrayList<>();

    public ReceivedProductItem(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ReceivedProductItemLine getProductItemLineAt(int position) {
        return productItemLines.get(position);
    }

    public ReceivedProductItemLine removeProductItemLineAt(int position) {
        return productItemLines.remove(position);
    }

    public List<ReceivedProductItemLine> getProductItemLines() {
        return productItemLines;
    }

    public void setProductItemLines(List<ReceivedProductItemLine> productItemLines) {
        this.productItemLines = productItemLines;
    }

    public boolean addProductItemLine(ReceivedProductItemLine productItemLine) {
        return productItemLines.add(productItemLine);
    }

    public double getSubtotal() {
        double total = 0d;
        for(ReceivedProductItemLine itemLine : productItemLines)
            total += itemLine.getTotal_subtotal();
        return total;
    }

    public double getActualQuantity() {
        double total = 0d;
        for(ReceivedProductItemLine itemLine : productItemLines)
            total += itemLine.getTotal_actual_quantity();
        return total;
    }
}
