package throwlib;

public class FieldTypeError extends Exception {
    String errorMessage;
    public FieldTypeError(String message){
        System.out.println(message);
        errorMessage=message;
    }
}
