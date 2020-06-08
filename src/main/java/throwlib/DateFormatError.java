package throwlib;

public class DateFormatError extends Exception {
    public DateFormatError() {
    }

    public DateFormatError(String message) {
        super(message);
    }
}
