package net.nueca.concessioengine.objects;

/**
 * Created by gama on 31/05/2016.
 */
public class ReceivedItemValue {
    private double quantity = 0d;
    private double price = 0d;
    private double outright_return = 0d;
    private double subtotal;

    public ReceivedItemValue(double quantity, double price, double outright_return) {
        this.quantity = quantity;
        this.price = price;
        this.outright_return = outright_return;
        refreshSubtotal();
    }

    public ReceivedItemValue(double quantity, double price) {
        this.quantity = quantity;
        this.price = price;
        refreshSubtotal();
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
        refreshSubtotal();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        refreshSubtotal();
    }

    public double getOutright_return() {
        return outright_return;
    }

    public void setOutright_return(double outright_return) {
        this.outright_return = outright_return;
    }

    private void refreshSubtotal() {
        this.subtotal = this.quantity * this.price;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
