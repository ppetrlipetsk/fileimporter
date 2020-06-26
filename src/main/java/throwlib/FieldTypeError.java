package throwlib;

public class FieldTypeError extends Exception {
    public FieldTypeError(String message){
        super(message);
        System.out.println(message);
    }
}
