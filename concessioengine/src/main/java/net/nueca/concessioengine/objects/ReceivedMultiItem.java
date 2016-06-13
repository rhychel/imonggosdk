package net.nueca.concessioengine.objects;

import android.util.Log;

import net.nueca.concessioengine.lists.ReceivedProductItemList;
import net.nueca.imonggosdk.objects.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 01/06/2016.
 */
public class ReceivedMultiItem {
    private ReceivedProductItem receivedProductItem;
    private ReceivedProductItemLine receivedProductItemLine;
    private ReceivedItemValue receivedItemValue;
    private boolean isHeader = false;

    private int productItemIndex = -1;
    private int productItemLineNo = -1;
    private int itemValueLineNo = -1;

    public ReceivedMultiItem() {}

    public ReceivedMultiItem(ReceivedProductItem receivedProductItem, int productItemIndex, ReceivedProductItemLine receivedProductItemLine,
                             int productItemLineNo, boolean isHeader) {
        this.receivedProductItem = receivedProductItem;
        this.productItemIndex = productItemIndex;

        this.receivedProductItemLine = receivedProductItemLine;
        this.productItemLineNo = productItemLineNo;

        this.isHeader = isHeader;
    }

    public ReceivedMultiItem(ReceivedProductItem receivedProductItem, int productItemIndex, ReceivedProductItemLine receivedProductItemLine, int productItemLineNo,
                             ReceivedItemValue receivedItemValue, int itemValueLineNo, boolean isHeader) {
        this.receivedProductItem = receivedProductItem;
        this.receivedProductItemLine = receivedProductItemLine;
        this.receivedItemValue = receivedItemValue;
        this.isHeader = isHeader;
        this.productItemIndex = productItemIndex;
        this.productItemLineNo = productItemLineNo;
        this.itemValueLineNo = itemValueLineNo;
    }

    public ReceivedProductItem getReceivedProductItem() {
        return receivedProductItem;
    }

    public void setReceivedProductItem(ReceivedProductItem receivedProductItem) {
        this.receivedProductItem = receivedProductItem;
    }

    public ReceivedProductItemLine getReceivedProductItemLine() {
        return receivedProductItemLine;
    }

    public void setReceivedProductItemLine(ReceivedProductItemLine receivedProductItemLine) {
        this.receivedProductItemLine = receivedProductItemLine;
    }

    public ReceivedItemValue getReceivedItemValue() {
        return receivedItemValue;
    }

    public void setReceivedItemValue(ReceivedItemValue receivedItemValue) {
        this.receivedItemValue = receivedItemValue;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public Product getProduct() {
        return receivedProductItem.getProduct();
    }

    public int getProductItemIndex() {
        return productItemIndex;
    }

    public int getProductItemLineNo() {
        return productItemLineNo;
    }

    public int getItemValueLineNo() {
        return itemValueLineNo;
    }

    public static List<ReceivedMultiItem> generateReceivedMultiItem(ReceivedProductItem receivedProductItem, int receivedProductItemIndex,
                                                                    ReceivedProductItemLine itemLine, int itemLineIndex) {
        List<ReceivedMultiItem> multiItems = new ArrayList<>();
        //multiItems.add(new ReceivedMultiItem(receivedProductItem, itemLine, null, true)); // for header

        for(ReceivedItemValue itemValue : itemLine.getItemValueList()) {
            multiItems.add(new ReceivedMultiItem(receivedProductItem, receivedProductItemIndex, itemLine, itemLineIndex, itemValue,
                    itemLine.getItemValueList().indexOf(itemValue), false)); // for sublist
        }
        return multiItems;
    }

    public static List<ReceivedMultiItem> generateReceivedMultiItem(ReceivedProductItem receivedProductItem, int index) {
        List<ReceivedMultiItem> multiItems = new ArrayList<>();
        multiItems.add(new ReceivedMultiItem(receivedProductItem, index, null, 0, true)); // for header

        for(ReceivedProductItemLine itemLine : receivedProductItem.getProductItemLines()) {
            multiItems.add(new ReceivedMultiItem(receivedProductItem, index, itemLine,
                    receivedProductItem.getProductItemLines().indexOf(itemLine), false)); // for sublist
        }
        return multiItems;
    }

    public static List<ReceivedMultiItem> generateReceivedMultiItem(List<ReceivedProductItem> receivedProductItemList) {
        List<ReceivedMultiItem> multiItems = new ArrayList<>();
        for(ReceivedProductItem productItem : receivedProductItemList)
            multiItems.addAll(generateReceivedMultiItem(productItem, multiItems.size()));
        return multiItems;
    }

    public static List<ReceivedMultiItem> generateReceivedMultiItem(ReceivedProductItemList receivedProductItemList) {
        List<ReceivedMultiItem> multiItems = new ArrayList<>();
        for(ReceivedProductItem productItem : receivedProductItemList.toList())
            multiItems.addAll(generateReceivedMultiItem(productItem, multiItems.size()));
        return multiItems;
    }
}
