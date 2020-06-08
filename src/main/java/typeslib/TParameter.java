package typeslib;

public class TParameter {
    private String value;
    private final boolean require;

    public String getValue() {
        return value;
    }

    public boolean isRequire() {
        return require;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TParameter(String value, boolean require) {
        this.value = value;
        this.require = require;
    }
}
