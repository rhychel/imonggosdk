package net.nueca.concessioengine.exceptions;

/**
 * Created by rhymart on 7/22/15.
 * imonggosdk (c)2015
 */
public class ProductsFragmentException extends RuntimeException {

    public ProductsFragmentException() {
    }

    public ProductsFragmentException(String detailMessage) {
        super(detailMessage);
    }

    public ProductsFragmentException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ProductsFragmentException(Throwable throwable) {
        super(throwable);
    }
}
