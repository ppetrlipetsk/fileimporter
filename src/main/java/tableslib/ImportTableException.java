package tableslib;

public class ImportTableException extends Exception {
    public ImportTableException() {
    }

    public ImportTableException(String message) {
        super(message);
    }

    public ImportTableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImportTableException(Throwable cause) {
        super(cause);
    }

    public ImportTableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
