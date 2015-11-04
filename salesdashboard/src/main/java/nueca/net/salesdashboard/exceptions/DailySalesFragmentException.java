package nueca.net.salesdashboard.exceptions;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class DailySalesFragmentException extends RuntimeException {
    public DailySalesFragmentException() {
    }

    public DailySalesFragmentException(String detailMessage) {
        super(detailMessage);
    }

    public DailySalesFragmentException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DailySalesFragmentException(Throwable throwable) {
        super(throwable);
    }
}
