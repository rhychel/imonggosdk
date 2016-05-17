package net.nueca.concessioengine.tools;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.base.BatchList;

import java.sql.SQLException;

/**
 * Created by rhymartmanchus on 17/05/2016.
 */
public class InventoryTools {


    /**
     *
     * @return the number of inventory objects updated
     */
    public static int updateInventoryFromSelectedItemList(ImonggoDBHelper2 dbHelper, boolean shouldAdd) {
        int updated = 0;
        BatchList<Inventory> newInventories = new BatchList<>(DatabaseOperation.INSERT, dbHelper);
        BatchList<Inventory> updateInventories = new BatchList<>(DatabaseOperation.UPDATE, dbHelper);
        for(SelectedProductItem selectedProductItem : ProductsAdapterHelper.getSelectedProductItems()) {
            if(selectedProductItem.getInventory() != null) {
                Inventory updateInventory = selectedProductItem.getInventory();
                updateInventory.setProduct(selectedProductItem.getProduct());
                updateInventory.setQuantity(Double.valueOf(selectedProductItem.updatedInventory(shouldAdd)));
                updateInventories.add(updateInventory);
                updated++;
            }
            else {
                Inventory newInventory = new Inventory();
                newInventory.setProduct(selectedProductItem.getProduct());
                newInventory.setQuantity(Double.valueOf(selectedProductItem.updatedInventory(shouldAdd)));
                newInventories.add(newInventory);
                updated++;
            }
        }
        newInventories.doOperation(Inventory.class);
        updateInventories.doOperation(Inventory.class);
        return updated;
    }

    public static int revertInventoryFromSelectedItemList(ImonggoDBHelper2 dbHelper) {
        return revertInventoryFromSelectedItemList(dbHelper, true);
    }

    public static int revertInventoryFromSelectedItemList(ImonggoDBHelper2 dbHelper, boolean shouldAdd) {
        int updated = 0;
        BatchList<Inventory> inventories = new BatchList<>(DatabaseOperation.UPDATE, dbHelper);
        for(SelectedProductItem selectedProductItem : ProductsAdapterHelper.getSelectedProductItems()) {
            try {
                Inventory inventory = dbHelper.fetchObjectsInt(Inventory.class).queryBuilder().where().eq("product_id", selectedProductItem.getProduct()).queryForFirst();
                for(Values values : selectedProductItem.getValues())
                    inventory.operationQuantity(Double.valueOf(values.getActualQuantity()), shouldAdd);
                inventories.add(inventory);
                updated++;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        inventories.doOperation(Inventory.class);
        return updated;
    }
}
