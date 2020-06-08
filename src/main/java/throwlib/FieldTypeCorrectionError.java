package throwlib;

public class FieldTypeCorrectionError extends Exception {
    String errorMessage;

    public FieldTypeCorrectionError(String message) {
        super(message);
        this.errorMessage = message;
    }
}
