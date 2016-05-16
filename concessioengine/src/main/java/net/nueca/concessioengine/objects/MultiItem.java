package net.nueca.concessioengine.objects;

import net.nueca.imonggosdk.objects.Product;

/**
 * Created by rhymartmanchus on 12/05/2016.
 */
public class MultiItem {

    private boolean isHeader = false;
    private Values values;
    private Product product;
    private SelectedProductItem selectedProductItem;
    private int sectionFirstPosition = 0;

    public MultiItem() {
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public Values getValues() {
        return values;
    }

    public void setValues(Values values) {
        this.values = values;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public SelectedProductItem getSelectedProductItem() {
        return selectedProductItem;
    }

    public void setSelectedProductItem(SelectedProductItem selectedProductItem) {
        this.selectedProductItem = selectedProductItem;
    }

    public int getSectionFirstPosition() {
        return sectionFirstPosition;
    }

    public void setSectionFirstPosition(int sectionFirstPosition) {
        this.sectionFirstPosition = sectionFirstPosition;
    }
}
