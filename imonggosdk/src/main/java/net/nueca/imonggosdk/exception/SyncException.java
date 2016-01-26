package net.nueca.imonggosdk.exception;

/**
 * Created by Jn on 6/8/2015.
 */
public class SyncException extends Exception {

    private static final long serialVersionUID = 6666L;

    public SyncException(){}

    public SyncException(String message){
        super(message);
    }

    public SyncException(Throwable cause){
        super(cause);
    }

    public SyncException(String message, Throwable cause){
        super(message, cause);
    }
}
